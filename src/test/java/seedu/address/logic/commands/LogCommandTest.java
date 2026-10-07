package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code LogCommand}.
 */
public class LogCommandTest {

    private static final Lesson LESSON_QUADRATICS = new Lesson("Quadratic inequalities");
    private static final Lesson LESSON_BINOMIAL = new Lesson("Binomial theorem");

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LogCommand(null, LESSON_QUADRATICS));
        assertThrows(NullPointerException.class, () -> new LogCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person studentWithLesson = student.withLessonAdded(LESSON_QUADRATICS);
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS);

        String expectedMessage = String.format(LogCommand.MESSAGE_LOG_LESSON_SUCCESS,
                student.getName(), LESSON_QUADRATICS);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, studentWithLesson);

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person studentWithLesson = student.withLessonAdded(LESSON_QUADRATICS);
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS);

        String expectedMessage = String.format(LogCommand.MESSAGE_LOG_LESSON_SUCCESS,
                student.getName(), LESSON_QUADRATICS);

        // logging a lesson keeps the currently displayed (filtered) list
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, studentWithLesson);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentWithLessons_appendsLessonAtEnd() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS).execute(model);
        new LogCommand(INDEX_FIRST_PERSON, LESSON_BINOMIAL).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(LESSON_QUADRATICS, LESSON_BINOMIAL), student.getLessons());
    }

    @Test
    public void execute_sameLessonTwice_bothKept() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS).execute(model);
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(LESSON_QUADRATICS, LESSON_QUADRATICS), student.getLessons());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        LogCommand logCommand = new LogCommand(outOfBoundIndex, LESSON_QUADRATICS);

        assertCommandFailure(logCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        LogCommand logCommand = new LogCommand(outOfBoundIndex, LESSON_QUADRATICS);

        assertCommandFailure(logCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        LogCommand logFirstCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS);

        // same object -> returns true
        assertTrue(logFirstCommand.equals(logFirstCommand));

        // same values -> returns true
        assertTrue(logFirstCommand.equals(new LogCommand(INDEX_FIRST_PERSON, new Lesson("Quadratic inequalities"))));

        // different types -> returns false
        assertFalse(logFirstCommand.equals(1));

        // null -> returns false
        assertFalse(logFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(logFirstCommand.equals(new LogCommand(INDEX_SECOND_PERSON, LESSON_QUADRATICS)));

        // different lesson -> returns false
        assertFalse(logFirstCommand.equals(new LogCommand(INDEX_FIRST_PERSON, LESSON_BINOMIAL)));
    }

    @Test
    public void toStringMethod() {
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS);
        String expected = LogCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", lesson=" + LESSON_QUADRATICS + "}";
        assertEquals(expected, logCommand.toString());
    }
}
