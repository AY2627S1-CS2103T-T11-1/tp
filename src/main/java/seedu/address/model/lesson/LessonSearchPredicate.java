package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests lesson content and an optional inclusive date range without changing the lesson history.
 * Every keyword must occur as a literal, case-insensitive substring of the content.
 */
public class LessonSearchPredicate implements Predicate<Lesson> {
    public static final String MESSAGE_REVERSED_RANGE = "The from date must be on or before the to date.";

    private final List<String> keywords;
    /** Inclusive lower boundary, or null when the range has no lower boundary. */
    private final LocalDate fromDate;
    /** Inclusive upper boundary, or null when the range has no upper boundary. */
    private final LocalDate toDate;

    /**
     * Creates immutable search criteria. Future boundaries are allowed because this is a query, not a new lesson.
     *
     * @param keywords Content keywords; an empty list means content is not restricted.
     * @param fromDate Inclusive lower boundary, or null if absent.
     * @param toDate Inclusive upper boundary, or null if absent.
     * @throws NullPointerException if the keyword list or a keyword is null.
     * @throws IllegalArgumentException if there are no criteria, a keyword is blank, or a date range is invalid.
     */
    public LessonSearchPredicate(List<String> keywords, LocalDate fromDate, LocalDate toDate) {
        this.keywords = List.copyOf(keywords);
        checkArgument(!this.keywords.isEmpty() || fromDate != null || toDate != null,
                "At least one lesson search criterion is required.");
        checkArgument(this.keywords.stream().noneMatch(word -> word.matches("[\\p{IsWhite_Space}\\p{Cf}]*")),
                "Search keywords must not be blank.");
        checkArgument(fromDate == null || !fromDate.isBefore(Lesson.EARLIEST_DATE), Lesson.MESSAGE_DATE_CONSTRAINTS);
        checkArgument(toDate == null || !toDate.isBefore(Lesson.EARLIEST_DATE), Lesson.MESSAGE_DATE_CONSTRAINTS);
        checkArgument(fromDate == null || toDate == null || !fromDate.isAfter(toDate), MESSAGE_REVERSED_RANGE);
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /**
     * Returns true if the lesson meets every content and date criterion.
     * Undated lessons match only when neither date boundary is supplied.
     */
    @Override
    public boolean test(Lesson lesson) {
        requireNonNull(lesson);
        return keywords.stream().allMatch(word -> StringUtil.containsIgnoreCase(lesson.content, word))
                && matchesDate(lesson);
    }

    private boolean matchesDate(Lesson lesson) {
        if (fromDate == null && toDate == null) {
            return true;
        }
        return lesson.getDate().filter(date -> (fromDate == null || !date.isBefore(fromDate))
                && (toDate == null || !date.isAfter(toDate))).isPresent();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonSearchPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords)
                && Objects.equals(fromDate, otherPredicate.fromDate)
                && Objects.equals(toDate, otherPredicate.toDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keywords, fromDate, toDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords)
                .add("fromDate", fromDate).add("toDate", toDate).toString();
    }
}
