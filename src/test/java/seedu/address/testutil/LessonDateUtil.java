package seedu.address.testutil;

import java.time.LocalDate;

import seedu.address.logic.Messages;
import seedu.address.model.lesson.Lesson;

/**
 * A utility class for tests that log lessons through the app, which dates them with the system clock.
 */
public class LessonDateUtil {

    /**
     * Returns today's date as shown in {@code log} and {@code view} messages.
     */
    public static String todayText() {
        return LocalDate.now().format(Messages.LESSON_DATE_FORMATTER);
    }

    /**
     * Returns a lesson with {@code content}, logged today.
     */
    public static Lesson loggedToday(String content) {
        return new Lesson(content, LocalDate.now());
    }
}
