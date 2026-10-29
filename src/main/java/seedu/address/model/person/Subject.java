package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents the subject a student is taught in the address book,
 * such as "E-Math", "Physics" or "H2 Chemistry".
 * Guarantees: immutable; is valid as declared in {@link #isValidSubject(String)}
 */
public class Subject {

    public static final int MAX_LENGTH = 50;

    public static final String MESSAGE_CONSTRAINTS = "Subjects should be 1 to " + MAX_LENGTH
            + " characters long, start with an English letter or a digit 0 to 9, and use only English letters, "
            + "digits 0 to 9, spaces, hyphens, &, (, ), + and commas.";

    /*
     * The first character must be a letter or digit, so " " (a blank string) and "-Math" are rejected.
     * \p{Alnum} matches only A-Z, a-z and 0-9, so the message says "English letters" and "digits 0 to 9".
     * '/' is not allowed, so text such as "t/friend" typed after sub/ is not saved as part of the subject.
     */
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} \\-&()+,]*";

    public final String value;

    /**
     * Constructs a {@code Subject}.
     *
     * @param subject A valid subject.
     */
    public Subject(String subject) {
        requireNonNull(subject);
        checkArgument(isValidSubject(subject), MESSAGE_CONSTRAINTS);
        value = subject;
    }

    /**
     * Returns true if a given string is a valid subject.
     */
    public static boolean isValidSubject(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Subject otherSubject)) {
            return false;
        }

        return value.equals(otherSubject.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
