package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteLessonCommand;

/**
 * Verifies that lesson deletion requires exactly two valid displayed indexes.
 */
public class DeleteLessonCommandParserTest {
    private final DeleteLessonCommandParser parser = new DeleteLessonCommandParser();

    /**
     * Accepts positive indexes and the whitespace used between command arguments.
     */
    @Test
    public void parse_validIndexes_success() {
        assertParseSuccess(parser, "2 1", new DeleteLessonCommand(Index.fromOneBased(2), Index.fromOneBased(1)));
        assertParseSuccess(parser, " \t2  \t1 \t",
                new DeleteLessonCommand(Index.fromOneBased(2), Index.fromOneBased(1)));
        assertParseSuccess(parser, "1001 2147483647",
                new DeleteLessonCommand(Index.fromOneBased(1001), Index.fromOneBased(Integer.MAX_VALUE)));
    }

    /**
     * Rejects missing, extra and non-integer arguments with the command usage.
     */
    @Test
    public void parse_malformedArguments_reportsUsage() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteLessonCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", " ", "1", "1 2 3", "abc 1", "1 abc", "+1 1", "1 +1",
            "1.5 1", "1 1.5", "1 c/note", "1 1 extra"}) {
            assertParseFailure(parser, args, expected);
        }
    }

    /**
     * Identifies invalid numeric student indexes separately from lesson indexes.
     */
    @Test
    public void parse_invalidNumericIndexes_reportsRelevantIndex() {
        for (String index : new String[]{"0", "-1", "01", "001", "2147483648", "999999999999999999999"}) {
            assertParseFailure(parser, index + " 1", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
            assertParseFailure(parser, "1 " + index, MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
    }

    /**
     * Rejects a missing argument string as a programming error.
     */
    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
