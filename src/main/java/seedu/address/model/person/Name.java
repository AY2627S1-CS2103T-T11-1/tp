package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS = "Names should be 1 to " + MAX_LENGTH + " characters long, "
            + "contain at least one letter, and use only letters, spaces, apostrophes, hyphens, periods "
            + "and slashes.";

    /*
     * Letters include accented and non-Latin letters, with their combining marks.
     * '/' is allowed so that names such as "Ramesh s/o Kumar" can be entered.
     * The first character must not be a whitespace, otherwise " " (a blank string) becomes a valid input.
     * The lookahead requires at least one letter, so names such as "-" or "..." are rejected.
     */
    public static final String VALIDATION_REGEX = "(?=.*\\p{L})[\\p{L}\\p{M}'.\\-/][\\p{L}\\p{M}'.\\-/ ]*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Trims surrounding Unicode whitespace and collapses internal whitespace to one space.
     * Commands and stored data normalize names before validating them and checking student identity.
     */
    public static String normalize(String name) {
        requireNonNull(name);
        return StringUtil.stripUnicodeWhitespace(name).replaceAll("(?U)\\s+", " ");
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
