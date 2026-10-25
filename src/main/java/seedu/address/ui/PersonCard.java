package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private VBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label flaggedBadge;
    @FXML
    private Label flagReason;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label level;
    @FXML
    private Label subject;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        flaggedBadge.setVisible(person.isFlagged());
        flaggedBadge.setManaged(person.isFlagged());
        person.getFlag().flatMap(flag -> flag.getReason()).ifPresentOrElse(reason -> {
            flagReason.setText("Flag reason: " + reason);
            flagReason.setVisible(true);
            flagReason.setManaged(true);
        }, () -> {
            flagReason.setVisible(false);
            flagReason.setManaged(false);
        });
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        level.setText(person.getLevel().value);
        subject.setText(person.getSubject().value);
    }
}
