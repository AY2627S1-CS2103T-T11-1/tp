package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Removes a student's follow-up flag and reason.
 */
public class UnflagCommand extends Command {

    public static final String COMMAND_WORD = "unflag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes the flag and reason from the student identified by the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 2";

    public static final String MESSAGE_UNFLAGGED_SUCCESS = "Unflagged student: %1$s.";
    public static final String MESSAGE_NOT_FLAGGED = "%1$s is not flagged.";

    private final Index targetIndex;

    /**
     * Creates a command that unflags the displayed student at {@code targetIndex}.
     */
    public UnflagCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedStudents = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Person student = displayedStudents.get(targetIndex.getZeroBased());
        if (!student.isFlagged()) {
            throw new CommandException(String.format(MESSAGE_NOT_FLAGGED, student.getName()));
        }

        Person unflaggedStudent = student.withoutFlag();
        model.setPerson(student, unflaggedStudent);
        return new CommandResult(String.format(MESSAGE_UNFLAGGED_SUCCESS, student.getName()), unflaggedStudent);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UnflagCommand otherCommand)) {
            return false;
        }

        return targetIndex.equals(otherCommand.targetIndex);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetIndex.getOneBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
