package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed student index for a {@code ViewCommand}.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses a single positive index, allowing surrounding whitespace.
     *
     * @throws ParseException If the arguments are not a single valid index.
     */
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            return new ViewCommand(ParserUtil.parseIndex(args));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE), pe);
        }
    }
}
