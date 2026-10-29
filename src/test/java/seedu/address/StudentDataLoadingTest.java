package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises imported data, recovery protection and subsequent commands with disposable files. */
public class StudentDataLoadingTest {
    @TempDir
    public Path temporaryFolder;

    @TestFactory
    public Stream<DynamicTest> load_normalizedDuplicates_explainsFailureAndProtectsBothStudents() {
        return Stream.of("Amy  Tan", " Amy Tan ", "aMY tAN", "\tAmy \tTan\n", "\u00a0Amy\u2003Tan\u00a0")
                .map(name -> DynamicTest.dynamicTest("Duplicate name: " + name, () -> {
                    String first = studentJson("Amy Tan", "81230001",
                            "[{\"content\":\"Algebra\",\"date\":\"2026-10-20\"}]");
                    String second = studentJson(name, "81230001", "[{\"content\":\"Geometry\"}]");
                    assertProtectedRecovery("{\"persons\":[" + first + "," + second + "]}",
                            "Persons list contains duplicate person(s).");
                }));
    }

    @TestFactory
    public Stream<DynamicTest> load_invalidLessonDates_explainsFailureAndPreservesOriginalFile() {
        return Stream.of("1990-01-01", "1999-12-31", "2026-02-30", "20/10/2026")
                .map(date -> DynamicTest.dynamicTest(date, () -> assertProtectedRecovery(
                        "{\"persons\":[" + studentJson("Amy Tan", "81230001",
                                "[{\"content\":\"Algebra\",\"date\":\"" + date + "\"}]") + "]}",
                        "YYYY-MM-DD")));
    }

    @Test
    public void load_invalidPhone_reportsConstraintInStartupWarning() throws Exception {
        assertProtectedRecovery("{\"persons\":[" + studentJson("Amy Tan", "1234", "[]") + "]}",
                Phone.MESSAGE_CONSTRAINTS);
    }

