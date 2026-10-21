package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsKeywordsPredicate;

/**
 * Finds students whose names contain all search keywords, ignoring case.
 * Adapted from AddressBook-Level3's {@code FindCommand}
 * (https://github.com/se-edu/addressbook-level3), with student-specific feedback.
 */
public class FindCommand extends Command {
    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds students whose names contain all the specified keywords, ignoring case.\n"
            + "Keywords can match part of a name and appear in any order.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " tan wei\n"
            + "Use list to show all students again.";
    public static final String MESSAGE_SUCCESS = "Found %1$d students.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Found 1 student.";
    public static final String MESSAGE_NO_MATCHES = "No matching students found. Use list to show all students.";

    private final NameContainsKeywordsPredicate predicate;

    /**
     * Creates a command that finds students matching {@code predicate}.
     */
    public FindCommand(NameContainsKeywordsPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int studentCount = model.getFilteredPersonList().size();
        if (studentCount == 0) {
            return new CommandResult(MESSAGE_NO_MATCHES);
        }
        if (studentCount == 1) {
            return new CommandResult(MESSAGE_SUCCESS_SINGLE);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, studentCount));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
