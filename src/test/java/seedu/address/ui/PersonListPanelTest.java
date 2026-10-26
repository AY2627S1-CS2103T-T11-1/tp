package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.LessonDateUtil.todayText;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindLessonCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies student-card layout, empty states and command feedback in the main window.
 */
public class PersonListPanelTest {

    // Allow for first-window initialization and scheduling delays on CI runners.
    private static final long FX_THREAD_TIMEOUT_SECONDS = 15;
    private static final double LAYOUT_TOLERANCE = 1;
    /** Amy's history in {@link #clickTestModel()}, whose lessons have no dates. */
    private static final String AMY_FIXTURE_HISTORY = "Lesson records for Amy (3):\n1. (undated): Algebra"
            + "\n2. (undated): Geometry\n3. (undated): Algebra";
    private static final Person SHORT_PERSON = new PersonBuilder().withName("Alicia Tan")
            .withAddress("10 Short Street").withLevel("Sec 2").withSubject("E-Math").build();
    private static final Person CHARLOTTE = new PersonBuilder().withName("Charlotte O'Brien")
            .withPhone("81234568")
            .withAddress("Block 1234567890 Sunset View Avenue Unit 999999999999 Singapore 999999")
            .withLevel("Upper Secondary Two")
            .withSubject("Higher Order Thinking in Additional Mathematics").build();

    /** Records successful startup so cleanup never waits for a failed toolkit. */
    private static boolean isJavaFxStarted;

    @TempDir
    public Path temporaryFolder;

