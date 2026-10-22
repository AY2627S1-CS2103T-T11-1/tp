package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Lesson;

public class MessagesTest {

    @Test
    public void formatLessonDate_datedLesson_returnsDayMonthYear() {
        Lesson lesson = new Lesson("Algebra", LocalDate.of(2026, 10, 18));
        assertEquals("18/10/2026", Messages.formatLessonDate(lesson));
    }

    @Test
    public void formatLessonDate_singleDigitDayAndMonth_padsWithZeros() {
        Lesson lesson = new Lesson("Algebra", LocalDate.of(2026, 3, 5));
        assertEquals("05/03/2026", Messages.formatLessonDate(lesson));
    }

    @Test
    public void formatLessonDate_undatedLesson_returnsUndated() {
        assertEquals("(undated)", Messages.formatLessonDate(new Lesson("Algebra")));
    }
}
