package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnflagCommand;

public class UnflagCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnflagCommand.MESSAGE_USAGE);
    private final UnflagCommandParser parser = new UnflagCommandParser();

    @Test
    public void parse_validIndex_returnsCommand() {
        assertParseSuccess(parser, "1", new UnflagCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " 1 ", new UnflagCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidIndex_reportsExpectedMessage() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, "one", INVALID_FORMAT);
        assertParseFailure(parser, "1 extra", INVALID_FORMAT);
        assertParseFailure(parser, "0", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "01", MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
