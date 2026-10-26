package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void saveAddressBook_afterFailedRead_blocksBothSaveOverloadsWithoutChangingFiles() throws Exception {
        Path file = testFolder.resolve("invalid.json");
        Files.writeString(file, "{broken student data}");
        byte[] original = Files.readAllBytes(file);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());

        assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                storage.saveAddressBook(new AddressBook()));
        assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                storage.saveAddressBook(getTypicalAddressBook(), file));
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    public void saveAddressBook_afterRepair_staysBlockedUntilNewStorageSession() throws Exception {
        Path file = testFolder.resolve("repaired.json");
        Files.writeString(file, "{broken student data}");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        Files.writeString(file, "{\"persons\":[]}");
        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                storage.saveAddressBook(getTypicalAddressBook()));
        assertEquals("{\"persons\":[]}", Files.readString(file));

        JsonAddressBookStorage restarted = new JsonAddressBookStorage(file);
        restarted.readAddressBook();
        restarted.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), new AddressBook(restarted.readAddressBook().orElseThrow()));
    }

    @TestFactory
    public Stream<DynamicTest> readAddressBook_nullJson_blocksBothSaveOverloads() {
        return Stream.of("null", "{\"persons\":[null]}").map(contents -> DynamicTest.dynamicTest(contents, () -> {
            Path file = Files.createTempFile(testFolder, "null-data-", ".json");
            Files.writeString(file, contents);
            byte[] original = Files.readAllBytes(file);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
            assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
            assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                    storage.saveAddressBook(new AddressBook()));
            Path otherFile = file.resolveSibling("other.json");
            assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                    storage.saveAddressBook(new AddressBook(), otherFile));
            assertFalse(Files.exists(otherFile));
            assertArrayEquals(original, Files.readAllBytes(file));
        }));
    }

    @Test
    public void readAddressBook_ioFailure_blocksSaving() throws Exception {
        Path directory = testFolder.resolve("students.json");
        Files.createDirectory(directory);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(directory);
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                storage.saveAddressBook(getTypicalAddressBook()));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
