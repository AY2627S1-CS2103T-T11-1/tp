package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LogCommand;
import seedu.address.model.lesson.Lesson;

public class LogCommandParserTest {

    private static final String VALID_CONTENT = "Quadratic inequalities";
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);
    private static final String MESSAGE_DATE_WITH_HINT =
            Lesson.MESSAGE_DATE_CONSTRAINTS + "\n" + Messages.MESSAGE_LESSON_DATE_TEXT_HINT;

    private LogCommandParser parser = new LogCommandParser();

    @Test
    public void parse_validArgs_returnsLogCommand() {
        LogCommand expectedCommand = new LogCommand(INDEX_FIRST_PERSON, new Lesson(VALID_CONTENT));

        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + VALID_CONTENT, expectedCommand);

        // extra whitespace around the index and content is ignored
        assertParseSuccess(parser, "   1   " + PREFIX_CONTENT + "  " + VALID_CONTENT + "  ", expectedCommand);

        // there is no arbitrary upper limit such as 1000
        assertParseSuccess(parser, " 1001 " + PREFIX_CONTENT + VALID_CONTENT,
                new LogCommand(Index.fromOneBased(1001), new Lesson(VALID_CONTENT)));
    }

    @Test
    public void parse_contentWithSymbols_keepsContentAsTyped() {
        String content = "Q1-5 (p. 42), revise   surds & indices!";
        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + content,
                new LogCommand(INDEX_FIRST_PERSON, new Lesson(content)));
    }

    @Test
    public void parse_withDate_returnsDatedLogCommand() {
        LogCommand expectedCommand = new LogCommand(INDEX_FIRST_PERSON,
                new Lesson(VALID_CONTENT, LocalDate.of(2026, 10, 21)));

        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + VALID_CONTENT + " " + PREFIX_DATE + "21/10/2026",
                expectedCommand);

        // date before content
        assertParseSuccess(parser, " 1 " + PREFIX_DATE + "21/10/2026 " + PREFIX_CONTENT + VALID_CONTENT,
                expectedCommand);
    }

    @Test
    public void parse_contentContainingShortDateText_keepsContent() {
        // d/ is not a prefix, so text such as d/dx stays in the content
        String content = "Revised d/dx notation";
        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + content,
                new LogCommand(INDEX_FIRST_PERSON, new Lesson(content)));
    }

    @Test
    public void parse_invalidDate_failureWithNotesHint() {
        String withContent = " 1 " + PREFIX_CONTENT + VALID_CONTENT + " " + PREFIX_DATE;

        // impossible date
        assertParseFailure(parser, withContent + "31/02/2026", MESSAGE_DATE_WITH_HINT);

        // year first
        assertParseFailure(parser, withContent + "2026-10-21", MESSAGE_DATE_WITH_HINT);

        // empty date
        assertParseFailure(parser, withContent, MESSAGE_DATE_WITH_HINT);
    }

    @Test
    public void parse_dateTextInContent_failureWithNotesHint() {
        // date/ inside the notes is still read as the date, so the error suggests rewording
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "Revised date/time conversions", MESSAGE_DATE_WITH_HINT);
    }

    @Test
    public void parse_nonDateErrors_omitDateHint() {
        // the index and content are checked before the date, so their errors have no date hint
        assertParseFailure(parser, " 0 " + PREFIX_CONTENT + "Revised date/time",
                MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "  " + PREFIX_DATE + "time", Lesson.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_rewordedNotes_success() {
        for (String content : new String[]{"Worksheet collected care of Mum", "Revised date and time conversions"}) {
            assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + content,
                    new LogCommand(INDEX_FIRST_PERSON, new Lesson(content)));
        }
    }

    @Test
    public void parse_duplicateDatePrefix_failure() {
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + VALID_CONTENT + " " + PREFIX_DATE + "21/10/2026 "
                + PREFIX_DATE + "20/10/2026", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
    }

    @Test
    public void parse_missingParts_failure() {
        // no index
        assertParseFailure(parser, " " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // no content prefix
        assertParseFailure(parser, " 1 " + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // nothing at all
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidNumericIndex_reportsInvalidStudentIndex() {
        // zero
        assertParseFailure(parser, " 0 " + PREFIX_CONTENT + VALID_CONTENT,
                MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);

        // negative
        assertParseFailure(parser, " -1 " + PREFIX_CONTENT + VALID_CONTENT,
                MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);

        // leading zero
        assertParseFailure(parser, " 01 " + PREFIX_CONTENT + VALID_CONTENT,
                MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);

        // too large to parse
        assertParseFailure(parser, " 2147483648 " + PREFIX_CONTENT + VALID_CONTENT,
                MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void parse_malformedIndex_reportsInvalidCommandFormat() {
        // not a number
        assertParseFailure(parser, " a " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // decimal
        assertParseFailure(parser, " 1.5 " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // explicit positive sign
        assertParseFailure(parser, " +1 " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidContent_failure() {
        // blank content
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "   ", Lesson.MESSAGE_CONSTRAINTS);

        // content over the limit
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "a".repeat(Lesson.MAX_LENGTH + 1),
                Lesson.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_contentAtLimit_success() {
        String content = "a".repeat(Lesson.MAX_LENGTH);
        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + content,
                new LogCommand(INDEX_FIRST_PERSON, new Lesson(content)));
    }

    @Test
    public void parse_duplicateContentPrefix_failure() {
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + VALID_CONTENT + " " + PREFIX_CONTENT + "Binomial theorem",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT));
        // c/ inside the notes is still read as a second content field, and the error suggests rewording
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "Worksheet collected c/o Mum",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT));
    }
}
