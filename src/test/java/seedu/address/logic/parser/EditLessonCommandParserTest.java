package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditLessonCommand;
import seedu.address.logic.commands.EditLessonCommand.EditLessonDescriptor;
import seedu.address.model.lesson.Lesson;

/**
 * Verifies that lesson editing requires two valid displayed indexes and at least one valid new detail.
 */
public class EditLessonCommandParserTest {
    private static final Index SECOND = Index.fromOneBased(2);
    private static final Index FIRST = Index.fromOneBased(1);
    private static final LocalDate OCT_21 = LocalDate.of(2026, 10, 21);
    private static final String MESSAGE_DATE_WITH_HINT =
            Lesson.MESSAGE_DATE_CONSTRAINTS + "\n" + Messages.MESSAGE_LESSON_DATE_TEXT_HINT;

    private final EditLessonCommandParser parser = new EditLessonCommandParser();

    @Test
    public void parse_contentOnly_success() {
        assertParseSuccess(parser, "2 1 c/Vectors", new EditLessonCommand(SECOND, FIRST, descriptor("Vectors", null)));
        // surrounding spaces are trimmed and spaces inside the content are kept
        assertParseSuccess(parser, " \t2  1  c/  Vectors  and   scalars  ",
                new EditLessonCommand(SECOND, FIRST, descriptor("Vectors  and   scalars", null)));
    }

    @Test
    public void parse_dateOnly_success() {
        assertParseSuccess(parser, "2 1 date/21/10/2026", new EditLessonCommand(SECOND, FIRST,
                descriptor(null, OCT_21)));
        assertParseSuccess(parser, "2 1 date/1/1/2000", new EditLessonCommand(SECOND, FIRST,
                descriptor(null, Lesson.EARLIEST_DATE)));
    }

    @Test
    public void parse_contentAndDateInEitherOrder_success() {
        EditLessonCommand expected = new EditLessonCommand(SECOND, FIRST, descriptor("Vectors", OCT_21));
        assertParseSuccess(parser, "2 1 c/Vectors date/21/10/2026", expected);
        assertParseSuccess(parser, "2 1 date/21/10/2026 c/Vectors", expected);
    }

    /**
     * Keeps text that only looks like another command's prefix inside the content.
     */
    @Test
    public void parse_otherPrefixesInContent_keptAsContent() {
        assertParseSuccess(parser, "1 1 c/Revised a/level n/notes", new EditLessonCommand(FIRST, FIRST,
                descriptor("Revised a/level n/notes", null)));
    }

    @Test
    public void parse_noFieldToEdit_failure() {
        assertParseFailure(parser, "2 1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, " 2 \t 1 ", EditCommand.MESSAGE_NOT_EDITED);
    }

    /**
     * Rejects missing, extra and non-integer index arguments with the command usage.
     */
    @Test
    public void parse_malformedIndexes_reportsUsage() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditLessonCommand.MESSAGE_USAGE);
        for (String preamble : new String[]{"", " ", "1", "1 2 3", "abc 1", "1 abc", "+1 1", "1 +1",
            "1.5 1", "1 1.5", "1 1 extra", "1 1c/Vectors"}) {
            assertParseFailure(parser, preamble + " c/Vectors", expected);
        }
    }

    /**
     * Identifies invalid numeric student indexes separately from lesson indexes.
     */
    @Test
    public void parse_invalidNumericIndexes_reportsRelevantIndex() {
        for (String index : new String[]{"0", "-1", "01", "2147483648"}) {
            assertParseFailure(parser, index + " 1 c/Vectors", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
            assertParseFailure(parser, "1 " + index + " c/Vectors", MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
    }

    @Test
    public void parse_invalidValues_failure() {
        // blank and too-long content
        assertParseFailure(parser, "1 1 c/", Lesson.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 1 c/   ", Lesson.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 1 c/" + "a".repeat(Lesson.MAX_LENGTH + 1), Lesson.MESSAGE_CONSTRAINTS);

        // dates that do not exist, are in the wrong format or are before the earliest date
        for (String date : new String[]{"", "31/02/2026", "2026-10-21", "21/10/26", "1/1/-2026", "31/12/1999"}) {
            assertParseFailure(parser, "1 1 date/" + date, MESSAGE_DATE_WITH_HINT);
        }
    }

    @Test
    public void parse_prefixTextInContent_failureWithRewordingHint() {
        // c/ and date/ inside the notes still start fields, so the errors suggest rewording
        assertParseFailure(parser, "1 1 c/Notes collected c/o tutor",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT));
        assertParseFailure(parser, "1 1 c/Revised date/time conversions", MESSAGE_DATE_WITH_HINT);
    }

    @Test
    public void parse_nonDateErrors_omitDateHint() {
        // the indexes and content are checked before the date, so their errors have no date hint
        assertParseFailure(parser, "1 0 c/Revised date/time", MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        assertParseFailure(parser, "1 1 c/  date/time", Lesson.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_rewordedNotes_success() {
        for (String content : new String[]{"Notes collected care of tutor", "Revised date and time conversions"}) {
            assertParseSuccess(parser, "1 1 c/" + content, new EditLessonCommand(FIRST, FIRST,
                    descriptor(content, null)));
        }
    }

    /**
     * Accepts the longest valid content, one character within the limit.
     */
    @Test
    public void parse_maximumLengthContent_success() {
        String content = "a".repeat(Lesson.MAX_LENGTH);
        assertParseSuccess(parser, "1 1 c/" + content, new EditLessonCommand(FIRST, FIRST,
                descriptor(content, null)));
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, "1 1 c/Vectors c/Surds",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT));
        assertParseFailure(parser, "1 1 date/21/10/2026 date/22/10/2026",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_DATE));
        assertParseFailure(parser, "1 1 c/Vectors date/21/10/2026 c/Surds date/22/10/2026",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT, PREFIX_DATE));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    private static EditLessonDescriptor descriptor(String content, LocalDate date) {
        EditLessonDescriptor descriptor = new EditLessonDescriptor();
        descriptor.setContent(content);
        descriptor.setDate(date);
        return descriptor;
    }
}
