package seedu.address.logic;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX =
            "The student index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /** Day, month and four-digit year, as Singapore users write dates, e.g. 18/10/2026. */
    public static final DateTimeFormatter LESSON_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    public static final String UNDATED_LESSON = "(undated)";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Level: ")
                .append(person.getLevel())
                .append("; Subject: ")
                .append(person.getSubject());
        return builder.toString();
    }

    /**
     * Formats the date of {@code lesson} for display to the user, or marks the lesson as undated.
     */
    public static String formatLessonDate(Lesson lesson) {
        return lesson.getDate()
                .map(date -> date.format(LESSON_DATE_FORMATTER))
                .orElse(UNDATED_LESSON);
    }

}
