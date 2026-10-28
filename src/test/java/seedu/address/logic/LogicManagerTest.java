package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.LessonDateUtil.todayText;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_addUnsupportedPrefix_preservesDataAndFilter() throws Exception {
        logic.execute("add n/Amy Tan p/81230001 addr/1 Example Street l/Sec 3 sub/Math");
        logic.execute("find amy");
        AddressBook before = new AddressBook(model.getAddressBook());
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        byte[] savedBefore = Files.readAllBytes(dataFile);
        Person visibleBefore = model.getFilteredPersonList().getFirst();

        assertParseException("add n/Eve Lee p/81230015 addr/6 St l/Sec 3 sub/Math t/friend",
                Subject.MESSAGE_CONSTRAINTS + "\n"
                        + String.format(AddCommandParser.MESSAGE_UNSUPPORTED_PREFIX_HINT, "t/"));

        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(visibleBefore), model.getFilteredPersonList());
        assertTrue(model.getFilteredPersonListPredicate().test(
                new PersonBuilder().withName("Amy Lee").build()));
        assertFalse(model.getFilteredPersonListPredicate().test(
                new PersonBuilder().withName("Ben Lim").build()));
        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
    }

    @Test
    public void execute_prefixTextInLessonNotes_rejectedWithoutChangingData() throws Exception {
        logic.execute("add n/Amy Tan p/81230001 addr/1 Example Street l/Sec 3 sub/Math");
        logic.execute("log 1 c/Algebra date/21/10/2026");
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        byte[] savedBefore = Files.readAllBytes(dataFile);
        AddressBook dataBefore = new AddressBook(model.getAddressBook());

        String duplicateContent = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT);
        String dateWithHint = Lesson.MESSAGE_DATE_CONSTRAINTS + "\n" + Messages.MESSAGE_LESSON_DATE_TEXT_HINT;
        assertParseException("log 1 c/Worksheet collected c/o Mum", duplicateContent);
        assertParseException("log 1 c/Revised date/time conversions", dateWithHint);
        assertParseException("editlesson 1 1 c/Notes collected c/o tutor", duplicateContent);
        assertParseException("editlesson 1 1 c/Revised date/time conversions", dateWithHint);
        assertEquals(dataBefore, model.getAddressBook());
        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));

        // the reworded notes are logged and saved
        logic.execute("log 1 c/Worksheet collected care of Mum");
        assertTrue(model.getFilteredPersonList().getFirst().getLessons().stream()
                .anyMatch(lesson -> lesson.content.equals("Worksheet collected care of Mum")));
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow());
    }

    @Test
    public void execute_nbspFieldBoundaries_savesSeparateFieldsAndPreservesHistory() throws Exception {
        logic.execute("add n/Amy Tan p/81230001 addr/1 Example Street l/Sec 3 sub/Math");
        logic.execute("log 1\u00a0c/Quadratic\u00a0  inequalities date/21/10/2026");
        logic.execute("edit 1 addr/New Rd\u00a0l/Sec 4");

        Person student = model.getFilteredPersonList().getFirst();
        assertEquals("New Rd", student.getAddress().value);
        assertEquals("Sec 4", student.getLevel().value);
        assertEquals("Quadratic\u00a0  inequalities", student.getLessons().getFirst().content);
        JsonAddressBookStorage saved = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), saved.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_lessonSequences_returnsLatestStudentsHistory() throws Exception {
        model.addPerson(AMY);
        Person otherStudent = new PersonBuilder().withName("Ben Tan").withPhone("81234567").build();
        model.addPerson(otherStudent);
        assertEquals("No lessons logged yet for " + AMY.getName() + ".",
                logic.execute("view 1").getFeedbackToUser());

        String today = todayText();
        logic.execute("log 1 c/Algebra");
        assertEquals("Lesson records for " + AMY.getName() + " (1):\n1. " + today + ": Algebra",
                logic.execute("view 1").getFeedbackToUser());
        logic.execute("log 1 c/Geometry");
        logic.execute("log 1 c/Algebra");
        String amyHistory = "Lesson records for " + AMY.getName() + " (3):\n1. " + today + ": Algebra\n2. "
                + today + ": Geometry\n3. " + today + ": Algebra";
        assertEquals(amyHistory, logic.execute("view 1").getFeedbackToUser());
        assertEquals("No lessons logged yet for Ben Tan.", logic.execute("view 2").getFeedbackToUser());
        logic.execute("log 2 c/Calculus");
        String benHistory = "Lesson records for Ben Tan (1):\n1. " + today + ": Calculus";
        assertEquals(benHistory, logic.execute("view 2").getFeedbackToUser());
        assertEquals(amyHistory, logic.execute("view 1").getFeedbackToUser());

        assertCommandException("view 3", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseException("view 1 extra", String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                ViewCommand.MESSAGE_USAGE));
        assertEquals(amyHistory, logic.execute("view 1").getFeedbackToUser());
        assertEquals(benHistory, logic.execute("view 2").getFeedbackToUser());
    }

    @Test
    public void execute_logPastDate_viewShowsLessonsInDateOrder() throws Exception {
        model.addPerson(AMY);
        String yesterday = LocalDate.now().minusDays(1).format(Messages.LESSON_DATE_FORMATTER);
        String tomorrow = LocalDate.now().plusDays(1).format(Messages.LESSON_DATE_FORMATTER);

        logic.execute("log 1 c/Vectors");
        assertEquals("Logged lesson for " + AMY.getName() + " on " + yesterday + ": Surds",
                logic.execute("log 1 c/Surds date/" + yesterday).getFeedbackToUser());
        assertCommandException("log 1 c/Matrices date/" + tomorrow, LogCommand.MESSAGE_FUTURE_DATE);

        assertEquals("Lesson records for " + AMY.getName() + " (2):\n1. " + todayText() + ": Vectors\n2. "
                + yesterday + ": Surds", logic.execute("view 1").getFeedbackToUser());
    }

    @Test
    public void execute_deleteThenView_usesUpdatedStudentIndexes() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder().withName("Ben Tan").withPhone("81234567").build());
        logic.execute("log 1 c/Algebra");
        logic.execute("log 2 c/Calculus");
        logic.execute("view 1");
        logic.execute("delete 1");
        assertEquals("Lesson records for Ben Tan (1):\n1. " + todayText() + ": Calculus",
                logic.execute("view 1").getFeedbackToUser());
        assertCommandException("view 2", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_logReloadThenView_returnsPersistedHistoryWithoutChangingFile() throws Exception {
        model.addPerson(AMY);
        logic.execute("log 1 c/Algebra");
        logic.execute("log 1 c/Geometry");
        Path dataFile = temporaryFolder.resolve("addressBook.json");
        String savedData = Files.readString(dataFile);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFile);
        Model reloadedModel = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        Logic reloadedLogic = new LogicManager(reloadedModel, storage);

        assertEquals("Lesson records for " + AMY.getName()
                + " (2):\n1. " + todayText() + ": Geometry\n2. " + todayText() + ": Algebra",
                reloadedLogic.execute("view 1").getFeedbackToUser());
        assertEquals(model.getAddressBook(), reloadedModel.getAddressBook());
        assertEquals(savedData, Files.readString(dataFile));
    }

    @Test
    public void execute_viewInvalidFormat_reportsUsageWithoutChangingModel() {
        model.addPerson(AMY);
        assertParseException("view 1 extra", String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                ViewCommand.MESSAGE_USAGE));
    }

    @Test
    public void execute_viewInvalidIndex_reportsCommandErrorWithoutChangingModel() {
        model.addPerson(AMY);
        assertCommandException("view 2", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_viewNoLessons_returnsMessage() throws Exception {
        model.addPerson(AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandSuccess("view 1", "No lessons logged yet for " + AMY.getName() + ".", expectedModel);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_EMPTY, model);
    }

    @Test
    public void execute_unchangedEdit_doesNotAttemptSaving() throws Exception {
        model.addPerson(AMY);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                throw new AssertionError("Unchanged student data must not be saved");
            }
        };
        Logic readOnlyLogic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        assertEquals("Edited student: " + Messages.format(AMY),
                readOnlyLogic.execute("edit 1 n/" + AMY.getName().fullName).getFeedbackToUser());
        assertEquals(AMY, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void execute_listAfterAddingAndDeleting_updatesCount() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + ADDRESS_DESC_AMY + LEVEL_DESC_AMY + SUBJECT_DESC_AMY);
        assertEquals("Listed 1 student.", logic.execute("list").getFeedbackToUser());
        assertParseException("list extra", ListCommand.MESSAGE_INVALID_ARGUMENTS);
        assertEquals("Listed 1 student.", logic.execute("list").getFeedbackToUser());
        logic.execute("delete 1");
        assertEquals("No students found. Use add to create a student.", logic.execute("list").getFeedbackToUser());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + ADDRESS_DESC_AMY + LEVEL_DESC_AMY + SUBJECT_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
