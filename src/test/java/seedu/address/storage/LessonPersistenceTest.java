package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestLogicUtil.logicOver;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonDateUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that students and their lesson records survive saving and reloading the data file,
 * and that malformed lesson data and failed writes follow the existing storage contract.
 * A "restart" is simulated by reading the saved data file back with a fresh storage object,
 * which is what {@code MainApp} does on startup.
 */
public class LessonPersistenceTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "LessonPersistenceTest");

    private static final Person ALEX = new PersonBuilder().withName("Alex Lim").withPhone("91234567")
            .withLevel("Sec 3").withSubject("E-Math").build();
    private static final Person BEA = new PersonBuilder().withName("Bea Tan").withPhone("98765432")
            .withLevel("JC1").withSubject("H2 Math").build();
    private static final Person CHEN = new PersonBuilder().withName("Chen Wei").withPhone("87654321")
            .withLevel("Sec 1").withSubject("E-Math").build();

    @TempDir
    public Path testFolder;

    private Path dataFile;
    private Model model;
    private LogicManager logic;

    /**
     * Starts the app logic with {@code students} and a data file in a temporary folder.
     */
    private void startWith(Person... students) throws IOException {
        AddressBook addressBook = new AddressBook();
        for (Person student : students) {
            addressBook.addPerson(student);
        }
        dataFile = testFolder.resolve("addressbook.json");
        model = new ModelManager(addressBook, new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        storage.saveAddressBook(addressBook);
        logic = logicOver(model, storage, testFolder);
    }

    /**
     * Reads the data file back from disk with a fresh storage object, as the app does on startup.
     */
    private ReadOnlyAddressBook restart() throws DataLoadingException {
        return new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow();
    }

    private static List<Lesson> lessons(String... contents) {
        return Arrays.stream(contents).map(Lesson::new).toList();
    }

    /**
     * Returns lessons with {@code contents}, as logged today through the app.
     */
    private static List<Lesson> lessonsLoggedToday(String... contents) {
        return Arrays.stream(contents).map(LessonDateUtil::loggedToday).toList();
    }

    private static Person findByName(ReadOnlyAddressBook addressBook, String name) {
        return addressBook.getPersonList().stream()
                .filter(person -> person.getName().fullName.equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No student named " + name + " after reload"));
    }

    @Test
    public void restart_multipleStudentsWithDistinctHistories_eachHistoryKept() throws Exception {
        startWith(ALEX, BEA, CHEN);
        logic.execute("loglesson 1 c/Quadratic inequalities");
        logic.execute("loglesson 2 c/Binomial theorem");
        logic.execute("loglesson 1 c/Surds and indices");
        // student 3 has no lessons

        ReadOnlyAddressBook reloaded = restart();

        assertEquals(model.getAddressBook(), new AddressBook(reloaded));
        assertEquals(lessonsLoggedToday("Quadratic inequalities", "Surds and indices"),
                findByName(reloaded, "Alex Lim").getLessons());
        assertEquals(lessonsLoggedToday("Binomial theorem"), findByName(reloaded, "Bea Tan").getLessons());
        assertEquals(List.of(), findByName(reloaded, "Chen Wei").getLessons());
    }

    @Test
    public void restart_duplicateLessonsAndOrder_kept() throws Exception {
        startWith(ALEX);
        logic.execute("loglesson 1 c/Quadratic inequalities");
        logic.execute("loglesson 1 c/Binomial theorem");
        logic.execute("loglesson 1 c/Quadratic inequalities");
        logic.execute("loglesson 1 c/Surds   and   indices");

        List<Lesson> expected = lessonsLoggedToday("Quadratic inequalities", "Binomial theorem",
                "Quadratic inequalities", "Surds   and   indices");
        assertEquals(expected, findByName(restart(), "Alex Lim").getLessons());
    }

    @Test
    public void restart_reloadAndSaveAgain_lessonsUnchanged() throws Exception {
        startWith(ALEX, BEA);
        logic.execute("loglesson 1 c/Quadratic inequalities");
        logic.execute("loglesson 2 c/Binomial theorem");
        ReadOnlyAddressBook firstLoad = restart();

        // second session: start from the reloaded data and save it again unchanged
        new JsonAddressBookStorage(dataFile).saveAddressBook(firstLoad);

        assertEquals(new AddressBook(firstLoad), new AddressBook(restart()));
    }

    @Test
    public void restart_afterDeletingStudent_theirLessonsGoneOthersKept() throws Exception {
        startWith(ALEX, BEA);
        logic.execute("loglesson 1 c/Quadratic inequalities");
        logic.execute("loglesson 2 c/Binomial theorem");

        logic.execute("delete 1");
        ReadOnlyAddressBook reloaded = restart();

        assertEquals(1, reloaded.getPersonList().size());
        assertEquals(lessonsLoggedToday("Binomial theorem"), findByName(reloaded, "Bea Tan").getLessons());
        assertFalse(Files.readString(dataFile).contains("Quadratic inequalities"));
    }

    @Test
    public void restart_afterEditingStudent_lessonsKept() throws Exception {
        startWith(ALEX);
        logic.execute("loglesson 1 c/Quadratic inequalities");

        logic.execute("edit 1 n/Alex Tan l/Sec 4");

        assertEquals(lessonsLoggedToday("Quadratic inequalities"),
                findByName(restart(), "Alex Tan").getLessons());
    }

    /**
     * Deletes the newest duplicate through typed commands and checks the remaining history after a restart.
     */
    @Test
    public void restart_afterDeletingLesson_selectedOccurrenceRemovedOthersKept() throws Exception {
        startWith(new PersonBuilder(ALEX).withLessons("Algebra", "Geometry", "Algebra").build(),
                new PersonBuilder(BEA).withLessons("Calculus").build());
        assertEquals("Deleted lesson 1 for Alex Lim (undated): Algebra",
                logic.execute("deletelesson 1 1").getFeedbackToUser());
        ReadOnlyAddressBook reloaded = restart();
        assertEquals(new PersonBuilder(ALEX).withLessons("Algebra", "Geometry").build(),
                findByName(reloaded, "Alex Lim"));
        assertEquals(new PersonBuilder(BEA).withLessons("Calculus").build(), findByName(reloaded, "Bea Tan"));

        model = new ModelManager(reloaded, new UserPrefs());
        logic = logicOver(model, new JsonAddressBookStorage(dataFile), testFolder);
        assertEquals("Lesson records for Alex Lim (2):\n1. (undated): Geometry\n2. (undated): Algebra",
                logic.execute("view 1").getFeedbackToUser());
        logic.execute("deletelesson 1 2");
        logic.execute("deletelesson 1 1");
        assertEquals(ALEX, findByName(restart(), "Alex Lim"));
        assertEquals("No lessons logged yet for Alex Lim.", logic.execute("view 1").getFeedbackToUser());
    }

    /**
     * Keeps both saved and in-memory data unchanged after malformed or out-of-range deletion commands.
     */
    @Test
    public void deleteLesson_invalidInput_keepsSavedData() throws Exception {
        startWith(new PersonBuilder(ALEX).withLessons("Algebra").build(), BEA);
        logic.execute("list");
        String saved = Files.readString(dataFile);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(ParseException.class, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX, () ->
                logic.execute("deletelesson 1 01"));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX, () ->
                logic.execute("deletelesson 1 2"));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, () ->
                logic.execute("deletelesson 3 1"));
        assertThrows(CommandException.class, "No lessons logged yet for Bea Tan.", () ->
                logic.execute("deletelesson 2 1"));
        assertEquals(before, model.getAddressBook());
        assertEquals(saved, Files.readString(dataFile));
    }

    /**
     * Reports failed deletion saves through the existing storage contract and preserves the last saved file.
     */
    @Test
    public void deleteLesson_saveFails_reportsErrorAndKeepsLastSavedFile() throws Exception {
        for (IOException exception : List.of(new IOException("disk full"), new AccessDeniedException("read-only"))) {
            startWith(new PersonBuilder(ALEX).withLessons("Algebra").build());
            logic.execute("list");
            String saved = Files.readString(dataFile);
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataFile) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw exception;
                }
            };
            LogicManager failingLogic = logicOver(model, failingStorage, testFolder);
            String format = exception instanceof AccessDeniedException
                    ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
            Object detail = exception instanceof AccessDeniedException ? dataFile : exception.getMessage();
            assertThrows(CommandException.class, String.format(format, detail), () ->
                    failingLogic.execute("deletelesson 1 1"));
            assertEquals(saved, Files.readString(dataFile));
            assertEquals(lessons("Algebra"), findByName(restart(), "Alex Lim").getLessons());
        }
    }

    @Test
    public void read_dataFileWithoutLessons_loadsStudentsWithNoLessons() throws Exception {
        for (String file : List.of("noLessonsField.json", "nullLessonsField.json")) {
            ReadOnlyAddressBook loaded = new JsonAddressBookStorage(TEST_DATA_FOLDER.resolve(file))
                    .readAddressBook().orElseThrow();
            assertEquals(List.of(), findByName(loaded, "Alex Lim").getLessons(), file);
        }
    }

    @Test
    public void restart_loggedLesson_savesDateInIsoFormat() throws Exception {
        startWith(ALEX);
        logic.execute("loglesson 1 c/Quadratic inequalities");

        assertTrue(Files.readString(dataFile).contains("\"date\" : \"" + LocalDate.now() + "\""));
    }

    @Test
    public void read_dataFileWithUndatedLessons_loadsAndSavesThemUndated() throws Exception {
        // data files saved by v1.3 have lessons without a date field
        Path undatedFile = TEST_DATA_FOLDER.resolve("undatedLessons.json");
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(undatedFile).readAddressBook().orElseThrow();
        assertEquals(lessons("Quadratic inequalities", "Binomial theorem"),
                findByName(loaded, "Alex Lim").getLessons());

        dataFile = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(dataFile).saveAddressBook(loaded);
        assertFalse(Files.readString(dataFile).contains("date"));
        assertEquals(new AddressBook(loaded), new AddressBook(restart()));
    }

    @Test
    public void read_malformedLessonData_throwsDataLoadingException() {
        // the existing contract: invalid data is rejected as a whole, and the app then starts empty
        for (String file : List.of("blankLessonContent.json", "missingLessonContent.json",
                "lineBreakInLessonContent.json", "tooLongLessonContent.json", "lessonsNotAList.json",
                "nullLessonEntry.json", "validAndInvalidLesson.json", "invalidLessonDate.json")) {
            JsonAddressBookStorage storage = new JsonAddressBookStorage(TEST_DATA_FOLDER.resolve(file));
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    @Test
    public void log_saveFails_reportsErrorAndKeepsLastSavedFile() throws Exception {
        startWith(ALEX);
        logic.execute("loglesson 1 c/Quadratic inequalities");
        String lastSaved = Files.readString(dataFile);

        assertLogSaveFailure(new IOException("disk full"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "disk full"));
        assertLogSaveFailure(new AccessDeniedException("read-only"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, dataFile));

        // the file on disk is untouched by the failed saves
        assertEquals(lastSaved, Files.readString(dataFile));
        assertEquals(lessonsLoggedToday("Quadratic inequalities"),
                findByName(restart(), "Alex Lim").getLessons());
    }

    /**
     * Runs a {@code loglesson} command against storage that fails to save with {@code exception},
     * and checks the error message shown to the user.
     */
    private void assertLogSaveFailure(IOException exception, String expectedMessage) {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw exception;
            }
        };
        LogicManager failingLogic = logicOver(model, failingStorage, testFolder);

        assertThrows(CommandException.class, expectedMessage, ()
                -> failingLogic.execute("loglesson 1 c/Binomial theorem"));
        assertTrue(Files.exists(dataFile));
    }
}
