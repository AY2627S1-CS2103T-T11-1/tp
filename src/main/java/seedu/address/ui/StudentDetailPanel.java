package seedu.address.ui;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Panel showing one student's details, flag and lesson records, or guidance when no student is selected.
 * Lessons are numbered newest first, the same numbers that {@code view}, {@code editlesson}
 * and {@code deletelesson} use.
 */
public class StudentDetailPanel extends UiPart<Region> {

    static final String NO_FLAG_REASON = "Flagged (no reason given)";
    static final String SELECT_GUIDANCE = "Click a student, or type view INDEX, to see their details and lessons here.";
    static final String FILTERED_GUIDANCE = "No students are displayed. Type list to show all students.";
    static final String NO_STUDENTS_GUIDANCE = "Add a student first. Their details and lessons will show here.";
    private static final String ADD_FORMAT = AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_PHONE
            + "PHONE " + PREFIX_ADDRESS + "ADDRESS " + PREFIX_LEVEL + "LEVEL " + PREFIX_SUBJECT + "SUBJECT";

    private static final String FXML = "StudentDetailPanel.fxml";

    /** More than the width of the details panel's vertical scroll bar. */
    private static final double SCROLL_BAR_ALLOWANCE = 20;

    private final Logger logger = LogsCenter.getLogger(StudentDetailPanel.class);

    private final ObservableList<Person> displayedStudents;
    private final ObservableList<Person> allStudents;
    private Person student;

    @FXML
    private VBox noStudentState;
    @FXML
    private Label noStudentText;
    @FXML
    private Label noStudentCommand;
    @FXML
    private ScrollPane detailScrollPane;
    @FXML
    private Label avatar;
    @FXML
    private Label name;
    @FXML
    private SVGPath flagIcon;
    @FXML
    private VBox chips;
    @FXML
    private HBox chipRow;
    @FXML
    private Label level;
    @FXML
    private Label subject;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label flagBanner;
    @FXML
    private Label lessonHeading;
    @FXML
    private Label lessonOrder;
    @FXML
    private VBox lessonList;
    @FXML
    private VBox noLessonsState;
    @FXML
    private Label noLessonsText;
    @FXML
    private Label noLessonsCommand;

    /**
     * Creates a panel that starts with no student selected. Its guidance suggests a command that works
     * for the {@code displayedStudents} and {@code allStudents} saved, and follows changes to either list.
     */
    public StudentDetailPanel(ObservableList<Person> displayedStudents, ObservableList<Person> allStudents) {
        super(FXML);
        this.displayedStudents = requireNonNull(displayedStudents);
        this.allStudents = requireNonNull(allStudents);
        // A chip wider than the panel wraps its own text instead of being cut off
        level.maxWidthProperty().bind(chips.widthProperty());
        subject.maxWidthProperty().bind(chips.widthProperty());
        chips.widthProperty().addListener((unused1, unused2, unused3) -> arrangeChips());
        displayedStudents.addListener((InvalidationListener) unused -> updateGuidance());
        allStudents.addListener((InvalidationListener) unused -> updateGuidance());
        updateGuidance();
        clear();
    }

    /**
     * Returns the student currently shown, if any.
     */
    public Optional<Person> getStudent() {
        return Optional.ofNullable(student);
    }

    /**
     * Shows {@code student} and scrolls back to the top. {@code displayedIndex} is the student's
     * one-based position in the displayed list, or 0 if it is not displayed; it is only used in the
     * empty-lesson hint.
     */
    public void show(Person student, int displayedIndex) {
        requireNonNull(student);
        this.student = student;
        setShowingStudent(true);

        avatar.setText(PersonCard.initialsOf(student.getName().fullName));
        name.setText(student.getName().fullName);
        setShown(flagIcon, student.isFlagged());
        level.setText(student.getLevel().value);
        subject.setText(student.getSubject().value);
        arrangeChips();
        phone.setText("Phone: " + student.getPhone().value);
        address.setText("Address: " + student.getAddress().value);
        flagBanner.setText(student.getFlag().map(flag -> flag.getReason().orElse(NO_FLAG_REASON)).orElse(""));
        setShown(flagBanner, student.isFlagged());

        List<Lesson> lessons = student.getLessons();
        lessonHeading.setText("Lessons (" + lessons.size() + ")");
        lessonList.getChildren().setAll(createLessonRows(lessons));
        setShown(lessonOrder, !lessons.isEmpty());
        setShown(noLessonsState, lessons.isEmpty());
        noLessonsText.setText("No lessons logged yet for " + student.getName() + ".");
        String indexText = displayedIndex > 0 ? String.valueOf(displayedIndex) : "INDEX";
        noLessonsCommand.setText(LogCommand.COMMAND_WORD + " " + indexText + " c/Topic covered");
        detailScrollPane.setVvalue(0);
        logger.fine("Showing details of a student with " + lessons.size() + " lessons");
    }

