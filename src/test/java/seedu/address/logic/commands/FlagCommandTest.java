package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;

public class FlagCommandTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FlagCommand(null));
        assertThrows(NullPointerException.class, () -> new FlagCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_unflaggedWithoutReason_flagsStudent() {
        Person expectedStudent = ALICE.withFlag(new Flag());
        Model expectedModel = copyModel();
        expectedModel.setPerson(ALICE, expectedStudent);
        assertCommandSuccess(new FlagCommand(INDEX_FIRST_PERSON), model,
                new CommandResult("Flagged student: Alice Pauline.", expectedStudent), expectedModel);
    }

    @Test
    public void execute_unflaggedWithReason_flagsStudent() {
        Flag flag = new Flag("Missed homework twice");
        Model expectedModel = copyModel();
        expectedModel.setPerson(ALICE, ALICE.withFlag(flag));
        assertCommandSuccess(new FlagCommand(INDEX_FIRST_PERSON, flag), model,
                new CommandResult("Flagged student: Alice Pauline. Reason: Missed homework twice",
                        ALICE.withFlag(flag)), expectedModel);
    }

    @Test
    public void execute_existingDifferentReason_replacesReason() throws Exception {
        model.setPerson(ALICE, ALICE.withFlag(new Flag("Missed homework")));
        Flag replacement = new Flag("Call parent");
        CommandResult result = new FlagCommand(INDEX_FIRST_PERSON, replacement).execute(model);
        assertEquals("Updated flag for Alice Pauline. Reason: Call parent", result.getFeedbackToUser());
        assertEquals(replacement, model.getFilteredPersonList().get(0).getFlag().orElseThrow());
        assertEquals(model.getFilteredPersonList().get(0), result.getStudent().orElseThrow());
    }

    @Test
    public void execute_existingSameReason_reportsNoChange() {
        Flag flag = new Flag("Missed homework");
        model.setPerson(ALICE, ALICE.withFlag(flag));
        assertCommandFailure(new FlagCommand(INDEX_FIRST_PERSON, flag), model,
                "Alice Pauline is already flagged with that reason.");
    }

    @Test
    public void execute_existingWithoutRequestedReason_keepsExistingReason() {
        model.setPerson(ALICE, ALICE.withFlag(new Flag("Call parent")));
        assertCommandFailure(new FlagCommand(INDEX_FIRST_PERSON), model,
                "Alice Pauline is already flagged.");
        assertEquals("Call parent", model.getFilteredPersonList().get(0).getFlag().orElseThrow()
                .getReason().orElseThrow());
    }

    @Test
    public void execute_filteredList_targetsDisplayedIndexAndRetainsFilter() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Model expectedModel = copyModel();
        Person flaggedBenson = BENSON.withFlag(new Flag());
        expectedModel.setPerson(BENSON, flaggedBenson);
        expectedModel.updateFilteredPersonList(flaggedBenson::isSamePerson);
        assertCommandSuccess(new FlagCommand(INDEX_FIRST_PERSON), model,
                new CommandResult("Flagged student: Benson Meier.", flaggedBenson), expectedModel);
    }

    @Test
    public void execute_invalidIndex_reportsSharedMessage() {
        assertCommandFailure(new FlagCommand(Index.fromOneBased(8)), model,
                Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void equalsHashCodeAndToString_useIndexAndFlag() {
        FlagCommand command = new FlagCommand(INDEX_FIRST_PERSON, new Flag("Call parent"));
        FlagCommand equivalent = new FlagCommand(INDEX_FIRST_PERSON, new Flag("Call parent"));
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertTrue(command.equals(command));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new FlagCommand(INDEX_SECOND_PERSON, new Flag("Call parent"))));
        assertFalse(command.equals(new FlagCommand(INDEX_FIRST_PERSON, new Flag("Missed homework"))));
        assertTrue(command.toString().contains("targetIndex=" + INDEX_FIRST_PERSON));
        assertTrue(command.toString().contains("requestedFlag="));
    }

    private Model copyModel() {
        return new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
    }
}
