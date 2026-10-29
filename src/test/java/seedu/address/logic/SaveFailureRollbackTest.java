package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks real command execution, filtering, JSON preservation and retries when a save fails.
 */
public class SaveFailureRollbackTest {
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withPhone("81230011")
            .withLessons("Algebra", "Geometry").build().withFlag(new Flag("Review homework"));
    private static final Person BEN = new PersonBuilder().withName("Ben Lim").withPhone("81230012")
            .withLessons("Calculus").build();

    @TempDir
    public Path temporaryFolder;

    @TestFactory
    public Stream<DynamicTest> execute_mutationSaveFails_restoresDataFilterAndRetryTarget() {
        List<String> mutations = List.of(
                "add n/Cara Lee p/81230013 addr/30 Example Street l/Sec 3 sub/E-Math",
                "edit 1 n/Amy Updated",
                "loglesson 1 c/Trigonometry",
                "delete 1",
                "deletelesson 1 1",
                "editlesson 1 1 c/Revised geometry",
                "flag 1 r/Call parent",
                "unflag 1");
        return Stream.of(false, true).flatMap(isPermissionFailure -> mutations.stream().map(command ->
                DynamicTest.dynamicTest(command + (isPermissionFailure ? " (permission)" : " (IO)"), () ->
                    assertRollbackAndRetry(command, isPermissionFailure))));
    }

