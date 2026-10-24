package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Lists all students flagged for follow-up.
 */
public class FlaggedCommand extends Command {

    public static final String COMMAND_WORD = "flagged";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all students flagged for follow-up.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Listed %1$d flagged students.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Listed 1 flagged student.";
    public static final String MESSAGE_NO_FLAGGED = "No flagged students found. Use list to show all students.";
    public static final String MESSAGE_INVALID_ARGUMENTS = "The flagged command does not accept parameters.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(Person::isFlagged);
        int flaggedCount = model.getFilteredPersonList().size();
        if (flaggedCount == 0) {
            return new CommandResult(MESSAGE_NO_FLAGGED);
        }
        if (flaggedCount == 1) {
            return new CommandResult(MESSAGE_SUCCESS_SINGLE);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, flaggedCount));
    }
}
