package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Flag;

/**
 * Jackson-friendly version of {@link Flag}.
 * A missing reason represents a flag without a reason.
 */
class JsonAdaptedFlag {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String reason;

    /**
     * Constructs a {@code JsonAdaptedFlag}. {@code reason} may be null.
     */
    @JsonCreator
    public JsonAdaptedFlag(@JsonProperty("reason") String reason) {
        this.reason = reason;
    }

    /**
     * Converts a given {@code Flag} into this class for Jackson use.
     */
    public JsonAdaptedFlag(Flag source) {
        reason = source.getReason().orElse(null);
    }

    /**
     * Converts this Jackson-friendly flag into the model's {@code Flag}.
     *
     * @throws IllegalValueException if the stored reason violates flag constraints
     */
    public Flag toModelType() throws IllegalValueException {
        if (reason == null) {
            return new Flag();
        }
        if (!Flag.isValidReason(reason)) {
            throw new IllegalValueException(Flag.MESSAGE_REASON_CONSTRAINTS);
        }
        return new Flag(reason);
    }
}
