package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.StudentSummary;

/**
 * Reports totals for all students and lesson records without changing the displayed filter.
 */
public class SummaryCommand extends Command {
    public static final String COMMAND_WORD = "summary";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows totals for all students and lesson records. Accepts no parameters.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Summary:\n%1$s";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        StudentSummary summary = new StudentSummary(model.getAddressBook());
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatSummaryParagraph(summary)));
    }
}
