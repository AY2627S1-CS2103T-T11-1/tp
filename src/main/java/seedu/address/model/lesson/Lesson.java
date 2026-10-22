package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a record of a lesson taught to a student in the address book.
 * A lesson records the date it was logged. Lessons saved before dates were recorded have no date.
 * Guarantees: immutable; is valid as declared in {@link #isValidContent(String)}
 */
public class Lesson {

    public static final int MAX_LENGTH = 300;

    public static final String MESSAGE_CONSTRAINTS =
            "Lesson content must not be blank and must be at most 300 characters.";

    /*
     * The first character of the content must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     * Control characters (e.g. line breaks) are not allowed anywhere in the content.
     */
    public static final String VALIDATION_REGEX = "[^\\s\\p{Cntrl}][^\\p{Cntrl}]*";

    public final String content;
    private final LocalDate date;

    /**
     * Constructs an undated {@code Lesson}.
     *
     * @param content A valid description of what was covered in the lesson.
     */
    public Lesson(String content) {
        this.content = requireValidContent(content);
        this.date = null;
    }

    /**
     * Constructs a {@code Lesson} logged on {@code date}.
     *
     * @param content A valid description of what was covered in the lesson.
     * @param date The date the lesson was logged.
     */
    public Lesson(String content, LocalDate date) {
        this.content = requireValidContent(content);
        this.date = requireNonNull(date);
    }

    private static String requireValidContent(String content) {
        requireNonNull(content);
        checkArgument(isValidContent(content), MESSAGE_CONSTRAINTS);
        return content;
    }

    /**
     * Returns true if a given string is valid lesson content.
     * Values composed entirely of Unicode whitespace or format characters are invalid.
     */
    public static boolean isValidContent(String test) {
        return test.length() <= MAX_LENGTH && !test.matches("[\\p{IsWhite_Space}\\p{Cf}]*")
                && test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns the date the lesson was logged, or an empty {@code Optional} if the lesson is undated.
     */
    public Optional<LocalDate> getDate() {
        return Optional.ofNullable(date);
    }

    /**
     * Returns a copy of this lesson with the same content, logged on {@code date}.
     */
    public Lesson withDate(LocalDate date) {
        return new Lesson(content, date);
    }

    /**
     * Returns the lesson content only. Command confirmations may print a lesson through this method,
     * so it must not include the date or other fields.
     */
    @Override
    public String toString() {
        return content;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Lesson otherLesson)) {
            return false;
        }

        return content.equals(otherLesson.content)
                && Objects.equals(date, otherLesson.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, date);
    }

}
