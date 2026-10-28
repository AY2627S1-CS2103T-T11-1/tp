package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Lists students whose lesson history is empty.
 */
public class UnloggedCommand extends Command {

    public static final String COMMAND_WORD = "unlogged";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists students with no recorded lessons. Accepts no parameters.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Listed %1$d students with no recorded lessons.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Listed 1 student with no recorded lessons.";
    public static final String MESSAGE_NO_MATCHES =
            "No students without lesson records found. Use list to show all students.";
    public static final String MESSAGE_INVALID_ARGUMENTS = "The unlogged command does not accept parameters.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(person -> person.getLessons().isEmpty());
        int count = model.getFilteredPersonList().size();
        if (count == 0) {
            return new CommandResult(MESSAGE_NO_MATCHES);
        }
        if (count == 1) {
            return new CommandResult(MESSAGE_SUCCESS_SINGLE);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, count));
    }
}
