package seedu.address.testutil;

import java.nio.file.Path;

import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Builds {@code LogicManager} instances for tests that execute commands against real or fake student storage.
 */
public class TestLogicUtil {

    /**
     * Returns a {@code LogicManager} that runs commands on {@code model} and saves students to {@code studentStorage}.
     * Preferences go to {@code preferences.json} in {@code folder}; these tests never read them back.
     */
    public static LogicManager logicOver(Model model, JsonAddressBookStorage studentStorage, Path folder) {
        return new LogicManager(model, new StorageManager(studentStorage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}
