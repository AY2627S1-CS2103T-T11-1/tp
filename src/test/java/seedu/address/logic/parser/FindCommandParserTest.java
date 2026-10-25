package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.StudentContainsKeywordsPredicate;
import seedu.address.model.person.Subject;

/**
 * Tests bare name keywords, the n/, sub/ and l/ fields, whitespace handling and invalid searches.
 */
public class FindCommandParserTest {
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankInput_reportsUsage() {
        for (String input : List.of("", " ", " \t\r\n ", "  ")) {
            assertParseFailure(parser, input, String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_keywordsWithExtraWhitespace_createsFindCommand() {
        FindCommand expected = find(List.of("tan", "WEI"), List.of(), List.of());
        assertParseSuccess(parser, "tan WEI", expected);
        assertParseSuccess(parser, "  tan  \t WEI  ", expected);
        assertParseSuccess(parser, " tan WEI ", expected);
    }

    @Test
    public void parse_namePunctuation_doesNotInterpretUnknownPrefixes() {
        assertParseSuccess(parser, "Muthu a/l Raju", find(List.of("Muthu", "a/l", "Raju"), List.of(), List.of()));
        assertParseSuccess(parser, "O'Brien", find(List.of("O'Brien"), List.of(), List.of()));
        // only n/, sub/ and l/ start a field, so other prefixes stay part of the name keywords
        assertParseSuccess(parser, "a/Bishan p/9123", find(List.of("a/Bishan", "p/9123"), List.of(), List.of()));
        assertParseSuccess(parser, "SUB/Math", find(List.of("SUB/Math"), List.of(), List.of()));
    }

    @Test
    public void parse_otherLiteralKeywords_doesNotApplyFullNameValidation() {
        assertParseSuccess(parser, "123", find(List.of("123"), List.of(), List.of()));
        assertParseSuccess(parser, ".*", find(List.of(".*"), List.of(), List.of()));
    }

    @Test
    public void parse_singleField_createsFindCommand() {
        assertParseSuccess(parser, " n/Amy Tan", find(List.of("Amy", "Tan"), List.of(), List.of()));
        assertParseSuccess(parser, " sub/Physics", find(List.of(), List.of("Physics"), List.of()));
        assertParseSuccess(parser, " l/Sec 3", find(List.of(), List.of(), List.of("Sec", "3")));
    }

    @Test
    public void parse_bareKeywordsAndNameField_combinesNameKeywords() {
        assertParseSuccess(parser, "tan n/wei ming", find(List.of("tan", "wei", "ming"), List.of(), List.of()));
    }

    @Test
    public void parse_everyFieldInAnyOrder_createsFindCommand() {
        FindCommand expected = find(List.of("tan", "wei"), List.of("H2", "Math"), List.of("JC1"));
        assertParseSuccess(parser, "tan sub/H2 Math l/JC1 n/wei", expected);
        assertParseSuccess(parser, "tan l/JC1 n/wei sub/H2 Math", expected);
        assertParseSuccess(parser, "tan  n/ wei  sub/ H2 Math  l/ JC1 ", expected);
    }

    @Test
    public void parse_fieldsWithoutBareKeywords_searchesOnlyThoseFields() {
        assertParseSuccess(parser, " sub/Physics l/Sec 3",
                find(List.of(), List.of("Physics"), List.of("Sec", "3")));
    }

    @Test
    public void parse_duplicateField_reportsDuplicatePrefix() {
        assertParseFailure(parser, " n/amy n/ben", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, " sub/math sub/physics",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
        assertParseFailure(parser, "tan l/sec 3 l/jc1", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));
    }

    @Test
    public void parse_blankField_reportsFieldConstraints() {
        for (String blank : List.of("", "   ", " ", " ", "​")) {
            assertParseFailure(parser, " n/" + blank, Name.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, " sub/" + blank, Subject.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, " l/" + blank, Level.MESSAGE_CONSTRAINTS);
        }
        // a blank field is rejected even when other criteria are usable
        assertParseFailure(parser, "tan sub/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " sub/Math l/", Level.MESSAGE_CONSTRAINTS);
    }

    private static FindCommand find(List<String> names, List<String> subjects, List<String> levels) {
        return new FindCommand(new StudentContainsKeywordsPredicate(names, subjects, levels));
    }
}
