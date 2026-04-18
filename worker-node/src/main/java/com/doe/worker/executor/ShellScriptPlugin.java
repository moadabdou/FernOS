package com.doe.worker.executor;

import com.doe.core.executor.ExecutionContext;
import com.doe.core.executor.JobDefinition;
import com.doe.core.executor.TaskExecutor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Plugin for {@code "type": "bash"} jobs. Executes a shell script using
 * {@code bash -c} via {@link ProcessBuilder}.
 */
public class ShellScriptPlugin implements TaskExecutor {

    private static final Gson GSON = new Gson();

    /** Maximum bytes captured from process output before truncation (64 KB). */
    public static final int MAX_OUTPUT_BYTES = 64 * 1024;

    private static final long DEFAULT_TIMEOUT_MS = 30_000;
    private static final String TRUNCATION_SUFFIX = "\n[...output truncated at 64 KB...]";

    private final AtomicReference<Process> activeProcess = new AtomicReference<>();

    @Override
    public String getType() {
        return "bash";
    }

    @Override
    public String execute(JobDefinition definition, ExecutionContext context) throws Exception {
        JsonObject json = GSON.fromJson(definition.payload(), JsonObject.class);
        if (json == null || !json.has("script")) {
            throw new IllegalArgumentException("bash payload requires a 'script' field");
        }

        String script = json.get("script").getAsString();
        long timeoutMs = json.has("timeoutMs")
                ? json.get("timeoutMs").getAsLong()
                : DEFAULT_TIMEOUT_MS;

        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("bash 'timeoutMs' must be positive, got: " + timeoutMs);
        }

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", script);
        pb.redirectErrorStream(true); // merge stderr into stdout
        
        // Add environment variables from context
        pb.environment().putAll(context.getEnvVars());
        // Note: secrets should be handled carefully, here we just put them as env vars if they exist
        pb.environment().putAll(context.getSecrets());

        context.log("Starting bash script: " + (script.length() > 50 ? script.substring(0, 47) + "..." : script));
        Process process = pb.start();
        activeProcess.set(process);

        StringBuilder resultBuilder = new StringBuilder();
        var outputFuture = CompletableFuture.supplyAsync(
                () -> {
                    try (InputStream is = process.getInputStream();
                         var reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        boolean truncated = false;
                        while ((line = reader.readLine()) != null) {
                            context.log(line);
                            if (!truncated) {
                                if (resultBuilder.length() + line.length() + 1 > MAX_OUTPUT_BYTES) {
                                    truncated = true;
                                    resultBuilder.append(TRUNCATION_SUFFIX);
                                } else {
                                    resultBuilder.append(line).append("\n");
                                }
                            }
                        }
                        return resultBuilder.toString();
                    } catch (IOException e) {
                        return resultBuilder.toString();
                    }
                });

        try {
            boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                outputFuture.cancel(true);
                throw new IllegalStateException(
                        "bash script exceeded timeout of " + timeoutMs + "ms and was killed");
            }

            int exitCode = process.exitValue();
            String output = outputFuture.join();

            if (exitCode != 0) {
                String firstLine = output.lines().findFirst().orElse("(no output)");
                throw new IllegalStateException(
                        "Process exited with code " + exitCode + ": " + firstLine);
            }

            return output;
        } finally {
            process.destroy();
            activeProcess.set(null);
        }
    }

    @Override
    public void cancel() {
        Process p = activeProcess.get();
        if (p != null && p.isAlive()) {
            p.destroyForcibly();
        }
    }

    @Override
    public void validate(JobDefinition definition) {
        JsonObject json = GSON.fromJson(definition.payload(), JsonObject.class);
        if (json == null || !json.has("script")) {
            throw new IllegalArgumentException("bash payload requires a 'script' field");
        }
    }

}
