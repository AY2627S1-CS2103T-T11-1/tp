package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    private static final EditPersonDescriptor DESC_AMY = new EditPersonDescriptorBuilder().withName("Amy Bee")
            .withPhone("11111111").withAddress("Block 312, Amy Street 1").withLevel("Sec 4")
            .withSubject("E-Math").build();

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different name -> returns false
        assertFalse(DESC_AMY.equals(new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build()));

        // different phone -> returns false
        assertFalse(DESC_AMY.equals(new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build()));

        // different address -> returns false
        assertFalse(DESC_AMY.equals(new EditPersonDescriptorBuilder(DESC_AMY).withAddress(VALID_ADDRESS_BOB).build()));

        // different level -> returns false
        assertFalse(DESC_AMY.equals(new EditPersonDescriptorBuilder(DESC_AMY).withLevel(VALID_LEVEL_BOB).build()));

        // different subject -> returns false
        assertFalse(DESC_AMY.equals(new EditPersonDescriptorBuilder(DESC_AMY).withSubject(VALID_SUBJECT_BOB).build()));
    }

    @Test
    public void isAnyFieldEdited() {
        assertFalse(new EditPersonDescriptor().isAnyFieldEdited());
        assertTrue(new EditPersonDescriptorBuilder().withLevel(VALID_LEVEL_BOB).build().isAnyFieldEdited());
        assertTrue(new EditPersonDescriptorBuilder().withSubject(VALID_SUBJECT_BOB).build().isAnyFieldEdited());
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + descriptor.getName().orElse(null) + ", phone=" + descriptor.getPhone().orElse(null)
                + ", address=" + descriptor.getAddress().orElse(null) + ", level="
                + descriptor.getLevel().orElse(null) + ", subject=" + descriptor.getSubject().orElse(null) + "}";
        assertEquals(expected, descriptor.toString());
    }
}
