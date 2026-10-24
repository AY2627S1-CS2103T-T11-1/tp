package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FlagTest {

    private static final List<String> BLANK_VALUES = List.of("", " ", "\u2003", "\u00a0", "\u200b",
            " \u2003\u00a0\u200b\u2060 ");

    @Test
    public void constructor_withoutReason_hasEmptyReason() {
        assertEquals(Optional.empty(), new Flag().getReason());
    }

    @Test
    public void constructor_nullReason_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Flag(null));
    }

    @Test
    public void constructor_invalidReason_throwsIllegalArgumentException() {
        for (String reason : BLANK_VALUES) {
            assertThrows(IllegalArgumentException.class, Flag.MESSAGE_REASON_CONSTRAINTS, () -> new Flag(reason));
        }
        assertThrows(IllegalArgumentException.class, Flag.MESSAGE_REASON_CONSTRAINTS, () ->
                new Flag("a".repeat(Flag.MAX_REASON_LENGTH + 1)));
        assertThrows(IllegalArgumentException.class, Flag.MESSAGE_REASON_CONSTRAINTS, () ->
                new Flag("Call parent\nthis evening"));
    }

    @Test
    public void isValidReason_acceptsBoundariesAndUsefulPunctuation() {
        assertTrue(Flag.isValidReason("a"));
        assertTrue(Flag.isValidReason("a".repeat(Flag.MAX_REASON_LENGTH)));
        assertTrue(Flag.isValidReason("Missed a/level worksheet; call parent at 6:30."));
        assertFalse(Flag.isValidReason("a".repeat(Flag.MAX_REASON_LENGTH + 1)));
    }

    @Test
    public void equalsAndHashCode_useReason() {
        Flag flag = new Flag("Missed homework");
        Flag equivalent = new Flag("Missed homework");
        assertEquals(flag, equivalent);
        assertEquals(flag.hashCode(), equivalent.hashCode());
        assertTrue(flag.equals(flag));
        assertFalse(flag.equals(new Flag("Needs revision")));
        assertFalse(flag.equals(new Flag()));
        assertFalse(flag.equals(null));
        assertFalse(flag.equals("Missed homework"));
    }

    @Test
    public void toStringMethod_includesReason() {
        assertEquals(Flag.class.getCanonicalName() + "{reason=Missed homework}",
                new Flag("Missed homework").toString());
    }
}
