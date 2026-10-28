package seedu.address;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/** Holds an operating-system lock that allows only one TutorLah instance per data folder. */
final class ApplicationLock implements AutoCloseable {

    static final String LOCK_FILE_NAME = ".tutorlah.lock";

    private final FileChannel channel;
    private final FileLock lock;

    private ApplicationLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Tries to lock the folder containing {@code dataFilePath}.
     *
     * @return an acquired lock, or an empty result when another instance holds it
     */
    static Optional<ApplicationLock> tryAcquire(Path dataFilePath) throws IOException {
        Path dataFolder = dataFilePath.toAbsolutePath().normalize().getParent();
        Files.createDirectories(dataFolder);
        Path lockFile = dataFolder.resolve(LOCK_FILE_NAME);
        FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                return Optional.empty();
            }
            return Optional.of(new ApplicationLock(channel, lock));
        } catch (OverlappingFileLockException e) {
            channel.close();
            return Optional.empty();
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            lock.release();
        } finally {
            channel.close();
        }
    }
}
