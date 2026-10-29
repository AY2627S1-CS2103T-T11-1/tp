package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies newest-first lesson deletion, duplicate handling and displayed student indexes.
 */
public class DeleteLessonCommandTest {
    private static final Person STUDENT = new PersonBuilder(ALICE)
            .withLessons("Algebra", "Geometry", "Algebra").build();
    private static final Person OTHER_STUDENT = new PersonBuilder(BOB).withLessons("Calculus").build();
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Index SECOND = Index.fromOneBased(2);

    private Model model;

    /**
     * Creates two students with distinct histories for each test.
     */
    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(STUDENT);
        model.addPerson(OTHER_STUDENT);
    }

    /**
     * Deletes the newest duplicate rather than the first equal lesson in storage.
     */
    @Test
    public void execute_newestLesson_removesSelectedDuplicate() throws Exception {
        assertDeletion(1, List.of("Algebra", "Geometry"), "Algebra");
    }

    /**
     * Removes the middle lesson while keeping the duplicate entries in order.
     */
    @Test
    public void execute_middleLesson_keepsRemainingOrder() throws Exception {
        assertDeletion(2, List.of("Algebra", "Algebra"), "Geometry");
    }

    /**
     * Maps the last displayed lesson to the oldest stored lesson.
     */
    @Test
    public void execute_oldestLesson_removesOldestOccurrence() throws Exception {
        assertDeletion(3, List.of("Geometry", "Algebra"), "Algebra");
    }

    /**
     * Reports the selected duplicate's date while preserving the filter, flag and other records.
     */
    @Test
    public void execute_sameContentOnDifferentDates_reportsSelectedDate() throws Exception {
        Lesson older = new Lesson("Fractions", LocalDate.of(2026, 10, 20));
        Lesson newer = new Lesson("Fractions", LocalDate.of(2026, 10, 21));
        Person datedStudent = new PersonBuilder(STUDENT).withLessons().withFlag("Bring worksheet").build()
                .withLessonAdded(older).withLessonAdded(newer);
        model.setPerson(STUDENT, datedStudent);
        model.updateFilteredPersonList(person -> person.getName().equals(datedStudent.getName()));

        CommandResult result = new DeleteLessonCommand(FIRST, SECOND).execute(model);

        assertEquals("Deleted lesson 2 for " + STUDENT.getName() + " (20/10/2026): Fractions",
                result.getFeedbackToUser());
        Person expected = new PersonBuilder(datedStudent).withLessons().build().withLessonAdded(newer);
        assertEquals(expected, result.getStudent().orElseThrow());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(List.of(expected, OTHER_STUDENT), model.getAddressBook().getPersonList());
        assertEquals(List.of(older, newer), datedStudent.getLessons());
    }

    /**
     * Keeps the student after deleting their only lesson.
     */
    @Test
    public void execute_onlyLesson_keepsStudentWithEmptyHistory() throws Exception {
        new DeleteLessonCommand(SECOND, FIRST).execute(model);
        assertEquals(List.of(STUDENT, new PersonBuilder(OTHER_STUDENT).withLessons().build()),
                model.getAddressBook().getPersonList());
        assertEquals("No lessons logged yet for " + OTHER_STUDENT.getName() + ".",
                new ViewCommand(SECOND).execute(model).getFeedbackToUser());
    }

    /**
     * Targets the first displayed student rather than the first student in the full address book.
     */
    @Test
    public void execute_filteredList_targetsDisplayedStudentAndKeepsFilter() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().equals(OTHER_STUDENT.getName()));
        new DeleteLessonCommand(FIRST, FIRST).execute(model);
        Person updated = new PersonBuilder(OTHER_STUDENT).withLessons().build();
        assertEquals(List.of(updated), model.getFilteredPersonList());
        assertEquals(List.of(STUDENT, updated), model.getAddressBook().getPersonList());
        assertCommandFailure(new DeleteLessonCommand(FIRST, FIRST), model,
                "No lessons logged yet for " + OTHER_STUDENT.getName() + ".");
        assertCommandFailure(new DeleteLessonCommand(SECOND, FIRST), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    /**
     * Rejects an absent student without changing the model.
     */
    @Test
    public void execute_invalidStudentIndex_keepsData() {
        assertCommandFailure(new DeleteLessonCommand(Index.fromOneBased(3), FIRST), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertCommandFailure(new DeleteLessonCommand(FIRST, FIRST), new ModelManager(),
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    /**
     * Rejects lessons outside a non-empty history.
     */
    @Test
    public void execute_invalidLessonIndex_keepsData() {
        assertCommandFailure(new DeleteLessonCommand(FIRST, Index.fromOneBased(4)), model,
                Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    /**
     * Reports an empty history without changing either student.
     */
    @Test
    public void execute_noLessons_reportsEmptyHistoryAndKeepsData() {
        model.setPerson(STUDENT, new PersonBuilder(STUDENT).withLessons().build());
        assertCommandFailure(new DeleteLessonCommand(FIRST, FIRST), model,
                "No lessons logged yet for " + STUDENT.getName() + ".");
    }

    /**
     * Rejects null constructor arguments and execution without a model.
     */
    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteLessonCommand(null, FIRST));
        assertThrows(NullPointerException.class, () -> new DeleteLessonCommand(FIRST, null));
        assertThrows(NullPointerException.class, () -> new DeleteLessonCommand(FIRST, FIRST).execute(null));
    }

    /**
     * Checks both assertion failures: a converted position below zero or beyond the last lesson.
     */
    @Test
    public void toStoredLessonIndex_invalidPosition_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> DeleteLessonCommand.toStoredLessonIndex(3, 4));
        assertThrows(AssertionError.class, () -> DeleteLessonCommand.toStoredLessonIndex(3, 0));
    }

    /**
     * Compares both target indexes rather than the student alone.
     */
    @Test
    public void equals_comparesStudentAndLessonIndexes() {
        DeleteLessonCommand command = new DeleteLessonCommand(FIRST, FIRST);
        assertTrue(command.equals(command));
        DeleteLessonCommand equivalent = new DeleteLessonCommand(Index.fromOneBased(1), Index.fromOneBased(1));
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertTrue(new HashSet<>(List.of(command)).contains(equivalent));
        assertFalse(command.equals(null));
        assertFalse(command.equals("deletelesson 1 1"));
        assertFalse(command.equals(new DeleteLessonCommand(SECOND, FIRST)));
        assertFalse(command.equals(new DeleteLessonCommand(FIRST, SECOND)));
    }

    /**
     * Includes both indexes in diagnostic output.
     */
    @Test
    public void toStringMethod() {
        String expected = DeleteLessonCommand.class.getCanonicalName()
                + "{studentIndex=" + FIRST + ", lessonIndex=" + SECOND + "}";
        assertEquals(expected, new DeleteLessonCommand(FIRST, SECOND).toString());
    }

    /**
     * Checks the confirmation, all student fields, unaffected students and the original immutable object.
     */
    private void assertDeletion(int displayedLesson, List<String> remaining, String deletedContent) throws Exception {
        CommandResult result = new DeleteLessonCommand(FIRST, Index.fromOneBased(displayedLesson)).execute(model);
        assertEquals("Deleted lesson " + displayedLesson + " for " + STUDENT.getName()
                + " (undated): " + deletedContent,
                result.getFeedbackToUser());
        Person expected = new PersonBuilder(STUDENT).withLessons(remaining.toArray(String[]::new)).build();
        assertEquals(expected, result.getStudent().orElseThrow());
        assertEquals(List.of(expected, OTHER_STUDENT), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected, OTHER_STUDENT), model.getFilteredPersonList());
        assertEquals(new PersonBuilder(ALICE).withLessons("Algebra", "Geometry", "Algebra").build(), STUDENT);
    }
}
