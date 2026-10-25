package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REASON;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.FlagCommand;
import seedu.address.model.person.Flag;

public class FlagCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FlagCommand.MESSAGE_USAGE);
    private final FlagCommandParser parser = new FlagCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_indexOnly_returnsFlagWithoutReason() {
        assertParseSuccess(parser, "1", new FlagCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1001  ", new FlagCommand(Index.fromOneBased(1001)));
    }

    @Test
    public void parse_reason_trimsEdgesAndKeepsInternalText() {
        String reason = "Missed   a/level worksheet; revise date/ notation";
        assertParseSuccess(parser, "1 " + PREFIX_REASON + "  " + reason + "  ",
                new FlagCommand(INDEX_FIRST_PERSON, new Flag(reason)));
        assertParseSuccess(parser, "1 " + PREFIX_REASON + "\u00a0Call parent\u2003",
                new FlagCommand(INDEX_FIRST_PERSON, new Flag("Call parent")));
    }

    @Test
    public void parse_reasonAtLimit_success() {
        String reason = "a".repeat(Flag.MAX_REASON_LENGTH);
        assertParseSuccess(parser, "1 " + PREFIX_REASON + reason,
                new FlagCommand(INDEX_FIRST_PERSON, new Flag(reason)));
    }

    @Test
    public void parse_invalidReason_reportsReasonConstraints() {
        assertParseFailure(parser, "1 " + PREFIX_REASON, Flag.MESSAGE_REASON_CONSTRAINTS);
        assertParseFailure(parser, "1 " + PREFIX_REASON + "\u200b\u2060", Flag.MESSAGE_REASON_CONSTRAINTS);
        assertParseFailure(parser, "1 " + PREFIX_REASON + "a".repeat(Flag.MAX_REASON_LENGTH + 1),
                Flag.MESSAGE_REASON_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateReasonPrefix_reportsDuplicate() {
        assertParseFailure(parser, "1 r/First reason r/Second reason",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_REASON));
    }

    @Test
    public void parse_missingOrMalformedIndex_reportsUsage() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "r/Call parent", INVALID_FORMAT);
        assertParseFailure(parser, "one r/Call parent", INVALID_FORMAT);
        assertParseFailure(parser, "+1", INVALID_FORMAT);
    }

    @Test
    public void parse_invalidNumericIndex_reportsSharedMessage() {
        assertParseFailure(parser, "0", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "-1", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "01", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "2147483648", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }
}
