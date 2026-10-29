package seedu.address.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private StudentDetailPanel studentDetailPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane studentDetailPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList(), this::handlePersonClick);
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        studentDetailPanel = new StudentDetailPanel(logic.getFilteredPersonList(),
                logic.getAddressBook().getPersonList());
        studentDetailPanelPlaceholder.getChildren().add(studentDetailPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Views a student chosen by a click or keyboard navigation through the same command path as typed input.
     */
    private void handlePersonClick(Index index) {
        try {
            executeCommand(ViewCommand.COMMAND_WORD + " " + index.getOneBased());
        } catch (CommandException | ParseException e) {
            // executeCommand has already displayed the error in the result area.
            logger.warning("Unable to view student lessons: " + e.getMessage());
        }
    }

    /**
     * Shows the student the command acted on. Otherwise keeps the shown student while it is still
     * in the displayed list, so filtering or deleting it returns the panel to its guidance.
     */
    private void updateSelectedStudent(CommandResult commandResult) {
        commandResult.getStudent()
                .or(() -> studentDetailPanel.getStudent().filter(logic.getFilteredPersonList()::contains))
                .ifPresentOrElse(this::showStudent, this::clearSelectedStudent);
    }

    /**
     * Shows {@code student} in the detail panel and highlights it if it is in the displayed list.
     */
    private void showStudent(Person student) {
        int displayedIndex = logic.getFilteredPersonList().indexOf(student);
        studentDetailPanel.show(student, displayedIndex + 1);
        if (displayedIndex < 0) {
            personListPanel.clearSelection();
            return;
        }
        personListPanel.selectPerson(Index.fromZeroBased(displayedIndex));
    }

    /**
     * Highlights the shown student again after a failed command. A failed save restores the list,
     * which clears its selection, while the details panel keeps showing the student.
     */
    private void restoreShownStudentHighlight() {
        studentDetailPanel.getStudent()
                .map(logic.getFilteredPersonList()::indexOf)
                .filter(displayedIndex -> displayedIndex >= 0)
                .ifPresent(displayedIndex -> personListPanel.restoreSelection(Index.fromZeroBased(displayedIndex)));
    }

    private void clearSelectedStudent() {
        studentDetailPanel.clear();
        personListPanel.clearSelection();
    }

    /**
     * Sets the default size based on {@code guiSettings}, shrunk to fit the primary screen's usable area.
     * A saved position is kept only when the complete window fits within a connected screen's usable area;
     * otherwise, the window is centred on the primary screen.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        Rectangle2D primaryScreen = Screen.getPrimary().getVisualBounds();
        primaryStage.setHeight(Math.min(guiSettings.getWindowHeight(), primaryScreen.getHeight()));
        primaryStage.setWidth(Math.min(guiSettings.getWindowWidth(), primaryScreen.getWidth()));
        List<Rectangle2D> screens = Screen.getScreens().stream().map(Screen::getVisualBounds).toList();
        getRestoredWindowPosition(guiSettings, primaryStage.getWidth(), primaryStage.getHeight(), screens,
                primaryScreen).ifPresent(position -> {
                    primaryStage.setX(position.getX());
                    primaryStage.setY(position.getY());
                });
    }

    /**
     * Returns the saved position if the complete window fits on a connected screen, or a position centred within
     * the primary screen otherwise. An empty result leaves first-launch positioning to the window system.
     */
    static Optional<Point2D> getRestoredWindowPosition(GuiSettings guiSettings, double windowWidth,
            double windowHeight, List<Rectangle2D> screens, Rectangle2D primaryScreen) {
        if (guiSettings.getWindowCoordinates() == null) {
            return Optional.empty();
        }
        double savedX = guiSettings.getWindowCoordinates().getX();
        double savedY = guiSettings.getWindowCoordinates().getY();
        boolean fitsConnectedScreen = screens.stream().anyMatch(screen -> savedX >= screen.getMinX()
                && savedY >= screen.getMinY()
                && savedX + windowWidth <= screen.getMaxX()
                && savedY + windowHeight <= screen.getMaxY());
        if (fitsConnectedScreen) {
            return Optional.of(new Point2D(savedX, savedY));
        }
        double centredX = primaryScreen.getMinX() + (primaryScreen.getWidth() - windowWidth) / 2;
        double centredY = primaryScreen.getMinY() + (primaryScreen.getHeight() - windowHeight) / 2;
        return Optional.of(new Point2D(centredX, centredY));
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            updateSelectedStudent(commandResult);

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setErrorToUser(e.getMessage());
            restoreShownStudentHighlight();
            throw e;
        }
    }
}
