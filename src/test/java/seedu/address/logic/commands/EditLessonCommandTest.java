package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditLessonCommand.EditLessonDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code EditLessonCommand}.
 */
public class EditLessonCommandTest {

    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    /** 10am on 24 Oct 2026 in Singapore. */
    private static final Clock OCT_24_CLOCK = Clock.fixed(Instant.parse("2026-10-24T02:00:00Z"), SINGAPORE);
    private static final LocalDate OCT_19 = LocalDate.of(2026, 10, 19);
    private static final LocalDate OCT_21 = LocalDate.of(2026, 10, 21);
    private static final LocalDate OCT_22 = LocalDate.of(2026, 10, 22);
    private static final LocalDate OCT_23 = LocalDate.of(2026, 10, 23);
    private static final LocalDate OCT_24 = LocalDate.of(2026, 10, 24);
    private static final LocalDate OCT_25 = LocalDate.of(2026, 10, 25);

    // Stored oldest first. view shows them newest first as 1. TRIGONOMETRY to 5. PLACE_VALUE.
    private static final Lesson PLACE_VALUE = new Lesson("Place value");
    private static final Lesson ALGEBRA = new Lesson("Algebra", OCT_19);
    private static final Lesson SURDS = new Lesson("Surds", OCT_21);
    private static final Lesson VECTORS = new Lesson("Vectors", OCT_21);
    private static final Lesson TRIGONOMETRY = new Lesson("Trigonometry", OCT_23);

