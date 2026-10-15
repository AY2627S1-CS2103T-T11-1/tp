package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
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
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Consumer<Index> onPersonClick;

    @FXML
    private ListView<Person> personListView;

    @FXML
    private VBox emptyState;

    /**
     * Creates a panel that passes a clicked student's displayed index to {@code onPersonClick}.
     */
    public PersonListPanel(ObservableList<Person> personList, Consumer<Index> onPersonClick) {
        super(FXML);
        this.onPersonClick = requireNonNull(onPersonClick);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());

        BooleanBinding isEmpty = Bindings.isEmpty(personList);
        emptyState.visibleProperty().bind(isEmpty);
        emptyState.managedProperty().bind(isEmpty);
        personListView.visibleProperty().bind(isEmpty.not());
        personListView.managedProperty().bind(isEmpty.not());
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
            setOnMouseClicked(event -> {
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
