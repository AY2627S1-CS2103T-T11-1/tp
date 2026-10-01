package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String NON_EMPTY_REMARK = "Some remark.";

    private RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_indexSpecified_success() {
        // have remark
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + NON_EMPTY_REMARK;
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(NON_EMPTY_REMARK));
        assertParseSuccess(parser, userInput, expectedCommand);

        // no remark
        userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK;
        expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_missingCompulsoryField_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

        // no index
        assertParseFailure(parser, " " + PREFIX_REMARK + NON_EMPTY_REMARK, expectedMessage);

        // invalid index
        assertParseFailure(parser, "a " + PREFIX_REMARK + NON_EMPTY_REMARK, expectedMessage);
    }
}
