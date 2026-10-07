package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Subject(null));
    }

    @Test
    public void constructor_invalidSubject_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Subject(""));
    }

    @Test
    public void isValidSubject() {
        // null
        assertThrows(NullPointerException.class, () -> Subject.isValidSubject(null));

        // invalid
        assertFalse(Subject.isValidSubject("")); // empty string
        assertFalse(Subject.isValidSubject(" ")); // spaces only
        assertFalse(Subject.isValidSubject("Math!")); // contains disallowed characters
        assertFalse(Subject.isValidSubject("Math/Science")); // slash not allowed in subjects
        assertFalse(Subject.isValidSubject("-Math")); // starts with a hyphen
        assertFalse(Subject.isValidSubject("a".repeat(51))); // longer than 50 characters

        // valid
        assertTrue(Subject.isValidSubject("E-Math"));
        assertTrue(Subject.isValidSubject("H2 Math"));
        assertTrue(Subject.isValidSubject("Additional Mathematics"));
        assertTrue(Subject.isValidSubject("a")); // one character
        assertTrue(Subject.isValidSubject("a".repeat(50))); // exactly 50 characters
    }

    @Test
    public void equals() {
        Subject value = new Subject("E-Math");

        // same values -> returns true
        assertTrue(value.equals(new Subject("E-Math")));

        // same object -> returns true
        assertTrue(value.equals(value));

        // null -> returns false
        assertFalse(value.equals(null));

        // different types -> returns false
        assertFalse(value.equals(5.0f));

        // different values -> returns false
        assertFalse(value.equals(new Subject("H2 Math")));
    }

    @Test
    public void hashCode_sameValue_sameHashCode() {
        assertTrue(new Subject("E-Math").hashCode() == new Subject("E-Math").hashCode());
    }

    @Test
    public void toStringMethod() {
        assertTrue(new Subject("E-Math").toString().equals("E-Math"));
    }
}
