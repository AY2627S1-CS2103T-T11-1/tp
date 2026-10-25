package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;

public class UnflagCommandTest {

    private static final Person FLAGGED_ALICE = ALICE.withFlag(new Flag("Call parent"));
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(FLAGGED_ALICE);
        model.addPerson(BENSON);
    }

    @Test
    public void execute_flaggedStudent_removesFlagAndReason() throws Exception {
        assertEquals("Unflagged student: Alice Pauline.",
                new UnflagCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_unflaggedStudent_reportsNoChange() {
        assertCommandFailure(new UnflagCommand(INDEX_SECOND_PERSON), model,
                "Benson Meier is not flagged.");
    }

    @Test
    public void execute_flaggedFilter_studentDisappears() throws Exception {
        new FlaggedCommand().execute(model);
        assertEquals(List.of(FLAGGED_ALICE), model.getFilteredPersonList());
        new UnflagCommand(INDEX_FIRST_PERSON).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(List.of(ALICE, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidIndex_reportsSharedMessage() {
        assertCommandFailure(new UnflagCommand(Index.fromOneBased(3)), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnflagCommand(null));
    }

    @Test
    public void equalsHashCodeAndToString_useIndex() {
        UnflagCommand command = new UnflagCommand(INDEX_FIRST_PERSON);
        UnflagCommand equivalent = new UnflagCommand(Index.fromOneBased(1));
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertTrue(command.equals(command));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new UnflagCommand(INDEX_SECOND_PERSON)));
        assertTrue(command.toString().contains("targetIndex=" + INDEX_FIRST_PERSON));
    }
}
