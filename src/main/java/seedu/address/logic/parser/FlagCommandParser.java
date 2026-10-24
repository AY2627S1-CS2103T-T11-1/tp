package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REASON;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FlagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Flag;

/**
 * Parses input arguments and creates a {@code FlagCommand}.
 */
public class FlagCommandParser implements Parser<FlagCommand> {

    @Override
    public FlagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_REASON);
        if (argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FlagCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseStudentIndex(argMultimap.getPreamble(), FlagCommand.MESSAGE_USAGE);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REASON);
        if (argMultimap.getValue(PREFIX_REASON).isEmpty()) {
            return new FlagCommand(index);
        }

        Flag flag = ParserUtil.parseFlagReason(argMultimap.getValue(PREFIX_REASON).orElseThrow());
        return new FlagCommand(index, flag);
    }
}
