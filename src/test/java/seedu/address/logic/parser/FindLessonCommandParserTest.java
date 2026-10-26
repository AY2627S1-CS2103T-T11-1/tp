package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FROM;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TO;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindLessonCommand;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSearchPredicate;

/** Tests the lesson search syntax and shared index/date validation policies. */
public class FindLessonCommandParserTest {
    private static final LocalDate START = LocalDate.of(2020, 1, 2);
    private static final LocalDate END = LocalDate.of(2020, 1, 4);
    private static final String USAGE = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindLessonCommand.MESSAGE_USAGE);
    private final FindLessonCommandParser parser = new FindLessonCommandParser();

    @Test
    public void parse_missingIndexOrCriteria_reportsUsage() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
        for (String args : List.of("", " ", "1", "1 \u00a0", "1 \u200b", "from/2/1/2020", "algebra")) {
            assertParseFailure(parser, args, USAGE);
        }
        assertParseFailure(parser, "1 algebra \u2060 from/2/1/2020", USAGE);
    }

    @Test
    public void parse_indexPolicy_distinguishesInvalidNumbersFromMalformedInput() {
        for (String index : List.of("0", "-1", "01", "2147483648", "999999999999999")) {
            assertParseFailure(parser, index + " algebra", Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }
        for (String index : List.of("+1", "1.0", "one")) {
            assertParseFailure(parser, index + " algebra", USAGE);
        }
        assertParseSuccess(parser, "2147483647 algebra", new FindLessonCommand(Index.fromOneBased(Integer.MAX_VALUE),
                new LessonSearchPredicate(List.of("algebra"), null, null)));
    }

    @Test
    public void parse_keywordsAndWhitespace_keepsLiteralKeywords() {
        FindLessonCommand expected = search(List.of("ALG", "revision"), null, null);
        assertParseSuccess(parser, "1 ALG revision", expected);
        assertParseSuccess(parser, " \u20031\tALG\u00a0revision\u3000 ", expected);
        assertParseSuccess(parser, "1 d/dx .* c/algebra",
                search(List.of("d/dx", ".*", "c/algebra"), null, null));
    }

    @Test
    public void parse_dateOnlyAndCombined_acceptsEitherPrefixOrder() {
        assertParseSuccess(parser, "1 from/2/1/2020", search(List.of(), START, null));
        assertParseSuccess(parser, "1 to/04/01/2020", search(List.of(), null, END));
        assertParseSuccess(parser, "1 from/2/1/2020 to/4/1/2020", search(List.of(), START, END));
        assertParseSuccess(parser, "1 alg revision to/4/1/2020 from/2/1/2020",
                search(List.of("alg", "revision"), START, END));
        assertParseSuccess(parser, "1\u00a0alg\tfrom/2/1/2020\u2003to/4/1/2020",
                search(List.of("alg"), START, END));
    }

    @Test
    public void parse_sameDayEarliestAndFutureDates_acceptsQueryLimits() {
        assertParseSuccess(parser, "1 from/2/1/2020 to/2/1/2020", search(List.of(), START, START));
        assertParseSuccess(parser, "1 from/1/1/2000", search(List.of(), Lesson.EARLIEST_DATE, null));
        LocalDate future = LocalDate.of(9999, 12, 31);
        assertParseSuccess(parser, "1 to/31/12/9999", search(List.of(), null, future));
        assertParseSuccess(parser, "1 from/31/12/9999", search(List.of(), future, null));
        assertParseSuccess(parser, "1 from/29/2/2020", search(List.of(), LocalDate.of(2020, 2, 29), null));
    }

    @Test
    public void parse_duplicatePrefixes_reportsSharedDuplicateMessage() {
        assertParseFailure(parser, "1 from/2/1/2020 from/3/1/2020",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FROM));
        assertParseFailure(parser, "1 to/4/1/2020\u00a0to/4/1/2020",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TO));
    }

    @Test
    public void parse_invalidOrBlankDates_reportsDateConstraints() {
        for (String date : List.of("", "\u00a0", "\u200b", "31/2/2020", "29/2/2019", "1/1/1999",
                "2020-01-02", "1/1/20", "+1/1/2020", "1/1/2020 algebra")) {
            assertParseFailure(parser, "1 from/" + date, Lesson.MESSAGE_DATE_CONSTRAINTS);
            assertParseFailure(parser, "1 algebra to/" + date, Lesson.MESSAGE_DATE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_reversedRange_reportsRangeError() {
        assertParseFailure(parser, "1 from/4/1/2020 to/2/1/2020", LessonSearchPredicate.MESSAGE_REVERSED_RANGE);
    }

    private static FindLessonCommand search(List<String> keywords, LocalDate from, LocalDate to) {
        return new FindLessonCommand(Index.fromOneBased(1), new LessonSearchPredicate(keywords, from, to));
    }
}
