package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AtomicFileWriterTest {
    private static final String ORIGINAL = "{\"persons\":[]}";
    private static final String REPLACEMENT = "{\"name\":\"Zoë Tan\"}";

    @TempDir
    public Path testFolder;

    @Test
    public void write_firstSave_createsParentsAndCompleteUtf8File() throws IOException {
        Path target = testFolder.resolve("new/data/students.json");
        FileUtil.writeToFile(target, REPLACEMENT);
        assertEquals(REPLACEMENT, Files.readString(target));
        assertDirectoryContainsOnly(target.getParent(), target);
    }

    @Test
    public void write_existingFile_replacesOnlyAfterContentIsComplete() throws IOException {
        Path target = existingFile();
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void replaceFile(Path temporaryFile, Path destination) throws IOException {
                assertEquals(target.toAbsolutePath(), destination);
                assertEquals(destination.getParent(), temporaryFile.getParent());
                assertEquals(ORIGINAL, Files.readString(destination));
                assertEquals(REPLACEMENT, Files.readString(temporaryFile));
                super.replaceFile(temporaryFile, destination);
            }
        };
        writer.write(target, REPLACEMENT);
        assertEquals(REPLACEMENT, Files.readString(target));
        assertDirectoryContainsOnly(testFolder, target);
    }

    @Test
    public void write_partialWriteFailure_preservesOriginalAndCleansTemporaryFile() throws IOException {
        Path target = existingFile();
        byte[] before = Files.readAllBytes(target);
        IOException failure = new IOException("Simulated full disk");
        assertSame(failure, assertThrows(IOException.class, () -> partialWriter(failure).write(target, REPLACEMENT)));
        assertArrayEquals(before, Files.readAllBytes(target));
        assertDirectoryContainsOnly(testFolder, target);
    }

    @Test
    public void write_partialFirstWriteFailure_leavesNoDestinationOrTemporaryFile() throws IOException {
        Path target = testFolder.resolve("students.json");
        assertThrows(IOException.class, () -> partialWriter(new IOException("Full disk")).write(target, REPLACEMENT));
        assertFalse(Files.exists(target));
        assertDirectoryContainsOnly(testFolder);
    }

    @Test
    public void write_unsupportedAtomicMove_preservesOriginalAndReportsFailure() throws IOException {
        Path target = existingFile();
        AtomicMoveNotSupportedException failure = new AtomicMoveNotSupportedException("temp", "target", "Unsupported");
        assertSame(failure, assertThrows(IOException.class, () -> failingMover(failure).write(target, REPLACEMENT)));
        assertEquals(ORIGINAL, Files.readString(target));
        assertDirectoryContainsOnly(testFolder, target);
    }

    @Test
    public void write_replacementFailureOnFirstSave_leavesNoDestination() throws IOException {
        Path target = testFolder.resolve("students.json");
        assertThrows(IOException.class, () ->
                failingMover(new IOException("Cannot replace")).write(target, REPLACEMENT));
        assertFalse(Files.exists(target));
        assertDirectoryContainsOnly(testFolder);
    }

    @Test
    public void write_nonemptyDirectoryAsDestination_preservesDirectoryAndCleansTemporaryFile() throws IOException {
        Path target = Files.createDirectory(testFolder.resolve("students.json"));
        Path child = Files.writeString(target.resolve("keep.txt"), ORIGINAL);
        assertThrows(IOException.class, () -> FileUtil.writeToFile(target, REPLACEMENT));
        assertEquals(ORIGINAL, Files.readString(child));
        assertDirectoryContainsOnly(testFolder, target);
    }

    @Test
    public void write_parentIsFile_doesNotChangeParent() throws IOException {
        Path parent = existingFile();
        assertThrows(IOException.class, () -> FileUtil.writeToFile(parent.resolve("students.json"), REPLACEMENT));
        assertEquals(ORIGINAL, Files.readString(parent));
        assertDirectoryContainsOnly(testFolder, parent);
    }

    @Test
    public void write_cleanupFailure_keepsOriginalSaveException() throws IOException {
        Path target = existingFile();
        IOException saveError = new IOException("Cannot replace");
        IOException cleanupError = new IOException("Cannot clean up");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void replaceFile(Path temporaryFile, Path destination) throws IOException {
                throw saveError;
            }

            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw cleanupError;
            }
        };
        IOException actual = assertThrows(IOException.class, () -> writer.write(target, REPLACEMENT));
        assertSame(saveError, actual);
        assertArrayEquals(new Throwable[] {cleanupError}, actual.getSuppressed());
        assertEquals(ORIGINAL, Files.readString(target));
        try (var files = Files.list(testFolder)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    public void write_runtimeFailure_cleansTemporaryFileAndPreservesOriginal() throws IOException {
        Path target = existingFile();
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            void writeContent(Path temporaryFile, String content) {
                throw new SecurityException("Access denied");
            }
        };
        assertThrows(SecurityException.class, () -> writer.write(target, REPLACEMENT));
        assertEquals(ORIGINAL, Files.readString(target));
        assertDirectoryContainsOnly(testFolder, target);
    }

    @Test
    public void write_nullInput_createsNothing() throws IOException {
        assertThrows(NullPointerException.class, () -> FileUtil.writeToFile(null, REPLACEMENT));
        assertThrows(NullPointerException.class, () -> FileUtil.writeToFile(testFolder.resolve("students.json"), null));
        assertDirectoryContainsOnly(testFolder);
    }

    private AtomicFileWriter partialWriter(IOException failure) {
        return new AtomicFileWriter() {
            @Override
            void writeContent(Path temporaryFile, String content) throws IOException {
                Files.writeString(temporaryFile, "{\"partial\":");
                throw failure;
            }
        };
    }

    private AtomicFileWriter failingMover(IOException failure) {
        return new AtomicFileWriter() {
            @Override
            void replaceFile(Path temporaryFile, Path target) throws IOException {
                assertEquals(REPLACEMENT, Files.readString(temporaryFile));
                throw failure;
            }
        };
    }

    private Path existingFile() throws IOException {
        return Files.writeString(testFolder.resolve("students.json"), ORIGINAL);
    }

    private void assertDirectoryContainsOnly(Path directory, Path... expected) throws IOException {
        try (var files = Files.list(directory)) {
            List<Path> actual = files.toList();
            assertEquals(expected.length, actual.size());
            assertTrue(actual.containsAll(List.of(expected)));
        }
    }
}
