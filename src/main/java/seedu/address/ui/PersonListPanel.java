package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /** Keys with which the list itself moves the selection to another student. */
    private static final Set<KeyCode> NAVIGATION_KEYS = Set.of(KeyCode.UP, KeyCode.DOWN,
            KeyCode.PAGE_UP, KeyCode.PAGE_DOWN, KeyCode.HOME, KeyCode.END);
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Consumer<Index> onPersonClick;

    @FXML
    private ListView<Person> personListView;

    @FXML
    private VBox emptyState;

    /**
     * Creates a panel that passes the displayed index of a student chosen by a click or by keyboard
     * navigation to {@code onPersonClick}.
     */
    public PersonListPanel(ObservableList<Person> personList, Consumer<Index> onPersonClick) {
        super(FXML);
        this.onPersonClick = requireNonNull(onPersonClick);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        // Selection moves on key press; reporting on release views the student it settled on.
        // Programmatic selection and list changes fire no key events, so they never run a command.
        personListView.addEventHandler(KeyEvent.KEY_RELEASED, this::handleNavigationKey);

        BooleanBinding isEmpty = Bindings.isEmpty(personList);
        emptyState.visibleProperty().bind(isEmpty);
        emptyState.managedProperty().bind(isEmpty);
        personListView.visibleProperty().bind(isEmpty.not());
        personListView.managedProperty().bind(isEmpty.not());
    }

    /**
     * Highlights and scrolls to the displayed student without moving focus from the command box.
     */
    public void selectPerson(Index index) {
        requireNonNull(index);
        personListView.getSelectionModel().clearAndSelect(index.getZeroBased());
        personListView.scrollTo(index.getZeroBased());
    }

    /**
     * Highlights the displayed student again if the highlight has moved or been cleared, for example
     * after a failed save restored the list. Unlike {@link #selectPerson}, an unchanged highlight is
     * left alone, so the list does not scroll.
     */
    public void restoreSelection(Index index) {
        requireNonNull(index);
        if (personListView.getSelectionModel().getSelectedIndex() != index.getZeroBased()) {
            selectPerson(index);
        }
    }

    private void handleNavigationKey(KeyEvent event) {
        int selectedIndex = personListView.getSelectionModel().getSelectedIndex();
        if (NAVIGATION_KEYS.contains(event.getCode()) && selectedIndex >= 0) {
            onPersonClick.accept(Index.fromZeroBased(selectedIndex));
        }
    }

    /**
     * Removes the highlight, for when the student shown in the detail panel is not in the displayed list.
     */
    public void clearSelection() {
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        /**
         * Creates a cell that fits the viewport instead of requesting the full unwrapped text width.
         */
        PersonListViewCell() {
            setPrefWidth(0);
            addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getButton() == MouseButton.PRIMARY && !isEmpty()) {
                    onPersonClick.accept(Index.fromZeroBased(getIndex()));
                }
            });
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }

}
