package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Prefers weekend lessons");

        assertTrue(remark.equals(new Remark("Prefers weekend lessons")));
        assertTrue(remark.equals(remark));
        assertFalse(remark.equals(null));
        assertFalse(remark.equals("Prefers weekend lessons"));
        assertFalse(remark.equals(new Remark("Different remark")));
    }

    @Test
    public void toStringMethod() {
        assertEquals("Prefers weekend lessons", new Remark("Prefers weekend lessons").toString());
    }
}
