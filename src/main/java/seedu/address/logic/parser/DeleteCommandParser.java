package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();

        if (trimmedArgs.matches("-?\\d+")) {
            try {
                int oneBasedIndex = Integer.parseInt(trimmedArgs);
                if (oneBasedIndex <= 0) {
                    throw new ParseException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
                }
                return new DeleteCommand(Index.fromOneBased(oneBasedIndex));
            } catch (NumberFormatException numberFormatException) {
                throw new ParseException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, numberFormatException);
            }
        }

        try {
            Index index = ParserUtil.parseIndex(args);
            return new DeleteCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }

}
