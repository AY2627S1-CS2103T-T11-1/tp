package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests find feedback, filter replacement and preservation of stored students and lessons.
 */
public class FindCommandTest {
    private static final Person WEI_MING = new PersonBuilder().withName("Tan Wei Ming")
            .withPhone("81230001").withLessons("Algebra", "Geometry").build();
    private static final Person WEI_JIE = new PersonBuilder().withName("Tan Wei Jie")
            .withPhone("81230002").build();
    private static final Person ALICIA = new PersonBuilder().withName("Alicia Lim")
            .withPhone("81230003").withLessons("Calculus").build();

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(WEI_MING);
        model.addPerson(WEI_JIE);
        model.addPerson(ALICIA);
    }

    @Test
    public void constructor_nullPredicate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> command("wei").execute(null));
    }

    @Test
    public void execute_multipleMatches_reportsCountAndKeepsOrderAndData() {
        AddressBook beforeFind = new AddressBook(model.getAddressBook());
        assertEquals("Found 2 students.", command("wEi").execute(model).getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE), model.getFilteredPersonList());
        assertEquals(beforeFind, model.getAddressBook());
    }

    @Test
    public void execute_singleMatch_reportsSingularCount() {
        assertEquals("Found 1 student.", command("ali").execute(model).getFeedbackToUser());
        assertEquals(List.of(ALICIA), model.getFilteredPersonList());
    }

    @Test
    public void execute_allKeywordsInAnyOrder_narrowsResults() {
        assertEquals("Found 1 student.", command("ming", "tan").execute(model).getFeedbackToUser());
        assertEquals(List.of(WEI_MING), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatches_reportsHowToRestoreFullList() {
        AddressBook beforeFind = new AddressBook(model.getAddressBook());
        assertEquals("No matching students found. Use list to show all students.",
                command("unknown").execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(beforeFind, model.getAddressBook());
        assertEquals("Listed 3 students.", new ListCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE, ALICIA), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyAddressBook_returnsNoMatches() {
        Model emptyModel = new ModelManager();
        assertEquals(FindCommand.MESSAGE_NO_MATCHES, command("wei").execute(emptyModel).getFeedbackToUser());
        assertTrue(emptyModel.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_previousFilter_searchesAllStudentsAgain() {
        command("wei").execute(model);
        assertEquals("Found 1 student.", command("ali").execute(model).getFeedbackToUser());
        assertEquals(List.of(ALICIA), model.getFilteredPersonList());
        command("unknown").execute(model);
        assertEquals("Found 2 students.", command("tan").execute(model).getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE), model.getFilteredPersonList());
    }

    @Test
    public void equalsAndHashCode_equalPredicates_workInHashSet() {
        FindCommand find = command("wei");
        FindCommand equivalent = command("WEI");
        assertTrue(find.equals(find));
        assertEquals(find, equivalent);
        assertFalse(find.equals(null));
        assertFalse(find.equals(new ListCommand()));
        assertFalse(find.equals(command("lim")));
        assertEquals(find.hashCode(), equivalent.hashCode());
        Set<FindCommand> commands = new HashSet<>(List.of(find));
        assertTrue(commands.contains(equivalent));
        assertFalse(commands.add(equivalent));
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("wei"));
        FindCommand find = new FindCommand(predicate);
        assertEquals(FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}", find.toString());
    }

    private static FindCommand command(String... keywords) {
        return new FindCommand(new NameContainsKeywordsPredicate(List.of(keywords)));
    }
}
