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
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    /** 10am on 18 Oct 2026 in Singapore. */
    private static final Clock OCT_18_CLOCK = Clock.fixed(Instant.parse("2026-10-18T02:00:00Z"), SINGAPORE);
    private static final LocalDate OCT_18 = LocalDate.of(2026, 10, 18);

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LogCommand(null, LESSON_QUADRATICS));
        assertThrows(NullPointerException.class, () -> new LogCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () ->
                new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, null));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person studentWithLesson = student.withLessonAdded(LESSON_QUADRATICS.withDate(OCT_18));
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK);

        String expectedMessage = "Logged lesson for " + student.getName()
                + " on 18/10/2026: Quadratic inequalities";

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, studentWithLesson);

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person studentWithLesson = student.withLessonAdded(LESSON_QUADRATICS.withDate(OCT_18));
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK);

        String expectedMessage = "Logged lesson for " + student.getName()
                + " on 18/10/2026: Quadratic inequalities";

        // logging a lesson keeps the currently displayed (filtered) list
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(student, studentWithLesson);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(logCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_justAfterMidnightInSingapore_usesLocalDate() throws Exception {
        // 16:30 UTC on 18 Oct is 00:30 on 19 Oct in Singapore
        Clock justAfterMidnight = Clock.fixed(Instant.parse("2026-10-18T16:30:00Z"), SINGAPORE);
        CommandResult result = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, justAfterMidnight)
                .execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals("Logged lesson for " + student.getName() + " on 19/10/2026: Quadratic inequalities",
                result.getFeedbackToUser());
        assertEquals(List.of(LESSON_QUADRATICS.withDate(LocalDate.of(2026, 10, 19))), student.getLessons());
    }

    @Test
    public void execute_pastDate_keepsDateAndShowsItInMessage() throws Exception {
        Lesson pastLesson = LESSON_QUADRATICS.withDate(LocalDate.of(2026, 10, 17));
        CommandResult result = new LogCommand(INDEX_FIRST_PERSON, pastLesson, OCT_18_CLOCK).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals("Logged lesson for " + student.getName() + " on 17/10/2026: Quadratic inequalities",
                result.getFeedbackToUser());
        assertEquals(List.of(pastLesson), student.getLessons());
    }

    @Test
    public void execute_todayGivenExplicitly_success() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS.withDate(OCT_18), OCT_18_CLOCK).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(LESSON_QUADRATICS.withDate(OCT_18)), student.getLessons());
    }

    @Test
    public void execute_futureDate_throwsCommandException() {
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON,
                LESSON_QUADRATICS.withDate(LocalDate.of(2026, 10, 19)), OCT_18_CLOCK);

        assertCommandFailure(logCommand, model, LogCommand.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void execute_backdatedLesson_insertedInDateOrder() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK).execute(model);
        Lesson earlierLesson = LESSON_BINOMIAL.withDate(LocalDate.of(2026, 10, 16));
        new LogCommand(INDEX_FIRST_PERSON, earlierLesson, OCT_18_CLOCK).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(earlierLesson, LESSON_QUADRATICS.withDate(OCT_18)), student.getLessons());
    }

    @Test
    public void execute_studentWithLessons_appendsLessonAtEnd() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK).execute(model);
        new LogCommand(INDEX_FIRST_PERSON, LESSON_BINOMIAL, OCT_18_CLOCK).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(LESSON_QUADRATICS.withDate(OCT_18), LESSON_BINOMIAL.withDate(OCT_18)),
                student.getLessons());
    }

    @Test
    public void execute_sameLessonTwice_bothKept() throws Exception {
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK).execute(model);
        new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK).execute(model);

        Person student = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(List.of(LESSON_QUADRATICS.withDate(OCT_18), LESSON_QUADRATICS.withDate(OCT_18)),
                student.getLessons());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        LogCommand logCommand = new LogCommand(outOfBoundIndex, LESSON_QUADRATICS);

        assertCommandFailure(logCommand, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        LogCommand logCommand = new LogCommand(outOfBoundIndex, LESSON_QUADRATICS);

        assertCommandFailure(logCommand, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
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

        // default clock is the system clock -> returns true
        assertTrue(logFirstCommand.equals(
                new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, Clock.systemDefaultZone())));

        // different clock -> returns false
        assertFalse(logFirstCommand.equals(
                new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS, OCT_18_CLOCK)));
    }

    /**
     * Checks both assertion failures: no lesson added, and a different student returned.
     */
    @Test
    public void assertOneLessonAdded_invalidResult_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> LogCommand.assertOneLessonAdded(ALICE, ALICE));
        assertThrows(AssertionError.class, () -> LogCommand.assertOneLessonAdded(ALICE,
                BOB.withLessonAdded(LESSON_QUADRATICS)));
    }

    @Test
    public void toStringMethod() {
        LogCommand logCommand = new LogCommand(INDEX_FIRST_PERSON, LESSON_QUADRATICS);
        String expected = LogCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON
                + ", lesson=" + LESSON_QUADRATICS + "}";
        assertEquals(expected, logCommand.toString());
    }
}
