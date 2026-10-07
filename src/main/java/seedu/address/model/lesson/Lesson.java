package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a record of a lesson taught to a student in the address book.
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

    /**
     * Constructs a {@code Lesson}.
     *
     * @param content A valid description of what was covered in the lesson.
     */
    public Lesson(String content) {
        requireNonNull(content);
        checkArgument(isValidContent(content), MESSAGE_CONSTRAINTS);
        this.content = content;
    }

    /**
     * Returns true if a given string is valid lesson content.
     */
    public static boolean isValidContent(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

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

        return content.equals(otherLesson.content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

}
