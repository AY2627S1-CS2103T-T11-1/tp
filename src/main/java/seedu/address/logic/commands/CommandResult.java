package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** The displayed student whose history was viewed, if applicable. */
    private final Index viewedStudentIndex;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, Index viewedStudentIndex) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.viewedStudentIndex = viewedStudentIndex;
    }

    /**
     * Constructs feedback that identifies the displayed student whose lessons were viewed.
     */
    public CommandResult(String feedbackToUser, Index viewedStudentIndex) {
        this(feedbackToUser, false, false, requireNonNull(viewedStudentIndex));
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /**
     * Returns the displayed student index to highlight after a successful view.
     */
    public Optional<Index> getViewedStudentIndex() {
        return Optional.ofNullable(viewedStudentIndex);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(viewedStudentIndex, otherCommandResult.viewedStudentIndex);
    }

    @Override
    public int hashCode() {
        Integer indexValue = viewedStudentIndex == null ? null : viewedStudentIndex.getOneBased();
        return Objects.hash(feedbackToUser, showHelp, exit, indexValue);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("viewedStudentIndex", viewedStudentIndex)
                .toString();
    }

}
