package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.UnloggedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an unlogged-student request, accepting only whitespace after the command word.
 */
public class UnloggedCommandParser implements Parser<UnloggedCommand> {
    @Override
    public UnloggedCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.matches("\\p{IsWhite_Space}*")) {
            throw new ParseException(UnloggedCommand.MESSAGE_INVALID_ARGUMENTS);
        }
        return new UnloggedCommand();
    }
}
