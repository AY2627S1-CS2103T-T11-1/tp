package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
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
 * Verifies empty-state switching and student-card layout, including resizing and reused list cells.
 */
public class PersonListPanelTest {

    private static final long FX_THREAD_TIMEOUT_SECONDS = 5;
    private static final double LAYOUT_TOLERANCE = 1;
    private static final Person SHORT_PERSON = new PersonBuilder().withName("Alicia Tan")
            .withAddress("10 Short Street").withLevel("Sec 2").withSubject("E-Math").build();
    private static final Person CHARLOTTE = new PersonBuilder().withName("Charlotte O'Brien")
            .withPhone("81234568")
            .withAddress("Block 1234567890 Sunset View Avenue Unit 999999999999 Singapore 999999")
            .withLevel("Upper Secondary Two")
            .withSubject("Higher Order Thinking in Additional Mathematics").build();

    /** Records successful startup so cleanup never waits for a failed toolkit. */
    private static boolean isJavaFxStarted;

    /**
     * Starts JavaFX and waits until its application thread is ready.
     *
     * @throws InterruptedException if interrupted while waiting for startup
     */
    @BeforeAll
    static void initialiseJavaFxToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
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
            PersonListPanel panel = new PersonListPanel(persons);
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

    /**
     * Creates the real list panel in a scene using the application's stylesheets.
     */
    private static ListView<?> createList(ObservableList<Person> persons) {
        PersonListPanel panel = new PersonListPanel(persons);
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
            String[] ids = {"id", "name", "phone", "address", "level", "subject"};
            String[] expected = {cell.getIndex() + 1 + ". ", person.getName().fullName, person.getPhone().value,
                person.getAddress().value, person.getLevel().value, person.getSubject().value};
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
     * Verifies that the space below the subject contains only its container's intended bottom padding.
     */
    private static void assertBottomPadding(ListCell<?> cell) {
        Region card = (Region) cell.getGraphic();
        Label subject = label(cell, "subject");
        Region content = (Region) subject.getParent();
        double bottom = card.sceneToLocal(subject.localToScene(subject.getBoundsInLocal())).getMaxY();
        assertEquals(content.getInsets().getBottom(), cell.getHeight() - bottom, LAYOUT_TOLERANCE,
                "Space below the subject must be padding, not stale wrapping height");
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
