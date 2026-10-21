package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Arrays;
import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Parses search keywords for a {@code FindCommand} without interpreting field prefixes.
 * Adapted from AddressBook-Level3's {@code FindCommandParser}
 * (https://github.com/se-edu/addressbook-level3).
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Creates a find command from whitespace-separated literal keywords.
     *
     * @throws ParseException if no search keyword is provided.
     */
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        List<String> keywords = Arrays.stream(args.split("(?U)\\s+"))
                .filter(keyword -> !keyword.isEmpty())
                .toList();
        if (keywords.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return new FindCommand(new NameContainsKeywordsPredicate(keywords));
    }
}
