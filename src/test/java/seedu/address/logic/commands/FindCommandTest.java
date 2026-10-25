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
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentContainsKeywordsPredicate;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests find feedback, field matching, filter replacement and preservation of stored students and lessons.
 */
public class FindCommandTest {
    private static final Person WEI_MING = new PersonBuilder().withName("Tan Wei Ming")
            .withPhone("81230001").withLevel("Sec 3").withSubject("Physics")
            .withLessons("Algebra", "Geometry").build();
    private static final Person WEI_JIE = new PersonBuilder().withName("Tan Wei Jie")
            .withPhone("81230002").withLevel("Sec 4").withSubject("Physics").build();
    private static final Person ALICIA = new PersonBuilder().withName("Alicia Lim")
            .withPhone("81230003").withLevel("Sec 3").withSubject("E-Math").withLessons("Calculus").build();

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
    public void execute_subjectAndLevel_matchesEveryGivenField() {
        assertEquals("Found 2 students.", fields(List.of(), List.of("phys"), List.of()).execute(model)
                .getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE), model.getFilteredPersonList());
        assertEquals("Found 2 students.", fields(List.of(), List.of(), List.of("sec", "3")).execute(model)
                .getFeedbackToUser());
        assertEquals(List.of(WEI_MING, ALICIA), model.getFilteredPersonList());
        assertEquals("Found 1 student.", fields(List.of(), List.of("PHYSICS"), List.of("3")).execute(model)
                .getFeedbackToUser());
        assertEquals(List.of(WEI_MING), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameAndFieldsCombined_requiresAllToMatch() {
        assertEquals("Found 1 student.", fields(List.of("tan"), List.of("physics"), List.of("4")).execute(model)
                .getFeedbackToUser());
        assertEquals(List.of(WEI_JIE), model.getFilteredPersonList());
        assertEquals(FindCommand.MESSAGE_NO_MATCHES, fields(List.of("alicia"), List.of("physics"), List.of())
                .execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_noMatches_reportsHowToRestoreFullList() {
        AddressBook beforeFind = new AddressBook(model.getAddressBook());
        assertEquals("No matching students found. Use list to show all students. "
                + "To search by subject or level, use sub/ or l/, e.g. find sub/Physics.",
                command("unknown").execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(beforeFind, model.getAddressBook());
        assertEquals("Listed 3 students.", new ListCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(WEI_MING, WEI_JIE, ALICIA), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatchesWithSubjectOrLevel_omitsFieldHint() {
        // the hint about sub/ and l/ is only for searches that used names alone
        assertEquals(FindCommand.MESSAGE_NO_MATCHES, fields(List.of(), List.of("chem"), List.of()).execute(model)
                .getFeedbackToUser());
        assertEquals(FindCommand.MESSAGE_NO_MATCHES, fields(List.of(), List.of(), List.of("jc")).execute(model)
                .getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_emptyAddressBook_returnsNoMatches() {
        Model emptyModel = new ModelManager();
        assertEquals(FindCommand.MESSAGE_NO_NAME_MATCHES, command("wei").execute(emptyModel).getFeedbackToUser());
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
        FindCommand equivalent = command("wei");
        assertTrue(find.equals(find));
        assertEquals(find, equivalent);
        assertFalse(find.equals(null));
        assertFalse(find.equals(new ListCommand()));
        assertFalse(find.equals(command("lim")));
        assertFalse(find.equals(fields(List.of(), List.of("wei"), List.of())));
        assertEquals(find.hashCode(), equivalent.hashCode());
        Set<FindCommand> commands = new HashSet<>(List.of(find));
        assertTrue(commands.contains(equivalent));
        assertFalse(commands.add(equivalent));
    }

    @Test
    public void toStringMethod() {
        StudentContainsKeywordsPredicate predicate = names("wei");
        FindCommand find = new FindCommand(predicate);
        assertEquals(FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}", find.toString());
    }

    private static FindCommand command(String... nameKeywords) {
        return new FindCommand(names(nameKeywords));
    }

    private static FindCommand fields(List<String> names, List<String> subjects, List<String> levels) {
        return new FindCommand(new StudentContainsKeywordsPredicate(names, subjects, levels));
    }

    private static StudentContainsKeywordsPredicate names(String... keywords) {
        return new StudentContainsKeywordsPredicate(List.of(keywords), List.of(), List.of());
    }
}
