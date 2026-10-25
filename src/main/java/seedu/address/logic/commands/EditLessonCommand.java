package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Edits the content or date of one lesson, using the displayed student index and the newest-first
 * lesson number from {@code view}.
 * A lesson whose date changes moves to its new place in date order, so its lesson number can change.
 */
public class EditLessonCommand extends Command {

    public static final String COMMAND_WORD = "editlesson";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits one lesson record of the student identified by the displayed student index.\n"
            + "Parameters: STUDENT_INDEX LESSON_INDEX (both positive integers without leading zeros) "
            + "[" + PREFIX_CONTENT + "CONTENT] "
            + "[" + PREFIX_DATE + "DATE (DD/MM/YYYY, not in the future)]\n"
            + "At least one of " + PREFIX_CONTENT + " and " + PREFIX_DATE + " must be given. "
            + "Use view STUDENT_INDEX to check the current lesson numbers (newest first).\n"
            + "Example: " + COMMAND_WORD + " 2 1 " + PREFIX_CONTENT + "Quadratic inequalities "
            + PREFIX_DATE + "21/10/2026";

    public static final String MESSAGE_EDIT_LESSON_SUCCESS = "Edited lesson %1$d for %2$s (now lesson %3$d). "
            + "%4$s: %5$s";
    public static final String MESSAGE_LESSON_UNCHANGED = "The lesson already has those details.";

    private static final Logger logger = LogsCenter.getLogger(EditLessonCommand.class);

    private final Index studentIndex;
    private final Index lessonIndex;
    private final EditLessonDescriptor editLessonDescriptor;
    private final Clock clock;

    /**
     * Creates an EditLessonCommand to edit the lesson at {@code lessonIndex} of the displayed student at
     * {@code studentIndex} with {@code editLessonDescriptor}.
     */
    public EditLessonCommand(Index studentIndex, Index lessonIndex, EditLessonDescriptor editLessonDescriptor) {
        this(studentIndex, lessonIndex, editLessonDescriptor, Clock.systemDefaultZone());
    }

    /**
     * Creates an EditLessonCommand that checks for future dates using {@code clock},
     * so that tests can fix today's date.
     */
    public EditLessonCommand(Index studentIndex, Index lessonIndex, EditLessonDescriptor editLessonDescriptor,
            Clock clock) {
        requireNonNull(studentIndex);
        requireNonNull(lessonIndex);
        requireNonNull(editLessonDescriptor);
        requireNonNull(clock);
        this.studentIndex = studentIndex;
        this.lessonIndex = lessonIndex;
        this.editLessonDescriptor = new EditLessonDescriptor(editLessonDescriptor);
        this.clock = clock;
    }

