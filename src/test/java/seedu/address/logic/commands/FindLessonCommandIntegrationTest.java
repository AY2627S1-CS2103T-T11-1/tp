package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests typed lesson searches with filters, later mutations and real JSON persistence.
 */
public class FindLessonCommandIntegrationTest {
    private static final LocalDate DAY = LocalDate.of(2020, 1, 2);
    private static final Person BEN = new PersonBuilder().withName("Ben Lim").withPhone("81230001").build();
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withPhone("81230002")
            .withFlag("Call parent").withLessons("Algebra legacy").build()
            .withLessonAdded(new Lesson("Algebra", DAY))
            .withLessonAdded(new Lesson("Geometry", DAY.plusDays(1)))
            .withLessonAdded(new Lesson("Algebra", DAY.plusDays(2)))
            .withLessonAdded(new Lesson("Vectors", DAY.plusDays(3)));
    private static final String ALGEBRA_MATCHES = "Matching lesson records for Amy Tan (3):"
            + "\n2. 04/01/2020: Algebra\n4. 02/01/2020: Algebra\n5. (undated): Algebra legacy";

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private Path dataFile;
    private byte[] originalData;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(BEN);
        model.addPerson(AMY);
        dataFile = temporaryFolder.resolve("students.json");
        storage = new JsonAddressBookStorage(dataFile);
        storage.saveAddressBook(model.getAddressBook());
        originalData = Files.readAllBytes(dataFile);
        logic = logic(storage);
    }

    @Test
    public void execute_studentFilters_useDisplayedIndexAndRetainCompleteSavedData() throws Exception {
        for (String filter : List.of("find amy", "flagged")) {
            logic.execute(filter);
            assertEquals(ALGEBRA_MATCHES, feedback("findlesson 1 ALG"));
            assertEquals(List.of(AMY), logic.getFilteredPersonList());
            assertThrows(CommandException.class, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, ()
                    -> logic.execute("findlesson 2 algebra"));
            assertUnchanged();
        }
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(BEN, AMY), reloaded.getFilteredPersonList());
    }

    @Test
    public void execute_dateAndContentCriteria_combineAndDistinguishEmptyResults() throws Exception {
        assertEquals("Matching lesson records for Amy Tan (1):\n4. 02/01/2020: Algebra",
                feedback("findlesson 2 algebra from/2/1/2020 to/2/1/2020"));
        assertEquals("Matching lesson records for Amy Tan (2):"
                + "\n3. 03/01/2020: Geometry\n4. 02/01/2020: Algebra", feedback("findlesson 2 to/3/1/2020"));
        assertEquals("Matching lesson records for Amy Tan (2):"
                + "\n1. 05/01/2020: Vectors\n2. 04/01/2020: Algebra", feedback("findlesson 2 from/4/1/2020"));
        assertEquals("No matching lesson records found for Amy Tan.", feedback("findlesson 2 from/31/12/9999"));
        assertEquals("No lessons logged yet for Ben Lim.", feedback("findlesson 1 algebra"));
        assertUnchanged();
    }

    @Test
    public void execute_laterSearchAndView_doNotRetainLessonFilter() throws Exception {
        assertEquals(ALGEBRA_MATCHES, feedback("findlesson 2 algebra"));
        assertEquals("Matching lesson records for Amy Tan (1):\n3. 03/01/2020: Geometry",
                feedback("findlesson 2 geometry"));
        assertEquals("Lesson records for Amy Tan (5):\n1. 05/01/2020: Vectors\n2. 04/01/2020: Algebra"
                + "\n3. 03/01/2020: Geometry\n4. 02/01/2020: Algebra\n5. (undated): Algebra legacy",
                feedback("view 2"));
        assertUnchanged();
    }

    @Test
    public void execute_searchThenEditAndDelete_targetsFullHistoryNumber() throws Exception {
        logic.execute("find amy");
        assertEquals(ALGEBRA_MATCHES, feedback("findlesson 1 algebra"));
        assertEquals("Edited lesson 4 for Amy Tan (now lesson 4). 02/01/2020: Algebra corrected",
                feedback("editlesson 1 4 c/Algebra corrected"));
        assertEquals(new Lesson("Algebra corrected", DAY),
                model.getFilteredPersonList().getFirst().getLessons().get(1));
        assertEquals(new Lesson("Algebra", DAY.plusDays(2)),
                model.getFilteredPersonList().getFirst().getLessons().get(3));
        assertEquals("Deleted lesson 2 for Amy Tan: Algebra", feedback("deletelesson 1 2"));
        assertEquals("Matching lesson records for Amy Tan (2):"
                + "\n3. 02/01/2020: Algebra corrected\n4. (undated): Algebra legacy", feedback("findlesson 1 algebra"));
        Person saved = storage.readAddressBook().orElseThrow().getPersonList().get(1);
        assertEquals(4, saved.getLessonCount());
        assertEquals(AMY.getFlag(), saved.getFlag());
        assertEquals(BEN, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
        assertEquals(List.of(saved), model.getFilteredPersonList());
    }

    @Test
    public void execute_identicalRecords_deletesOnlySelectedOccurrence() throws Exception {
        logic.execute("log 2 c/Algebra date/2/1/2020");
        assertEquals("Matching lesson records for Amy Tan (2):\n4. 02/01/2020: Algebra\n5. 02/01/2020: Algebra",
                feedback("findlesson 2 algebra from/2/1/2020 to/2/1/2020"));
        logic.execute("deletelesson 2 5");
        Person saved = storage.readAddressBook().orElseThrow().getPersonList().get(1);
        assertEquals(5, saved.getLessonCount());
        assertEquals(1, saved.getLessons().stream().filter(new Lesson("Algebra", DAY)::equals).count());
        assertEquals("Matching lesson records for Amy Tan (1):\n4. 02/01/2020: Algebra",
                feedback("findlesson 2 algebra from/2/1/2020 to/2/1/2020"));
    }

    @Test
    public void execute_newOrRedatedLesson_recomputesNumbersFromCurrentHistory() throws Exception {
        logic.execute("find amy");
        assertEquals(ALGEBRA_MATCHES, feedback("findlesson 1 algebra"));
        logic.execute("editlesson 1 4 date/6/1/2020");
        assertEquals("Matching lesson records for Amy Tan (3):"
                + "\n1. 06/01/2020: Algebra\n3. 04/01/2020: Algebra\n5. (undated): Algebra legacy",
                feedback("findlesson 1 algebra"));
        logic.execute("log 1 c/Algebra extension date/7/1/2020");
        assertEquals("Matching lesson records for Amy Tan (1):\n1. 07/01/2020: Algebra extension",
                feedback("findlesson 1 extension"));
        assertEquals(6, storage.readAddressBook().orElseThrow().getPersonList().get(1).getLessonCount());
    }

    @Test
    public void execute_invalidSearch_preservesFilterAndOriginalBytes() throws Exception {
        logic.execute("flagged");
        for (String command : List.of("findlesson 1", "findlesson 0 algebra", "findlesson 1 from/31/2/2020",
                "findlesson 1 from/4/1/2020 to/2/1/2020", "findlesson 1 to/2/1/2020 to/3/1/2020")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(AMY), model.getFilteredPersonList());
            assertUnchanged();
        }
    }

    @Test
    public void execute_savingUnavailable_returnsResultsWithoutAttemptingWrite() throws Exception {
        JsonAddressBookStorage unavailable = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError("Lesson searches must not save student data");
            }
        };
        assertEquals(ALGEBRA_MATCHES, logic(unavailable).execute("findlesson 2 algebra").getFeedbackToUser());
        assertUnchanged();
        Path absent = temporaryFolder.resolve("not-created.json");
        assertFalse(Files.exists(absent));
        assertEquals(ALGEBRA_MATCHES, logic(new JsonAddressBookStorage(absent))
                .execute("findlesson 2 algebra").getFeedbackToUser());
        assertFalse(Files.exists(absent));
    }

    private Logic logic(JsonAddressBookStorage studentStorage) {
        return new LogicManager(model, new StorageManager(studentStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private String feedback(String command) throws Exception {
        return logic.execute(command).getFeedbackToUser();
    }

    private void assertUnchanged() throws Exception {
        assertArrayEquals(originalData, Files.readAllBytes(dataFile));
        assertEquals(List.of(BEN, AMY), model.getAddressBook().getPersonList());
    }
}