    @TestFactory
    public Stream<DynamicTest> load_malformedJson_reportsLocationWithoutQuotingStudentData() throws Exception {
        String student = studentJson("Private Example", "81234567", "[]");
        return Stream.of("{\"persons\":[\n" + student, "{\"persons\":[" + student + ",]}",
                "\ufeff{\"persons\":[" + student + "]}").map(contents -> DynamicTest.dynamicTest(contents, () -> {
                    String warning = assertProtectedRecovery(contents, "Reason: The file is not valid JSON (line ");
                    assertTrue(Pattern.compile("Reason: The file is not valid JSON \\(line [1-9][0-9]*, "
                            + "column [1-9][0-9]*\\)\\.").matcher(warning).find(), warning);
                    assertSanitizedWarning(warning);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> load_invalidJsonStructure_showsSafeRecoveryGuidance() {
        return Stream.of("{\"persons\":null}", "{}", "null", "[]",
                "{\"persons\":\"Private Example 81234567\"}").map(contents -> DynamicTest.dynamicTest(contents, () -> {
                    String warning = assertProtectedRecovery(contents,
                            "Reason: Check that the file is readable and contains valid student data.");
                    assertSanitizedWarning(warning);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> load_trailingContent_protectsOriginalFileAndSanitizesWarning() throws IOException {
        String trailingStudents = "{\"persons\":[" + studentJson("Private Example", "81234567", "[]") + "]}";
        return Stream.of(trailingStudents, "42", "null", "garbage")
                .map(suffix -> DynamicTest.dynamicTest(suffix, () -> {
                    String warning = assertProtectedRecovery("{\"persons\":[]}\n" + suffix,
                            "Reason: The file is not valid JSON (line 2, column ");
                    assertSanitizedWarning(warning);
                }));
    }

    @Test
    public void init_trailingStudentData_blocksMutationAndPreservesBytesAfterStop() throws Exception {
        Path file = temporaryFolder.resolve("data/addressbook.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{\"persons\":[]}\n{\"persons\":["
                + studentJson("Private Example", "81234567", "[]") + "]}");
        byte[] original = Files.readAllBytes(file);
        MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"), file);

        app.init();
        try {
            String warning = app.getStartupWarning().orElseThrow();
            assertTrue(warning.contains("Reason: The file is not valid JSON (line 2, column 1)."), warning);
            assertSanitizedWarning(warning);
            assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
            assertEquals("No students found. Use add to create a student.",
                    app.logic.execute("list").getFeedbackToUser());
            app.logic.execute("help");
            assertThrows(CommandException.class, LogicManager.FILE_OPS_ERROR_FORMAT.formatted(
                    JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED), () -> app.logic.execute(
                            "add n/Ben Lim p/81230002 addr/2 Example Street l/Sec 3 sub/Math"));
            assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
        } finally {
            app.stop();
        }
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    public void load_legacyHistoryWithTrailingWhitespace_keepsRecordsAndAllowsSaving() throws Exception {
        Path file = temporaryFolder.resolve("students.json");
        Files.writeString(file, "{\"persons\":[" + studentJson("Amy Tan", "81230001",
                "[{\"content\":\"Algebra\"}]") + "]} \t\r\n");
        byte[] original = Files.readAllBytes(file);
        StorageManager storage = storage(file);
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        Logic logic = new LogicManager(model, storage);

        assertTrue(app.getStartupWarning().isEmpty());
        assertEquals("Lesson records for Amy Tan (1):\n1. (undated): Algebra",
                logic.execute("view 1").getFeedbackToUser());
        assertArrayEquals(original, Files.readAllBytes(file));
        logic.execute("loglesson 1 c/Geometry date/20/10/2026");
        assertEquals(List.of(new Lesson("Algebra"), new Lesson("Geometry", LocalDate.of(2026, 10, 20))),
                model.getAddressBook().getPersonList().getFirst().getLessons());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void load_sameNormalizedNameWithDifferentPhones_keepsBothStudentsAndReadOnlyBytes() throws Exception {
        Path file = temporaryFolder.resolve("students.json");
        Files.writeString(file, "{\"persons\":[" + studentJson(" Amy  Tan ", "81230001", "[]") + ","
                + studentJson("aMY   tAN", "81230002", "[]") + "]}");
        byte[] original = Files.readAllBytes(file);
        StorageManager storage = storage(file);
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        Logic logic = new LogicManager(model, storage);

        assertTrue(app.getStartupWarning().isEmpty());
        assertEquals(List.of("Amy Tan", "aMY tAN"), model.getAddressBook().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
        assertEquals("Listed 2 students.", logic.execute("list").getFeedbackToUser());
        assertArrayEquals(original, Files.readAllBytes(file));
        assertThrows(CommandException.class, "This student already exists in the list.", () -> logic.execute(
                "add n/AMY  TAN p/81230001 addr/2 Example Street l/Sec 3 sub/Math"));
        assertArrayEquals(original, Files.readAllBytes(file));

        logic.execute("loglesson 1 c/Algebra date/20/10/2026");
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void load_unsortedHistory_commandsUseChronologicalNumbersAndPersistEveryOccurrence() throws Exception {
        Lesson newer = new Lesson("Newer", LocalDate.of(2026, 10, 20));
        Lesson older = new Lesson("Older", LocalDate.of(2026, 10, 10));
        Lesson sameDay = new Lesson("Same day", LocalDate.of(2026, 10, 20));
        Lesson undated = new Lesson("Undated");
        Person amy = new PersonBuilder().withName("Amy Tan").withFlag("Call parent").build();
        Person unsorted = new Person(amy.getName(), amy.getPhone(), amy.getAddress(), amy.getLevel(), amy.getSubject(),
                List.of(newer, undated, older, sameDay, sameDay), amy.getFlag());
        AddressBook data = new AddressBook();
        data.addPerson(unsorted);
        Path file = temporaryFolder.resolve("students.json");
        new JsonAddressBookStorage(file).saveAddressBook(data);
        byte[] original = Files.readAllBytes(file);
        StorageManager storage = storage(file);
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        Logic logic = new LogicManager(model, storage);

        assertTrue(app.getStartupWarning().isEmpty());
        assertEquals("Lesson records for Amy Tan (5):\n1. 20/10/2026: Same day\n2. 20/10/2026: Same day"
                + "\n3. 20/10/2026: Newer\n4. 10/10/2026: Older\n5. (undated): Undated",
                logic.execute("view 1").getFeedbackToUser());
        assertArrayEquals(original, Files.readAllBytes(file));

        logic.execute("loglesson 1 c/Mid date/15/10/2026");
        assertTrue(logic.execute("view 1").getFeedbackToUser().contains("4. 15/10/2026: Mid\n5. 10/10/2026: Older"));
        logic.execute("editlesson 1 5 c/Older corrected date/21/10/2026");
        assertTrue(logic.execute("view 1").getFeedbackToUser().contains("1. 21/10/2026: Older corrected"));
        logic.execute("deletelesson 1 3");
        assertEquals("Matching lesson records for Amy Tan (1):\n2. 20/10/2026: Same day",
                logic.execute("findlesson 1 same").getFeedbackToUser());

        Person result = model.getAddressBook().getPersonList().getFirst();
        assertEquals(List.of(undated, new Lesson("Mid", LocalDate.of(2026, 10, 15)), newer, sameDay,
                new Lesson("Older corrected", LocalDate.of(2026, 10, 21))), result.getLessons());
        assertEquals(amy.getFlag(), result.getFlag());
        assertEquals(amy.getName(), result.getName());
        assertEquals(amy.getPhone(), result.getPhone());
        assertEquals(amy.getAddress(), result.getAddress());
        assertEquals(amy.getLevel(), result.getLevel());
        assertEquals(amy.getSubject(), result.getSubject());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(file).readAddressBook().orElseThrow());
    }

    @Test
    public void initModelManager_unrecognisedOrEmptyError_showsRecoveryGuidance() {
        for (Exception cause : new Exception[]{null, new IOException(), new IOException("   "),
            new IOException("Private Example 81234567 [Source: file contents]"),
            new IllegalValueException(null), new IllegalValueException("   ")}) {
            StorageManager storage = new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
                @Override
                public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                    throw new DataLoadingException(cause);
                }
            };
            MainApp app = new MainApp();
            app.initModelManager(storage, new UserPrefs());

            String warning = app.getStartupWarning().orElseThrow();
            assertTrue(warning.contains("Reason: Check that the file is readable and contains valid student data."));
            assertFalse(warning.contains("null"));
            assertTrue(warning.contains(JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED));
            assertSanitizedWarning(warning);
        }
    }

    @Test
    public void initModelManager_jsonErrorWithoutKnownLocation_omitsInvalidCoordinatesAndRawMessage() {
        for (JsonLocation location : new JsonLocation[]{null, JsonLocation.NA, new JsonLocation(null, -1L, 1, -1)}) {
            StorageManager storage = new StorageManager(
                    new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))) {
                @Override
                public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                    throw new DataLoadingException(new JsonParseException(null,
                            "Private Example 81234567 [Source: file contents]", location));
                }
            };
            MainApp app = new MainApp();
            app.initModelManager(storage, new UserPrefs());

            String warning = app.getStartupWarning().orElseThrow();
            assertTrue(warning.contains("Reason: The file is not valid JSON.\n"), warning);
            assertFalse(warning.contains("line -1"));
            assertFalse(warning.contains("column -1"));
            assertSanitizedWarning(warning);
        }
    }

    @Test
    public void init_transferredFileIntoFreshFolder_loadsStudentsLessonsAndFlags() throws Exception {
        Path source = temporaryFolder.resolve("old/data/addressbook.json");
        Files.createDirectories(source.getParent());
        Files.writeString(source, "{\"persons\":[]}");
        MainApp oldApp = new MainApp(temporaryFolder.resolve("old/preferences.json"), source);
        oldApp.init();
        oldApp.logic.execute("add n/Amy Tan p/81230001 addr/1 Example Street l/Sec 3 sub/Math");
        oldApp.logic.execute("loglesson 1 c/Algebra date/20/10/2026");
        oldApp.logic.execute("flag 1 r/Call parent");
        oldApp.stop();
        byte[] original = Files.readAllBytes(source);

        Path destination = temporaryFolder.resolve("new/data/addressbook.json");
        assertFalse(Files.exists(destination.getParent()));
        Files.createDirectories(destination.getParent());
        Files.copy(source, destination);
        MainApp newApp = new MainApp(temporaryFolder.resolve("new/preferences.json"), destination);
        newApp.init();
        try {
            assertTrue(newApp.getStartupWarning().isEmpty());
            assertEquals(oldApp.model.getAddressBook(), newApp.model.getAddressBook());
            assertEquals("Listed 1 student.", newApp.logic.execute("list").getFeedbackToUser());
            assertTrue(newApp.logic.execute("view 1").getFeedbackToUser().contains("20/10/2026: Algebra"));
        } finally {
            newApp.stop();
        }
        assertArrayEquals(original, Files.readAllBytes(source));
        assertArrayEquals(original, Files.readAllBytes(destination));
    }

    private String assertProtectedRecovery(String contents, String reason) throws Exception {
        Path file = Files.createTempDirectory(temporaryFolder, "recovery-").resolve("students.json");
        Files.writeString(file, contents);
        byte[] original = Files.readAllBytes(file);
        StorageManager storage = storage(file);
        MainApp app = new MainApp();
        Model model = app.initModelManager(storage, new UserPrefs());
        String warning = app.getStartupWarning().orElseThrow();
        assertTrue(warning.contains(file.toAbsolutePath().toString()));
        assertTrue(warning.contains("Reason: "));
        assertTrue(warning.contains(reason), warning);
        assertTrue(warning.contains(JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        Logic logic = new LogicManager(model, storage);
        logic.execute("list");
        logic.execute("help");
        assertThrows(CommandException.class, LogicManager.FILE_OPS_ERROR_FORMAT.formatted(
                JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED), () -> logic.execute(
                        "add n/Ben Lim p/81230002 addr/2 Example Street l/Sec 3 sub/Math"));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertArrayEquals(original, Files.readAllBytes(file));

        Files.writeString(file, "{\"persons\":[]}");
        assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                storage.saveAddressBook(new AddressBook()));
        StorageManager restarted = storage(file);
        Model repairedModel = app.initModelManager(restarted, new UserPrefs());
        assertTrue(app.getStartupWarning().isEmpty());
        new LogicManager(repairedModel, restarted).execute(
                "add n/Ben Lim p/81230002 addr/2 Example Street l/Sec 3 sub/Math");
        assertEquals(repairedModel.getAddressBook(), restarted.readAddressBook().orElseThrow());
        return warning;
    }

    private void assertSanitizedWarning(String warning) {
        for (String internalText : List.of("[Source:", "seedu.address", "java.lang", "NullPointerException",
            "JsonSerializableAddressBook", "Private Example", "81234567", "0xfeff")) {
            assertFalse(warning.contains(internalText), warning);
        }
    }

    private StorageManager storage(Path file) {
        return new StorageManager(new JsonAddressBookStorage(file),
                new JsonUserPrefsStorage(file.resolveSibling("preferences.json")));
    }

    private String studentJson(String name, String phone, String lessons) throws IOException {
        return """
                {"name":%s,"phone":%s,"address":"1 Example Street","level":"Sec 3","subject":"Math",
                 "lessons":%s,"flag":{"reason":"Call parent"}}
                """.formatted(JsonUtil.toJsonString(name), JsonUtil.toJsonString(phone), lessons);
    }
}
