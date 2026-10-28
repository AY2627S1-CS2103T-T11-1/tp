package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks which {@code c/} and {@code date/} occurrences start a lesson field and which stay in the content.
 */
public class LessonArgumentTokenizerTest {

    @Test
    public void tokenize_contentOnly_splitsPreambleAndContent() {
        ArgumentMultimap argMultimap = LessonArgumentTokenizer.tokenize(" 1 c/Algebra ");

        assertTokens(argMultimap, "1", List.of("Algebra"), List.of());
    }

    @Test
    public void tokenize_laterContentPrefix_keptAsContent() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Worksheet collected c/o Mum"),
                "1", List.of("Worksheet collected c/o Mum"), List.of());
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Vectors c/Surds"),
                "1", List.of("Vectors c/Surds"), List.of());
    }

    @Test
    public void tokenize_datePrefixFollowedByNonDigit_keptAsContent() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Revised date/time conversions"),
                "1", List.of("Revised date/time conversions"), List.of());
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Revised date/  today's work"),
                "1", List.of("Revised date/  today's work"), List.of());
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Compared date/(old) notes"),
                "1", List.of("Compared date/(old) notes"), List.of());
    }

    @Test
    public void tokenize_datePrefixFollowedByDigit_readAsDate() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/21/10/2026"),
                "1", List.of("Algebra"), List.of("21/10/2026"));
        // spaces after the prefix are skipped before checking for a digit
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/ 21/10/2026"),
                "1", List.of("Algebra"), List.of("21/10/2026"));
        // dates that cannot be parsed are still given to the date parser, so they are rejected
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/2026-10-21"),
                "1", List.of("Algebra"), List.of("2026-10-21"));
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/21/10/2026 homework"),
                "1", List.of("Algebra"), List.of("21/10/2026 homework"));
    }

    @Test
    public void tokenize_emptyDateAfterContent_readAsDate() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/"), "1", List.of("Algebra"), List.of(""));
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/   "), "1", List.of("Algebra"), List.of(""));
    }

    @Test
    public void tokenize_contentPrefixTextAndRealDate_splitsBoth() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Revised date/time c/o Mum date/21/10/2026"),
                "1", List.of("Revised date/time c/o Mum"), List.of("21/10/2026"));
    }

    @Test
    public void tokenize_dateBeforeContent_parsedAsBefore() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 date/21/10/2026 c/Algebra c/o Mum"),
                "1", List.of("Algebra c/o Mum"), List.of("21/10/2026"));
        // before the content, any text after date/ is the date, as with ArgumentTokenizer
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 date/time c/Algebra"),
                "1", List.of("Algebra"), List.of("time"));
    }

    @Test
    public void tokenize_twoDates_keepsBothForDuplicateCheck() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 date/20/10/2026 c/Algebra date/21/10/2026"),
                "1", List.of("Algebra"), List.of("20/10/2026", "21/10/2026"));
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Algebra date/20/10/2026 date/21/10/2026"),
                "1", List.of("Algebra"), List.of("20/10/2026", "21/10/2026"));
    }

    @Test
    public void tokenize_noContentPrefix_parsedAsBefore() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 2 1 date/21/10/2026"),
                "2 1", List.of(), List.of("21/10/2026"));
        assertTokens(LessonArgumentTokenizer.tokenize(" 2 1"), "2 1", List.of(), List.of());
        // a prefix needs a space before it
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 1c/Vectors"), "1 1c/Vectors", List.of(), List.of());
    }

    @Test
    public void tokenize_blankContent_keepsEmptyContent() {
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/"), "1", List.of(""), List.of());
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/   date/21/10/2026"),
                "1", List.of(""), List.of("21/10/2026"));
    }

    @Test
    public void tokenize_unicodeSeparators_matchOrdinarySpaces() {
        for (String separator : List.of(" ", "\t", "\n", " ", "　")) {
            assertTokens(LessonArgumentTokenizer.tokenize(separator + "1" + separator + "c/Notes c/o Mum"
                    + separator + "date/" + separator + "21/10/2026" + separator),
                    "1", List.of("Notes c/o Mum"), List.of("21/10/2026"));
            assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Revised" + separator + "date/time"),
                    "1", List.of("Revised" + separator + "date/time"), List.of());
        }
        // whitespace inside the content is kept
        assertTokens(LessonArgumentTokenizer.tokenize(" 1 c/Quadratic   inequalities"),
                "1", List.of("Quadratic   inequalities"), List.of());
        // invisible characters that are not whitespace do not start a prefix
        assertTokens(LessonArgumentTokenizer.tokenize(" 1​c/Algebra"), "1​c/Algebra", List.of(), List.of());
    }

    @Test
    public void tokenize_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> LessonArgumentTokenizer.tokenize(null));
    }

    private static void assertTokens(ArgumentMultimap argMultimap, String preamble, List<String> contents,
            List<String> dates) {
        assertEquals(preamble, argMultimap.getPreamble());
        assertEquals(contents, argMultimap.getAllValues(PREFIX_CONTENT));
        assertEquals(dates, argMultimap.getAllValues(PREFIX_DATE));
    }
}
