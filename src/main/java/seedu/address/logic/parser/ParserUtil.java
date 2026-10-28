package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    private static final Logger logger = LogsCenter.getLogger(ParserUtil.class);

    /** One or two digits for the day and month, and exactly four for the year, with no signs. */
    private static final String LESSON_DATE_INPUT_REGEX = "\\d{1,2}/\\d{1,2}/\\d{4}";
    /** Day, month and year. Impossible dates such as 31/02/2026 are rejected. */
    private static final DateTimeFormatter LESSON_DATE_INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is not a positive integer without leading zeros.
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = StringUtil.stripUnicodeWhitespace(oneBasedIndex);
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex) || trimmedIndex.startsWith("0")) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a student index used by a command.
     *
     * <p>Numeric-looking but invalid indexes use the shared student-index error. Other malformed input uses the
     * command's usage message.</p>
     *
     * @param oneBasedIndex index text to parse
     * @param commandUsage usage text for the command being parsed
     * @throws ParseException if the specified index is invalid or malformed
     */
    public static Index parseStudentIndex(String oneBasedIndex, String commandUsage) throws ParseException {
        return parseCommandIndex(oneBasedIndex, commandUsage, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    /**
     * Parses a command index, using {@code invalidIndexMessage} for invalid numeric input.
     * Malformed input uses {@code commandUsage} instead.
     *
     * @throws ParseException if the specified index is invalid or malformed
     */
    public static Index parseCommandIndex(String oneBasedIndex, String commandUsage, String invalidIndexMessage)
            throws ParseException {
        requireNonNull(oneBasedIndex);
        requireNonNull(commandUsage);
        requireNonNull(invalidIndexMessage);

        String trimmedIndex = StringUtil.stripUnicodeWhitespace(oneBasedIndex);
        if (!trimmedIndex.matches("-?\\d+")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, commandUsage));
        }

        try {
            return parseIndex(trimmedIndex);
        } catch (ParseException pe) {
            throw new ParseException(invalidIndexMessage, pe);
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed, and repeated spaces inside the name
     * will be reduced to one space.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String normalisedName = name.trim().replaceAll("\\s+", " ");
        if (!Name.isValidName(normalisedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(normalisedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * All whitespaces, including those between digits such as in "9123 4567", will be removed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String phoneWithoutSpaces = phone.replaceAll("\\s+", "");
        if (!Phone.isValidPhone(phoneWithoutSpaces)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(phoneWithoutSpaces);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String level} into a {@code Level}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code level} is invalid.
     */
    public static Level parseLevel(String level) throws ParseException {
        requireNonNull(level);
        String trimmedLevel = level.trim();
        if (!Level.isValidLevel(trimmedLevel)) {
            throw new ParseException(Level.MESSAGE_CONSTRAINTS);
        }
        return new Level(trimmedLevel);
    }

    /**
     * Parses a {@code String subject} into a {@code Subject}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code subject} is invalid.
     */
    public static Subject parseSubject(String subject) throws ParseException {
        requireNonNull(subject);
        String trimmedSubject = subject.trim();
        if (!Subject.isValidSubject(trimmedSubject)) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        return new Subject(trimmedSubject);
    }

    /**
     * Parses a flag reason after trimming surrounding Unicode whitespace.
     * Meaningful internal spacing and punctuation are kept.
     *
     * @throws ParseException if the supplied reason is invalid
     */
    public static Flag parseFlagReason(String reason) throws ParseException {
        requireNonNull(reason);
        String trimmedReason = StringUtil.stripUnicodeWhitespace(reason);
        if (!Flag.isValidReason(trimmedReason)) {
            throw new ParseException(Flag.MESSAGE_REASON_CONSTRAINTS);
        }
        return new Flag(trimmedReason);
    }

    /**
     * Parses a {@code String content} into a {@code Lesson}.
     * Leading and trailing whitespaces will be trimmed; spaces inside the content are kept.
     *
     * @throws ParseException if the given {@code content} is invalid.
     */
    public static Lesson parseLessonContent(String content) throws ParseException {
        requireNonNull(content);
        String trimmedContent = content.trim();
        if (!Lesson.isValidContent(trimmedContent)) {
            throw new ParseException(Lesson.MESSAGE_CONSTRAINTS);
        }
        return new Lesson(trimmedContent);
    }

    /**
     * Parses a {@code String date} in the format DD/MM/YYYY into a {@code LocalDate}.
     * Leading and trailing whitespaces will be trimmed. Single-digit days and months are accepted.
     * Dates before {@link Lesson#EARLIEST_DATE} are rejected as likely typos.
     *
     * @throws ParseException if the given {@code date} is not a real date in that format.
     */
    public static LocalDate parseLessonDate(String date) throws ParseException {
        requireNonNull(date);
        String trimmedDate = date.trim();
        if (!trimmedDate.matches(LESSON_DATE_INPUT_REGEX)) {
            logger.fine("Rejected lesson date not in D/M/YYYY form: " + trimmedDate);
            throw new ParseException(Lesson.MESSAGE_DATE_CONSTRAINTS);
        }
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(trimmedDate, LESSON_DATE_INPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.fine("Rejected lesson date that does not exist: " + trimmedDate);
            throw new ParseException(Lesson.MESSAGE_DATE_CONSTRAINTS, e);
        }
        if (parsedDate.isBefore(Lesson.EARLIEST_DATE)) {
            logger.fine("Rejected lesson date before " + Lesson.EARLIEST_DATE + ": " + trimmedDate);
            throw new ParseException(Lesson.MESSAGE_DATE_CONSTRAINTS);
        }
        return parsedDate;
    }
}
