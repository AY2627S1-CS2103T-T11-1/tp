package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Records a lesson for a student identified using its displayed index in the student list.
 * The lesson is dated today unless the user gives an earlier date.
 */
public class LogCommand extends Command {

    public static final String COMMAND_WORD = "loglesson";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Logs a lesson for the student identified by the index number used in the displayed"
            + " student list. The lesson is dated today unless DATE is given.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_CONTENT + "CONTENT "
            + "[" + PREFIX_DATE + "DATE (DD/MM/YYYY, not in the future)]\n"
            + "Example: " + COMMAND_WORD + " 3 " + PREFIX_CONTENT + "Quadratic inequalities "
            + PREFIX_DATE + "21/10/2026";

    public static final String MESSAGE_LOG_LESSON_SUCCESS = "Logged lesson for %1$s on %2$s: %3$s";
    public static final String MESSAGE_FUTURE_DATE =
            "Lesson dates cannot be in the future. Log a lesson on or after the day it is taught.";

    private static final Logger logger = LogsCenter.getLogger(LogCommand.class);

    private final Index targetIndex;
    private final Lesson lesson;
    private final Clock clock;

    /**
     * Creates a LogCommand to add {@code lesson} to the student at {@code targetIndex}.
     * An undated {@code lesson} is dated today.
     */
    public LogCommand(Index targetIndex, Lesson lesson) {
        this(targetIndex, lesson, Clock.systemDefaultZone());
    }

    /**
     * Creates a LogCommand that dates the lesson using {@code clock}, so that tests can fix today's date.
     */
    public LogCommand(Index targetIndex, Lesson lesson, Clock clock) {
        requireNonNull(targetIndex);
        requireNonNull(lesson);
        requireNonNull(clock);
        this.targetIndex = targetIndex;
        this.lesson = lesson;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate lessonDate = lesson.getDate().orElse(today);
        if (lessonDate.isAfter(today)) {
            throw new CommandException(MESSAGE_FUTURE_DATE);
        }

        Lesson datedLesson = lesson.withDate(lessonDate);
        Person studentToLog = lastShownList.get(targetIndex.getZeroBased());
        Person studentWithLesson = studentToLog.withLessonAdded(datedLesson);
        assertOneLessonAdded(studentToLog, studentWithLesson);
        model.setPerson(studentToLog, studentWithLesson);
        logger.fine("Logged lesson dated " + lessonDate + " for displayed student " + targetIndex.getOneBased());

        return new CommandResult(String.format(MESSAGE_LOG_LESSON_SUCCESS, studentWithLesson.getName(),
                Messages.formatLessonDate(datedLesson), datedLesson.content), studentWithLesson);
    }

    /**
     * Asserts that {@code after} is the same student as {@code before} with exactly one more lesson.
     */
    static void assertOneLessonAdded(Person before, Person after) {
        assert after.getLessons().size() == before.getLessons().size() + 1 : "Logging must add exactly one lesson";
        assert after.isSamePerson(before) : "Logging must not change the student's identity";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LogCommand otherLogCommand)) {
            return false;
        }

        return targetIndex.equals(otherLogCommand.targetIndex)
                && lesson.equals(otherLogCommand.lesson)
                && clock.equals(otherLogCommand.clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("lesson", lesson)
                .toString();
    }
}
