package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Deletes one lesson using the displayed student index and the newest-first lesson number from {@code view}.
 */
public class DeleteLessonCommand extends Command {
    public static final String COMMAND_WORD = "deletelesson";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes one lesson record for the student identified by the displayed student index.\n"
            + "Parameters: STUDENT_INDEX LESSON_INDEX (both positive integers without leading zeros)\n"
            + "Use view STUDENT_INDEX to check the current lesson numbers (newest first).\n"
            + "Example: " + COMMAND_WORD + " 2 1";
    public static final String MESSAGE_DELETE_LESSON_SUCCESS = "Deleted lesson %1$d for %2$s: %3$s";

    private final Index studentIndex;
    private final Index lessonIndex;

    /**
     * Creates a command to delete the lesson at {@code lessonIndex} for the displayed {@code studentIndex}.
     */
    public DeleteLessonCommand(Index studentIndex, Index lessonIndex) {
        this.studentIndex = requireNonNull(studentIndex);
        this.lessonIndex = requireNonNull(lessonIndex);
    }

    /**
     * Deletes the selected lesson without changing student details or the current student filter.
     *
     * @throws CommandException if either index does not identify an existing student or lesson
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> students = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }

        Person student = students.get(studentIndex.getZeroBased());
        List<Lesson> lessons = student.getLessons();
        if (lessonIndex.getZeroBased() >= lessons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }

        // View numbers lessons newest first, while the model stores them oldest first.
        int storedIndex = lessons.size() - lessonIndex.getOneBased();
        Lesson deletedLesson = lessons.get(storedIndex);
        model.setPerson(student, student.withLessonRemoved(storedIndex));
        return new CommandResult(String.format(MESSAGE_DELETE_LESSON_SUCCESS,
                lessonIndex.getOneBased(), student.getName(), deletedLesson));
    }

    /**
     * Returns whether both commands target the same displayed student and lesson numbers.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteLessonCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && lessonIndex.equals(otherCommand.lessonIndex);
    }

    /**
     * Returns a hash based on the same numeric indexes used by equality.
     */
    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getOneBased(), lessonIndex.getOneBased());
    }

    /**
     * Returns the student and lesson indexes for diagnostic output.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("lessonIndex", lessonIndex)
                .toString();
    }
}
