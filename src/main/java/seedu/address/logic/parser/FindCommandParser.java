package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.commons.util.StringUtil;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.StudentContainsKeywordsPredicate;
import seedu.address.model.person.Subject;

/**
 * Parses name keywords and the optional {@code n/}, {@code sub/} and {@code l/} fields for a {@code FindCommand}.
 * Keywords before the first of these prefixes are name keywords. Other prefixes are kept as literal keywords.
 * Adapted from AddressBook-Level3's {@code FindCommandParser}
 * (https://github.com/se-edu/addressbook-level3).
 */
public class FindCommandParser implements Parser<FindCommand> {

    /** Any single Unicode whitespace character, such as a non-breaking space, EM SPACE or tab. */
    private static final String UNICODE_WHITESPACE_REGEX = "(?U)\\s";

    /**
     * Creates a find command from the bare name keywords and the given fields.
     *
     * @throws ParseException if no search keyword is given, a field is given twice or a field is blank.
     */
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        // Search keywords treat all Unicode whitespace as word separators.
        String normalisedArgs = args.replaceAll(UNICODE_WHITESPACE_REGEX, " ");
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(normalisedArgs, PREFIX_NAME, PREFIX_SUBJECT, PREFIX_LEVEL);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_SUBJECT, PREFIX_LEVEL);

        List<String> nameKeywords = new ArrayList<>(splitKeywords(argMultimap.getPreamble()));
        nameKeywords.addAll(parseField(argMultimap, PREFIX_NAME, Name.MESSAGE_CONSTRAINTS));
        List<String> subjectKeywords = parseField(argMultimap, PREFIX_SUBJECT, Subject.MESSAGE_CONSTRAINTS);
        List<String> levelKeywords = parseField(argMultimap, PREFIX_LEVEL, Level.MESSAGE_CONSTRAINTS);

        if (nameKeywords.isEmpty() && subjectKeywords.isEmpty() && levelKeywords.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return new FindCommand(new StudentContainsKeywordsPredicate(nameKeywords, subjectKeywords, levelKeywords));
    }

    /**
     * Returns the keywords given after {@code prefix}, or an empty list if {@code prefix} was not given.
     *
     * @throws ParseException with {@code constraintsMessage} if {@code prefix} was given with a blank value.
     */
    private static List<String> parseField(ArgumentMultimap argMultimap, Prefix prefix, String constraintsMessage)
            throws ParseException {
        if (argMultimap.getValue(prefix).isEmpty()) {
            return List.of();
        }
        String value = argMultimap.getValue(prefix).get();
        if (StringUtil.isBlankOrInvisible(value)) {
            throw new ParseException(constraintsMessage);
        }
        return splitKeywords(value);
    }

    /**
     * Splits {@code text} on any Unicode whitespace into literal keywords.
     */
    private static List<String> splitKeywords(String text) {
        return Arrays.stream(text.split("(?U)\\s+"))
                .filter(keyword -> !keyword.isEmpty())
                .toList();
    }
}
