package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.SummaryCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SummaryCommandParserTest {
    private final SummaryCommandParser parser = new SummaryCommandParser();

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_emptyOrWhitespace_acceptsSummary() throws Exception {
        for (String whitespace : List.of("", " ", "\t", "\u00a0", "\u2003", " \t\u3000 ")) {
            assertNotNull(parser.parse(whitespace));
        }
    }

    @Test
    public void parse_arguments_rejectsWithSummaryUsage() {
        for (String argument : List.of("1", "all", "n/Amy", "sub/Physics", "\u200b", "\u00a0 extra")) {
            assertThrows(ParseException.class, String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                    SummaryCommand.MESSAGE_USAGE), () -> parser.parse(argument));
        }
    }
}
