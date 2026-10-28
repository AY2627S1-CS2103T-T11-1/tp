package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import seedu.address.logic.commands.UnloggedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class UnloggedCommandParserTest {
    private final UnloggedCommandParser parser = new UnloggedCommandParser();

    @Test
    public void parse_whitespaceOnly_returnsCommand() throws Exception {
        assertTrue(parser.parse("") instanceof UnloggedCommand);
        assertTrue(parser.parse(" \t\r\n\u00a0\u2003") instanceof UnloggedCommand);
    }

    @TestFactory
    public Stream<DynamicTest> parse_arguments_rejected() {
        return Stream.of("1", " all", " n/Amy", "\u200b").map(args -> DynamicTest.dynamicTest(args, () -> {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals(UnloggedCommand.MESSAGE_INVALID_ARGUMENTS, error.getMessage());
        }));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