    private void assertRollbackAndRetry(String command, boolean isPermissionFailure) throws Exception {
        Path folder = Files.createTempDirectory(temporaryFolder, "mutation-");
        Model model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BEN);
        FailOnceStorage storage = new FailOnceStorage(folder.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
        logic.execute("find amy");
        AddressBook before = new AddressBook(model.getAddressBook());
        String savedData = Files.readString(storage.getAddressBookFilePath());
        var displayedList = model.getFilteredPersonList();
        var preferences = new UserPrefs(model.getUserPrefs());
        IOException failure = isPermissionFailure ? new AccessDeniedException("students.json")
                : new IOException("simulated disk full");
        storage.failNextSave(failure);

        String message = isPermissionFailure
                ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, storage.getAddressBookFilePath())
                : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage());
        assertThrows(CommandException.class, message, () -> logic.execute(command));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(AMY), model.getFilteredPersonList());
        assertSame(displayedList, model.getFilteredPersonList());
        assertEquals(preferences, model.getUserPrefs());
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));

        logic.execute("list");
        assertEquals(before, new AddressBook(storage.readAddressBook().orElseThrow()));
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("find amy");
        logic.execute(command);
        assertNotEquals(before, model.getAddressBook());
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertTrue(model.getAddressBook().getPersonList().contains(BEN));
    }

    @Test
    public void execute_failedUnflag_restoresFlaggedFilterForFutureChanges() throws Exception {
        Model model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BEN);
        FailOnceStorage storage = new FailOnceStorage(temporaryFolder.resolve("flagged.json"));
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("flagged");
        storage.failNextSave(new IOException("simulated failure"));
        assertThrows(CommandException.class, () -> logic.execute("unflag 1"));
        assertEquals(List.of(AMY), model.getFilteredPersonList());

        Person flaggedBen = BEN.withFlag(new Flag());
        model.setPerson(BEN, flaggedBen);
        assertEquals(List.of(AMY, flaggedBen), model.getFilteredPersonList());
    }

    @Test
    public void execute_savePathBecomesDirectory_restoresDeletionAfterRealIoFailure() throws Exception {
        Path file = temporaryFolder.resolve("students.json");
        Path preservedFile = temporaryFolder.resolve("students.saved.json");
        Model model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BEN);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(model.getAddressBook());
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("find amy");
        String originalBytes = Files.readString(file);
        Files.move(file, preservedFile);
        Files.createDirectory(file);
        assertThrows(CommandException.class, () -> logic.execute("delete 1"));
        assertEquals(List.of(AMY), model.getFilteredPersonList());
        assertEquals(List.of(AMY, BEN), model.getAddressBook().getPersonList());
        assertEquals(originalBytes, Files.readString(preservedFile));

        Files.delete(file);
        Files.move(preservedFile, file);
        logic.execute("list");
        assertEquals(originalBytes, Files.readString(file));
        logic.execute("delete 1");
        assertEquals(List.of(BEN), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @TestFactory
    public Stream<DynamicTest> execute_filesystemFailure_reportsDestinationAndKeepsOriginalCause() {
        String temporaryName = ".tutorlah-12345.tmp";
        return Stream.of(new FileSystemException(temporaryName, null, "No space left on device"),
                new AtomicMoveNotSupportedException(temporaryName, "other.json", "Atomic move is not supported"),
                new FileSystemException(temporaryName), new FileSystemException(temporaryName, null, " "))
                .map(failure -> DynamicTest.dynamicTest(failure.toString(), () -> {
                    Path file = Files.createTempDirectory(temporaryFolder, "filesystem-").resolve("addressbook.json");
                    Model model = new ModelManager();
                    model.addPerson(AMY);
                    FailOnceStorage storage = new FailOnceStorage(file);
                    storage.saveAddressBook(model.getAddressBook());
                    byte[] original = Files.readAllBytes(file);
                    Logic logic = new LogicManager(model, new StorageManager(storage,
                            new JsonUserPrefsStorage(file.resolveSibling("preferences.json"))));
                    storage.failNextSave(failure);

                    CommandException error = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, ()
                            -> logic.execute("loglesson 1 c/New lesson"));
                    String reason = failure.getReason();
                    if (reason == null || reason.isBlank()) {
                        reason = "The file or folder could not be written.";
                    }
                    assertEquals(String.format(LogicManager.FILE_OPS_FILESYSTEM_ERROR_FORMAT, file, reason),
                            error.getMessage());
                    assertFalse(error.getMessage().contains(".tutorlah-"));
                    assertSame(failure, error.getCause());
                    assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
                    assertArrayEquals(original, Files.readAllBytes(file));
                    logic.execute("loglesson 1 c/New lesson");
                    assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
                }));
    }

    @Test
    public void execute_readOnlyDataFolder_reportsDataFileAndPreservesSavedData() throws Exception {
        Path directory = Files.createDirectory(temporaryFolder.resolve("data"));
        Assumptions.assumeTrue(Files.getFileStore(directory).supportsFileAttributeView("posix"),
                "Read-only directory permissions require a POSIX filesystem");
        Path file = directory.resolve("addressbook.json");
        Model model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BEN);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        storage.saveAddressBook(model.getAddressBook());
        byte[] original = Files.readAllBytes(file);
        Logic logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("find amy");
        Set<PosixFilePermission> previousPermissions = Files.getPosixFilePermissions(directory);
        try {
            Files.setPosixFilePermissions(directory,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
            Assumptions.assumeFalse(Files.isWritable(directory), "This user can bypass directory permissions");
            CommandException error = org.junit.jupiter.api.Assertions.assertThrows(CommandException.class, ()
                    -> logic.execute("loglesson 1 c/New lesson"));
            assertEquals(String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, file), error.getMessage());
            assertFalse(error.getMessage().contains(".tutorlah-"));
            assertEquals(List.of(AMY), model.getFilteredPersonList());
            assertEquals(List.of(AMY, BEN), model.getAddressBook().getPersonList());
            assertArrayEquals(original, Files.readAllBytes(file));
            try (var files = Files.list(directory)) {
                assertEquals(List.of(file), files.toList());
            }
        } finally {
            Files.setPosixFilePermissions(directory, previousPermissions);
        }
        logic.execute("loglesson 1 c/New lesson");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @TestFactory
    public Stream<DynamicTest> execute_filterCommands_skipSavingAndKeepRequestedFilter() {
        return Stream.of("list", "find ben", "flagged").map(command -> DynamicTest.dynamicTest(command, () -> {
            Path folder = Files.createTempDirectory(temporaryFolder, "filter-");
            Model model = new ModelManager();
            model.addPerson(AMY);
            model.addPerson(BEN);
            FailOnceStorage storage = new FailOnceStorage(folder.resolve("students.json"));
            storage.saveAddressBook(model.getAddressBook());
            Logic logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
            logic.execute("find ben");
            String savedData = Files.readString(storage.getAddressBookFilePath());
            storage.failNextSave(new IOException("simulated failure"));
            logic.execute(command);
            List<Person> expected = command.equals("list") ? List.of(AMY, BEN)
                    : command.equals("flagged") ? List.of(AMY) : List.of(BEN);
            assertEquals(expected, model.getFilteredPersonList());
            assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
            assertThrows(CommandException.class, () -> logic.execute("loglesson 1 c/New lesson"));
            assertEquals(expected, model.getFilteredPersonList());
            assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
        }));
    }

    /** JSON storage whose next save can fail before writing, then resume normal persistence. */
    private static class FailOnceStorage extends JsonAddressBookStorage {
        private IOException nextFailure;

        FailOnceStorage(Path filePath) {
            super(filePath);
        }

        void failNextSave(IOException failure) {
            nextFailure = failure;
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            if (nextFailure != null) {
                IOException failure = nextFailure;
                nextFailure = null;
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
