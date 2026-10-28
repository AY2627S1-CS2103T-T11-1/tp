package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /** Shown when the address is missing but the address prefix used before v1.5 was typed. */
    public static final String MESSAGE_ADDRESS_PREFIX_HINT =
            "The address prefix is " + PREFIX_ADDRESS + ", for example " + PREFIX_ADDRESS + "1 Example Street.";

    /** Appended only to invalid fields containing an unsupported prefix-like token. */
    public static final String MESSAGE_UNSUPPORTED_PREFIX_HINT = "If %1$s was intended as a field, add accepts only "
            + PREFIX_NAME + ", " + PREFIX_PHONE + ", " + PREFIX_ADDRESS + ", " + PREFIX_LEVEL
            + " and " + PREFIX_SUBJECT + ".";

    private static final String OLD_ADDRESS_PREFIX = "a/";
    private static final Pattern PREFIX_LIKE_TOKEN = Pattern.compile("(?U)(?<!\\S)[A-Za-z]+/");

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_LEVEL, PREFIX_SUBJECT);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_LEVEL, PREFIX_SUBJECT)
                || !argMultimap.getPreamble().isEmpty()) {
            String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
            boolean usesOldAddressPrefix = argMultimap.getValue(PREFIX_ADDRESS).isEmpty()
                    && args.matches("(?Us).*(^|\\s)" + OLD_ADDRESS_PREFIX + ".*");
            throw new ParseException(usesOldAddressPrefix ? usage + "\n" + MESSAGE_ADDRESS_PREFIX_HINT : usage);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_LEVEL, PREFIX_SUBJECT);
        Name name = parseField(argMultimap.getValue(PREFIX_NAME).get(), ParserUtil::parseName);
        Phone phone = parseField(argMultimap.getValue(PREFIX_PHONE).get(), ParserUtil::parsePhone);
        Address address = parseField(argMultimap.getValue(PREFIX_ADDRESS).get(), ParserUtil::parseAddress);
        Level level = parseField(argMultimap.getValue(PREFIX_LEVEL).get(), ParserUtil::parseLevel);
        Subject subject = parseField(argMultimap.getValue(PREFIX_SUBJECT).get(), ParserUtil::parseSubject);

        Person person = new Person(name, phone, address, level, subject);

        return new AddCommand(person);
    }

    /** Parses a field normally, adding conditional prefix guidance only after validation fails. */
    private <T> T parseField(String value, FieldParser<T> parser) throws ParseException {
        try {
            return parser.parse(value);
        } catch (ParseException e) {
            Matcher matcher = PREFIX_LIKE_TOKEN.matcher(value);
            while (matcher.find()) {
                String candidate = matcher.group();
                boolean isSupported = Stream.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_LEVEL, PREFIX_SUBJECT)
                        .anyMatch(prefix -> prefix.getPrefix().equals(candidate));
                if (!isSupported) {
                    throw new ParseException(e.getMessage() + "\n"
                            + String.format(MESSAGE_UNSUPPORTED_PREFIX_HINT, candidate), e);
                }
            }
            throw e;
        }
    }

    /** Parses one field value using its existing validator. */
    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String value) throws ParseException;
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
