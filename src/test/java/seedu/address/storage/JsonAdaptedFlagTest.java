package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Flag;

public class JsonAdaptedFlagTest {

    @Test
    public void toModelType_missingReason_returnsFlagWithoutReason() throws Exception {
        assertEquals(new Flag(), new JsonAdaptedFlag((String) null).toModelType());
    }

    @Test
    public void toModelType_validReason_returnsFlagWithReason() throws Exception {
        Flag flag = new Flag("Missed homework twice");
        assertEquals(flag, new JsonAdaptedFlag(flag).toModelType());
        assertEquals(flag, new JsonAdaptedFlag("Missed homework twice").toModelType());
    }

    @Test
    public void toModelType_invalidReason_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Flag.MESSAGE_REASON_CONSTRAINTS, () ->
                new JsonAdaptedFlag(" ").toModelType());
        assertThrows(IllegalValueException.class, Flag.MESSAGE_REASON_CONSTRAINTS, () ->
                new JsonAdaptedFlag("a".repeat(Flag.MAX_REASON_LENGTH + 1)).toModelType());
    }
}
