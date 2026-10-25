package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises the real startup model initialization and subsequent commands with isolated data files.
 */
public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @TestFactory
    public Stream<DynamicTest> initModelManager_unreadableFile_commandsPreserveOriginalBytes() {
        String malformed = "{\"persons\": [\n  {\"name\": \"Amy Tan\"";
        String invalidPhone = "{\"persons\":[{\"name\":\"Amy Tan\",\"phone\":\"12\","
                + "\"address\":\"10 Example Street\",\"level\":\"Sec 3\",\"subject\":\"E-Math\"}]}";
        return Stream.of(malformed, invalidPhone).map(contents -> DynamicTest.dynamicTest(contents, () -> {
            Path folder = Files.createTempDirectory(temporaryFolder, "recovery-");
            Path file = folder.resolve("students.json");
            byte[] original = contents.getBytes(StandardCharsets.UTF_8);
            Files.write(file, original);
            StorageManager storage = storage(file);
            MainApp app = new MainApp();
            Model model = app.initModelManager(storage, new UserPrefs());
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            String warning = app.getStartupWarning().orElseThrow();
            assertTrue(warning.contains(file.toAbsolutePath().toString()));
            assertTrue(warning.contains(JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED));
            assertArrayEquals(original, Files.readAllBytes(file));

            Logic logic = new LogicManager(model, storage);
            for (String command : List.of("list", "find amy", "help", "exit",
                    "add n/Ben Lim p/81230012 a/20 Example Street l/JC1 sub/H2 Math")) {
                assertThrows(CommandException.class, LogicManager.FILE_OPS_ERROR_FORMAT.formatted(
                        JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED), () -> logic.execute(command));
                assertArrayEquals(original, Files.readAllBytes(file));
            }
        }));
    }

    @Test
    public void initModelManager_missingFile_usesSamplesAndAllowsSaving() throws Exception {
        Path file = temporaryFolder.resolve("missing.json");
        StorageManager storage = storage(file);
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        assertFalse(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(app.getStartupWarning().isEmpty());
        new LogicManager(model, storage).execute("list");
        assertEquals(model.getAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void initModelManager_validLegacyData_keepsUndatedLessonsAndNormalSaving() throws Exception {
        Path file = temporaryFolder.resolve("legacy.json");
        Person amy = new PersonBuilder().withName("Amy Tan").withPhone("81230011")
                .withLessons("Algebra").build();
        AddressBook expected = new AddressBook();
        expected.addPerson(amy);
        new JsonAddressBookStorage(file).saveAddressBook(expected);
        StorageManager storage = storage(file);
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        assertEquals(expected, model.getAddressBook());
        assertTrue(app.getStartupWarning().isEmpty());
        new LogicManager(model, storage).execute("log 1 c/Geometry");
        var lessons = storage.readAddressBook().orElseThrow().getPersonList().getFirst().getLessons();
        assertEquals(2, lessons.size());
        assertTrue(lessons.getFirst().getDate().isEmpty());
        assertTrue(lessons.getLast().getDate().isPresent());
    }

    @Test
    public void initModelManager_repairedFileAndNewSession_loadsAndSavesNormally() throws Exception {
        Path file = temporaryFolder.resolve("repaired.json");
        Files.writeString(file, "not JSON");
        MainApp app = new MainApp();
        app.initModelManager(storage(file), new UserPrefs());
        assertTrue(app.getStartupWarning().isPresent());

        Files.writeString(file, "{\"persons\":[]}");
        StorageManager restartedStorage = storage(file);
        Model model = app.initModelManager(restartedStorage, new UserPrefs());
        assertTrue(app.getStartupWarning().isEmpty());
        new LogicManager(model, restartedStorage)
                .execute("add n/Amy Tan p/81230011 a/10 Example Street l/Sec 3 sub/E-Math");
        assertEquals(1, restartedStorage.readAddressBook().orElseThrow().getPersonList().size());
    }

    private StorageManager storage(Path file) {
        return new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
