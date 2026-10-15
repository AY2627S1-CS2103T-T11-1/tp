package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
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
        Index index = ParserUtil.parseStudentIndex(args, ViewCommand.MESSAGE_USAGE);
        return new ViewCommand(index);
    }
}
