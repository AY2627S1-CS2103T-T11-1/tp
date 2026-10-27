package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    static final String ERROR_STYLE_CLASS = "result-error";

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates a read-only command result display.
     */
    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Shows the latest feedback from its beginning, including when the text is unchanged.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        showFeedback(feedbackToUser, false);
    }

    /**
     * Shows an error message in the error style until the next feedback replaces it.
     */
    public void setErrorToUser(String errorMessage) {
        showFeedback(errorMessage, true);
    }

    private void showFeedback(String feedbackToUser, boolean isError) {
        requireNonNull(feedbackToUser);
        resultDisplay.getStyleClass().remove(ERROR_STYLE_CLASS);
        if (isError) {
            resultDisplay.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        resultDisplay.setText(feedbackToUser);
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
        resultDisplay.setScrollLeft(0);
    }

}
