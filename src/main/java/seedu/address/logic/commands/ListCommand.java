package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all students and reports how many are shown.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed %1$d students.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Listed 1 student.";
    public static final String MESSAGE_EMPTY = "No students found. Use add to create a student.";
    public static final String MESSAGE_INVALID_ARGUMENTS = "The list command does not accept parameters.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        int studentCount = model.getFilteredPersonList().size();
        if (studentCount == 0) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        if (studentCount == 1) {
            return new CommandResult(MESSAGE_SUCCESS_SINGLE);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentCount));
    }
}
