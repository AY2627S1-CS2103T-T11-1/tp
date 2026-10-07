package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_noArguments_returnsListCommand() throws Exception {
        assertTrue(parser.parse("") instanceof ListCommand);
        assertTrue(parser.parse("   ") instanceof ListCommand);
    }

    @Test
    public void parse_withArguments_throwsParseException() {
        assertThrows(ParseException.class, ListCommand.MESSAGE_INVALID_ARGUMENTS, ()
                -> parser.parse(" student"));
        assertThrows(ParseException.class, ListCommand.MESSAGE_INVALID_ARGUMENTS, ()
                -> parser.parse(" 1"));
    }
}
