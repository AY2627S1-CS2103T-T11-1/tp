package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

/**
 * Tests all-keyword partial matching on names, subjects and levels independently of the command parser.
 */
public class StudentContainsKeywordsPredicateTest {
    private static final Person PHYSICS_SEC_3 = new PersonBuilder().withName("Tan Wei Ming")
            .withSubject("Physics").withLevel("Sec 3").build();

    @Test
    public void constructor_invalidKeywords_rejectsInvalidPredicate() {
        assertThrows(NullPointerException.class, ()
                -> new StudentContainsKeywordsPredicate(null, List.of(), List.of()));
        assertThrows(NullPointerException.class, ()
                -> new StudentContainsKeywordsPredicate(List.of(), null, List.of()));
        assertThrows(NullPointerException.class, ()
                -> new StudentContainsKeywordsPredicate(List.of(), List.of(), null));
        assertThrows(NullPointerException.class, () -> names(Arrays.asList("tan", null)));
        assertThrows(IllegalArgumentException.class, () -> names(List.of()));
        assertThrows(IllegalArgumentException.class, () -> names(List.of("")));
        assertThrows(IllegalArgumentException.class, () -> names(List.of("tan", " \t")));
        assertThrows(IllegalArgumentException.class, ()
                -> new StudentContainsKeywordsPredicate(List.of(), List.of(" "), List.of()));
        assertThrows(IllegalArgumentException.class, ()
                -> new StudentContainsKeywordsPredicate(List.of(), List.of(), List.of("")));
    }

    @Test
    public void test_partialAndFullNamesIgnoringCase_matchesAllKeywords() {
        Person student = new PersonBuilder().withName("Tan Wei Ming").build();
        assertTrue(names("tan", "wei", "ming").test(student));
        assertTrue(names("AN", "wE").test(student));
        assertTrue(names("ming", "tan").test(student));
        assertTrue(names("tan", "tan").test(student));
        assertFalse(names("tan", "jie").test(student));
        assertFalse(names("weil").test(student));
    }

    @Test
    public void test_onlyNameKeywords_ignoresOtherFields() {
        Person student = new PersonBuilder().withName("Alex")
                .withAddress("Tan Road").withLevel("Tan").withSubject("Tan").build();
        assertFalse(names("tan").test(student));
        assertFalse(names(student.getPhone().value).test(student));
    }

    @Test
    public void test_subjectKeywords_matchesSubjectPartiallyIgnoringCase() {
        assertTrue(subjects("physics").test(PHYSICS_SEC_3));
        assertTrue(subjects("PHYS").test(PHYSICS_SEC_3));
        assertFalse(subjects("chemistry").test(PHYSICS_SEC_3));
        Person h2Math = new PersonBuilder().withSubject("H2 Math").build();
        assertTrue(subjects("math", "h2").test(h2Math));
        assertFalse(subjects("h2", "chem").test(h2Math));
    }

    @Test
    public void test_levelKeywords_matchesEveryLevelKeyword() {
        assertTrue(levels("sec", "3").test(PHYSICS_SEC_3));
        assertTrue(levels("3", "SEC").test(PHYSICS_SEC_3));
        assertFalse(levels("sec", "4").test(PHYSICS_SEC_3));
        assertFalse(levels("jc").test(PHYSICS_SEC_3));
    }

    @Test
    public void withTrailingFieldWordsAsNames_singleWordFields_returnsNoAlternatives() {
        assertTrue(names("tan", "wei").withTrailingFieldWordsAsNames().isEmpty());
        assertTrue(new StudentContainsKeywordsPredicate(List.of("tan"), List.of("physics"), List.of("3"))
                .withTrailingFieldWordsAsNames().isEmpty());
    }

    @Test
    public void withTrailingFieldWordsAsNames_multiWordFields_movesFewestWordsFirst() {
        StudentContainsKeywordsPredicate predicate = new StudentContainsKeywordsPredicate(List.of("tan"),
                List.of("physics", "ming"), List.of("sec", "3", "wei"));

        assertEquals(List.of(
                new StudentContainsKeywordsPredicate(List.of("tan", "ming"), List.of("physics"),
                        List.of("sec", "3", "wei")),
                new StudentContainsKeywordsPredicate(List.of("tan", "wei"), List.of("physics", "ming"),
                        List.of("sec", "3")),
                new StudentContainsKeywordsPredicate(List.of("tan", "3", "wei"), List.of("physics", "ming"),
                        List.of("sec"))),
                predicate.withTrailingFieldWordsAsNames());
        // the original search is unchanged
        assertEquals(List.of("physics", "ming"), predicate.getSubjectKeywords());
    }

    @Test
    public void getters_returnKeywordsAsGiven() {
        StudentContainsKeywordsPredicate predicate = new StudentContainsKeywordsPredicate(List.of("Tan"),
                List.of("Physics"), List.of("Sec", "3"));
        assertEquals(List.of("Tan"), predicate.getNameKeywords());
        assertEquals(List.of("Physics"), predicate.getSubjectKeywords());
        assertEquals(List.of("Sec", "3"), predicate.getLevelKeywords());
    }

    @Test
    public void isNameSearchOnly_onlyNameKeywords_returnsTrue() {
        assertTrue(names("tan").isNameSearchOnly());
        assertFalse(subjects("math").isNameSearchOnly());
        assertFalse(levels("3").isNameSearchOnly());
        assertFalse(new StudentContainsKeywordsPredicate(List.of("tan"), List.of("math"), List.of())
                .isNameSearchOnly());
        assertFalse(new StudentContainsKeywordsPredicate(List.of("tan"), List.of(), List.of("3"))
                .isNameSearchOnly());
    }

