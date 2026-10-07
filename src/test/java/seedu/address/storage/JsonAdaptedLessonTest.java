package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedLesson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Lesson;

public class JsonAdaptedLessonTest {

    private static final String VALID_CONTENT = "Quadratic inequalities";
    private static final String INVALID_CONTENT = " ";

    @Test
    public void toModelType_validLesson_returnsLesson() throws Exception {
        Lesson lesson = new Lesson(VALID_CONTENT);
        assertEquals(lesson, new JsonAdaptedLesson(lesson).toModelType());
    }

    @Test
    public void toModelType_validContent_returnsLesson() throws Exception {
        assertEquals(new Lesson(VALID_CONTENT), new JsonAdaptedLesson(VALID_CONTENT).toModelType());
    }

    @Test
    public void toModelType_invalidContent_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson(INVALID_CONTENT);
        assertThrows(IllegalValueException.class, Lesson.MESSAGE_CONSTRAINTS, lesson::toModelType);
    }

    @Test
    public void toModelType_nullContent_throwsIllegalValueException() {
        JsonAdaptedLesson lesson = new JsonAdaptedLesson((String) null);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, "content");
        assertThrows(IllegalValueException.class, expectedMessage, lesson::toModelType);
    }
}
