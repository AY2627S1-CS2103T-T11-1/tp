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

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

public class ViewCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_multipleLessons_showsNewestFirstWithoutChangingModel() {
        Person student = model.getFilteredPersonList().get(0);
        model.setPerson(student, student.withLessonAdded(new Lesson("Algebra"))
                .withLessonAdded(new Lesson("Geometry")).withLessonAdded(new Lesson("Algebra"))
                .withLessonAdded(new Lesson("Calculus")));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expected = "Lesson records for " + student.getName()
                + " (4):\n1. Calculus\n2. Algebra\n3. Geometry\n4. Algebra";

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expected, expectedModel);
    }

    @Test
    public void execute_noLessons_returnsEmptyMessage() {
        Person student = model.getFilteredPersonList().get(0);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model,
                "No lessons logged yet for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndKeepsFilter() {
        Person student = model.getFilteredPersonList().get(1);
        model.setPerson(student, student.withLessonAdded(new Lesson("Only this student's lesson")));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        String expected = "Lesson records for " + student.getName()
                + " (1):\n1. Only this student's lesson";

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model, expected, expectedModel);
        assertCommandFailure(new ViewCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outsideList = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new ViewCommand(outsideList), model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertCommandFailure(new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyStudentList_throwsCommandException() {
        assertCommandFailure(new ViewCommand(INDEX_FIRST_PERSON), new ModelManager(),
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
        assertThrows(NullPointerException.class, () -> new ViewCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals() {
        ViewCommand command = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(command.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(command.equals(null));
        assertFalse(command.equals("view"));
    }

    @Test
    public void toStringMethod() {
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}",
                new ViewCommand(INDEX_FIRST_PERSON).toString());
    }
}
