package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * Tests that the person list panel switches between its list and empty states.
 */
public class PersonListPanelTest {

    private static final long FX_THREAD_TIMEOUT_SECONDS = 5;

    /**
     * Starts the JavaFX toolkit before tests create controls.
     */
    @BeforeAll
    static void initialiseJavaFxToolkit() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException exception) {
            // The toolkit may already be running when the test suite is executed.
        }
    }

    /**
     * Stops the JavaFX toolkit after all panel tests have completed.
     */
    @AfterAll
    static void shutDownJavaFxToolkit() {
        Platform.exit();
    }

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

            persons.clear();

            assertTrue(emptyState.isVisible());
            assertTrue(emptyState.isManaged());
            assertFalse(personListView.isVisible());
            assertFalse(personListView.isManaged());
        });
    }

    /**
     * Runs an assertion against JavaFX controls on the JavaFX application thread.
     *
     * @param action operation that accesses JavaFX controls
     * @throws InterruptedException if the current thread is interrupted while waiting
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
