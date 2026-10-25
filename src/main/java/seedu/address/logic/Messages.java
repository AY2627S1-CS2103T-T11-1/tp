package seedu.address.logic;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.StudentSummary;
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
    public static final String MESSAGE_INVALID_LESSON_DISPLAYED_INDEX =
            "The lesson index provided is invalid.";
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

    /**
     * Formats a student's lesson count and latest recorded date for their card.
     */
    public static String formatLessonStatus(Person person) {
        int count = person.getLessonCount();
        if (count == 0) {
            return "No lessons yet";
        }
        String lastDate = person.getLatestLessonDate()
                .map(date -> date.format(LESSON_DATE_FORMATTER))
                .orElse(UNDATED_LESSON);
        return count + (count == 1 ? " lesson, last " : " lessons, last ") + lastDate;
    }

    /**
     * Describes the complete collection and highlights students without recorded lessons.
     */
    public static String formatSummaryParagraph(StudentSummary summary) {
        int students = summary.getStudentCount();
        if (students == 0) {
            return "No students yet. Add a student to start recording lessons.";
        }
        long lessons = summary.getLessonCount();
        String overview = "You have " + students + (students == 1 ? " student" : " students")
                + " and " + lessons + (lessons == 1 ? " lesson record" : " lesson records") + " in total. ";
        int withoutLessons = summary.getStudentsWithoutLessons();
        if (withoutLessons == 0) {
            return overview + "Every student has at least one lesson recorded.";
        }
        return overview + withoutLessons + (withoutLessons == 1 ? " student has" : " students have")
                + " no lessons recorded.";
    }

}
