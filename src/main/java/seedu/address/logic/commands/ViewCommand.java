package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Displays a student's lesson records in reverse logging order without changing the student list.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Views lesson records for the student identified by the displayed student index.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_NO_LESSONS = "No lesson records for %1$s.";
    public static final String MESSAGE_LESSONS_HEADER = "Lesson records for %1$s (most recently logged first):";

    private final Index targetIndex;

    /**
     * Creates a command to view lessons for the student at {@code targetIndex}.
     */
    public ViewCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> students = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Person student = students.get(targetIndex.getZeroBased());
        List<Lesson> lessons = student.getLessons();
        if (lessons.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NO_LESSONS, student.getName()));
        }

        StringBuilder result = new StringBuilder(String.format(MESSAGE_LESSONS_HEADER, student.getName()));
        for (int i = lessons.size() - 1; i >= 0; i--) {
            result.append("\n").append(lessons.size() - i).append(". ").append(lessons.get(i).content);
        }
        return new CommandResult(result.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ViewCommand otherViewCommand
                && targetIndex.equals(otherViewCommand.targetIndex));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