    /**
     * Shows the guidance for when no student is selected.
     */
    public void clear() {
        student = null;
        lessonList.getChildren().clear();
        setShowingStudent(false);
    }

    /**
     * Creates one row per lesson, newest first, numbered from 1.
     */
    private static List<Node> createLessonRows(List<Lesson> lessons) {
        int count = lessons.size();
        return IntStream.range(0, count)
                .<Node>mapToObj(number -> createLessonRow(number + 1, lessons.get(count - 1 - number)))
                .toList();
    }

    private static HBox createLessonRow(int number, Lesson lesson) {
        Label numberLabel = new Label(number + ".");
        numberLabel.getStyleClass().add("lesson-number");
        numberLabel.setMinWidth(Region.USE_PREF_SIZE);
        Label dateLabel = new Label(Messages.formatLessonDate(lesson));
        dateLabel.getStyleClass().add("lesson-date");
        dateLabel.setMinWidth(Region.USE_PREF_SIZE);
        Label contentLabel = new Label(lesson.content);
        contentLabel.getStyleClass().add("lesson-content");
        contentLabel.setWrapText(true);
        contentLabel.setMinWidth(0);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(contentLabel, Priority.ALWAYS);

        HBox row = new HBox(10, numberLabel, dateLabel, contentLabel);
        row.getStyleClass().add("lesson-row");
        return row;
    }

    /**
     * Places the level and subject chips side by side when both fit on one line, and otherwise stacks
     * them, so that a chip which still does not fit can wrap its text within the full width.
     * Stacked chips return side by side only with room to spare for the scroll bar, which stacking
     * can add or remove; otherwise the two layouts would keep replacing each other.
     */
    private void arrangeChips() {
        boolean isStacked = !chips.getChildren().contains(chipRow);
        double availableWidth = chips.getWidth() - (isStacked ? SCROLL_BAR_ALLOWANCE : 0);
        boolean isSideBySide = level.prefWidth(-1) + chipRow.getSpacing() + subject.prefWidth(-1) <= availableWidth;
        if (isSideBySide && !chips.getChildren().contains(chipRow)) {
            chipRow.getChildren().setAll(level, subject);
            chips.getChildren().setAll(chipRow);
        } else if (!isSideBySide && chips.getChildren().contains(chipRow)) {
            chipRow.getChildren().clear();
            chips.getChildren().setAll(level, subject);
        }
    }

    /**
     * Suggests viewing the first displayed student, or else listing the students a filter hides,
     * or else adding a first student.
     */
    private void updateGuidance() {
        if (!displayedStudents.isEmpty()) {
            noStudentText.setText(SELECT_GUIDANCE);
            noStudentCommand.setText(ViewCommand.COMMAND_WORD + " 1");
        } else if (!allStudents.isEmpty()) {
            noStudentText.setText(FILTERED_GUIDANCE);
            noStudentCommand.setText(ListCommand.COMMAND_WORD);
        } else {
            noStudentText.setText(NO_STUDENTS_GUIDANCE);
            noStudentCommand.setText(ADD_FORMAT);
        }
    }

    private void setShowingStudent(boolean isShowingStudent) {
        setShown(detailScrollPane, isShowingStudent);
        setShown(noStudentState, !isShowingStudent);
    }

    private static void setShown(Node node, boolean isShown) {
        node.setVisible(isShown);
        node.setManaged(isShown);
    }
}
