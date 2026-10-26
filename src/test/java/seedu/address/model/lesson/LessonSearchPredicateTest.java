package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests lesson search criteria independently of parsing, displayed numbers and storage.
 */
public class LessonSearchPredicateTest {
    private static final LocalDate START = LocalDate.of(2020, 1, 2);
    private static final LocalDate END = LocalDate.of(2020, 1, 4);

    @Test
    public void constructor_invalidCriteria_rejectsInvalidPredicate() {
        assertThrows(NullPointerException.class, () -> new LessonSearchPredicate(null, null, null));
        assertThrows(NullPointerException.class, ()
                -> new LessonSearchPredicate(Arrays.asList("algebra", null), null, null));
        assertThrows(IllegalArgumentException.class, () -> new LessonSearchPredicate(List.of(), null, null));
        for (String blank : List.of("", " \t", "\u00a0", "\u200b", "\u2060")) {
            assertThrows(IllegalArgumentException.class, ()
                    -> new LessonSearchPredicate(List.of(blank), START, null));
        }
        assertThrows(IllegalArgumentException.class, LessonSearchPredicate.MESSAGE_REVERSED_RANGE, ()
                -> new LessonSearchPredicate(List.of(), END, START));
        assertThrows(IllegalArgumentException.class, ()
                -> new LessonSearchPredicate(List.of(), Lesson.EARLIEST_DATE.minusDays(1), null));
        assertThrows(IllegalArgumentException.class, ()
                -> new LessonSearchPredicate(List.of(), null, Lesson.EARLIEST_DATE.minusDays(1)));
    }

    @Test
    public void constructor_mutatedKeywordList_keepsOriginalCriteria() {
        List<String> words = new ArrayList<>(List.of("algebra"));
        LessonSearchPredicate predicate = new LessonSearchPredicate(words, null, null);
        words.set(0, "geometry");
        assertTrue(predicate.test(new Lesson("Algebra revision")));
        assertFalse(predicate.test(new Lesson("Geometry revision")));
    }

    @Test
    public void test_contentOnly_requiresEveryLiteralKeywordIgnoringCaseAndOrder() {
        Lesson lesson = new Lesson("Reviewed quadratic inequalities and algebra");
        assertTrue(content("ALG", "quadratic").test(lesson));
        assertTrue(content("quadratic", "alg", "alg").test(lesson));
        assertFalse(content("algebra", "vectors").test(lesson));
        assertFalse(content("algebras").test(lesson));
        assertFalse(content(".*").test(lesson));
        assertTrue(content(".*", "d/dx").test(new Lesson("Worked on .* and d/dx")));
        assertTrue(content("κος").test(new Lesson("ΚΟΣΜΑΣ revision")));
    }

    @Test
    public void test_contentOnly_matchesDatedAndUndatedLessons() {
        LessonSearchPredicate predicate = content("algebra");
        assertTrue(predicate.test(new Lesson("Algebra")));
        assertTrue(predicate.test(new Lesson("Algebra", START)));
        assertFalse(predicate.test(new Lesson("Geometry", START)));
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void test_closedRange_includesBothBoundariesButExcludesUndatedLessons() {
        LessonSearchPredicate predicate = new LessonSearchPredicate(List.of(), START, END);
        assertFalse(predicate.test(new Lesson("Revision", START.minusDays(1))));
        assertTrue(predicate.test(new Lesson("Revision", START)));
        assertTrue(predicate.test(new Lesson("Revision", START.plusDays(1))));
        assertTrue(predicate.test(new Lesson("Revision", END)));
        assertFalse(predicate.test(new Lesson("Revision", END.plusDays(1))));
        assertFalse(predicate.test(new Lesson("Revision")));
    }

    @Test
    public void test_oneSidedRanges_leaveOtherBoundaryOpen() {
        LessonSearchPredicate fromOnly = new LessonSearchPredicate(List.of(), START, null);
        LessonSearchPredicate toOnly = new LessonSearchPredicate(List.of(), null, END);
        assertFalse(fromOnly.test(new Lesson("Revision", START.minusDays(1))));
        assertTrue(fromOnly.test(new Lesson("Revision", START)));
        assertTrue(fromOnly.test(new Lesson("Revision", END.plusYears(1))));
        assertTrue(toOnly.test(new Lesson("Revision", Lesson.EARLIEST_DATE)));
        assertTrue(toOnly.test(new Lesson("Revision", END)));
        assertFalse(toOnly.test(new Lesson("Revision", END.plusDays(1))));
        assertFalse(fromOnly.test(new Lesson("Revision")));
        assertFalse(toOnly.test(new Lesson("Revision")));
    }

    @Test
    public void test_sameDayAndFutureRanges_acceptQueryBoundaries() {
        LessonSearchPredicate sameDay = new LessonSearchPredicate(List.of(), START, START);
        assertTrue(sameDay.test(new Lesson("Revision", START)));
        assertFalse(sameDay.test(new Lesson("Revision", START.minusDays(1))));
        assertFalse(sameDay.test(new Lesson("Revision", START.plusDays(1))));
        LessonSearchPredicate futureEnd = new LessonSearchPredicate(
                List.of(), null, LocalDate.of(9999, 12, 31));
        assertTrue(futureEnd.test(new Lesson("Revision", START)));
    }

    @Test
    public void test_combinedCriteria_requiresContentAndDateToMatch() {
        LessonSearchPredicate predicate = new LessonSearchPredicate(List.of("alg", "revision"), START, END);
        assertTrue(predicate.test(new Lesson("Algebra revision", START)));
        assertFalse(predicate.test(new Lesson("Geometry revision", START)));
        assertFalse(predicate.test(new Lesson("Algebra revision", END.plusDays(1))));
        assertFalse(predicate.test(new Lesson("Algebra revision")));
    }

    @Test
    public void equalsAndHashCode_compareEveryCriterion() {
        LessonSearchPredicate predicate = new LessonSearchPredicate(List.of("algebra"), START, END);
        LessonSearchPredicate equivalent = new LessonSearchPredicate(List.of("algebra"), START, END);
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(equivalent));
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("algebra"));
        assertFalse(predicate.equals(new LessonSearchPredicate(List.of("geometry"), START, END)));
        assertFalse(predicate.equals(new LessonSearchPredicate(List.of("algebra"), null, END)));
        assertFalse(predicate.equals(new LessonSearchPredicate(List.of("algebra"), START, null)));
    }

    @Test
    public void toStringMethod_describesCriteria() {
        assertEquals(LessonSearchPredicate.class.getCanonicalName()
                + "{keywords=[algebra], fromDate=2020-01-02, toDate=2020-01-04}",
                new LessonSearchPredicate(List.of("algebra"), START, END).toString());
    }

    private static LessonSearchPredicate content(String... keywords) {
        return new LessonSearchPredicate(List.of(keywords), null, null);
    }
}
