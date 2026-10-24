package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;

public class FlaggedCommandTest {

    private static final Person FLAGGED_ALICE = ALICE.withFlag(new Flag());
    private static final Person FLAGGED_CARL = CARL.withFlag(new Flag("Needs revision"));
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(FLAGGED_ALICE);
        model.addPerson(BENSON);
        model.addPerson(FLAGGED_CARL);
    }

    @Test
    public void execute_multipleFlaggedStudents_filtersFullListInOriginalOrder() {
        model.updateFilteredPersonList(person -> person.equals(BENSON));
        assertEquals("Listed 2 flagged students.", new FlaggedCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(FLAGGED_ALICE, FLAGGED_CARL), model.getFilteredPersonList());
        assertEquals(List.of(FLAGGED_ALICE, BENSON, FLAGGED_CARL), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_singleFlaggedStudent_reportsSingular() {
        model.setPerson(FLAGGED_CARL, CARL);
        assertEquals("Listed 1 flagged student.", new FlaggedCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(FLAGGED_ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_noFlaggedStudents_reportsHowToRestoreList() {
        model.setPerson(FLAGGED_ALICE, ALICE);
        model.setPerson(FLAGGED_CARL, CARL);
        assertEquals(FlaggedCommand.MESSAGE_NO_FLAGGED,
                new FlaggedCommand().execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals("Listed 3 students.", new ListCommand().execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FlaggedCommand().execute(null));
    }
}
