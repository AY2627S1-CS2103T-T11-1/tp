package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Records a lesson for a student identified using its displayed index in the student list.
 */
public class LogCommand extends Command {

    public static final String COMMAND_WORD = "log";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Logs a lesson for the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_CONTENT + "CONTENT\n"
            + "Example: " + COMMAND_WORD + " 3 " + PREFIX_CONTENT + "Quadratic inequalities";

    public static final String MESSAGE_LOG_LESSON_SUCCESS = "Logged lesson for %1$s: %2$s";

    private final Index targetIndex;
    private final Lesson lesson;

    /**
     * Creates a LogCommand to add {@code lesson} to the student at {@code targetIndex}.
     */
    public LogCommand(Index targetIndex, Lesson lesson) {
        requireNonNull(targetIndex);
        requireNonNull(lesson);
        this.targetIndex = targetIndex;
        this.lesson = lesson;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person studentToLog = lastShownList.get(targetIndex.getZeroBased());
        Person studentWithLesson = studentToLog.withLessonAdded(lesson);
        model.setPerson(studentToLog, studentWithLesson);

        return new CommandResult(String.format(MESSAGE_LOG_LESSON_SUCCESS,
                studentWithLesson.getName(), lesson));
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
                && lesson.equals(otherLogCommand.lesson);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("lesson", lesson)
                .toString();
    }
}
