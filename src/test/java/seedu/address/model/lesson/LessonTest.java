package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestUtil.BLANK_OR_INVISIBLE_VALUES;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class LessonTest {
    private static final LocalDate OCT_18 = LocalDate.of(2026, 10, 18);
    private static final LocalDate OCT_19 = LocalDate.of(2026, 10, 19);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Lesson(null));
        assertThrows(NullPointerException.class, () -> new Lesson(null, OCT_18));
        assertThrows(NullPointerException.class, () -> new Lesson("Quadratic inequalities", null));
    }

    @Test
    public void constructor_datedInvalidContent_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Lesson("", OCT_18));
    }

    @Test
    public void getDate_undatedLesson_returnsEmpty() {
        assertEquals(Optional.empty(), new Lesson("Quadratic inequalities").getDate());
    }

    @Test
    public void getDate_datedLesson_returnsDate() {
        assertEquals(Optional.of(OCT_18), new Lesson("Quadratic inequalities", OCT_18).getDate());
    }

    @Test
    public void withDate_undatedLesson_returnsDatedCopyAndKeepsOriginal() {
        Lesson undated = new Lesson("Quadratic inequalities");
        assertEquals(new Lesson("Quadratic inequalities", OCT_18), undated.withDate(OCT_18));
        assertEquals(Optional.empty(), undated.getDate());
    }

    @Test
    public void withContent_datedLesson_keepsDateAndOriginal() {
        Lesson dated = new Lesson("Quadratic inequalities", OCT_18);
        assertEquals(new Lesson("Vectors", OCT_18), dated.withContent("Vectors"));
        assertEquals(new Lesson("Quadratic inequalities", OCT_18), dated);
    }

    @Test
    public void withContent_undatedLesson_staysUndated() {
        assertEquals(new Lesson("Vectors"), new Lesson("Quadratic inequalities").withContent("Vectors"));
    }

    @Test
    public void withContent_invalidContent_throwsException() {
        Lesson dated = new Lesson("Quadratic inequalities", OCT_18);
        assertThrows(NullPointerException.class, () -> dated.withContent(null));
        assertThrows(IllegalArgumentException.class, Lesson.MESSAGE_CONSTRAINTS, () -> dated.withContent(" "));
        assertThrows(IllegalArgumentException.class, Lesson.MESSAGE_CONSTRAINTS, () ->
                new Lesson("Quadratic inequalities").withContent("a".repeat(Lesson.MAX_LENGTH + 1)));
    }

    @Test
    public void constructor_invalidContent_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Lesson(""));
    }

    @Test
    public void isValidContent_whitespaceAndFormatOnly_returnsFalse() {
        for (String content : BLANK_OR_INVISIBLE_VALUES) {
            assertFalse(Lesson.isValidContent(content));
        }
    }

    @Test
    public void constructor_whitespaceAndFormatOnly_throwsIllegalArgumentException() {
        for (String content : BLANK_OR_INVISIBLE_VALUES) {
            assertThrows(IllegalArgumentException.class, Lesson.MESSAGE_CONSTRAINTS, () -> new Lesson(content));
        }
    }

    @Test
    public void constructor_visibleUnicodeContent_preservesTextAndFormatting() {
        for (String content : List.of("代数\u2003复习\u00a0Q1-5   完成", "می\u200cروم", "Teacher 👩\u200d🏫")) {
            assertEquals(content, new Lesson(content).content);
        }
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

        // same content, one dated and one undated -> returns false
        assertFalse(lesson.equals(new Lesson("Quadratic inequalities", OCT_18)));

        // same content and date -> returns true
        Lesson datedLesson = new Lesson("Quadratic inequalities", OCT_18);
        assertTrue(datedLesson.equals(new Lesson("Quadratic inequalities", OCT_18)));

        // same content, different dates -> returns false
        assertFalse(datedLesson.equals(new Lesson("Quadratic inequalities", OCT_19)));
    }

    @Test
    public void hashCodeMethod() {
        assertEquals(new Lesson("Quadratic inequalities").hashCode(),
                new Lesson("Quadratic inequalities").hashCode());
        assertEquals(new Lesson("Quadratic inequalities", OCT_18).hashCode(),
                new Lesson("Quadratic inequalities", OCT_18).hashCode());
    }

    @Test
    public void toStringMethod() {
        assertEquals("Quadratic inequalities", new Lesson("Quadratic inequalities").toString());
        // the date is left out, because command confirmations print lessons through toString
        assertEquals("Quadratic inequalities", new Lesson("Quadratic inequalities", OCT_18).toString());
    }
}
