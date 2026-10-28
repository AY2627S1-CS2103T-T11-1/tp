package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
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

    @Test
    public void init_unreadableFile_initializesUiAndProtectedStorage() throws Exception {
        Path file = temporaryFolder.resolve("students.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        byte[] original = "{unreadable student data}".getBytes(StandardCharsets.UTF_8);
        Files.write(file, original);
        MainApp app = new MainApp(preferences, file);

        app.init();
        assertNotNull(app.ui);
        assertTrue(app.getStartupWarning().isPresent());
        assertEquals(file, app.storage.getAddressBookFilePath());
        assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        assertEquals(ListCommand.MESSAGE_EMPTY, app.logic.execute("list").getFeedbackToUser());
        app.stop();
        assertArrayEquals(original, Files.readAllBytes(file));
        assertTrue(Files.exists(preferences));
    }

    @Test
    public void init_validFile_initializesUiAndNormalSaving() throws Exception {
        Path file = temporaryFolder.resolve("students.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        Files.writeString(file, "{\"persons\":[]}");
        MainApp app = new MainApp(preferences, file);

        app.init();
        assertNotNull(app.ui);
        assertTrue(app.getStartupWarning().isEmpty());
        app.logic.execute("add n/Amy Tan p/81230011 addr/10 Example Street l/Sec 3 sub/E-Math");
        app.stop();
        assertEquals(app.model.getAddressBook(), new AddressBook(app.storage.readAddressBook().orElseThrow()));
        assertEquals(1, app.model.getAddressBook().getPersonList().size());
        assertTrue(Files.exists(preferences));
    }

    @Test
    public void init_sameDataFolderAlreadyLocked_blocksSecondInstanceUntilFirstStops() throws Exception {
        Path file = temporaryFolder.resolve("data").resolve("students.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        MainApp firstApp = new MainApp(preferences, file);
        MainApp secondApp = new MainApp(preferences, file);

        firstApp.init();
        try {
            secondApp.init();
            assertTrue(secondApp.getStartupFailure().orElseThrow().contains("already using this data folder"));
            assertNotNull(secondApp.ui);
            assertTrue(secondApp.logic == null);
        } finally {
            secondApp.stop();
            firstApp.stop();
        }

        MainApp restartedApp = new MainApp(preferences, file);
        restartedApp.init();
        try {
            assertTrue(restartedApp.getStartupFailure().isEmpty());
            assertNotNull(restartedApp.ui);
        } finally {
            restartedApp.stop();
        }
    }

    @Test
    public void init_lockFileCannotBeCreated_blocksStartup() throws Exception {
        Path nonDirectory = temporaryFolder.resolve("not-a-directory");
        Files.writeString(nonDirectory, "content");
        MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"),
                nonDirectory.resolve("students.json"));

        app.init();
        assertTrue(app.getStartupFailure().orElseThrow().contains("could not secure exclusive access"));
        assertNotNull(app.ui);
        assertTrue(app.logic == null);
        app.stop();
    }

    @Test
    public void init_failureAfterLock_releasesLock() throws Exception {
        Path file = temporaryFolder.resolve("data").resolve("students.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        MainApp failingApp = new MainApp(preferences, file) {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                throw new IllegalStateException("simulated initialization failure");
            }
        };

        assertThrows(IllegalStateException.class, "simulated initialization failure", failingApp::init);

        MainApp restartedApp = new MainApp(preferences, file);
        restartedApp.init();
        try {
            assertTrue(restartedApp.getStartupFailure().isEmpty());
            assertNotNull(restartedApp.ui);
        } finally {
            restartedApp.stop();
        }
    }

    @Test
    public void stop_lockReleaseFails_logsFailureAndClearsLock() throws Exception {
        Path file = temporaryFolder.resolve("data").resolve("students.json");
        Path preferences = temporaryFolder.resolve("preferences.json");
        AtomicInteger closeAttempts = new AtomicInteger();
        MainApp app = new MainApp(preferences, file) {
            @Override
            Optional<? extends Closeable> tryAcquireDataFolderLock() {
                return Optional.of(() -> {
                    closeAttempts.incrementAndGet();
                    throw new IOException("simulated release failure");
                });
            }
        };

        app.init();
        app.stop();
        app.stop();

        assertEquals(1, closeAttempts.get());
    }

    @Test
    public void stop_storageInitializedWithoutModel_doesNotSavePreferences() {
        Path preferences = temporaryFolder.resolve("preferences.json");
        MainApp app = new MainApp(preferences, temporaryFolder.resolve("students.json"));
        app.storage = new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(preferences));

        app.stop();

        assertFalse(Files.exists(preferences));
    }

    @Test
    public void init_nullJson_initializesUiAndProtectedStorage() throws Exception {
        for (String contents : List.of("null", "{\"persons\":[null]}")) {
            Path folder = Files.createTempDirectory(temporaryFolder, "null-startup-");
            Path file = folder.resolve("students.json");
            Files.writeString(file, contents);
            MainApp app = new MainApp(folder.resolve("preferences.json"), file);
            app.init();
            assertNotNull(app.ui);
            assertTrue(app.getStartupWarning().isPresent());
            assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
            assertTrue(app.logic.execute("help").isShowHelp());
            assertTrue(app.logic.execute("exit").isExit());
            app.stop();
            assertEquals(contents, Files.readString(file));
        }
    }

    @TestFactory
    public Stream<DynamicTest> initModelManager_unreadableFile_commandsPreserveOriginalBytes() {
        String malformed = "{\"persons\": [\n  {\"name\": \"Amy Tan\"";
        String invalidPhone = "{\"persons\":[{\"name\":\"Amy Tan\",\"phone\":\"12\","
                + "\"address\":\"10 Example Street\",\"level\":\"Sec 3\",\"subject\":\"E-Math\"}]}";
        Stream<String> invalidFiles = Stream.of(malformed, invalidPhone, "null", "{\"persons\":[null]}",
                "{\"persons\":null}", "{}");
        return invalidFiles.map(contents -> DynamicTest.dynamicTest(contents, () -> {
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
            for (String command : List.of("list", "find amy", "flagged", "summary", "help", "exit")) {
                var result = logic.execute(command);
                if (command.equals("help")) {
                    assertTrue(result.isShowHelp());
                    assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, result.getFeedbackToUser());
                } else if (command.equals("exit")) {
                    assertTrue(result.isExit());
                    assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, result.getFeedbackToUser());
                } else if (command.equals("summary")) {
                    assertEquals("Summary:\nNo students yet. Add a student to start recording lessons.",
                            result.getFeedbackToUser());
                } else if (command.equals("find amy")) {
                    assertEquals(FindCommand.MESSAGE_NO_NAME_MATCHES, result.getFeedbackToUser());
                }
                assertArrayEquals(original, Files.readAllBytes(file));
            }
            assertThrows(CommandException.class, LogicManager.FILE_OPS_ERROR_FORMAT.formatted(
                    JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED), () -> logic.execute(
                        "add n/Ben Lim p/81230012 addr/20 Example Street l/JC1 sub/H2 Math"));
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            assertArrayEquals(original, Files.readAllBytes(file));
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
        Logic logic = new LogicManager(model, storage);
        logic.execute("list");
        assertFalse(Files.exists(file));
        logic.execute("log 1 c/Algebra");
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
                .execute("add n/Amy Tan p/81230011 addr/10 Example Street l/Sec 3 sub/E-Math");
        assertEquals(1, restartedStorage.readAddressBook().orElseThrow().getPersonList().size());
    }

    private StorageManager storage(Path file) {
        return new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
