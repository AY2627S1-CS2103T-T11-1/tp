package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSearchPredicate;
import seedu.address.model.person.Person;

/**
 * Searches one displayed student's lessons, keeping their complete-history view numbers
 * and the student filter.
 */
public class FindLessonCommand extends Command {
    public static final String COMMAND_WORD = "findlesson";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Searches a student's lessons by content or inclusive date range.\n"
            + "Parameters: INDEX [KEYWORDS] [from/DATE] [to/DATE]\n"
            + "Supply at least one keyword or date boundary. Put keywords before date prefixes.\n"
            + "Example: " + COMMAND_WORD + " 1 algebra from/01/01/2020";
    public static final String MESSAGE_MATCHES_HEADER = "Matching lesson records for %1$s (%2$d):";
    public static final String MESSAGE_NO_MATCHES = "No matching lesson records found for %1$s.";
    public static final String MESSAGE_KEYWORDS_AFTER_DATE = "Put keywords before from/ and to/. "
            + "Example: findlesson 1 algebra from/01/10/2026";

    private static final Logger logger = LogsCenter.getLogger(FindLessonCommand.class);

    private final Index studentIndex;
    private final LessonSearchPredicate predicate;

    /** Creates a search for the student at the displayed {@code studentIndex}. */
    public FindLessonCommand(Index studentIndex, LessonSearchPredicate predicate) {
        this.studentIndex = requireNonNull(studentIndex);
        this.predicate = requireNonNull(predicate);
    }

    /**
     * Returns matching records without changing student data or creating a persistent lesson filter.
     *
     * @throws CommandException if the student index is outside the displayed list.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> students = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }
        Person student = students.get(studentIndex.getZeroBased());
        List<Lesson> lessons = student.getLessons();
        if (lessons.isEmpty()) {
            return new CommandResult(
                    String.format(ViewCommand.MESSAGE_NO_LESSONS, student.getName()), student);
        }

        List<String> matches = formatMatchingLines(lessons);
        logger.fine("Matched " + matches.size() + " of " + lessons.size()
                + " lessons for displayed student " + studentIndex.getOneBased());
        String feedback = matches.isEmpty() ? String.format(MESSAGE_NO_MATCHES, student.getName())
                : String.format(MESSAGE_MATCHES_HEADER, student.getName(), matches.size())
                        + "\n" + String.join("\n", matches);
        return new CommandResult(feedback, student);
    }

    /** Formats matches in view order without renumbering gaps left by non-matching records. */
    private List<String> formatMatchingLines(List<Lesson> lessons) {
        List<String> matches = new ArrayList<>();
        for (int i = lessons.size() - 1; i >= 0; i--) {
            Lesson lesson = lessons.get(i);
            if (predicate.test(lesson)) {
                matches.add(String.format(ViewCommand.MESSAGE_LESSON_LINE,
                        lessons.size() - i, Messages.formatLessonDate(lesson), lesson.content));
            }
        }
        return matches;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindLessonCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex) && predicate.equals(otherCommand.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getOneBased(), predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex).add("predicate", predicate).toString();
    }
}
