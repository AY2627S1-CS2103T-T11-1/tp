package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a follow-up flag attached to a student.
 * A flag may include one validated reason.
 * Guarantees: immutable; the reason, when present, is valid as declared in
 * {@link #isValidReason(String)}.
 */
public class Flag {

    public static final int MAX_REASON_LENGTH = 100;

    public static final String MESSAGE_REASON_CONSTRAINTS =
            "Flag reasons must not be blank and must be at most 100 characters.";

    private final String reason;

    /**
     * Creates a flag without a reason.
     */
    public Flag() {
        reason = null;
    }

    /**
     * Creates a flag with {@code reason}.
     */
    public Flag(String reason) {
        requireNonNull(reason);
        checkArgument(isValidReason(reason), MESSAGE_REASON_CONSTRAINTS);
        this.reason = reason;
    }

    /**
     * Returns true if {@code test} is a valid reason.
     * Reasons cannot be blank, longer than 100 characters or contain control characters.
     * Unicode whitespace and invisible formatting characters do not count as meaningful text.
     */
    public static boolean isValidReason(String test) {
        return test != null
                && test.length() <= MAX_REASON_LENGTH
                && !test.matches("[\\p{IsWhite_Space}\\p{Cf}]*")
                && test.codePoints().noneMatch(Character::isISOControl);
    }

    /**
     * Returns this flag's reason, or an empty {@code Optional} if no reason was supplied.
     */
    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Flag otherFlag)) {
            return false;
        }

        return Objects.equals(reason, otherFlag.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("reason", reason)
                .toString();
    }
}