    @Test
    public void test_keywordsOnlyMatchTheirOwnField() {
        Person student = new PersonBuilder().withName("Sec Tan").withSubject("Math").withLevel("JC1").build();
        assertFalse(levels("tan").test(student));
        assertFalse(subjects("sec").test(student));
        assertFalse(names("math").test(student));
    }

    @Test
    public void test_severalFields_requiresEveryFieldToMatch() {
        assertTrue(new StudentContainsKeywordsPredicate(List.of("wei"), List.of("phys"), List.of("sec", "3"))
                .test(PHYSICS_SEC_3));
        assertFalse(new StudentContainsKeywordsPredicate(List.of("wei"), List.of("phys"), List.of("4"))
                .test(PHYSICS_SEC_3));
        assertFalse(new StudentContainsKeywordsPredicate(List.of("jie"), List.of("phys"), List.of("3"))
                .test(PHYSICS_SEC_3));
        assertFalse(new StudentContainsKeywordsPredicate(List.of(), List.of("math"), List.of("3"))
                .test(PHYSICS_SEC_3));
    }

    @Test
    public void test_punctuationAndUnicode_matchesLiterally() {
        assertTrue(names("o'br", "anne").test(new PersonBuilder().withName("Mary-Anne O'Brien").build()));
        assertTrue(names("a/l", "raju").test(new PersonBuilder().withName("Muthu A/L Raju").build()));
        assertTrue(names("élod").test(new PersonBuilder().withName("Élodie Tan").build()));
        assertTrue(names("小明").test(new PersonBuilder().withName("黄小明").build()));
        assertFalse(names(".*").test(new PersonBuilder().withName("Tan Wei Ming").build()));
        assertTrue(subjects("e-ma").test(new PersonBuilder().withSubject("E-Math").build()));
    }

    @Test
    public void test_turkishDefaultLocale_matchingDoesNotDependOnSystemLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(names("irf").test(new PersonBuilder().withName("IRFAN").build()));
            assertTrue(names("İRF").test(new PersonBuilder().withName("Irfan").build()));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_greekSigma_matchesUppercaseAndLowercaseSubstrings() {
        Person student = new PersonBuilder().withName("ΚΟΣΜΑΣ").build();
        for (String keyword : List.of("ΚΟΣ", "κοσ", "κος", "ΣΜΑΣ", "σμας", "ΚΟΣΜΑΣ")) {
            assertTrue(names(keyword).test(student), keyword);
        }
        assertFalse(names("ΚΟΣΜΑΣΑ").test(student));
        assertFalse(names("ΚΟΣ", "ΝΙΚ").test(student));
        assertTrue(names("ΚΟΣ").test(new PersonBuilder().withName("κοσμας").build()));
    }

    @Test
    public void test_supplementaryUnicodeLetters_matchesIgnoringCase() {
        // Deseret letters occupy surrogate pairs rather than single UTF-16 code units.
        assertTrue(names("𐐨").test(new PersonBuilder().withName("Tan 𐐀").build()));
        assertFalse(names("𐐩").test(new PersonBuilder().withName("Tan 𐐀").build()));
    }

    @Test
    public void constructor_mutableKeywords_copiesInput() {
        List<String> keywords = new ArrayList<>(List.of("tan"));
        StudentContainsKeywordsPredicate predicate = names(keywords);
        keywords.set(0, "lim");
        assertTrue(predicate.test(new PersonBuilder().withName("Tan Wei Ming").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Alicia Lim").build()));
    }

    @Test
    public void equalsAndHashCode_equalKeywords_workInHashSet() {
        StudentContainsKeywordsPredicate predicate = names("tan", "WEI");
        StudentContainsKeywordsPredicate equivalent = names("tan", "WEI");
        assertTrue(predicate.equals(predicate));
        assertEquals(predicate, equivalent);
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("tan wei"));
        assertFalse(predicate.equals(names("lim")));
        assertFalse(predicate.equals(names("TAN", "wei")));
        // the same keyword in a different field is a different search
        assertFalse(names("math").equals(subjects("math")));
        assertFalse(subjects("3").equals(levels("3")));
        StudentContainsKeywordsPredicate allFields =
                new StudentContainsKeywordsPredicate(List.of("tan"), List.of("math"), List.of("sec"));
        assertFalse(allFields.equals(new StudentContainsKeywordsPredicate(List.of("tan"), List.of("phys"),
                List.of("sec"))));
        assertFalse(allFields.equals(new StudentContainsKeywordsPredicate(List.of("tan"), List.of("math"),
                List.of("jc"))));
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        Set<StudentContainsKeywordsPredicate> predicates = new HashSet<>(List.of(predicate));
        assertTrue(predicates.contains(equivalent));
        assertFalse(predicates.add(equivalent));
    }

    @Test
    public void toStringMethod() {
        StudentContainsKeywordsPredicate predicate =
                new StudentContainsKeywordsPredicate(List.of("TAN", "wei"), List.of("Math"), List.of("Sec", "3"));
        assertEquals(StudentContainsKeywordsPredicate.class.getCanonicalName()
                + "{nameKeywords=[TAN, wei], subjectKeywords=[Math], levelKeywords=[Sec, 3]}", predicate.toString());
    }

    private static StudentContainsKeywordsPredicate names(String... keywords) {
        return names(List.of(keywords));
    }

    private static StudentContainsKeywordsPredicate names(List<String> keywords) {
        return new StudentContainsKeywordsPredicate(keywords, List.of(), List.of());
    }

    private static StudentContainsKeywordsPredicate subjects(String... keywords) {
        return new StudentContainsKeywordsPredicate(List.of(), List.of(keywords), List.of());
    }

    private static StudentContainsKeywordsPredicate levels(String... keywords) {
        return new StudentContainsKeywordsPredicate(List.of(), List.of(), List.of(keywords));
    }
}