    private static final Person STUDENT = studentWith(PLACE_VALUE, ALGEBRA, SURDS, VECTORS, TRIGONOMETRY);
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

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        EditLessonDescriptor descriptor = content("Vectors");
        assertThrows(NullPointerException.class, () -> new EditLessonCommand(null, FIRST, descriptor));
        assertThrows(NullPointerException.class, () -> new EditLessonCommand(FIRST, null, descriptor));
        assertThrows(NullPointerException.class, () -> new EditLessonCommand(FIRST, FIRST, null));
        assertThrows(NullPointerException.class, () -> new EditLessonCommand(FIRST, FIRST, descriptor, null));
        assertThrows(NullPointerException.class, () ->
                new EditLessonCommand(FIRST, FIRST, descriptor, OCT_24_CLOCK).execute(null));
    }

    /**
     * Keeps the lesson number when only the content changes, even though another lesson shares its date.
     */
    @Test
    public void execute_contentOnly_keepsPosition() {
        Lesson edited = new Lesson("Vectors and scalars", OCT_21);
        assertEditSuccess(2, content("Vectors and scalars"), 2, edited,
                studentWith(PLACE_VALUE, ALGEBRA, SURDS, edited, TRIGONOMETRY));
    }

    /**
     * Keeps the lesson number when the date given is the lesson's current date.
     */
    @Test
    public void execute_sameDateGiven_keepsPosition() {
        EditLessonDescriptor descriptor = contentAndDate("Surds and indices", OCT_21);
        Lesson edited = new Lesson("Surds and indices", OCT_21);
        assertEditSuccess(3, descriptor, 3, edited,
                studentWith(PLACE_VALUE, ALGEBRA, edited, VECTORS, TRIGONOMETRY));
    }

    /**
     * Moves a lesson to its new place in date order and reports its new number.
     */
    @Test
    public void execute_laterDate_movesLessonAndRenumbers() {
        Lesson edited = new Lesson("Algebra", OCT_22);
        assertEditSuccess(4, date(OCT_22), 2, edited,
                studentWith(PLACE_VALUE, SURDS, VECTORS, edited, TRIGONOMETRY));
    }

    /**
     * Moves a lesson earlier in date order, after the lesson already on its new date.
     */
    @Test
    public void execute_earlierDate_movesLessonAndRenumbers() {
        Lesson edited = new Lesson("Trigonometry", OCT_19);
        assertEditSuccess(1, date(OCT_19), 3, edited,
                studentWith(PLACE_VALUE, ALGEBRA, edited, SURDS, VECTORS));
    }

    /**
     * Places a lesson moved onto a busy date after the lessons already on that date,
     * so that view shows it before them.
     */
    @Test
    public void execute_dateSharedWithOtherLessons_placedAfterThem() {
        Lesson edited = new Lesson("Algebra", OCT_21);
        assertEditSuccess(4, date(OCT_21), 2, edited,
                studentWith(PLACE_VALUE, SURDS, VECTORS, edited, TRIGONOMETRY));
    }

    /**
     * Keeps an undated legacy lesson undated when only its content changes.
     */
    @Test
    public void execute_undatedLessonContentOnly_staysUndated() {
        Lesson edited = new Lesson("Place value and rounding");
        assertEditSuccess(5, content("Place value and rounding"), 5, edited,
                studentWith(edited, ALGEBRA, SURDS, VECTORS, TRIGONOMETRY));
    }

    /**
     * Dates an undated legacy lesson and moves it into date order.
     */
    @Test
    public void execute_undatedLessonGivenDate_movesIntoDateOrder() {
        Lesson edited = new Lesson("Place value", OCT_22);
        assertEditSuccess(5, date(OCT_22), 2, edited,
                studentWith(ALGEBRA, SURDS, VECTORS, edited, TRIGONOMETRY));
    }

    /**
     * Accepts today's date, the latest valid date, and makes the lesson the newest.
     */
    @Test
    public void execute_today_success() {
        Lesson edited = new Lesson("Algebra", OCT_24);
        assertEditSuccess(4, date(OCT_24), 1, edited,
                studentWith(PLACE_VALUE, SURDS, VECTORS, TRIGONOMETRY, edited));
    }

    /**
     * Rejects tomorrow's date, the earliest invalid date, without changing the model.
     */
    @Test
    public void execute_futureDate_failure() {
        assertCommandFailure(new EditLessonCommand(FIRST, FIRST, date(OCT_25), OCT_24_CLOCK), model,
                Messages.MESSAGE_FUTURE_DATE);
        assertCommandFailure(new EditLessonCommand(FIRST, FIRST, contentAndDate("Vectors", OCT_25),
                OCT_24_CLOCK), model, Messages.MESSAGE_FUTURE_DATE);
    }

    /**
     * Rejects edits that would leave the lesson as it is.
     */
    @Test
    public void execute_unchangedLesson_failure() {
        String expected = EditLessonCommand.MESSAGE_LESSON_UNCHANGED;
        assertCommandFailure(new EditLessonCommand(FIRST, SECOND, content("Vectors"), OCT_24_CLOCK), model,
                expected);
        assertCommandFailure(new EditLessonCommand(FIRST, SECOND, date(OCT_21), OCT_24_CLOCK), model, expected);
        assertCommandFailure(new EditLessonCommand(FIRST, Index.fromOneBased(5), content("Place value"),
                OCT_24_CLOCK), model, expected);
    }

    /**
     * Targets the first displayed student rather than the first student in the full address book.
     */
    @Test
    public void execute_filteredList_targetsDisplayedStudentAndKeepsFilter() throws Exception {
        model.updateFilteredPersonList(person -> person.getName().equals(OTHER_STUDENT.getName()));
        new EditLessonCommand(FIRST, FIRST, content("Differentiation"), OCT_24_CLOCK).execute(model);

        Person updated = new PersonBuilder(OTHER_STUDENT).withLessons("Differentiation").build();
        assertEquals(List.of(updated), model.getFilteredPersonList());
        assertEquals(List.of(STUDENT, updated), model.getAddressBook().getPersonList());
        assertCommandFailure(new EditLessonCommand(SECOND, FIRST, content("Vectors"), OCT_24_CLOCK), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    /**
     * Keeps a student's follow-up flag whether the edit keeps the lesson in place or moves it to a new date.
     */
    @Test
    public void execute_flaggedStudent_keepsFlag() throws Exception {
        Flag flag = new Flag("Review homework");
        Person flaggedStudent = STUDENT.withFlag(flag);
        model.setPerson(STUDENT, flaggedStudent);

        new EditLessonCommand(FIRST, SECOND, content("Vectors and scalars"), OCT_24_CLOCK).execute(model);
        assertEquals(Optional.of(flag), model.getFilteredPersonList().get(0).getFlag());

        new EditLessonCommand(FIRST, Index.fromOneBased(4), date(OCT_22), OCT_24_CLOCK).execute(model);
        assertEquals(Optional.of(flag), model.getFilteredPersonList().get(0).getFlag());
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        assertCommandFailure(new EditLessonCommand(Index.fromOneBased(3), FIRST, content("Vectors"),
                OCT_24_CLOCK), model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertCommandFailure(new EditLessonCommand(FIRST, FIRST, content("Vectors"), OCT_24_CLOCK),
                new ModelManager(), Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    /**
     * Rejects the first lesson number past the end of the history, even when the other details are invalid.
     */
    @Test
    public void execute_invalidLessonIndex_failure() {
        assertCommandFailure(new EditLessonCommand(FIRST, Index.fromOneBased(6), content("Vectors"),
                OCT_24_CLOCK), model, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        assertCommandFailure(new EditLessonCommand(SECOND, SECOND, date(OCT_25), OCT_24_CLOCK), model,
                Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_noLessons_failure() {
        model.setPerson(STUDENT, new PersonBuilder(STUDENT).withLessons().build());
        assertCommandFailure(new EditLessonCommand(FIRST, FIRST, content("Vectors"), OCT_24_CLOCK), model,
                "No lessons logged yet for " + STUDENT.getName() + ".");
    }

    /**
     * Checks both assertion failures: a changed identity or a changed number of lessons.
     */
    @Test
    public void assertOnlyLessonEdited_invalidEdit_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> EditLessonCommand.assertOnlyLessonEdited(STUDENT, OTHER_STUDENT));
        assertThrows(AssertionError.class, () ->
                EditLessonCommand.assertOnlyLessonEdited(STUDENT, STUDENT.withLessonRemoved(0)));
    }

    @Test
    public void equals() {
        EditLessonCommand command = new EditLessonCommand(FIRST, SECOND, content("Vectors"), OCT_24_CLOCK);

        // same values -> returns true
        assertTrue(command.equals(new EditLessonCommand(FIRST, SECOND, content("Vectors"), OCT_24_CLOCK)));
        assertEquals(command.hashCode(),
                new EditLessonCommand(FIRST, SECOND, content("Vectors"), OCT_24_CLOCK).hashCode());

        // same object -> returns true
        assertTrue(command.equals(command));

        // null or different type -> returns false
        assertFalse(command.equals(null));
        assertFalse(command.equals(5.0f));

        // different student index, lesson index, details or clock -> returns false
        assertFalse(command.equals(new EditLessonCommand(SECOND, SECOND, content("Vectors"), OCT_24_CLOCK)));
        assertFalse(command.equals(new EditLessonCommand(FIRST, FIRST, content("Vectors"), OCT_24_CLOCK)));
        assertFalse(command.equals(new EditLessonCommand(FIRST, SECOND, content("Surds"), OCT_24_CLOCK)));
        assertFalse(command.equals(new EditLessonCommand(FIRST, SECOND, content("Vectors"))));
    }

    /**
     * Copies the descriptor, so later changes to the caller's descriptor do not change the command.
     */
    @Test
    public void constructor_descriptorChangedLater_commandUnchanged() {
        EditLessonDescriptor descriptor = content("Vectors");
        EditLessonCommand command = new EditLessonCommand(FIRST, SECOND, descriptor, OCT_24_CLOCK);
        descriptor.setContent("Surds");
        assertEquals(new EditLessonCommand(FIRST, SECOND, content("Vectors"), OCT_24_CLOCK), command);
    }

    @Test
    public void descriptor_equalsAndFields() {
        EditLessonDescriptor descriptor = contentAndDate("Vectors", OCT_21);
        assertTrue(descriptor.equals(new EditLessonDescriptor(descriptor)));
        assertTrue(descriptor.equals(descriptor));
        assertEquals(descriptor.hashCode(), contentAndDate("Vectors", OCT_21).hashCode());
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals(content("Vectors")));
        assertFalse(descriptor.equals(date(OCT_21)));
        assertNotEquals(content("Vectors"), content("Surds"));
        assertFalse(new EditLessonDescriptor().isAnyFieldEdited());
        assertTrue(content("Vectors").isAnyFieldEdited());
        assertTrue(date(OCT_21).isAnyFieldEdited());
    }

    @Test
    public void toStringMethod() {
        EditLessonDescriptor descriptor = contentAndDate("Vectors", OCT_21);
        String expectedDescriptor = EditLessonDescriptor.class.getCanonicalName()
                + "{content=Vectors, date=2026-10-21}";
        assertEquals(expectedDescriptor, descriptor.toString());
        assertEquals(EditLessonCommand.class.getCanonicalName() + "{studentIndex=" + FIRST + ", lessonIndex="
                + SECOND + ", editLessonDescriptor=" + expectedDescriptor + "}",
                new EditLessonCommand(FIRST, SECOND, descriptor).toString());
    }

    /**
     * Edits lesson {@code lessonNumber} of the first student and checks the message, the student's new
     * lessons and that the other student and the filter are unchanged.
     */
    private void assertEditSuccess(int lessonNumber, EditLessonDescriptor descriptor, int newLessonNumber,
            Lesson editedLesson, Person expectedStudent) {
        EditLessonCommand command = new EditLessonCommand(FIRST, Index.fromOneBased(lessonNumber), descriptor,
                OCT_24_CLOCK);
        String expectedMessage = String.format(EditLessonCommand.MESSAGE_EDIT_LESSON_SUCCESS, lessonNumber,
                STUDENT.getName(), newLessonNumber, Messages.formatLessonDate(editedLesson), editedLesson.content);

        Model expectedModel = new ModelManager();
        expectedModel.addPerson(expectedStudent);
        expectedModel.addPerson(OTHER_STUDENT);

        assertCommandSuccess(command, model, new CommandResult(expectedMessage, expectedStudent), expectedModel);
    }

    private static Person studentWith(Lesson... lessons) {
        return new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getAddress(), ALICE.getLevel(),
                ALICE.getSubject(), List.of(lessons));
    }

    private static EditLessonDescriptor content(String content) {
        EditLessonDescriptor descriptor = new EditLessonDescriptor();
        descriptor.setContent(content);
        return descriptor;
    }

    private static EditLessonDescriptor date(LocalDate date) {
        EditLessonDescriptor descriptor = new EditLessonDescriptor();
        descriptor.setDate(date);
        return descriptor;
    }

    private static EditLessonDescriptor contentAndDate(String content, LocalDate date) {
        EditLessonDescriptor descriptor = content(content);
        descriptor.setDate(date);
        return descriptor;
    }
}