    @Test
    public void commandBox_findLesson_keepsNumbersAndSupportsEditing(@TempDir Path directory)
            throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                enterClickTestCommand(root, "find amy");
                assertFeedback(root, "findlesson 1 alg", "Matching lesson records for Amy (2):"
                        + "\n1. (undated): Algebra\n3. (undated): Algebra", false);
                assertEquals(1, list.getItems().size());
                assertEquals(0, list.getSelectionModel().getSelectedIndex());
                assertFeedback(root, "editlesson 1 3 c/Algebra revised",
                        "Edited lesson 3 for Amy (now lesson 3). (undated): Algebra revised", false);
                assertFeedback(root, "deletelesson 1 1", "Deleted lesson 1 for Amy: Algebra", false);
                assertFeedback(root, "findlesson 1 alg",
                        "Matching lesson records for Amy (1):\n2. (undated): Algebra revised", false);
                assertFeedback(root, "view 1",
                        "Lesson records for Amy (2):\n1. (undated): Geometry"
                                + "\n2. (undated): Algebra revised", false);
                clickStudent(root, 0);
                assertEquals("Lesson records for Amy (2):\n1. (undated): Geometry"
                        + "\n2. (undated): Algebra revised",
                        result.getText());
                assertFeedback(root, "findlesson 1 missing",
                        "No matching lesson records found for Amy.", false);
                assertFeedback(root, "findlesson 1", String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                        FindLessonCommand.MESSAGE_USAGE), true);
                assertFeedback(root, "findlesson 1 from/02/01/2020 algebra",
                        FindLessonCommand.MESSAGE_KEYWORDS_AFTER_DATE, true);
                assertFeedback(root, "findlesson 1 to/04/01/2020 algebra",
                        FindLessonCommand.MESSAGE_KEYWORDS_AFTER_DATE, true);
                assertFeedback(root, "findlesson 1 from/31/02/2020 algebra",
                        Lesson.MESSAGE_DATE_CONSTRAINTS, true);
                assertEquals(1, list.getItems().size());
                assertFeedback(root, "findlesson 1 alg",
                        "Matching lesson records for Amy (1):\n2. (undated): Algebra revised", false);
                enterClickTestCommand(root, "list");
                assertFeedback(root, "findlesson 2 alg", "No lessons logged yet for Ben.", false);
                assertEquals(1, list.getSelectionModel().getSelectedIndex());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_longLessonSearch_wrapsAndResetsScroll(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Person amy = model.getFilteredPersonList().getFirst();
            String content = "Algebra revision ".repeat(15).trim();
            String[] records = new String[40];
            Arrays.fill(records, content);
            model.setPerson(amy, new PersonBuilder(amy).withLessons(records).build());
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                StringBuilder expected = new StringBuilder("Matching lesson records for Amy (40):");
                for (int number = 1; number <= 40; number++) {
                    expected.append("\n").append(number).append(". (undated): ").append(content);
                }
                for (double width : new double[] {450, 900}) {
                    root.resize(width, 560);
                    root.layout();
                    assertFeedback(root, "findlesson 1 alg", expected.toString(), false);
                    assertTrue(result.isWrapText());
                    result.positionCaret(result.getLength());
                    result.setScrollTop(Double.MAX_VALUE);
                    root.layout();
                    assertTrue(result.getScrollTop() > 0);
                    assertFeedback(root, "findlesson 1 alg", expected.toString(), false);
                    assertEquals(0, result.getScrollTop());
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_summaryAndLessonChanges_refreshStatusWithoutChangingFilter(@TempDir Path directory)
            throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                assertEquals("3 lessons (undated)", label(cellAt(list, 0), "lessonStatus").getText());
                enterClickTestCommand(root, "flag 2 r/Call parent");
                enterClickTestCommand(root, "flagged");
                assertFeedback(root, "summary", "Summary:\nYou have 2 students and 3 lesson records in total. "
                        + "1 student has no lessons recorded.", false);
                assertEquals(1, list.getItems().size());
                assertEquals("Ben", label(cellAt(list, 0), "name").getText());
                assertEquals("No lessons yet", label(cellAt(list, 0), "lessonStatus").getText());

                enterClickTestCommand(root, "log 1 c/Vectors date/2/1/2020");
                assertEquals("1 lesson, last 02/01/2020", label(cellAt(list, 0), "lessonStatus").getText());
                enterClickTestCommand(root, "log 1 c/Algebra date/1/1/2020");
                assertEquals("2 lessons, last 02/01/2020", label(cellAt(list, 0), "lessonStatus").getText());
                enterClickTestCommand(root, "editlesson 1 1 date/31/12/2019");
                assertEquals("2 lessons, last 01/01/2020", label(cellAt(list, 0), "lessonStatus").getText());
                enterClickTestCommand(root, "deletelesson 1 1");
                assertEquals("1 lesson, last 31/12/2019", label(cellAt(list, 0), "lessonStatus").getText());
                enterClickTestCommand(root, "deletelesson 1 1");
                assertEquals("No lessons yet", label(cellAt(list, 0), "lessonStatus").getText());
                assertTrue(label(cellAt(list, 0), "flaggedBadge").isVisible());
                assertEquals("Flag reason: Call parent", label(cellAt(list, 0), "flagReason").getText());
                assertFeedback(root, "summary", "Summary:\nYou have 2 students and 3 lesson records in total. "
                        + "1 student has no lessons recorded.", false);
                assertEquals(1, list.getItems().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void studentCard_lessonStatusAndFlag_fitAtSupportedLogicalWidths() throws Exception {
        runOnJavaFxThread(() -> {
            Person student = new PersonBuilder(CHARLOTTE).withFlag("Follow up ".repeat(10))
                    .withLessons("Algebra", "Geometry").build();
            ListView<?> list = createList(FXCollections.observableArrayList(student, SHORT_PERSON));
            for (double width : new double[] {400, 1280 / 1.5, 1920 / 1.25, 1920}) {
                list.getSelectionModel().select(0);
                layout(list, width);
                ListCell<?> cell = cellAt(list, 0);
                cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
                list.applyCss();
                list.layout();
                assertEquals("2 lessons (undated)", label(cell, "lessonStatus").getText());
                assertTrue(label(cell, "lessonStatus").isWrapText());
                assertTrue(label(cell, "flaggedBadge").isVisible());
                Bounds reason = label(cell, "flagReason").localToScene(label(cell, "flagReason").getBoundsInLocal());
                Bounds phone = label(cell, "phone").localToScene(label(cell, "phone").getBoundsInLocal());
                assertTrue(reason.getMaxY() <= phone.getMinY() + LAYOUT_TOLERANCE);
                assertVisibleCardsFit(list);
                cell.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
            }
        });
    }

    @Test
    public void helpWindow_pointsToUserGuide() throws Exception {
        runOnJavaFxThread(() -> {
            HelpWindow help = new HelpWindow();
            Label message = (Label) help.getRoot().getScene().lookup("#helpMessage");
            assertEquals("Refer to the user guide: " + HelpWindow.USERGUIDE_URL, message.getText());
            assertNotNull(help.getRoot().getScene().lookup("#copyButton"));
            help.hide();
        });
    }

    @Test
    public void commandBox_saveUnavailable_filterHelpAndExitStillWork(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("students.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                    throw new AssertionError("Read-only commands must not attempt a save");
                }
            };
            MainWindow window = createClickableWindow(clickTestLogic(clickTestModel(), storage, directory), directory);
            Stage stage = window.getPrimaryStage();
            stage.show();
            try {
                Region root = (Region) stage.getScene().getRoot();
                assertFeedback(root, "find ben", "Found 1 student.", false);
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                assertEquals(1, list.getItems().size());
                assertEquals("Ben", label(cellAt(list, 0), "name").getText());
                assertFeedback(root, "help", HelpCommand.SHOWING_HELP_MESSAGE, false);
                assertTrue(Window.getWindows().stream().anyMatch(candidate -> candidate instanceof Stage help
                        && help.getTitle().equals("Help") && help.isShowing()));
                assertEquals("Ben", label(cellAt(list, 0), "name").getText());
                enterClickTestCommand(root, "exit");
                assertFalse(stage.isShowing());
                assertFalse(Window.getWindows().stream().anyMatch(candidate -> candidate instanceof Stage help
                        && help.getTitle().equals("Help") && help.isShowing()));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void start_recoveryWarning_showsAfterWindowIsReady() throws Exception {
        runOnJavaFxThread(() -> {
            Path file = temporaryFolder.resolve("students.json");
            Logic logic = new LogicManager(new ModelManager(), new StorageManager(new JsonAddressBookStorage(file),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            List<String> shownWarning = new ArrayList<>();
            UiManager ui = new UiManager(logic, file, "Recovery instructions") {
                @Override
                void showAlertDialogAndWait(AlertType type, String title, String header, String content) {
                    shownWarning.addAll(List.of(type.name(), title, header, content,
                            Boolean.toString(stage.isShowing())));
                }
            };
            try {
                ui.start(stage);
                assertEquals(List.of("WARNING", "Student data could not be loaded",
                        "Saving is disabled for this session", "Recovery instructions", "true"), shownWarning);
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void start_normalSession_doesNotShowRecoveryWarning() throws Exception {
        runOnJavaFxThread(() -> {
            Path file = temporaryFolder.resolve("students.json");
            Logic logic = new LogicManager(new ModelManager(), new StorageManager(new JsonAddressBookStorage(file),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            List<String> warnings = new ArrayList<>();
            UiManager ui = new UiManager(logic, file) {
                @Override
                void showAlertDialogAndWait(AlertType type, String title, String header, String content) {
                    warnings.add(content);
                }
            };
            try {
                ui.start(stage);
                assertTrue(stage.isShowing());
                assertTrue(warnings.isEmpty());
            } finally {
                stage.hide();
            }
        });
    }

    /**
     * Starts JavaFX and waits until its application thread is ready.
     *
     * @throws InterruptedException if interrupted while waiting for startup
     */
    @BeforeAll
    static void initialiseJavaFxToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            started.countDown();
        });
        assertTrue(started.await(FX_THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Timed out waiting for JavaFX startup to complete");
        isJavaFxStarted = true;
    }

    /**
     * Stops JavaFX only if startup succeeded.
     */
    @AfterAll
    static void shutDownJavaFxToolkit() {
        if (isJavaFxStarted) {
            Platform.exit();
        }
    }

    @Test
    public void studentCard_primaryClicksOnly_reportsCurrentDisplayedIndex() throws Exception {
        runOnJavaFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(SHORT_PERSON, CHARLOTTE);
            List<Index> clicked = new ArrayList<>();
            PersonListPanel panel = new PersonListPanel(persons, clicked::add);
            StackPane root = new StackPane(panel.getRoot());
            new Scene(root, 450, 450);
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            layout(list, 450);
            clickNode(label(cellAt(list, 1), "name"), MouseButton.PRIMARY);
            assertEquals(List.of(Index.fromOneBased(2)), clicked);

            clickNode(cellAt(list, 1), MouseButton.SECONDARY);
            clickNode(cellAt(list, 1), MouseButton.MIDDLE);
            ListCell<?> emptyCell = list.lookupAll(".list-cell").stream()
                    .filter(node -> node instanceof ListCell<?> cell && cell.isEmpty())
                    .map(node -> (ListCell<?>) node).findFirst().orElseThrow();
            clickNode(emptyCell, MouseButton.PRIMARY);
            list.getSelectionModel().select(0);
            persons.remove(0);
            layout(list, 450);
            assertEquals(List.of(Index.fromOneBased(2)), clicked);

            clickNode(label(cellAt(list, 0), "address"), MouseButton.PRIMARY);
            clickNode(cellAt(list, 0), MouseButton.PRIMARY);
            assertEquals(List.of(Index.fromOneBased(2), Index.fromOneBased(1), Index.fromOneBased(1)), clicked);
        });
    }

    @Test
    public void studentCard_scrollAndReuse_reportsCurrentIndex() throws Exception {
        runOnJavaFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList();
            for (int index = 0; index < 48; index++) {
                persons.add(index % 2 == 0 ? SHORT_PERSON : CHARLOTTE);
            }
            List<Index> clicked = new ArrayList<>();
            PersonListPanel panel = new PersonListPanel(persons, clicked::add);
            StackPane root = new StackPane(panel.getRoot());
            new Scene(root, 450, 450);
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            layout(list, 450);
            panel.selectPerson(Index.fromOneBased(45));
            layout(list, 450);
            assertEquals(44, list.getSelectionModel().getSelectedIndex());
            assertTrue(clicked.isEmpty());
            clickNode(label(cellAt(list, 44), "name"), MouseButton.PRIMARY);
            list.scrollTo(0);
            layout(list, 450);
            clickNode(label(cellAt(list, 0), "name"), MouseButton.PRIMARY);
            assertEquals(List.of(Index.fromOneBased(45), Index.fromOneBased(1)), clicked);
        });
    }

    @Test
    public void commandBox_findThenClickAndList_usesDisplayedStudent(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                enterClickTestCommand(root, "find BE");
                assertEquals("Found 1 student.", result.getText());
                assertEquals("Ben", label(cellAt(list, 0), "name").getText());
                assertEquals("1. ", label(cellAt(list, 0), "id").getText());
                clickStudent(root, 0);
                assertEquals("No lessons logged yet for Ben.", result.getText());
                enterClickTestCommand(root, "view 1");
                assertEquals("No lessons logged yet for Ben.", result.getText());
                enterClickTestCommand(root, "list");
                assertEquals("Listed 2 students.", result.getText());
                assertEquals("Amy", label(cellAt(list, 0), "name").getText());
                assertEquals("Ben", label(cellAt(list, 1), "name").getText());
                assertEquals("2. ", label(cellAt(list, 1), "id").getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_findNoMatches_showsEmptyFilterGuidance(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                enterClickTestCommand(root, "find unknown");
                assertFalse(list.isVisible());
                VBox emptyState = (VBox) root.lookup("#emptyState");
                assertTrue(emptyState.isVisible());
                assertEmptyStateGuidance(emptyState);
                assertEquals(2, model.getAddressBook().getPersonList().size());
                enterClickTestCommand(root, "list");
                assertTrue(list.isVisible());
                assertFalse(emptyState.isVisible());
                assertEquals(2, list.getItems().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_noSavedStudents_showsAddGuidanceAfterList(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = new ModelManager();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                VBox emptyState = (VBox) root.lookup("#emptyState");
                assertTrue(emptyState.isVisible());
                assertEmptyStateGuidance(emptyState);
                enterClickTestCommand(root, "list");
                assertTrue(model.getAddressBook().getPersonList().isEmpty());
                assertTrue(emptyState.isVisible());
                assertEmptyStateGuidance(emptyState);
                enterClickTestCommand(root, "add n/Alice Tan p/81234567 a/10 Short Street l/Sec 2 sub/E-Math");
                assertEquals(1, model.getAddressBook().getPersonList().size());
                assertFalse(emptyState.isVisible());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private static void assertEmptyStateGuidance(VBox emptyState) {
        List<String> instructions = emptyState.getChildren().stream()
                .map(node -> ((Label) node).getText()).toList();
        assertTrue(instructions.contains("No students to display."));
        assertTrue(instructions.contains("Type list to show all students, or add one:"));
        assertTrue(instructions.contains("add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT"));
        assertTrue(instructions.contains("add n/Alice Tan p/81234567 a/10 Short Street l/Sec 2 sub/E-Math"));
    }

    @Test
    public void studentCard_flaggedBadgeAndReason_followFlagState() throws Exception {
        runOnJavaFxThread(() -> {
            Person withoutReason = new PersonBuilder(SHORT_PERSON).withFlag().build();
            Person withReason = new PersonBuilder(CHARLOTTE)
                    .withFlag("Missed homework twice; call parent before the next lesson").build();
            ListView<?> list = createList(FXCollections.observableArrayList(SHORT_PERSON, withoutReason, withReason));
            layout(list, 400);

            Label unflaggedBadge = label(cellAt(list, 0), "flaggedBadge");
            Label unflaggedReason = label(cellAt(list, 0), "flagReason");
            assertFalse(unflaggedBadge.isVisible());
            assertFalse(unflaggedBadge.isManaged());
            assertFalse(unflaggedReason.isVisible());
            assertFalse(unflaggedReason.isManaged());

            assertEquals("FLAGGED", label(cellAt(list, 1), "flaggedBadge").getText());
            assertTrue(label(cellAt(list, 1), "flaggedBadge").isVisible());
            assertFalse(label(cellAt(list, 1), "flagReason").isManaged());

            Label reason = label(cellAt(list, 2), "flagReason");
            assertEquals("Flag reason: Missed homework twice; call parent before the next lesson", reason.getText());
            assertTrue(reason.isVisible());
            assertTrue(reason.isManaged());
            assertTrue(reason.isWrapText());
            assertVisibleCardsFit(list);
        });
    }

    @Test
    public void commandBox_flagFilterAndUnflag_updatesCardsAndDisplayedList(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                enterClickTestCommand(root, "flag 2 r/Call parent");
                assertTrue(label(cellAt(list, 1), "flaggedBadge").isVisible());
                assertEquals("Flag reason: Call parent", label(cellAt(list, 1), "flagReason").getText());

                enterClickTestCommand(root, "flagged");
                assertEquals(1, list.getItems().size());
                assertEquals("Ben", label(cellAt(list, 0), "name").getText());
                assertEquals("1. ", label(cellAt(list, 0), "id").getText());

                enterClickTestCommand(root, "unflag 1");
                assertTrue(model.getFilteredPersonList().isEmpty());
                assertTrue(root.lookup("#emptyState").isVisible());
                enterClickTestCommand(root, "list");
                assertEquals(2, list.getItems().size());
                assertFalse(label(cellAt(list, 1), "flaggedBadge").isVisible());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_viewMovesHighlight_retainsSelectionOnError(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                enterClickTestCommand(root, "view 2");
                assertEquals(1, list.getSelectionModel().getSelectedIndex());
                enterClickTestCommand(root, "view 1");
                assertEquals(0, list.getSelectionModel().getSelectedIndex());
                enterClickTestCommand(root, "view 999");
                assertEquals(0, list.getSelectionModel().getSelectedIndex());
                enterClickTestCommand(root, "view 2");
                assertEquals(1, list.getSelectionModel().getSelectedIndex());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    /**
     * Deletes through the command box and verifies that typed views and card clicks show the updated history.
     */
    @Test
    public void commandBox_deleteLesson_updatesHistoryWithoutRemovingStudent(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                enterClickTestCommand(root, "view 1");
                enterClickTestCommand(root, "deletelesson 1 1");
                assertEquals("Deleted lesson 1 for Amy: Algebra", result.getText());
                String historyAfterDelete = "Lesson records for Amy (2):\n1. (undated): Geometry"
                        + "\n2. (undated): Algebra";
                enterClickTestCommand(root, "view 1");
                assertEquals(historyAfterDelete, result.getText());
                clickStudent(root, 0);
                assertEquals(historyAfterDelete, result.getText());
                enterClickTestCommand(root, "deletelesson 1 3");
                assertEquals(Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX, result.getText());
                enterClickTestCommand(root, "deletelesson 1 2");
                enterClickTestCommand(root, "deletelesson 1 1");
                assertFeedback(root, "deletelesson 1 1", "No lessons logged yet for Amy.", true);
                clickStudent(root, 0);
                assertEquals("No lessons logged yet for Amy.", result.getText());
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                assertEquals(2, list.getItems().size());
                assertTrue(list.isVisible());
                assertFalse(root.lookup("#emptyState").isVisible());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void studentCard_firstPressAfterView_showsAndHighlightsStudent(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                ListView<?> list = (ListView<?>) root.lookup("#personListView");
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                enterClickTestCommand(root, "view 2");
                TextField input = (TextField) root.lookup("#commandTextField");
                input.requestFocus();
                label(cellAt(list, 0), "name").fireEvent(mouseEvent(MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY));
                root.applyCss();
                root.layout();
                assertEquals(AMY_FIXTURE_HISTORY, result.getText());
                assertEquals(0, list.getSelectionModel().getSelectedIndex());
                enterClickTestCommand(root, "view 1");
                label(cellAt(list, 1), "name").fireEvent(mouseEvent(MouseEvent.MOUSE_PRESSED, MouseButton.PRIMARY));
                root.layout();
                assertEquals("No lessons logged yet for Ben.", result.getText());
                assertEquals(1, list.getSelectionModel().getSelectedIndex());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void studentCard_clickAndView_showsSameHistoryAndKeepsCommandText(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                TextField input = (TextField) root.lookup("#commandTextField");
                String amyHistory = AMY_FIXTURE_HISTORY;
                List<Person> beforeClick = List.copyOf(logic.getFilteredPersonList());
                clickStudent(root, 0);
                assertEquals(amyHistory, result.getText());
                assertEquals(beforeClick, logic.getFilteredPersonList());
                enterClickTestCommand(root, "view 1");
                assertEquals(amyHistory, result.getText());
                clickStudent(root, 1);
                assertEquals("No lessons logged yet for Ben.", result.getText());
                enterClickTestCommand(root, "log 2 c/Calculus");
                assertEquals("Logged lesson for Ben on " + todayText() + ": Calculus", result.getText());
                input.setText("log 2 c/");
                clickStudent(root, 1);
                assertEquals("Lesson records for Ben (1):\n1. " + todayText() + ": Calculus",
                        result.getText());
                assertEquals("log 2 c/", input.getText());
                clickStudent(root, 0);
                assertEquals(amyHistory, result.getText());
                assertEquals("log 2 c/", input.getText());
                enterClickTestCommand(root, "view 999");
                clickStudent(root, 0);
                assertEquals(amyHistory, result.getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void studentCard_afterDeleteAndFilter_viewsCurrentStudent(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                model.updateFilteredPersonList(person -> person.getName().fullName.equals("Ben"));
                root.layout();
                clickStudent(root, 0);
                assertEquals("No lessons logged yet for Ben.", result.getText());
                enterClickTestCommand(root, "list");
                clickStudent(root, 0);
                enterClickTestCommand(root, "delete 1");
                assertTrue(result.getText().startsWith("Deleted student: Amy;"));
                clickStudent(root, 0);
                assertEquals("No lessons logged yet for Ben.", result.getText());
                enterClickTestCommand(root, "add n/Carol p/81234569 a/Test Address l/JC1 sub/H2 Math");
                assertTrue(result.getText().startsWith("New student added: Carol;"));
                clickStudent(root, 1);
                assertEquals("No lessons logged yet for Carol.", result.getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void studentCard_saveUnavailable_viewsHistoryButRejectsMutation(@TempDir Path directory) throws Exception {
        runOnJavaFxThread(() -> {
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(directory.resolve("students.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    throw new IOException("disk unavailable");
                }
            };
            MainWindow window = createClickableWindow(clickTestLogic(clickTestModel(), failingStorage, directory),
                    directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                clickStudent(root, 0);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                assertEquals(AMY_FIXTURE_HISTORY, result.getText());
                assertFeedback(root, "log 1 c/Trigonometry",
                        String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "disk unavailable"), true);
                clickStudent(root, 0);
                assertEquals(AMY_FIXTURE_HISTORY, result.getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    /**
     * Creates distinct test students with repeated lessons for Amy and no lessons for Ben.
     */
    private static ModelManager clickTestModel() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Amy").withPhone("81234567")
                .withLessons("Algebra", "Geometry", "Algebra").build());
        model.addPerson(new PersonBuilder().withName("Ben").withPhone("81234568").build());
        return model;
    }

    /**
     * Creates real logic with isolated student and preference storage for click tests.
     */
    private static Logic clickTestLogic(ModelManager model, JsonAddressBookStorage storage, Path directory) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
    }

    /**
     * Loads the real main window and settles its controls before clicking cards.
     */
    private static MainWindow createClickableWindow(Logic logic, Path directory) {
        MainWindow window = new MainWindow(new Stage(), logic, directory.resolve("students.json"));
        window.fillInnerParts();
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.resize(450, 560);
        root.applyCss();
        root.layout();
        return window;
    }

    /**
     * Fires a mouse click on a card's child label to exercise event bubbling into the list cell.
     */
    private static void clickStudent(Region root, int index) {
        ListView<?> list = (ListView<?>) root.lookup("#personListView");
        clickNode(label(cellAt(list, index), "name"), MouseButton.PRIMARY);
        root.applyCss();
        root.layout();
    }

    /**
     * Sends a complete mouse gesture and verifies that one press is sufficient to trigger the action.
     */
    private static void clickNode(Node node, MouseButton button) {
        for (EventType<MouseEvent> type : List.of(MouseEvent.MOUSE_PRESSED,
                MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED)) {
            node.fireEvent(mouseEvent(type, button));
        }
    }

    /**
     * Creates mouse events with button state matching the press or release phase.
     */
    private static MouseEvent mouseEvent(EventType<MouseEvent> type, MouseButton button) {
        boolean pressed = type == MouseEvent.MOUSE_PRESSED;
        return new MouseEvent(type, 1, 1, 1, 1, button, 1, false, false, false, false,
                pressed && button == MouseButton.PRIMARY, pressed && button == MouseButton.MIDDLE,
                pressed && button == MouseButton.SECONDARY, false, false, true, null);
    }

    /**
     * Submits a typed command through the command box before checking subsequent clicks.
     */
    private static void enterClickTestCommand(Region root, String command) {
        TextField input = (TextField) root.lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        root.applyCss();
        root.layout();
    }

    /**
     * Verifies that a wrapped address grows its row and that widening restores the compact height.
     */
    @Test
    public void longAddress_resizeNarrowThenWide_cardGrowsAndShrinksWithoutGaps() throws Exception {
        runOnJavaFxThread(() -> {
            ListView<?> list = createList(FXCollections.observableArrayList(SHORT_PERSON, CHARLOTTE, SHORT_PERSON));
            layout(list, 740);
            double wideHeight = cellAt(list, 1).getHeight();
            double shortHeight = cellAt(list, 0).getHeight();
            assertBottomPadding(cellAt(list, 1));
            assertVisibleCardsFit(list);

            layout(list, 400);
            ListCell<?> wrappedCell = cellAt(list, 1);
            assertTrue(label(wrappedCell, "address").getHeight() > label(wrappedCell, "phone").getHeight(),
                    "The address must actually wrap, rather than extend beyond the viewport");
            assertTrue(wrappedCell.getHeight() > wideHeight);
            assertEquals(shortHeight, cellAt(list, 0).getHeight(), LAYOUT_TOLERANCE);
            assertVisibleCardsFit(list);

            layout(list, 740);
            assertEquals(wideHeight, cellAt(list, 1).getHeight(), LAYOUT_TOLERANCE,
                    "Widening must not retain the taller wrapped-row height");
            assertVisibleCardsFit(list);
        });
    }

    /**
     * Verifies that adding and removing students toggles both visibility and layout participation.
     */
    @Test
    public void personListChanges_emptyStateTracksListContents() throws Exception {
        runOnJavaFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList();
            PersonListPanel panel = new PersonListPanel(persons, index -> {});
            StackPane root = (StackPane) panel.getRoot();
            ListView<?> personListView = (ListView<?>) root.getChildren().get(0);
            VBox emptyState = (VBox) root.getChildren().get(1);

            assertTrue(emptyState.isVisible());
            assertTrue(emptyState.isManaged());
            assertFalse(personListView.isVisible());
            assertFalse(personListView.isManaged());

            persons.add(ALICE);

            assertFalse(emptyState.isVisible());
            assertFalse(emptyState.isManaged());
            assertTrue(personListView.isVisible());
            assertTrue(personListView.isManaged());

            persons.remove(ALICE);

            assertTrue(emptyState.isVisible());
            assertTrue(emptyState.isManaged());
            assertFalse(personListView.isVisible());
            assertFalse(personListView.isManaged());
        });
    }

    /**
     * Verifies maximum-length fields with and without address spaces through cell reuse and selection.
     */
    @Test
    public void boundaryFields_resizeScrollAndSelect_fullTextVisibleWithoutGaps() throws Exception {
        runOnJavaFxThread(() -> {
            for (String address : List.of("A".repeat(Address.MAX_LENGTH),
                    "Block 123 Sunset View ".repeat(10).substring(0, Address.MAX_LENGTH))) {
                Person boundaryPerson = new PersonBuilder().withName("N".repeat(Name.MAX_LENGTH))
                        .withAddress(address).withLevel("L".repeat(Level.MAX_LENGTH))
                        .withSubject("S".repeat(Subject.MAX_LENGTH)).build();
                ObservableList<Person> persons = FXCollections.observableArrayList();
                for (int index = 0; index < 48; index++) {
                    persons.add(index % 3 == 1 ? boundaryPerson : SHORT_PERSON);
                }
                ListView<?> list = createList(persons);
                for (double width : new double[] {740, 400, 1000, 400, 740}) {
                    for (int index : new int[] {0, 44, 0}) {
                        list.scrollTo(index);
                        list.getSelectionModel().select(index + 1);
                        layout(list, width);
                        assertVisibleCardsFit(list);
                    }
                }
            }
        });
    }

    /**
     * Verifies that additions and deletions update card contents, heights, and displayed indexes.
     */
    @Test
    public void persons_addAndDelete_visibleCardsAndIndexesRefresh() throws Exception {
        runOnJavaFxThread(() -> {
            ObservableList<Person> persons = FXCollections.observableArrayList(SHORT_PERSON, CHARLOTTE);
            ListView<?> list = createList(persons);
            layout(list, 400);
            assertVisibleCardsFit(list);

            persons.add(0, SHORT_PERSON);
            layout(list, 400);
            assertEquals("Charlotte O'Brien", label(cellAt(list, 2), "name").getText());
            assertVisibleCardsFit(list);

            persons.remove(0);
            persons.remove(0);
            layout(list, 400);
            assertEquals("1. ", label(cellAt(list, 0), "id").getText());
            assertVisibleCardsFit(list);

            persons.clear();
            layout(list, 400);
            assertTrue(visibleCells(list).isEmpty());
        });
    }

    @Test
    public void mainWindow_resizeDivider_preservesCommandAndStudentAreas() throws Exception {
        runOnJavaFxThread(() -> {
            Path data = Path.of("unused-layout-test.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                    new JsonUserPrefsStorage(data));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, new LogicManager(new ModelManager(), storage), data);
            window.fillInnerParts();
            Region root = (Region) stage.getScene().getRoot();
            root.applyCss();
            SplitPane split = (SplitPane) root.lookup(".split-pane");
            Region result = (Region) root.lookup("#resultDisplayPlaceholder");
            Region students = (Region) root.lookup("#personList");
            Region command = (Region) root.lookup("#commandBoxPlaceholder");
            for (double width : new double[] {450, 900}) {
                root.resize(width, 560);
                root.layout();
                split.setDividerPositions(0.1);
                root.layout();
                double smallHeight = result.getHeight();
                split.setDividerPositions(0.9);
                root.layout();
                assertTrue(result.getHeight() > smallHeight);
                assertTrue(result.getHeight() >= 100);
                assertTrue(students.getHeight() >= 180);
                assertTrue(students.getWidth() >= 340);
                assertTrue(command.getHeight() > 0);
                assertTrue(command.getBoundsInParent().getMaxY() <= split.getLayoutY());
            }
            stage.close();
        });
    }

    @Test
    public void resultDisplay_longHistoryThenNewFeedback_resetsScrollAndKeepsText() throws Exception {
        runOnJavaFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            StackPane root = new StackPane(display.getRoot());
            new Scene(root, 400, 120);
            TextArea text = (TextArea) root.lookup("#resultDisplay");
            String history = "Lesson records for Amy (40):\n" + "Long lesson content ".repeat(600);
            display.setFeedbackToUser(history);
            root.applyCss();
            root.layout();
            assertTrue(text.isWrapText());
            assertFalse(text.isEditable());
            text.positionCaret(history.length());
            text.setScrollTop(500);
            root.layout();
            assertTrue(text.getScrollTop() > 0, "The history must be scrolled before testing the reset");
            display.setFeedbackToUser(history);
            root.layout();
            assertEquals(history, text.getText());
            assertEquals(0, text.getCaretPosition());
            assertEquals(0, text.getScrollTop());
            display.setFeedbackToUser("The student index provided is invalid.");
            root.layout();
            assertEquals("The student index provided is invalid.", text.getText());
            assertEquals(0, text.getScrollTop());
        });
    }

    @Test
    public void commandBox_viewSequences_displaysLatestHistoryAndErrors() throws Exception {
        runOnJavaFxThread(() -> {
            MainWindow window = createLessonWindow();
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                assertFeedback(root, "view 1", "No lessons logged yet for Amy.", false);
                String today = todayText();
                String loggedForAmy = "Logged lesson for Amy on " + today + ": ";
                assertFeedback(root, "log 1 c/Algebra", loggedForAmy + "Algebra", false);
                assertFeedback(root, "view 1", "Lesson records for Amy (1):\n1. " + today + ": Algebra",
                        false);
                assertFeedback(root, "log 1 c/Geometry", loggedForAmy + "Geometry", false);
                assertFeedback(root, "log 1 c/Algebra", loggedForAmy + "Algebra", false);
                String amyHistory = "Lesson records for Amy (3):\n1. " + today + ": Algebra\n2. " + today
                        + ": Geometry\n3. " + today + ": Algebra";
                assertFeedback(root, "view 1", amyHistory, false);
                assertFeedback(root, "view 2", "No lessons logged yet for Ben.", false);
                assertFeedback(root, "log 2 c/Calculus", "Logged lesson for Ben on " + today + ": Calculus",
                        false);
                String benHistory = "Lesson records for Ben (1):\n1. " + today + ": Calculus";
                assertFeedback(root, "view 2", benHistory, false);
                assertFeedback(root, "view 1", amyHistory, false);
                assertFeedback(root, "view 3", Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, true);
                assertFeedback(root, "view abc", String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                        ViewCommand.MESSAGE_USAGE), true);
                assertFeedback(root, "log 1 c/", Lesson.MESSAGE_CONSTRAINTS, true);
                assertFeedback(root, "view 1", amyHistory, false);
                submitCommand(root, "delete 1");
                assertFeedback(root, "view 1", benHistory, false);
                assertFeedback(root, "view 2", Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, true);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_unicodeBlankFields_showsErrorsWithoutChangingStudents(@TempDir Path directory)
            throws Exception {
        runOnJavaFxThread(() -> {
            ModelManager model = clickTestModel();
            List<Person> before = List.copyOf(model.getAddressBook().getPersonList());
            Logic logic = clickTestLogic(model,
                    new JsonAddressBookStorage(directory.resolve("students.json")), directory);
            MainWindow window = createClickableWindow(logic, directory);
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                clickStudent(root, 0);
                String historyBefore = result.getText();
                for (String blank : List.of("\u2003", "\u3000", "\u00a0", "\u200b", "\u2060", "\ufeff",
                        " \u2003\u200b\u2060 ")) {
                    assertFeedback(root, "add n/Bea Tan p/81234569 a/" + blank + " l/Sec 2 sub/E-Math",
                            Address.MESSAGE_CONSTRAINTS, true);
                    assertFeedback(root, "edit 1 a/" + blank, Address.MESSAGE_CONSTRAINTS, true);
                    assertFeedback(root, "log 1 c/" + blank, Lesson.MESSAGE_CONSTRAINTS, true);
                    assertEquals(before, model.getAddressBook().getPersonList());
                }
                clickStudent(root, 0);
                assertEquals(historyBefore, result.getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void commandBox_longHistoryAndStudentSwitch_resetsScrollWithoutLosingRecords() throws Exception {
        runOnJavaFxThread(() -> {
            MainWindow window = createLessonWindow();
            try {
                Region root = (Region) window.getPrimaryStage().getScene().getRoot();
                String boundaryContent = "A".repeat(Lesson.MAX_LENGTH);
                submitCommand(root, "log 1 c/" + boundaryContent);
                for (int index = 2; index <= 40; index++) {
                    submitCommand(root, "log 1 c/Topic " + index);
                }
                String today = todayText();
                StringBuilder expected = new StringBuilder("Lesson records for Amy (40):");
                for (int index = 40; index >= 2; index--) {
                    expected.append("\n").append(41 - index).append(". ").append(today)
                            .append(": Topic ").append(index);
                }
                expected.append("\n40. ").append(today).append(": ").append(boundaryContent);
                TextArea result = (TextArea) root.lookup("#resultDisplay");
                SplitPane split = (SplitPane) root.lookup(".split-pane");
                for (double width : new double[] {450, 900}) {
                    root.resize(width, 560);
                    root.layout();
                    for (double dividerPosition : new double[] {0.1, 0.9}) {
                        split.setDividerPositions(dividerPosition);
                        root.layout();
                        assertFeedback(root, "view 1", expected.toString(), false);
                        assertTrue(result.isWrapText());
                        assertEquals(0, result.getScrollTop());
                        result.positionCaret(result.getLength());
                        result.setScrollTop(Double.MAX_VALUE);
                        root.layout();
                        assertTrue(result.getScrollTop() > 0, "Older records must be reachable by scrolling");
                        assertFeedback(root, "view 2", "No lessons logged yet for Ben.", false);
                        assertEquals(0, result.getScrollTop());
                        assertFeedback(root, "view 1", expected.toString(), false);
                        assertEquals(0, result.getScrollTop());
                    }
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    /**
     * Creates a main window with isolated storage and two students without lessons.
     */
    private MainWindow createLessonWindow() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Amy").withPhone("81234567").build());
        model.addPerson(new PersonBuilder().withName("Ben").withPhone("81234568").build());
        Path data = temporaryFolder.resolve("students.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), data);
        window.fillInnerParts();
        Region root = (Region) window.getPrimaryStage().getScene().getRoot();
        root.resize(450, 560);
        root.applyCss();
        root.layout();
        return window;
    }

    /**
     * Sends input through the command box's Enter handler and settles the real scene layout.
     */
    private static void submitCommand(Region root, String command) {
        TextField input = (TextField) root.lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
        root.applyCss();
        root.layout();
    }

    /**
     * Checks the displayed feedback and command-box error state after a command.
     */
    private static void assertFeedback(Region root, String command, String expected, boolean isError) {
        submitCommand(root, command);
        TextArea result = (TextArea) root.lookup("#resultDisplay");
        TextField input = (TextField) root.lookup("#commandTextField");
        assertEquals(expected, result.getText());
        assertEquals(isError, input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        assertEquals(isError ? command : "", input.getText());
    }

    /**
     * Creates the real list panel in a scene using the application's stylesheets.
     */
    private static ListView<?> createList(ObservableList<Person> persons) {
        PersonListPanel panel = new PersonListPanel(persons, index -> {});
        StackPane root = new StackPane(panel.getRoot());
        Scene scene = new Scene(root, 740, 450);
        scene.getStylesheets().addAll(PersonListPanel.class.getResource("/view/DarkTheme.css").toExternalForm(),
                PersonListPanel.class.getResource("/view/Extensions.css").toExternalForm());
        return (ListView<?>) root.lookup("#personListView");
    }

    /**
     * Applies CSS and settles layout after changing the scene's available width.
     */
    private static void layout(ListView<?> list, double width) {
        Region root = (Region) list.getScene().getRoot();
        root.resize(width, 450);
        for (int pass = 0; pass < 4; pass++) {
            root.applyCss();
            root.layout();
        }
    }

    /**
     * Returns populated visible cells, excluding the skin's hidden measurement cells.
     */
    private static List<ListCell<?>> visibleCells(ListView<?> list) {
        List<ListCell<?>> cells = new ArrayList<>();
        for (Node node : list.lookupAll(".list-cell")) {
            if (node instanceof ListCell<?> cell && cell.isVisible() && !cell.isEmpty()
                    && cell.getIndex() >= 0 && cell.getIndex() < list.getItems().size()) {
                cells.add(cell);
            }
        }
        return cells;
    }

    /**
     * Returns a rendered cell with the given index, failing if the test did not make it visible.
     */
    private static ListCell<?> cellAt(ListView<?> list, int index) {
        return visibleCells(list).stream().filter(cell -> cell.getIndex() == index).findFirst().orElseThrow();
    }

    /**
     * Returns a label from the card displayed by a list cell.
     */
    private static Label label(ListCell<?> cell, String id) {
        return (Label) cell.getGraphic().lookup("#" + id);
    }

    /**
     * Checks rendered text, field bounds, row height, bottom padding, and indexes against the model.
     */
    private static void assertVisibleCardsFit(ListView<?> list) {
        List<ListCell<?>> cells = visibleCells(list);
        assertFalse(cells.isEmpty(), "The check must inspect rendered cells");
        for (ListCell<?> cell : cells) {
            Region card = (Region) cell.getGraphic();
            Person person = (Person) list.getItems().get(cell.getIndex());
            String[] ids = {"id", "name", "phone", "address", "level", "subject", "lessonStatus"};
            String[] expected = {cell.getIndex() + 1 + ". ", person.getName().fullName, person.getPhone().value,
                person.getAddress().value, person.getLevel().value, person.getSubject().value,
                Messages.formatLessonStatus(person)};
            double previousBottom = 0;
            for (int index = 0; index < ids.length; index++) {
                Label field = label(cell, ids[index]);
                Text renderedText = (Text) field.lookup(".text");
                assertNotNull(renderedText);
                assertEquals(expected[index], renderedText.getText(), "Clipped field: " + ids[index]);
                Bounds bounds = card.sceneToLocal(field.localToScene(field.getBoundsInLocal()));
                assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= card.getWidth() + LAYOUT_TOLERANCE);
                assertTrue(bounds.getMaxY() <= card.getHeight() + LAYOUT_TOLERANCE);
                assertTrue(renderedText.getLayoutBounds().getHeight() <= field.getHeight() + LAYOUT_TOLERANCE);
                if (index > 1) {
                    assertTrue(bounds.getMinY() >= previousBottom - LAYOUT_TOLERANCE, "Overlapping fields");
                }
                previousBottom = Math.max(previousBottom, bounds.getMaxY());
            }
            Bounds indexBounds = label(cell, "id").localToScene(label(cell, "id").getBoundsInLocal());
            Bounds nameBounds = label(cell, "name").localToScene(label(cell, "name").getBoundsInLocal());
            assertTrue(indexBounds.getMaxX() <= nameBounds.getMinX() + LAYOUT_TOLERANCE);
            assertEquals(card.prefHeight(card.getWidth()), cell.getHeight(), LAYOUT_TOLERANCE,
                    "Cell height must match the content at its rendered width");
            assertBottomPadding(cell);
            assertTrue(cell.getWidth() <= list.getWidth() + LAYOUT_TOLERANCE);
        }
    }

    /**
     * Verifies that the space below the lesson status contains only its container's intended bottom padding.
     */
    private static void assertBottomPadding(ListCell<?> cell) {
        Region card = (Region) cell.getGraphic();
        Label status = label(cell, "lessonStatus");
        Region content = (Region) status.getParent();
        double bottom = card.sceneToLocal(status.localToScene(status.getBoundsInLocal())).getMaxY();
        assertEquals(content.getInsets().getBottom(), cell.getHeight() - bottom, LAYOUT_TOLERANCE,
                "Space below the lesson status must be padding, not stale wrapping height");
    }

    /**
     * Runs layout assertions on the JavaFX thread and reports failures back to JUnit.
     *
     * @param action operation that accesses JavaFX controls
     * @throws InterruptedException if interrupted while waiting for the assertions
     */
    private static void runOnJavaFxThread(Runnable action) throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                completed.countDown();
            }
        });
        assertTrue(completed.await(FX_THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Timed out while waiting for the JavaFX application thread");

        Throwable thrown = failure.get();
        if (thrown instanceof AssertionError assertionError) {
            throw assertionError;
        }
        if (thrown != null) {
            throw new AssertionError(thrown);
        }
    }
}
