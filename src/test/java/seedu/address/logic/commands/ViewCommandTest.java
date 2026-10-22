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

import java.time.LocalDate;

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
        model.setPerson(student, student.withLessonAdded(new Lesson("Algebra", LocalDate.of(2026, 10, 1)))
                .withLessonAdded(new Lesson("Geometry", LocalDate.of(2026, 10, 8)))
                .withLessonAdded(new Lesson("Algebra", LocalDate.of(2026, 10, 15)))
                .withLessonAdded(new Lesson("Calculus", LocalDate.of(2026, 10, 15))));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expected = "Lesson records for " + student.getName() + " (4):\n1. 15/10/2026: Calculus"
                + "\n2. 15/10/2026: Algebra\n3. 08/10/2026: Geometry\n4. 01/10/2026: Algebra";

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model,
                new CommandResult(expected, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_undatedAndDatedLessons_marksUndatedLessons() {
        // lessons logged before v1.4 have no date and stay in logging order
        Person student = model.getFilteredPersonList().get(0);
        model.setPerson(student, student.withLessonAdded(new Lesson("Algebra"))
                .withLessonAdded(new Lesson("Geometry", LocalDate.of(2026, 10, 18))));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expected = "Lesson records for " + student.getName()
                + " (2):\n1. 18/10/2026: Geometry\n2. (undated): Algebra";

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model,
                new CommandResult(expected, INDEX_FIRST_PERSON), expectedModel);
    }

    @Test
    public void execute_noLessons_returnsEmptyMessage() {
        Person student = model.getFilteredPersonList().get(0);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model,
                new CommandResult("No lessons logged yet for " + student.getName() + ".", INDEX_FIRST_PERSON),
                expectedModel);
    }

    @Test
    public void execute_secondStudent_identifiesDisplayedStudentToHighlight() {
        Person student = model.getFilteredPersonList().get(1);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertCommandSuccess(new ViewCommand(INDEX_SECOND_PERSON), model,
                new CommandResult("No lessons logged yet for " + student.getName() + ".", INDEX_SECOND_PERSON),
                expectedModel);
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndKeepsFilter() {
        Person student = model.getFilteredPersonList().get(1);
        model.setPerson(student, student.withLessonAdded(new Lesson("Only this student's lesson")));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        String expected = "Lesson records for " + student.getName()
                + " (1):\n1. (undated): Only this student's lesson";

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model,
                new CommandResult(expected, INDEX_FIRST_PERSON), expectedModel);
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
