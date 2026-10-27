package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REASON;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;

/**
 * Flags a student for follow-up, optionally recording why.
 */
public class FlagCommand extends Command {

    public static final String COMMAND_WORD = "flag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Flags the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer) [" + PREFIX_REASON + "REASON]\n"
            + "Example: " + COMMAND_WORD + " 2 " + PREFIX_REASON + "Missed homework twice";

    public static final String MESSAGE_FLAGGED_SUCCESS = "Flagged student: %1$s.";
    public static final String MESSAGE_FLAGGED_WITH_REASON_SUCCESS = "Flagged student: %1$s. Reason: %2$s";
    public static final String MESSAGE_UPDATED_SUCCESS = "Updated flag for %1$s. Reason: %2$s";
    public static final String MESSAGE_ALREADY_FLAGGED = "%1$s is already flagged.";
    public static final String MESSAGE_ALREADY_FLAGGED_WITH_REASON =
            "%1$s is already flagged with that reason.";

    private static final Logger logger = LogsCenter.getLogger(FlagCommand.class);

    private final Index targetIndex;
    private final Flag requestedFlag;

    /**
     * Creates a command that flags the displayed student at {@code targetIndex} without a reason.
     */
    public FlagCommand(Index targetIndex) {
        this(targetIndex, new Flag());
    }

    /**
     * Creates a command that flags the displayed student at {@code targetIndex} with {@code requestedFlag}.
     */
    public FlagCommand(Index targetIndex, Flag requestedFlag) {
        this.targetIndex = requireNonNull(targetIndex);
        this.requestedFlag = requireNonNull(requestedFlag);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedStudents = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedStudents.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Person student = displayedStudents.get(targetIndex.getZeroBased());
        if (student.isFlagged()) {
            return updateExistingFlag(model, student);
        }

        Person flaggedStudent = student.withFlag(requestedFlag);
        model.setPerson(student, flaggedStudent);
        logger.fine("Flagged displayed student " + targetIndex.getOneBased());
        String feedback = requestedFlag.getReason()
                .map(reason -> String.format(MESSAGE_FLAGGED_WITH_REASON_SUCCESS, student.getName(), reason))
                .orElseGet(() -> String.format(MESSAGE_FLAGGED_SUCCESS, student.getName()));
        return new CommandResult(feedback, flaggedStudent);
    }

    /**
     * Replaces an existing reason when a different reason was supplied.
     * Omitting the reason never clears an existing one.
     */
    private CommandResult updateExistingFlag(Model model, Person student) throws CommandException {
        if (requestedFlag.getReason().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_FLAGGED, student.getName()));
        }
        if (student.getFlag().orElseThrow().equals(requestedFlag)) {
            throw new CommandException(String.format(MESSAGE_ALREADY_FLAGGED_WITH_REASON, student.getName()));
        }

        Person reflaggedStudent = student.withFlag(requestedFlag);
        model.setPerson(student, reflaggedStudent);
        String reason = requestedFlag.getReason().orElseThrow();
        logger.fine("Updated flag for displayed student " + targetIndex.getOneBased());
        return new CommandResult(String.format(MESSAGE_UPDATED_SUCCESS, student.getName(), reason), reflaggedStudent);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FlagCommand otherCommand)) {
            return false;
        }

        return targetIndex.equals(otherCommand.targetIndex)
                && requestedFlag.equals(otherCommand.requestedFlag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex.getOneBased(), requestedFlag);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("requestedFlag", requestedFlag)
                .toString();
    }
}
