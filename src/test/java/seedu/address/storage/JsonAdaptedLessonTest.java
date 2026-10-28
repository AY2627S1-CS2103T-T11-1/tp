package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedLesson.MESSAGE_INVALID_DATE;
import static seedu.address.storage.JsonAdaptedLesson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Lesson;

public class JsonAdaptedLessonTest {

    private static final String VALID_CONTENT = "Quadratic inequalities";
    private static final String INVALID_CONTENT = " ";
    private static final String VALID_DATE = "2026-10-18";

    @Test
    public void toModelType_validLesson_returnsLesson() throws Exception {
        Lesson lesson = new Lesson(VALID_CONTENT);
        assertEquals(lesson, new JsonAdaptedLesson(lesson).toModelType());
    }

    @Test
    public void toModelType_validDatedLesson_returnsLessonWithDate() throws Exception {
        Lesson lesson = new Lesson(VALID_CONTENT, LocalDate.of(2026, 10, 18));
        assertEquals(lesson, new JsonAdaptedLesson(lesson).toModelType());
    }

    @Test
    public void toModelType_validContentAndDate_returnsLesson() throws Exception {
        assertEquals(new Lesson(VALID_CONTENT, LocalDate.of(2026, 10, 18)),
                new JsonAdaptedLesson(VALID_CONTENT, VALID_DATE).toModelType());
    }

    @Test
    public void toModelType_missingDate_returnsUndatedLesson() throws Exception {
        // lessons saved by v1.3 have no date field
        assertEquals(new Lesson(VALID_CONTENT), new JsonAdaptedLesson(VALID_CONTENT, null).toModelType());
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        for (String invalidDate : new String[] {"18/10/2026", "2026-02-30", "2026-13-01", "", "today"}) {
            JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_CONTENT, invalidDate);
            assertThrows(IllegalValueException.class, MESSAGE_INVALID_DATE, lesson::toModelType);
        }
    }

    @Test
    public void toModelType_dateBeforeMinimum_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(VALID_CONTENT, "1999-12-31");
        assertThrows(IllegalValueException.class, MESSAGE_INVALID_DATE, lesson::toModelType);
    }

    @Test
    public void toModelType_minimumAndFutureDates_preservesRecordedDate() throws Exception {
        for (LocalDate date : new LocalDate[]{Lesson.EARLIEST_DATE, LocalDate.now().plusDays(1),
            LocalDate.of(9999, 12, 31)}) {
            assertEquals(new Lesson(VALID_CONTENT, date),
                    new JsonAdaptedLesson(VALID_CONTENT, date.toString()).toModelType());
        }
    }

    @Test
    public void toModelType_invalidContent_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(INVALID_CONTENT, VALID_DATE);
        assertThrows(IllegalValueException.class, Lesson.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullContent_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(null, VALID_DATE);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "content");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }
}
