package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.util.StringUtil;

/**
 * Tokenizes the arguments of commands that take lesson content and an optional date, such as {@code log}
 * and {@code editlesson}, so that lesson content can contain text that looks like a prefix.
 * <p>
 * Text before the first {@code c/} is tokenized as usual. The first {@code c/} starts the content, and a later
 * {@code c/} is part of the content, so a note such as {@code c/o Mum} is kept. After the first {@code c/}, a
 * {@code date/} starts the lesson date only if a digit or nothing follows it, ignoring whitespace. Text such as
 * {@code date/time} therefore stays in the content, while a mistyped date such as {@code date/31/02/2026} is
 * still passed on to be rejected. As with {@link ArgumentTokenizer}, a prefix must follow Unicode whitespace.
 */
public class LessonArgumentTokenizer {

    private static final Pattern CONTENT_PREFIX = prefixAfterWhitespace(PREFIX_CONTENT);
    private static final Pattern DATE_PREFIX = prefixAfterWhitespace(PREFIX_DATE);

    /**
     * Tokenizes {@code argsString} into a preamble, content values and date values.
     * Duplicate dates are kept so that callers can reject them with the usual duplicate-prefix message.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @return ArgumentMultimap object that maps {@code c/} and {@code date/} to their values
     */
    public static ArgumentMultimap tokenize(String argsString) {
        requireNonNull(argsString);
        Matcher contentPrefix = CONTENT_PREFIX.matcher(argsString);
        if (!contentPrefix.find()) {
            return ArgumentTokenizer.tokenize(argsString, PREFIX_DATE);
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                argsString.substring(0, contentPrefix.start()), PREFIX_DATE);
        // Matching within this substring means a date/ directly after c/ has no whitespace before it.
        String afterContentPrefix = argsString.substring(contentPrefix.end());
        List<Integer> datePrefixPositions = findDatePrefixPositions(afterContentPrefix);

        int contentEnd = datePrefixPositions.isEmpty() ? afterContentPrefix.length() : datePrefixPositions.get(0);
        argMultimap.put(PREFIX_CONTENT, StringUtil.stripUnicodeWhitespace(afterContentPrefix.substring(0, contentEnd)));
        for (int i = 0; i < datePrefixPositions.size(); i++) {
            int valueStart = datePrefixPositions.get(i) + PREFIX_DATE.getPrefix().length();
            int valueEnd = i + 1 < datePrefixPositions.size()
                    ? datePrefixPositions.get(i + 1)
                    : afterContentPrefix.length();
            argMultimap.put(PREFIX_DATE,
                    StringUtil.stripUnicodeWhitespace(afterContentPrefix.substring(valueStart, valueEnd)));
        }
        return argMultimap;
    }

    private static Pattern prefixAfterWhitespace(Prefix prefix) {
        return Pattern.compile("(?U)(?<=\\s)" + Pattern.quote(prefix.getPrefix()));
    }

    /**
     * Returns the positions in {@code text} of each {@code date/} that starts a date value.
     */
    private static List<Integer> findDatePrefixPositions(String text) {
        List<Integer> positions = new ArrayList<>();
        Matcher datePrefix = DATE_PREFIX.matcher(text);
        while (datePrefix.find()) {
            if (startsDateValue(text.substring(datePrefix.end()))) {
                positions.add(datePrefix.start());
            }
        }
        return positions;
    }

    /**
     * Returns true if {@code textAfterPrefix} is blank or its first non-whitespace character is an ASCII digit.
     */
    private static boolean startsDateValue(String textAfterPrefix) {
        String value = StringUtil.stripUnicodeWhitespace(textAfterPrefix);
        return value.isEmpty() || (value.charAt(0) >= '0' && value.charAt(0) <= '9');
    }
}
