package seedu.address.ui;

import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import seedu.address.logic.Messages;
import seedu.address.model.person.Person;

/**
 * A compact UI component that identifies a {@code Person} in the student list.
 * Contact details, the flag reason and lessons are shown in the {@code StudentDetailPanel}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final int MAX_INITIALS = 2;

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Label avatar;
    @FXML
    private Label name;
    @FXML
    private Label levelSubject;
    @FXML
    private Label lessonStatus;
    @FXML
    private SVGPath flagIcon;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        avatar.setText(initialsOf(person.getName().fullName));
        name.setText(person.getName().fullName);
        levelSubject.setText(formatLevelAndSubject(person));
        lessonStatus.setText(Messages.formatLessonStatus(person));
        flagIcon.setVisible(person.isFlagged());
        flagIcon.setManaged(person.isFlagged());
    }

    /**
     * Returns up to two initials from the letters that start the words of {@code fullName}.
     * Words that do not start with a letter, such as "-", are skipped.
     */
    static String initialsOf(String fullName) {
        String initials = Arrays.stream(fullName.trim().split("\\s+"))
                .filter(word -> !word.isEmpty() && Character.isLetter(word.codePointAt(0)))
                .limit(MAX_INITIALS)
                .map(word -> new String(Character.toChars(word.codePointAt(0))))
                .collect(Collectors.joining());
        return initials.isEmpty() ? "?" : initials.toUpperCase();
    }

    /**
     * Returns the level and subject on one line, such as "Sec 3 · Physics".
     */
    static String formatLevelAndSubject(Person person) {
        return person.getLevel().value + " · " + person.getSubject().value;
    }
}
