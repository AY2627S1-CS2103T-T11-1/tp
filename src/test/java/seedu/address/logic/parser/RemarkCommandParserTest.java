package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validInput_success() throws Exception {
        RemarkCommand command = parser.parse(INDEX_FIRST_PERSON.getOneBased() + " r/Prefers weekend lessons");
        assertEquals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Prefers weekend lessons")), command);
    }

    @Test
    public void parse_emptyRemark_success() throws Exception {
        RemarkCommand command = parser.parse(String.valueOf(INDEX_FIRST_PERSON.getOneBased()));
        assertEquals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")), command);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                RemarkCommand.MESSAGE_USAGE), () -> parser.parse("invalid r/Some remark"));
    }
}
