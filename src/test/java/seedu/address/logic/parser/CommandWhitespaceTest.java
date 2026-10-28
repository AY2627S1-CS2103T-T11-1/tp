package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FlaggedCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Verifies Unicode command separators without rewriting meaningful text inside field values.
 */
public class CommandWhitespaceTest {

    private static final List<String> SEPARATORS = List.of(" ", "\t", "\n", "\r", "\r\n", "\u000b", "\f",
            "\u0085", "\u00a0", "\u1680", "\u2000", "\u2001", "\u2002", "\u2003", "\u2004", "\u2005",
            "\u2006", "\u2007", "\u2008", "\u2009", "\u200a", "\u2028", "\u2029", "\u202f", "\u205f",
            "\u3000");

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_unicodeStructuralSeparators_matchesOrdinarySpaces() throws Exception {
        List<String> commands = List.of(
                "add|n/Amy Tan|p/81230001|addr/1 Example Street|l/Sec 3|sub/Math",
                "edit|1|addr/New Rd|l/Sec 4",
                "delete|1", "view|1", "unflag|1",
                "log|1|c/Quadratic inequalities|date/21/10/2026",
                "editlesson|1|2|c/Quadratic inequalities|date/21/10/2026",
                "deletelesson|1|2",
                "flag|1|r/Review homework",
                "find|Amy|sub/Math|l/Sec 3",
                "findlesson|1|quadratic|inequalities|from/01/10/2026|to/21/10/2026");
        for (String command : commands) {
            for (String separator : SEPARATORS) {
                String input = separator + command.replace("|", separator + separator) + separator;
                assertEquals(parser.parseCommand(command.replace('|', ' ')), parser.parseCommand(input), input);
            }
        }
    }

    @Test
    public void parseCommand_unicodeWhitespaceOnlyArguments_acceptsNoArgumentCommands() throws Exception {
        for (String separator : SEPARATORS) {
            assertInstanceOf(ListCommand.class, new ListCommandParser().parse(separator));
            assertInstanceOf(FlaggedCommand.class, new FlaggedCommandParser().parse(separator));
            for (String command : List.of("list", "flagged", "summary", "help", "exit")) {
                assertEquals(parser.parseCommand(command).getClass(),
                        parser.parseCommand(separator + command + separator).getClass());
            }
        }
    }

    @Test
    public void parseCommand_unicodeSeparators_preservesValidationErrors() {
        List<String> commands = List.of("view|0", "view|-1", "view|01", "view|2147483648", "view|+1",
                "view|1|extra", "deletelesson|1|0", "deletelesson|01|1", "deletelesson|1|1|extra",
                "editlesson|1|01|c/Algebra", "edit|1|l/", "log|1|c/",
                "edit|1|l/Sec3|l/Sec4", "log|1|c/Algebra|date/20/10/2026|date/21/10/2026",
                "log|1|c/Algebra|date/31/02/2026", "list|extra", "flagged|extra", "summary|extra",
                "add|n/Amy|p/81230001|a/Road|l/Sec3|sub/Math");
        for (String command : commands) {
            ParseException expected = assertThrows(ParseException.class, () ->
                    parser.parseCommand(command.replace('|', ' ')));
            for (String separator : SEPARATORS) {
                ParseException actual = assertThrows(ParseException.class, () ->
                        parser.parseCommand(command.replace("|", separator)));
                assertEquals(expected.getMessage(), actual.getMessage());
            }
        }
        for (String separator : SEPARATORS) {
            assertThrows(ParseException.class, () -> parser.parseCommand(separator));
        }
    }

    @Test
    public void parseCommand_fieldValues_preservesInternalSpacingAndFormatting() throws Exception {
        String value = "New\u00a0  Road क्\u200dष 👩\u200d🏫";
        for (String command : List.of("edit|1|addr/" + value, "log|1|c/" + value, "flag|1|r/" + value)) {
            assertEquals(parser.parseCommand(command.replace('|', ' ')),
                    parser.parseCommand(command.replace("|", "\u00a0")));
        }
        assertThrows(ParseException.class, () -> parser.parseCommand("view\u200b1"));
        assertThrows(ParseException.class, () -> parser.parseCommand("edit 1\u2060l/Sec3"));
    }
}
