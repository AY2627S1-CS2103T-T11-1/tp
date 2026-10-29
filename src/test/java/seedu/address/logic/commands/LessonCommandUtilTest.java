package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class LessonCommandUtilTest {

    private static final Person STUDENT_WITH_THREE_LESSONS =
            new PersonBuilder().withLessons("Surds", "Indices", "Logarithms").build();

    @Test
    public void getStoredLessonIndex_validNumber_returnsNewestFirstPosition() throws Exception {
        assertEquals(2, LessonCommandUtil.getStoredLessonIndex(STUDENT_WITH_THREE_LESSONS,
                Index.fromOneBased(1)));
        assertEquals(0, LessonCommandUtil.getStoredLessonIndex(STUDENT_WITH_THREE_LESSONS,
                Index.fromOneBased(3)));
    }

    @Test
    public void getStoredLessonIndex_numberBeyondHistory_throwsCommandException() {
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX, () ->
                LessonCommandUtil.getStoredLessonIndex(STUDENT_WITH_THREE_LESSONS, Index.fromOneBased(4)));
    }

    @Test
    public void getStoredLessonIndex_noLessons_throwsCommandException() {
        Person student = new PersonBuilder().build();
        assertThrows(CommandException.class, String.format(Messages.MESSAGE_NO_LESSONS, student.getName()), () ->
                LessonCommandUtil.getStoredLessonIndex(student, Index.fromOneBased(1)));
    }

    @Test
    public void getStoredLessonIndex_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                LessonCommandUtil.getStoredLessonIndex(null, Index.fromOneBased(1)));
        assertThrows(NullPointerException.class, () ->
                LessonCommandUtil.getStoredLessonIndex(STUDENT_WITH_THREE_LESSONS, null));
    }

    /**
     * Checks both assertion failures: a converted position below zero or beyond the last lesson.
     */
    @Test
    public void toStoredLessonIndex_invalidPosition_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> LessonCommandUtil.toStoredLessonIndex(3, 4));
        assertThrows(AssertionError.class, () -> LessonCommandUtil.toStoredLessonIndex(3, 0));
    }
}
