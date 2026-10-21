package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests typed find commands with storage and subsequent commands using displayed indexes.
 */
public class FindCommandIntegrationTest {
    private static final Person WEI_MING = new PersonBuilder().withName("Tan Wei Ming")
            .withPhone("81230001").withLessons("Algebra", "Geometry").build();
    private static final Person WEI_JIE = new PersonBuilder().withName("Tan Wei Jie")
            .withPhone("81230002").build();
    private static final Person ALICIA = new PersonBuilder().withName("Alicia Lim")
            .withPhone("81230003").withLessons("Calculus").build();

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private Path dataFile;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(WEI_MING);
        model.addPerson(WEI_JIE);
        model.addPerson(ALICIA);
        dataFile = temporaryFolder.resolve("students.json");
        storage = new JsonAddressBookStorage(dataFile);
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_findThenList_restoresAllStudentsWithoutChangingSavedData() throws Exception {
        logic.execute("list");
        String savedData = Files.readString(dataFile);
        AddressBook beforeFind = new AddressBook(model.getAddressBook());
        assertEquals("Found 2 students.", logic.execute("find WEI").getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE), logic.getFilteredPersonList());
        assertEquals("Found 1 student.", logic.execute("find ali").getFeedbackToUser());
        assertEquals(List.of(ALICIA), logic.getFilteredPersonList());
        assertEquals(FindCommand.MESSAGE_NO_MATCHES, logic.execute("find unknown").getFeedbackToUser());
        assertEquals("Found 1 student.", logic.execute("find ming tan").getFeedbackToUser());
        assertEquals(List.of(WEI_MING), logic.getFilteredPersonList());
        assertEquals(beforeFind, model.getAddressBook());
        assertEquals(savedData, Files.readString(dataFile));

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(WEI_MING, WEI_JIE, ALICIA), reloaded.getFilteredPersonList());
        assertEquals("Listed 3 students.", logic.execute("list").getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE, ALICIA), logic.getFilteredPersonList());
    }

    @Test
    public void execute_greekNameSearch_matchesUppercaseAndLowercasePrefixes() throws Exception {
        logic.execute("add n/ΚΟΣΜΑΣ p/81234567 a/10 Short Street l/Sec 2 sub/E-Math");
        Person student = model.getAddressBook().getPersonList().get(3);
        String savedData = Files.readString(dataFile);
        for (String keyword : List.of("ΚΟΣ", "κοσ", "κος")) {
            assertEquals("Found 1 student.", logic.execute("find " + keyword).getFeedbackToUser());
            assertEquals(List.of(student), logic.getFilteredPersonList());
            assertEquals("No lessons logged yet for ΚΟΣΜΑΣ.", logic.execute("view 1").getFeedbackToUser());
        }
        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_blankFind_keepsPreviousFilterAndSavedData() throws Exception {
        logic.execute("find ali");
        String savedData = Files.readString(dataFile);
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommand.MESSAGE_USAGE), () -> logic.execute("find   "));
        assertEquals(List.of(ALICIA), logic.getFilteredPersonList());
        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_findThenViewAndLog_targetsDisplayedStudentAndKeepsFilter() throws Exception {
        logic.execute("find ali");
        assertEquals("Lesson records for Alicia Lim (1):\n1. Calculus",
                logic.execute("view 1").getFeedbackToUser());
        assertEquals("Logged lesson for Alicia Lim: Statistics",
                logic.execute("log 1 c/Statistics").getFeedbackToUser());
        assertEquals(1, logic.getFilteredPersonList().size());
        assertEquals("Lesson records for Alicia Lim (2):\n1. Statistics\n2. Calculus",
                logic.execute("view 1").getFeedbackToUser());
        assertEquals(WEI_MING, model.getAddressBook().getPersonList().get(0));
        assertEquals(WEI_JIE, model.getAddressBook().getPersonList().get(1));
        assertEquals(List.of(new Lesson("Calculus"), new Lesson("Statistics")),
                storage.readAddressBook().orElseThrow().getPersonList().get(2).getLessons());
    }

    @Test
    public void execute_findThenDelete_deletesDisplayedStudentOnly() throws Exception {
        logic.execute("find ali");
        assertEquals("Deleted student: " + Messages.format(ALICIA), logic.execute("delete 1").getFeedbackToUser());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertEquals(List.of(WEI_MING, WEI_JIE), storage.readAddressBook().orElseThrow().getPersonList());
        assertEquals("Listed 2 students.", logic.execute("list").getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE), logic.getFilteredPersonList());
    }

    /**
     * Checks that lesson deletion uses the searched student's displayed index and saves the full address book.
     */
    @Test
    public void execute_findThenDeleteLesson_keepsStudentFilterAndOtherSavedStudents() throws Exception {
        logic.execute("find ali");
        assertEquals("Deleted lesson 1 for Alicia Lim: Calculus",
                logic.execute("deletelesson 1 1").getFeedbackToUser());
        Person aliciaWithoutLessons = new PersonBuilder(ALICIA).withLessons().build();
        assertEquals(List.of(aliciaWithoutLessons), logic.getFilteredPersonList());
        assertEquals("No lessons logged yet for Alicia Lim.", logic.execute("view 1").getFeedbackToUser());

        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(WEI_MING, WEI_JIE, aliciaWithoutLessons), reloaded.getFilteredPersonList());
        assertEquals("Listed 3 students.", logic.execute("list").getFeedbackToUser());
        assertEquals(reloaded.getFilteredPersonList(), logic.getFilteredPersonList());
    }

    @Test
    public void execute_findThenEdit_editsDisplayedStudentAndPreservesLessons() throws Exception {
        logic.execute("find ali");
        Person edited = new PersonBuilder(ALICIA).withName("Nadia Tan").build();
        assertEquals("Edited student: " + Messages.format(edited),
                logic.execute("edit 1 n/Nadia Tan").getFeedbackToUser());
        // Edit retains its existing behavior of restoring the full list.
        assertEquals(List.of(WEI_MING, WEI_JIE, edited), logic.getFilteredPersonList());
        assertEquals(edited, storage.readAddressBook().orElseThrow().getPersonList().get(2));
        logic.execute("find nad");
        assertEquals("Lesson records for Nadia Tan (1):\n1. Calculus", logic.execute("view 1").getFeedbackToUser());
    }
}
