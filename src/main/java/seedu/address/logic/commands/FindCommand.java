package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.StudentContainsKeywordsPredicate;

/**
 * Finds students whose name, subject and level contain all the keywords given for each field, ignoring case.
 * Adapted from AddressBook-Level3's {@code FindCommand}
 * (https://github.com/se-edu/addressbook-level3), with student-specific feedback.
 */
public class FindCommand extends Command {
    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds students matching all the given keywords, ignoring case.\n"
            + "Keywords before any prefix, and those after " + PREFIX_NAME + ", search names; "
            + PREFIX_SUBJECT + " searches subjects and " + PREFIX_LEVEL + " searches levels.\n"
            + "Keywords can match part of a field and appear in any order.\n"
            + "Parameters: [KEYWORD]... [" + PREFIX_NAME + "NAME] [" + PREFIX_SUBJECT + "SUBJECT] ["
            + PREFIX_LEVEL + "LEVEL] (at least one)\n"
            + "Example: " + COMMAND_WORD + " tan " + PREFIX_SUBJECT + "Physics " + PREFIX_LEVEL + "Sec 3\n"
            + "Use list to show all students again.";
    public static final String MESSAGE_SUCCESS = "Found %1$d students.";
    public static final String MESSAGE_SUCCESS_SINGLE = "Found 1 student.";
    public static final String MESSAGE_NO_MATCHES = "No matching students found. Use list to show all students.";
    /** Points users who searched names alone, e.g. {@code find physics}, to the subject and level fields. */
    public static final String MESSAGE_NO_NAME_MATCHES = MESSAGE_NO_MATCHES + " To search by subject or level, use "
            + PREFIX_SUBJECT + " or " + PREFIX_LEVEL + ", e.g. " + COMMAND_WORD + " " + PREFIX_SUBJECT + "Physics.";

    private final StudentContainsKeywordsPredicate predicate;

    /**
     * Creates a command that finds students matching {@code predicate}.
     */
    public FindCommand(StudentContainsKeywordsPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        int studentCount = model.getFilteredPersonList().size();
        if (studentCount == 0) {
            return new CommandResult(predicate.isNameSearchOnly() ? MESSAGE_NO_NAME_MATCHES : MESSAGE_NO_MATCHES);
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
