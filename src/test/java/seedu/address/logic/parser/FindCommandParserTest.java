package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Tests required keywords, whitespace handling and literal search input.
 */
public class FindCommandParserTest {
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankInput_reportsUsage() {
        for (String input : List.of("", " ", " \t\r\n ", "\u2003\u00a0")) {
            assertParseFailure(parser, input, String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_keywordsWithExtraWhitespace_createsFindCommand() {
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of("tan", "WEI")));
        assertParseSuccess(parser, "tan WEI", expected);
        assertParseSuccess(parser, "  tan  \t WEI  ", expected);
        assertParseSuccess(parser, "\u2003tan\u00a0WEI\u2003", expected);
    }

    @Test
    public void parse_namePunctuation_doesNotInterpretFieldPrefixes() {
        assertParseSuccess(parser, "Muthu a/l Raju", new FindCommand(
                new NameContainsKeywordsPredicate(List.of("Muthu", "a/l", "Raju"))));
        assertParseSuccess(parser, "O'Brien", new FindCommand(new NameContainsKeywordsPredicate(List.of("O'Brien"))));
    }

    @Test
    public void parse_otherLiteralKeywords_doesNotApplyFullNameValidation() {
        assertParseSuccess(parser, "123", new FindCommand(new NameContainsKeywordsPredicate(List.of("123"))));
        assertParseSuccess(parser, ".*", new FindCommand(new NameContainsKeywordsPredicate(List.of(".*"))));
    }
}
