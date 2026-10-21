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
 * Tests all-keyword partial name matching independently of the command parser.
 */
public class NameContainsKeywordsPredicateTest {
    @Test
    public void constructor_invalidKeywords_rejectsInvalidPredicate() {
        assertThrows(NullPointerException.class, () -> new NameContainsKeywordsPredicate(null));
        assertThrows(NullPointerException.class, ()
                -> new NameContainsKeywordsPredicate(Arrays.asList("tan", null)));
        assertThrows(IllegalArgumentException.class, () -> new NameContainsKeywordsPredicate(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new NameContainsKeywordsPredicate(List.of("")));
        assertThrows(IllegalArgumentException.class, () -> new NameContainsKeywordsPredicate(List.of("tan", " \t")));
    }

    @Test
    public void test_partialAndFullNamesIgnoringCase_matchesAllKeywords() {
        Person student = new PersonBuilder().withName("Tan Wei Ming").build();
        assertTrue(new NameContainsKeywordsPredicate(List.of("tan", "wei", "ming")).test(student));
        assertTrue(new NameContainsKeywordsPredicate(List.of("AN", "wE")).test(student));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ming", "tan")).test(student));
        assertTrue(new NameContainsKeywordsPredicate(List.of("tan", "tan")).test(student));
        assertFalse(new NameContainsKeywordsPredicate(List.of("tan", "jie")).test(student));
        assertFalse(new NameContainsKeywordsPredicate(List.of("weil")).test(student));
    }

    @Test
    public void test_onlyNameMatches_ignoresOtherFields() {
        Person student = new PersonBuilder().withName("Alex")
                .withAddress("Tan Road").withLevel("Tan").withSubject("Tan").build();
        assertFalse(new NameContainsKeywordsPredicate(List.of("tan")).test(student));
        assertFalse(new NameContainsKeywordsPredicate(List.of(student.getPhone().value)).test(student));
    }

    @Test
    public void test_punctuationAndUnicode_matchesLiterally() {
        assertTrue(new NameContainsKeywordsPredicate(List.of("o'br", "anne"))
                .test(new PersonBuilder().withName("Mary-Anne O'Brien").build()));
        assertTrue(new NameContainsKeywordsPredicate(List.of("a/l", "raju"))
                .test(new PersonBuilder().withName("Muthu A/L Raju").build()));
        assertTrue(new NameContainsKeywordsPredicate(List.of("élod"))
                .test(new PersonBuilder().withName("Élodie Tan").build()));
        assertTrue(new NameContainsKeywordsPredicate(List.of("小明"))
                .test(new PersonBuilder().withName("黄小明").build()));
        assertFalse(new NameContainsKeywordsPredicate(List.of(".*"))
                .test(new PersonBuilder().withName("Tan Wei Ming").build()));
    }

    @Test
    public void test_turkishDefaultLocale_matchingDoesNotDependOnSystemLocale() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(new NameContainsKeywordsPredicate(List.of("irf"))
                    .test(new PersonBuilder().withName("IRFAN").build()));
            assertTrue(new NameContainsKeywordsPredicate(List.of("İRF"))
                    .test(new PersonBuilder().withName("Irfan").build()));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void test_greekSigma_matchesUppercaseAndLowercaseSubstrings() {
        Person student = new PersonBuilder().withName("ΚΟΣΜΑΣ").build();
        for (String keyword : List.of("ΚΟΣ", "κοσ", "κος", "ΣΜΑΣ", "σμας", "ΚΟΣΜΑΣ")) {
            assertTrue(new NameContainsKeywordsPredicate(List.of(keyword)).test(student), keyword);
        }
        assertFalse(new NameContainsKeywordsPredicate(List.of("ΚΟΣΜΑΣΑ")).test(student));
        assertFalse(new NameContainsKeywordsPredicate(List.of("ΚΟΣ", "ΝΙΚ")).test(student));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ΚΟΣ"))
                .test(new PersonBuilder().withName("κοσμας").build()));
    }

    @Test
    public void test_supplementaryUnicodeLetters_matchesIgnoringCase() {
        // Deseret letters occupy surrogate pairs rather than single UTF-16 code units.
        assertTrue(new NameContainsKeywordsPredicate(List.of("\uD801\uDC28"))
                .test(new PersonBuilder().withName("Tan \uD801\uDC00").build()));
        assertFalse(new NameContainsKeywordsPredicate(List.of("\uD801\uDC29"))
                .test(new PersonBuilder().withName("Tan \uD801\uDC00").build()));
    }

    @Test
    public void constructor_mutableKeywords_copiesInput() {
        List<String> keywords = new ArrayList<>(List.of("tan"));
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);
        keywords.set(0, "lim");
        assertTrue(predicate.test(new PersonBuilder().withName("Tan Wei Ming").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("Alicia Lim").build()));
    }

    @Test
    public void equalsAndHashCode_equalKeywords_workInHashSet() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("tan", "WEI"));
        NameContainsKeywordsPredicate equivalent = new NameContainsKeywordsPredicate(List.of("tan", "WEI"));
        assertTrue(predicate.equals(predicate));
        assertEquals(predicate, equivalent);
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("tan wei"));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("lim"))));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("TAN", "wei"))));
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        Set<NameContainsKeywordsPredicate> predicates = new HashSet<>(List.of(predicate));
        assertTrue(predicates.contains(equivalent));
        assertFalse(predicates.add(equivalent));
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("TAN", "wei"));
        assertEquals(NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=[TAN, wei]}",
                predicate.toString());
    }
}
