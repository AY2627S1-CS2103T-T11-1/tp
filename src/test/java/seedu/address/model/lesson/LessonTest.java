package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LessonTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Lesson(null));
    }

    @Test
    public void constructor_invalidContent_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Lesson(""));
    }

    @Test
    public void isValidContent() {
        // null content
        assertThrows(NullPointerException.class, () -> Lesson.isValidContent(null));

        // invalid content
        assertFalse(Lesson.isValidContent("")); // empty string
        assertFalse(Lesson.isValidContent(" ")); // spaces only
        assertFalse(Lesson.isValidContent(" Quadratic inequalities")); // leading space
        assertFalse(Lesson.isValidContent("Surds\nIndices")); // line break
        assertFalse(Lesson.isValidContent("Surds\tIndices")); // tab
        assertFalse(Lesson.isValidContent("a".repeat(Lesson.MAX_LENGTH + 1))); // just over the limit

        // valid content
        assertTrue(Lesson.isValidContent("a")); // one character
        assertTrue(Lesson.isValidContent("Quadratic inequalities"));
        assertTrue(Lesson.isValidContent("Revision of trigonometric identities, Q1-5 (p. 42) & homework!"));
        assertTrue(Lesson.isValidContent("Surds   and   indices")); // repeated internal spaces kept
        assertTrue(Lesson.isValidContent("a".repeat(Lesson.MAX_LENGTH))); // exactly at the limit
    }

    @Test
    public void constructor_validContent_keepsContentUnchanged() {
        assertEquals("Surds   and   indices", new Lesson("Surds   and   indices").content);
    }

    @Test
    public void equals() {
        Lesson lesson = new Lesson("Quadratic inequalities");

        // same values -> returns true
        assertTrue(lesson.equals(new Lesson("Quadratic inequalities")));

        // same object -> returns true
        assertTrue(lesson.equals(lesson));

        // null -> returns false
        assertFalse(lesson.equals(null));

        // different types -> returns false
        assertFalse(lesson.equals(5.0f));

        // different values -> returns false
        assertFalse(lesson.equals(new Lesson("Binomial theorem")));
    }

    @Test
    public void hashCodeMethod() {
        assertEquals(new Lesson("Quadratic inequalities").hashCode(),
                new Lesson("Quadratic inequalities").hashCode());
    }

    @Test
    public void toStringMethod() {
        assertEquals("Quadratic inequalities", new Lesson("Quadratic inequalities").toString());
    }
}
