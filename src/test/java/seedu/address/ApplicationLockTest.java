package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests acquiring and releasing TutorLah's data-folder lock. */
public class ApplicationLockTest {
    private static final long PROCESS_TIMEOUT_SECONDS = 10;

    @TempDir
    public Path temporaryFolder;

    @Test
    public void tryAcquire_unavailableLock_returnsEmptyAndClosesChannel() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("students.json");

        assertTrue(ApplicationLock.tryAcquire(dataFile, channel -> null).isEmpty());
        try (ApplicationLock lock = ApplicationLock.tryAcquire(dataFile).orElseThrow()) {
            assertTrue(ApplicationLock.tryAcquire(dataFile).isEmpty());
        }
    }

    @Test
    public void tryAcquire_lockAttemptFails_closesChannelAndRethrows() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("students.json");

        ApplicationLock.LockAttempt failingLockAttempt = channel -> {
            channel.tryLock();
            throw new IOException("simulated lock failure");
        };
        assertThrows(IOException.class, "simulated lock failure", () ->
                ApplicationLock.tryAcquire(dataFile, failingLockAttempt));

        try (ApplicationLock lock = ApplicationLock.tryAcquire(dataFile).orElseThrow()) {
            assertTrue(ApplicationLock.tryAcquire(dataFile).isEmpty());
        }
    }

    @Test
    public void tryAcquire_lockHeldByAnotherProcess_returnsEmptyUntilProcessExits() throws Exception {
        Path dataFile = temporaryFolder.resolve("data").resolve("students.json");
        Path preferencesFile = temporaryFolder.resolve("preferences.json");
        Path readyFile = temporaryFolder.resolve("lock-holder-ready");
        byte[] originalData = "{\"persons\":[]}".getBytes(StandardCharsets.UTF_8);
        byte[] originalPreferences = "{unreadable preferences}".getBytes(StandardCharsets.UTF_8);
        Files.createDirectories(dataFile.getParent());
        Files.write(dataFile, originalData);
        Files.write(preferencesFile, originalPreferences);
        FileTime dataModifiedTime = Files.getLastModifiedTime(dataFile);
        FileTime preferencesModifiedTime = Files.getLastModifiedTime(preferencesFile);
        String classPath = Path.of(ApplicationLock.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(LockHolder.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String javaCommand = ProcessHandle.current().info().command().orElseThrow();
        Process lockHolder = new ProcessBuilder(javaCommand, "-cp", classPath, LockHolder.class.getName(),
                dataFile.toString(), readyFile.toString()).redirectErrorStream(true).start();

        try {
            waitUntilReady(lockHolder, readyFile);
            MainApp blockedApp = new MainApp(preferencesFile, dataFile);
            blockedApp.init();
            try {
                assertTrue(blockedApp.getStartupFailure().orElseThrow().contains("already using this data folder"));
                assertNull(blockedApp.storage);
                assertNull(blockedApp.model);
                assertNull(blockedApp.logic);
            } finally {
                blockedApp.stop();
            }
            assertArrayEquals(originalData, Files.readAllBytes(dataFile));
            assertArrayEquals(originalPreferences, Files.readAllBytes(preferencesFile));
            assertEquals(dataModifiedTime, Files.getLastModifiedTime(dataFile));
            assertEquals(preferencesModifiedTime, Files.getLastModifiedTime(preferencesFile));
        } finally {
            lockHolder.getOutputStream().close();
            if (!lockHolder.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                lockHolder.destroyForcibly();
                assertTrue(lockHolder.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            }
        }

        String processOutput = new String(lockHolder.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, lockHolder.exitValue(), processOutput);
        try (ApplicationLock lock = ApplicationLock.tryAcquire(dataFile).orElseThrow()) {
            assertTrue(Files.exists(temporaryFolder.resolve("data").resolve(ApplicationLock.LOCK_FILE_NAME)));
        }
    }

    private static void waitUntilReady(Process lockHolder, Path readyFile) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(PROCESS_TIMEOUT_SECONDS);
        while (!Files.exists(readyFile) && lockHolder.isAlive() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        if (!Files.exists(readyFile)) {
            lockHolder.destroyForcibly();
            lockHolder.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            String output = new String(lockHolder.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new AssertionError("Lock-holder process did not become ready. " + output);
        }
    }

    /** Holds the application lock in a separate JVM until its input stream closes. */
    public static final class LockHolder {
        private LockHolder() {
        }

        /** Acquires the requested lock and signals that it is ready. */
        public static void main(String[] args) throws Exception {
            try (ApplicationLock lock = ApplicationLock.tryAcquire(Path.of(args[0])).orElseThrow()) {
                Files.createFile(Path.of(args[1]));
                System.in.read();
            }
        }
    }
}
