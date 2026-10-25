package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class SummaryCommandTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SummaryCommand().execute(null));
    }

    @Test
    public void execute_emptyModel_reportsZero() {
        CommandResult result = new SummaryCommand().execute(new ModelManager());
        assertEquals(new CommandResult("Summary:\nStudents: 0\nLesson records: 0\nStudents with no lessons: 0\n\n"
                + "No students yet. Add a student to start recording lessons."),
                result);
    }

    @Test
    public void execute_filters_countAllStudentsAndPreserveSavedData() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Amy Tan").withSubject("Physics")
                .withLessons("Algebra", "Algebra").build());
        model.addPerson(new PersonBuilder().withName("Ben Lim").withFlag("Call parent").build());
        Path file = temporaryFolder.resolve("students.json");
        Logic logic = logic(model, new JsonAddressBookStorage(file));
        logic.execute("list");
        String savedData = Files.readString(file);
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String filter : List.of("find ben", "find sub/Physics", "flagged", "find nobody", "list")) {
            logic.execute(filter);
            List<Person> visible = List.copyOf(logic.getFilteredPersonList());
            var liveList = logic.getFilteredPersonList();
            assertEquals("Summary:\nStudents: 2\nLesson records: 2\nStudents with no lessons: 1\n\n"
                + "You have 2 students and 2 lesson records in total. 1 student has no lessons recorded.",
                    logic.execute("summary").getFeedbackToUser());
            assertEquals(visible, logic.getFilteredPersonList());
            assertSame(liveList, logic.getFilteredPersonList());
            assertEquals(before, model.getAddressBook());
            assertEquals(savedData, Files.readString(file));
        }
        logic.execute("find ben");
        assertThrows(ParseException.class, String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                SummaryCommand.MESSAGE_USAGE), () -> logic.execute("summary 1"));
        assertEquals("Ben Lim", logic.getFilteredPersonList().getFirst().getName().fullName);
        logic.execute("delete 1");
        assertEquals("Summary:\nStudents: 1\nLesson records: 2\nStudents with no lessons: 0\n\n"
                + "You have 1 student and 2 lesson records in total. Every student has at least one lesson recorded.",
                logic.execute("summary").getFeedbackToUser());
        assertTrue(logic.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_lessonChangesAndRestart_reportsCurrentTotals() throws Exception {
        ModelManager model = new ModelManager();
        Path file = temporaryFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Logic logic = logic(model, storage);
        logic.execute("add n/Amy Tan p/81230001 a/10 Example Street l/Sec 3 sub/E-Math");
        assertEquals("Summary:\nStudents: 1\nLesson records: 0\nStudents with no lessons: 1\n\n"
                + "You have 1 student and 0 lesson records in total. 1 student has no lessons recorded.",
                logic.execute("summary").getFeedbackToUser());
        logic.execute("log 1 c/Algebra date/2/1/2020");
        logic.execute("editlesson 1 1 c/Vectors date/1/1/2020");
        assertEquals("Summary:\nStudents: 1\nLesson records: 1\nStudents with no lessons: 0\n\n"
                + "You have 1 student and 1 lesson record in total. Every student has at least one lesson recorded.",
                logic.execute("summary").getFeedbackToUser());
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(new SummaryCommand().execute(model), new SummaryCommand().execute(reloaded));
        logic.execute("deletelesson 1 1");
        assertEquals("Summary:\nStudents: 1\nLesson records: 0\nStudents with no lessons: 1\n\n"
                + "You have 1 student and 0 lesson records in total. 1 student has no lessons recorded.",
                logic.execute("summary").getFeedbackToUser());
    }

    @Test
    public void execute_failedSave_reportsErrorAndRetainsFilter() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withLessons("Algebra").build());
        model.updateFilteredPersonList(person -> false);
        AddressBook before = new AddressBook(model.getAddressBook());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("disk unavailable");
            }
        };
        String error = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "disk unavailable");
        assertThrows(CommandException.class, error, () -> logic(model, storage).execute("summary"));
        assertEquals(before, model.getAddressBook());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    private Logic logic(ModelManager model, JsonAddressBookStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }
}
