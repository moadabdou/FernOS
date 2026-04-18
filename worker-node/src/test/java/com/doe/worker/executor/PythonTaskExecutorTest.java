package com.doe.worker.executor;

import com.doe.core.executor.JobDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PythonTaskExecutorTest {

    private final PythonTaskExecutor plugin = new PythonTaskExecutor();
    private final DefaultExecutionContext context = new DefaultExecutionContext();

    @Test
    @DisplayName("executes python script and returns success message")
    void execute_script() throws Exception {
        JobDefinition def = new JobDefinition(UUID.randomUUID(), "python", "{\"script\":\"print('hello python')\"}");
        String result = plugin.execute(def, context);
        assertTrue(result.startsWith("Executed successfully in"));
        assertTrue(context.getBufferedLogs().contains("hello python"));
    }

    @Test
    @DisplayName("executes python script with arguments")
    void execute_with_args() throws Exception {
        JobDefinition def = new JobDefinition(UUID.randomUUID(), "python", 
                "{\"script\":\"import sys; print(sys.argv[1])\", \"args\": [\"test_arg\"]}");
        String result = plugin.execute(def, context);
        assertTrue(result.startsWith("Executed successfully in"));
        assertTrue(context.getBufferedLogs().contains("test_arg"));
    }

    @Test
    @DisplayName("streams output to context logs")
    void streams_output() throws Exception {
        JobDefinition def = new JobDefinition(UUID.randomUUID(), "python", 
                "{\"script\":\"print('line1'); print('line2')\"}");
        plugin.execute(def, context);
        
        assertTrue(context.getBufferedLogs().contains("line1"));
        assertTrue(context.getBufferedLogs().contains("line2"));
    }

    @Test
    @DisplayName("script failure throws Exception")
    void execute_failure() {
        JobDefinition def = new JobDefinition(UUID.randomUUID(), "python", "{\"script\":\"import sys; sys.exit(1)\"}");
        assertThrows(IllegalStateException.class, () -> plugin.execute(def, context));
    }

    @Test
    @DisplayName("invalid payload throws Exception")
    void validate_invalid() {
        JobDefinition def = new JobDefinition(UUID.randomUUID(), "python", "{}");
        assertThrows(IllegalArgumentException.class, () -> plugin.validate(def));
    }

    @Test
    @DisplayName("cancel kills the process")
    void cancel_killsProcess() throws Exception {
        JobDefinition def = new JobDefinition(UUID.randomUUID(), "python", 
                "{\"script\":\"import time; time.sleep(10)\"}");
        
        Thread t = new Thread(() -> {
            try {
                plugin.execute(def, context);
            } catch (Exception ignored) {}
        });
        t.start();
        Thread.sleep(500); // Wait for process to start
        
        plugin.cancel();
        t.join(1000);
        assertFalse(t.isAlive());
    }
}
