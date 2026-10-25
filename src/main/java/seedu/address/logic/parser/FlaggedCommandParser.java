package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.FlaggedCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for {@code flagged}.
 */
public class FlaggedCommandParser implements Parser<FlaggedCommand> {

    @Override
    public FlaggedCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.trim().isEmpty()) {
            throw new ParseException(FlaggedCommand.MESSAGE_INVALID_ARGUMENTS);
        }
        return new FlaggedCommand();
    }
}
