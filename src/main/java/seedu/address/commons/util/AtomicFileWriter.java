package seedu.address.commons.util;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stages complete UTF-8 content beside its destination before replacing that destination atomically.
 * Filesystems without atomic replacement support report a save error instead of risking the previous file.
 */
class AtomicFileWriter {

    /**
     * Writes without truncating the destination, including on the first save.
     * A failed cleanup is attached to the original save error.
     */
    void write(Path file, String content) throws IOException {
        requireNonNull(file);
        requireNonNull(content);
        Path target = file.toAbsolutePath();
        Path directory = target.getParent();
        Files.createDirectories(directory);
        Path temporaryFile = Files.createTempFile(directory, ".tutorlah-", ".tmp");
        try {
            writeContent(temporaryFile, content);
            replaceFile(temporaryFile, target);
        } catch (IOException | RuntimeException e) {
            try {
                deleteTemporaryFile(temporaryFile);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }

    /** Writes the staged content. Kept separate so tests can simulate a partial write. */
    void writeContent(Path temporaryFile, String content) throws IOException {
        Files.writeString(temporaryFile, content, StandardCharsets.UTF_8);
    }

    /** Replaces the destination only if the filesystem supports an atomic move. */
    void replaceFile(Path temporaryFile, Path target) throws IOException {
        Files.move(temporaryFile, target, ATOMIC_MOVE, REPLACE_EXISTING);
    }

    /** Removes an incomplete staged file after a failed save. */
    void deleteTemporaryFile(Path temporaryFile) throws IOException {
        Files.deleteIfExists(temporaryFile);
    }
}
