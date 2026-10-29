package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Finds a student's lesson by the number {@code view} shows, for commands that act on one lesson.
 */
final class LessonCommandUtil {

    private LessonCommandUtil() {
        // Holds static helpers only.
    }

    /**
     * Returns the zero-based stored position of the lesson that {@code view} shows as {@code lessonNumber}.
     *
     * @throws CommandException if the student has no lessons or {@code lessonNumber} is beyond their history
     */
    static int getStoredLessonIndex(Person student, Index lessonNumber) throws CommandException {
        requireAllNonNull(student, lessonNumber);
        List<Lesson> lessons = student.getLessons();
        if (lessons.isEmpty()) {
            throw new CommandException(String.format(Messages.MESSAGE_NO_LESSONS, student.getName()));
        }
        if (lessonNumber.getZeroBased() >= lessons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
        return toStoredLessonIndex(lessons.size(), lessonNumber.getOneBased());
    }

    /**
     * Converts a validated newest-first lesson number to its zero-based position in stored order.
     * Asserts that the converted position is within the history.
     */
    static int toStoredLessonIndex(int lessonCount, int displayedIndex) {
        int storedIndex = lessonCount - displayedIndex;
        assert storedIndex >= 0 && storedIndex < lessonCount
                : "Converted lesson index must identify an existing lesson";
        return storedIndex;
    }
}
