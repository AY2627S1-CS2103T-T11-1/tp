package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LogCommand;
import seedu.address.model.lesson.Lesson;

public class LogCommandParserTest {

    private static final String VALID_CONTENT = "Quadratic inequalities";
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);

    private LogCommandParser parser = new LogCommandParser();

    @Test
    public void parse_validArgs_returnsLogCommand() {
        LogCommand expectedCommand = new LogCommand(INDEX_FIRST_PERSON, new Lesson(VALID_CONTENT));

        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + VALID_CONTENT, expectedCommand);

        // extra whitespace around the index and content is ignored
        assertParseSuccess(parser, "   1   " + PREFIX_CONTENT + "  " + VALID_CONTENT + "  ", expectedCommand);
    }

    @Test
    public void parse_contentWithSymbols_keepsContentAsTyped() {
        String content = "Q1-5 (p. 42), revise   surds & indices!";
        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + content,
                new LogCommand(INDEX_FIRST_PERSON, new Lesson(content)));
    }

    @Test
    public void parse_missingParts_failure() {
        // no index
        assertParseFailure(parser, " " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // no content prefix
        assertParseFailure(parser, " 1 " + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // nothing at all
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // zero
        assertParseFailure(parser, " 0 " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // negative
        assertParseFailure(parser, " -1 " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);

        // not a number
        assertParseFailure(parser, " a " + PREFIX_CONTENT + VALID_CONTENT, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidContent_failure() {
        // blank content
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "   ", Lesson.MESSAGE_CONSTRAINTS);

        // content over the limit
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + "a".repeat(Lesson.MAX_LENGTH + 1),
                Lesson.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_contentAtLimit_success() {
        String content = "a".repeat(Lesson.MAX_LENGTH);
        assertParseSuccess(parser, " 1 " + PREFIX_CONTENT + content,
                new LogCommand(INDEX_FIRST_PERSON, new Lesson(content)));
    }

    @Test
    public void parse_duplicateContentPrefix_failure() {
        assertParseFailure(parser, " 1 " + PREFIX_CONTENT + VALID_CONTENT + " " + PREFIX_CONTENT + "Binomial theorem",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CONTENT));
    }
}
