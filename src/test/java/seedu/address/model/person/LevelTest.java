package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class LevelTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Level(null));
    }

    @Test
    public void constructor_invalidLevel_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Level(""));
    }

    @Test
    public void isValidLevel() {
        // null
        assertThrows(NullPointerException.class, () -> Level.isValidLevel(null));

        // invalid
        assertFalse(Level.isValidLevel("")); // empty string
        assertFalse(Level.isValidLevel(" ")); // spaces only
        assertFalse(Level.isValidLevel("Sec#4")); // contains disallowed characters
        assertFalse(Level.isValidLevel("E-Math")); // hyphen not allowed in levels
        assertFalse(Level.isValidLevel(" Sec 4")); // leading space
        assertFalse(Level.isValidLevel("a".repeat(21))); // longer than 20 characters

        // valid
        assertTrue(Level.isValidLevel("Sec 4"));
        assertTrue(Level.isValidLevel("JC1"));
        assertTrue(Level.isValidLevel("P6"));
        assertTrue(Level.isValidLevel("IP Year 3"));
        assertTrue(Level.isValidLevel("a")); // one character
        assertTrue(Level.isValidLevel("a".repeat(20))); // exactly 20 characters
    }

    @Test
    public void equals() {
        Level value = new Level("Sec 4");

        // same values -> returns true
        assertTrue(value.equals(new Level("Sec 4")));

        // same object -> returns true
        assertTrue(value.equals(value));

        // null -> returns false
        assertFalse(value.equals(null));

        // different types -> returns false
        assertFalse(value.equals(5.0f));

        // different values -> returns false
        assertFalse(value.equals(new Level("JC1")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertTrue(new Level("Sec 4").hashCode() == new Level("Sec 4").hashCode());
    }

    @Test
    public void toStringMethod() {
        assertTrue(new Level("Sec 4").toString().equals("Sec 4"));
    }
}
