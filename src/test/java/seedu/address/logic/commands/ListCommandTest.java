package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, String.format(ListCommand.MESSAGE_SUCCESS,
                expectedModel.getFilteredPersonList().size()), expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, String.format(ListCommand.MESSAGE_SUCCESS,
                expectedModel.getFilteredPersonList().size()), expectedModel);
    }

    @Test
    public void execute_emptyList_returnsEmptyMessage() {
        Model emptyModel = new ModelManager();
        assertCommandSuccess(new ListCommand(), emptyModel,
                "No students found. Use add to create a student.", new ModelManager());
    }

    @Test
    public void execute_singleStudent_returnsCount() {
        Model singleStudentModel = new ModelManager();
        singleStudentModel.addPerson(AMY);
        Model expectedSingleStudentModel = new ModelManager(singleStudentModel.getAddressBook(), new UserPrefs());
        assertCommandSuccess(new ListCommand(), singleStudentModel,
                "Listed 1 students.", expectedSingleStudentModel);
    }

    @Test
    public void execute_noFilterMatches_restoresStudentsAndReportsTotal() {
        model.updateFilteredPersonList(person -> false);
        assertCommandSuccess(new ListCommand(), model,
                String.format(ListCommand.MESSAGE_SUCCESS, expectedModel.getFilteredPersonList().size()),
                expectedModel);
    }
}
