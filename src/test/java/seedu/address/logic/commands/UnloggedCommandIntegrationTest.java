package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class UnloggedCommandIntegrationTest {
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withPhone("81230001")
            .withLessons("Legacy algebra").build();
    private static final Person BEN = new PersonBuilder().withName("Ben Lim").withPhone("81230002").build();
    private static final Person CAROL = new PersonBuilder().withName("Carol Ng").withPhone("81230003")
            .withFlag().build();

    @TempDir
    public Path directory;

    private ModelManager model;
    private JsonAddressBookStorage storage;
    private Logic logic;
    private Path dataFile;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BEN);
        model.addPerson(CAROL);
        dataFile = directory.resolve("students.json");
        storage = new JsonAddressBookStorage(dataFile);
        storage.saveAddressBook(model.getAddressBook());
        logic = logicWith(storage);
    }

    @Test
    public void execute_filtersAndSummary_doNotChangeDataOrSave() throws Exception {
        String original = Files.readString(dataFile);
        logic.execute("find amy");
        assertEquals("Listed 2 students with no recorded lessons.", logic.execute("unlogged").getFeedbackToUser());
        assertEquals(List.of(BEN, CAROL), model.getFilteredPersonList());
        assertEquals("Summary:\nYou have 3 students and 1 lesson record in total. 2 students have no lessons recorded.",
                logic.execute("summary").getFeedbackToUser());
        assertEquals(List.of(BEN, CAROL), model.getFilteredPersonList());
        logic.execute("flagged");
        assertEquals(List.of(CAROL), model.getFilteredPersonList());
        logic.execute("unlogged");
        assertEquals(List.of(BEN, CAROL), model.getFilteredPersonList());
        logic.execute("find amy");
        assertEquals(List.of(AMY), model.getFilteredPersonList());
        logic.execute("unlogged");
        logic.execute("list");
        assertEquals(List.of(AMY, BEN, CAROL), model.getFilteredPersonList());
        assertEquals(original, Files.readString(dataFile));

        Files.delete(dataFile);
        logic.execute("unlogged");
        assertFalse(Files.exists(dataFile), "Filtering must not create a student file");
    }

    @Test
    public void execute_invalidArguments_preserveFilterAndData() throws Exception {
        logic.execute("find amy");
        String before = Files.readString(dataFile);
        ParseException error = assertThrows(ParseException.class, () -> logic.execute("unlogged all"));
        assertEquals(UnloggedCommand.MESSAGE_INVALID_ARGUMENTS, error.getMessage());
        assertEquals(List.of(AMY), model.getFilteredPersonList());
        assertEquals(before, Files.readString(dataFile));
        Person anotherAmy = new PersonBuilder().withName("Amy Lee").withPhone("81230008").build();
        assertTrue(model.getFilteredPersonListPredicate().test(anotherAmy));
        assertFalse(model.getFilteredPersonListPredicate().test(BEN));
    }

    @Test
    public void execute_logFirstLesson_removesStudentAndRenumbersWithoutRetargeting() throws Exception {
        logic.execute("unlogged");
        logic.execute("view 1");
        logic.execute("log 1 c/Vectors date/1/1/2020");
        Person loggedBen = BEN.withLessonAdded(new Lesson("Vectors", LocalDate.of(2020, 1, 1)));
        assertEquals(List.of(CAROL), model.getFilteredPersonList());
        assertEquals("No lessons logged yet for Carol Ng.", logic.execute("view 1").getFeedbackToUser());
        logic.execute("log 1 c/Calculus date/2/1/2020");
        Person loggedCarol = CAROL.withLessonAdded(new Lesson("Calculus", LocalDate.of(2020, 1, 2)));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(AMY, loggedBen, loggedCarol), model.getAddressBook().getPersonList());
        assertEquals(new AddressBook(model.getAddressBook()), new AddressBook(storage.readAddressBook().orElseThrow()));
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(AMY, loggedBen, loggedCarol), restarted.getFilteredPersonList());
        new UnloggedCommand().execute(restarted);
        assertTrue(restarted.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_blockedSaving_allowsReadOnlyFilter() throws Exception {
        Files.writeString(dataFile, "{broken json}");
        assertThrows(DataLoadingException.class, () -> storage.readAddressBook());
        model.setAddressBook(new AddressBook());
        assertEquals(UnloggedCommand.MESSAGE_NO_MATCHES, logic.execute("unlogged").getFeedbackToUser());
        assertEquals("{broken json}", Files.readString(dataFile));
    }

    @Test
    public void execute_failedFirstLesson_restoresEmptyHistoryAndFilterForRetry() throws Exception {
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataFile) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Simulated write failure");
            }
        };
        Logic failingLogic = logicWith(failingStorage);
        failingLogic.execute("unlogged");
        assertThrows(CommandException.class, () -> failingLogic.execute("log 1 c/Vectors date/1/1/2020"));
        assertEquals(List.of(BEN, CAROL), model.getFilteredPersonList());
        assertEquals(List.of(AMY, BEN, CAROL), model.getAddressBook().getPersonList());
        logic.execute("log 1 c/Vectors date/1/1/2020");
        assertEquals(List.of(CAROL), model.getFilteredPersonList());
    }

    private Logic logicWith(JsonAddressBookStorage bookStorage) {
        return new LogicManager(model, new StorageManager(bookStorage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }
}
