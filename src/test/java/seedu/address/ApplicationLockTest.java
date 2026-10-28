package seedu.address;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests acquiring and releasing TutorLah's data-folder lock. */
public class ApplicationLockTest {
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
}
