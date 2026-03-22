package com.doe.worker.client;

import com.doe.core.protocol.Message;
import com.doe.core.protocol.MessageType;
import com.doe.core.protocol.ProtocolDecoder;
import com.doe.core.protocol.ProtocolEncoder;
import com.doe.core.util.RetryPolicy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

/**
 * The Worker client: establishes a TCP connection to the Manager, registers itself,
 * and enters a main loop to receive commands such as {@code ASSIGN_JOB}.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Connect to Manager at {@code host:port} via TCP (with exponential-backoff retry).</li>
 *   <li>Send {@code REGISTER_WORKER} containing the local hostname.</li>
 *   <li>Read {@code REGISTER_ACK} — the manager assigns the worker UUID.</li>
 *   <li>Enter main loop: block on {@link ProtocolDecoder#decode} waiting for commands.</li>
 *   <li>On disconnect / exception → log, reconnect with backoff.</li>
 * </ol>
 */
public class WorkerClient {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerClient.class);
    private static final Gson GSON = new Gson();

    /** Backoff: 1 s → 2 s → 4 s … capped at 30 s, unlimited attempts. */
    private static final RetryPolicy RETRY_POLICY =
            new RetryPolicy(1_000, 30_000, Integer.MAX_VALUE);

    private final String host;
    private final int port;

    private volatile boolean running = true;
    private volatile Socket socket;

    /**
     * Creates a new WorkerClient.
     *
     * @param host the manager hostname or IP address
     * @param port the manager TCP port
     */
    public WorkerClient(String host, int port) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 0 and 65535, got: " + port);
        }
        this.host = host;
        this.port = port;
    }

    /**
     * Starts the worker: connects, registers, and runs the main command loop.
     * <p>
     * On disconnect, automatically reconnects with exponential backoff.
     * Blocks until {@link #shutdown()} is called.
     */
    public void start() {
        LOG.info("WorkerClient starting, targeting manager at {}:{}", host, port);

        while (running) {
            try {
                connectAndRun();
            } catch (ConnectException e) {
                // RetryPolicy exhausted (won't happen with MAX_VALUE, but guard anyway)
                LOG.error("Could not connect to manager at {}:{}: {}", host, port, e.getMessage());
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.info("WorkerClient interrupted, shutting down.");
                break;
            } catch (IOException e) {
                if (running) {
                    LOG.error("Unexpected I/O error, will attempt reconnect.", e);
                    sleepQuietly(2_000);
                }
            }
        }

        LOG.info("WorkerClient stopped.");
    }

    /**
     * One full connection lifecycle: connect → register → main loop.
     * Throws on non-retryable errors; {@link ConnectException} is handled by {@link RetryPolicy}.
     */
    private void connectAndRun() throws IOException, InterruptedException {
        // ── 1. Connect with exponential backoff ───────────────────────────────
        RETRY_POLICY.execute(() -> {
            LOG.info("Connecting to manager at {}:{}...", host, port);
            socket = new Socket(host, port);
            LOG.info("Connected to manager at {}", socket.getRemoteSocketAddress());
        });

        try (Socket s = socket) {
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();

            // ── 2. Send REGISTER_WORKER ───────────────────────────────────────
            String hostname = resolveHostname();
            JsonObject regPayload = new JsonObject();
            regPayload.addProperty("hostname", hostname);

            byte[] regBytes = ProtocolEncoder.encode(
                    MessageType.REGISTER_WORKER, GSON.toJson(regPayload));
            out.write(regBytes);
            out.flush();

            LOG.info("Sent REGISTER_WORKER (hostname: {})", hostname);

            // ── 3. Read REGISTER_ACK — manager assigns our UUID ───────────────
            Message ack = ProtocolDecoder.decode(in);
            if (ack.type() != MessageType.REGISTER_ACK) {
                throw new IOException(
                        "Expected REGISTER_ACK but received: " + ack.type());
            }

            JsonObject ackJson = GSON.fromJson(ack.payloadAsString(), JsonObject.class);

            if (!ackJson.has("workerId")) {
                throw new IOException("REGISTER_ACK missing required 'workerId' field");
            }

            UUID workerId = UUID.fromString(ackJson.get("workerId").getAsString());
            LOG.info("Registered with manager, assigned worker ID: {}", workerId);

            // ── 4. Main command loop ──────────────────────────────────────────
            runMainLoop(in, workerId);
        }finally {
            // to avoid calling close() twice by shutdown if socket was already closed by try-with-resources
            socket = null;
        }
    }

    /**
     * Blocks reading messages from the manager and dispatching by type.
     * Returns normally on disconnect (EOF or socket close); calling code will
     * then attempt reconnection if {@link #running} is still {@code true}.
     */
    private void runMainLoop(InputStream in, UUID workerId) throws IOException {
        LOG.info("Worker {} entering main loop, waiting for commands...", workerId);

        while (running) {
            Message message;
            try {
                message = ProtocolDecoder.decode(in);
            } catch (EOFException e) {
                LOG.info("Worker {}: manager closed the connection (stream ended).", workerId);
                return;
            } catch (SocketException e) {
                if (running) {
                    LOG.info("Worker {}: connection reset by manager: {}", workerId, e.getMessage());
                }
                return;
            }

            switch (message.type()) {
                case ASSIGN_JOB -> {
                    LOG.info("Worker {}: received ASSIGN_JOB — payload: {}",
                            workerId, message.payloadAsString());
                    // TODO (Issue #008): execute the job and send JOB_RESULT
                }
                default -> LOG.warn("Worker {}: unexpected message type: {}",
                        workerId, message.type());
            }
        }
    }

    /**
     * Signals the client to stop and closes the current socket.
     */
    public void shutdown() {
        LOG.info("WorkerClient shutdown requested.");
        running = false;
        Socket s = socket;
        if (s != null && !s.isClosed()) {
            try {
                s.close();
            } catch (IOException e) {
                LOG.warn("Error closing socket during shutdown", e);
            }
        }
    }

    // ──── Helpers ────────────────────────────────────────────────────────────

    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            LOG.warn("Could not resolve local hostname, using 'unknown'", e);
            return "unknown";
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