    /**
     * Edits the selected lesson without changing student details, other lessons or the current student filter.
     *
     * @throws CommandException if either index does not identify an existing student or lesson,
     *     if the new date is in the future, or if the edit would not change the lesson
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person student = getDisplayedStudent(model);
        int storedIndex = getStoredLessonIndex(student);
        Lesson lessonToEdit = student.getLessons().get(storedIndex);

        checkDateNotInFuture();
        Lesson editedLesson = createEditedLesson(lessonToEdit, editLessonDescriptor);
        if (editedLesson.equals(lessonToEdit)) {
            throw new CommandException(MESSAGE_LESSON_UNCHANGED);
        }

        Person editedStudent = withLessonEdited(student, storedIndex, editedLesson);
        assertOnlyLessonEdited(student, editedStudent);
        model.setPerson(student, editedStudent);

        int newLessonNumber = getLessonNumber(editedStudent, editedLesson);
        logger.fine("Edited lesson " + lessonIndex.getOneBased() + " of displayed student "
                + studentIndex.getOneBased() + ", now lesson " + newLessonNumber);
        return new CommandResult(String.format(MESSAGE_EDIT_LESSON_SUCCESS, lessonIndex.getOneBased(),
                student.getName(), newLessonNumber, Messages.formatLessonDate(editedLesson), editedLesson.content));
    }

    /**
     * Returns the student at {@code studentIndex} in the displayed student list.
     *
     * @throws CommandException if the index is outside the displayed list
     */
    private Person getDisplayedStudent(Model model) throws CommandException {
        List<Person> students = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= students.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        }
        return students.get(studentIndex.getZeroBased());
    }

    /**
     * Returns the zero-based position in logging order of the lesson that {@code lessonIndex} shows in
     * {@code view}.
     *
     * @throws CommandException if the student has no lessons or the index is outside their history
     */
    private int getStoredLessonIndex(Person student) throws CommandException {
        List<Lesson> lessons = student.getLessons();
        if (lessons.isEmpty()) {
            throw new CommandException(String.format(ViewCommand.MESSAGE_NO_LESSONS, student.getName()));
        }
        if (lessonIndex.getZeroBased() >= lessons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        }
        return DeleteLessonCommand.toStoredLessonIndex(lessons.size(), lessonIndex.getOneBased());
    }

    /**
     * Rejects a new date after today, as {@code log} does.
     *
     * @throws CommandException if the new date is in the future
     */
    private void checkDateNotInFuture() throws CommandException {
        LocalDate today = LocalDate.now(clock);
        if (editLessonDescriptor.getDate().filter(date -> date.isAfter(today)).isPresent()) {
            throw new CommandException(LogCommand.MESSAGE_FUTURE_DATE);
        }
    }

    /**
     * Returns a copy of {@code lessonToEdit} with the content and date given in {@code editLessonDescriptor}.
     * Fields that are not given keep their current values.
     */
    private static Lesson createEditedLesson(Lesson lessonToEdit, EditLessonDescriptor editLessonDescriptor) {
        String updatedContent = editLessonDescriptor.getContent().orElse(lessonToEdit.content);
        Lesson lessonWithContent = lessonToEdit.withContent(updatedContent);
        return editLessonDescriptor.getDate().map(lessonWithContent::withDate).orElse(lessonWithContent);
    }

    /**
     * Returns a copy of {@code student} with the lesson at {@code storedIndex} replaced by {@code editedLesson}.
     * A lesson with an unchanged date keeps its position. A lesson with a new date is removed and added again,
     * so it goes after the other lessons on its new date.
     */
    private static Person withLessonEdited(Person student, int storedIndex, Lesson editedLesson) {
        Lesson lessonToEdit = student.getLessons().get(storedIndex);
        if (editedLesson.getDate().equals(lessonToEdit.getDate())) {
            return student.withLessonReplaced(storedIndex, editedLesson);
        }
        return student.withLessonRemoved(storedIndex).withLessonAdded(editedLesson);
    }

    /**
     * Returns the newest-first number that {@code view} shows for {@code lesson} in {@code student}'s history.
     * Lessons are compared by identity, because another lesson can be equal to the edited one.
     * {@code lesson} must be one of {@code student}'s lessons.
     */
    private static int getLessonNumber(Person student, Lesson lesson) {
        List<Lesson> lessons = student.getLessons();
        int storedIndex = IntStream.range(0, lessons.size())
                .filter(index -> lessons.get(index) == lesson)
                .findFirst()
                .getAsInt();
        return lessons.size() - storedIndex;
    }

    /**
     * Asserts that {@code after} is the same student as {@code before} with the same number of lessons.
     */
    static void assertOnlyLessonEdited(Person before, Person after) {
        assert after.isSamePerson(before) : "Editing a lesson must not change the student's identity";
        assert after.getLessons().size() == before.getLessons().size()
                : "Editing a lesson must not add or remove lessons";
    }

    /**
     * Returns whether both commands target the same lesson with the same edits and clock.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditLessonCommand otherCommand)) {
            return false;
        }

        return studentIndex.equals(otherCommand.studentIndex)
                && lessonIndex.equals(otherCommand.lessonIndex)
                && editLessonDescriptor.equals(otherCommand.editLessonDescriptor)
                && clock.equals(otherCommand.clock);
    }

    /**
     * Returns a hash based on the same values used by equality.
     */
    @Override
    public int hashCode() {
        return Objects.hash(studentIndex.getOneBased(), lessonIndex.getOneBased(), editLessonDescriptor, clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("lessonIndex", lessonIndex)
                .add("editLessonDescriptor", editLessonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the lesson with. Each non-empty field value will replace the
     * corresponding field value of the lesson.
     */
    public static class EditLessonDescriptor {
        private String content;
        private LocalDate date;

        public EditLessonDescriptor() {}

        /**
         * Creates a copy of {@code toCopy}.
         */
        public EditLessonDescriptor(EditLessonDescriptor toCopy) {
            setContent(toCopy.content);
            setDate(toCopy.date);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(content, date);
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Optional<String> getContent() {
            return Optional.ofNullable(content);
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public Optional<LocalDate> getDate() {
            return Optional.ofNullable(date);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditLessonDescriptor otherDescriptor)) {
                return false;
            }

            return Objects.equals(content, otherDescriptor.content)
                    && Objects.equals(date, otherDescriptor.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content, date);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("content", content)
                    .add("date", date)
                    .toString();
        }
    }
}
