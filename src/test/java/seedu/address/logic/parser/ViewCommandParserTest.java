package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {
    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validIndex_success() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " \t1 \t", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "2147483647", new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_invalidArguments_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", " ", "0", "-1", "+1", "1.5", "abc", "1 2", "1 c/note", "2147483648",
            "999999999999999999999999"}) {
            assertParseFailure(parser, args, expected);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
