package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Lesson;

/**
 * Jackson-friendly version of {@link Lesson}.
 * The date is stored as {@code YYYY-MM-DD}. Lessons saved before dates were recorded have no date field.
 */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";
    public static final String MESSAGE_INVALID_DATE =
            "Lesson dates should be valid dates in the format YYYY-MM-DD.";

    private static final Logger logger = LogsCenter.getLogger(JsonAdaptedLesson.class);

    private final String content;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String date;

    /**
     * Constructs a {@code JsonAdaptedLesson} with the given lesson details. {@code date} may be null.
     */
    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("content") String content, @JsonProperty("date") String date) {
        this.content = content;
        this.date = date;
    }

    /**
     * Converts a given {@code Lesson} into this class for Jackson use.
     */
    public JsonAdaptedLesson(Lesson source) {
        content = source.content;
        date = source.getDate().map(LocalDate::toString).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted lesson object into the model's {@code Lesson} object.
     * A missing date gives an undated lesson.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted lesson.
     */
    public Lesson toModelType() throws IllegalValueException {
        if (content == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "content"));
        }
        if (!Lesson.isValidContent(content)) {
            logger.warning("Rejected a stored lesson with invalid content of length " + content.length());
            throw new IllegalValueException(Lesson.MESSAGE_CONSTRAINTS);
        }
        if (date == null) {
            return new Lesson(content);
        }
        try {
            return new Lesson(content, LocalDate.parse(date));
        } catch (DateTimeParseException e) {
            logger.warning("Rejected a stored lesson with invalid date: " + date);
            throw new IllegalValueException(MESSAGE_INVALID_DATE);
        }
    }

}
