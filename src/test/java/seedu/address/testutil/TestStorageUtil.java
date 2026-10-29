package seedu.address.testutil;

import java.io.IOException;
import java.nio.file.Path;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Builds student storage for tests that must not save, or whose saves must fail.
 */
public class TestStorageUtil {

    /**
     * Returns storage that reads {@code file} but fails the test with {@code reason} if a command tries to save.
     */
    public static JsonAddressBookStorage savingForbidden(Path file, String reason) {
        return new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError(reason);
            }
        };
    }

    /**
     * Returns storage that reads {@code file} but throws {@code exception} whenever a command saves.
     */
    public static JsonAddressBookStorage failingSaves(Path file, IOException exception) {
        return new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw exception;
            }
        };
    }
}
