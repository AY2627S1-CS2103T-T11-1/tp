package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UnflagCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates an {@code UnflagCommand}.
 */
public class UnflagCommandParser implements Parser<UnflagCommand> {

    @Override
    public UnflagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Index index = ParserUtil.parseStudentIndex(args, UnflagCommand.MESSAGE_USAGE);
        return new UnflagCommand(index);
    }
}
