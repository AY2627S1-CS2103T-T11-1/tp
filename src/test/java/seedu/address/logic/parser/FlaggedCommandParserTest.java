package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FlaggedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class FlaggedCommandParserTest {

    private final FlaggedCommandParser parser = new FlaggedCommandParser();

    @Test
    public void parse_noArguments_returnsCommand() throws Exception {
        assertTrue(parser.parse("") instanceof FlaggedCommand);
        assertTrue(parser.parse("  ") instanceof FlaggedCommand);
    }

    @Test
    public void parse_arguments_reportsUnsupportedParameters() {
        assertThrows(ParseException.class, FlaggedCommand.MESSAGE_INVALID_ARGUMENTS, () ->
                parser.parse(" 1"));
        assertThrows(ParseException.class, FlaggedCommand.MESSAGE_INVALID_ARGUMENTS, () ->
                parser.parse(" r/reason"));
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
