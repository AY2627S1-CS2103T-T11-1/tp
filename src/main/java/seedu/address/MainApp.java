package seedu.address;

import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");
    private static final String ALREADY_RUNNING_MESSAGE = "Another copy of TutorLah is already using this data "
            + "folder. Close the other copy before starting TutorLah again.";
    private static final String LOCK_FAILURE_MESSAGE = "TutorLah could not secure exclusive access to the student "
            + "data folder. No student data was loaded or changed.";

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    private final Path userPrefsFilePath;
    private final Path addressBookFilePath;
    private String startupWarning;
    private String startupFailure;
    private Closeable applicationLock;

    /** Creates the application with its normal preference and student data paths. */
    public MainApp() {
        this(USER_PREFS_FILE_PATH, ADDRESS_BOOK_FILE_PATH);
    }

    /** Creates the application with explicit storage paths for isolated startup checks. */
    MainApp(Path userPrefsFilePath, Path addressBookFilePath) {
        this.userPrefsFilePath = requireNonNull(userPrefsFilePath);
        this.addressBookFilePath = requireNonNull(addressBookFilePath);
    }

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing TutorLah ]===========================");
        super.init();

        if (!acquireApplicationLock()) {
            ui = new UiManager(startupFailure);
            return;
        }

        try {
            JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(userPrefsFilePath);
            UserPrefs userPrefs = initPrefs(userPrefsStorage);
            JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookFilePath);
            storage = new StorageManager(addressBookStorage, userPrefsStorage);

            model = initModelManager(storage, userPrefs);

            logic = new LogicManager(model, storage);

            ui = new UiManager(logic, storage.getAddressBookFilePath(), startupWarning);
        } catch (Exception | Error e) {
            releaseApplicationLock();
            throw e;
        }
    }

    private boolean acquireApplicationLock() {
        startupFailure = null;
        try {
            Optional<? extends Closeable> acquiredLock = tryAcquireDataFolderLock();
            if (acquiredLock.isEmpty()) {
                startupFailure = ALREADY_RUNNING_MESSAGE;
                logger.warning(startupFailure);
                return false;
            }
            applicationLock = acquiredLock.get();
            return true;
        } catch (IOException e) {
            startupFailure = LOCK_FAILURE_MESSAGE + "\n\nData folder: "
                    + addressBookFilePath.toAbsolutePath().normalize().getParent();
            logger.severe(startupFailure + "\n" + StringUtil.getDetails(e));
            return false;
        }
    }

    /** Tries to acquire exclusive use of the student data folder. */
    Optional<? extends Closeable> tryAcquireDataFolderLock() throws IOException {
        return ApplicationLock.tryAcquire(addressBookFilePath);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book with a recovery warning is used if loading fails. Storage blocks saving after failure.
     */
    protected Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());
        startupWarning = null;

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with a sample AddressBook.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Saving is disabled for this session.");
            startupWarning = "The student data file could not be loaded:\n"
                    + storage.getAddressBookFilePath().toAbsolutePath().normalize()
                    + "\n\nReason: " + getDataLoadingReason(e)
                    + "\n\nThe original file has not been changed. No students were loaded, and changes in this "
                    + "session cannot be saved. You can still use help and exit normally.\n\n"
                    + JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED;
            initialData = new AddressBook();
        }

        return new ModelManager(initialData, userPrefs);
    }

    /** Returns recovery guidance without quoting JSON contents or internal exception messages. */
    private static String getDataLoadingReason(DataLoadingException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof IllegalValueException && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        // Jackson can wrap syntax failures in a mapping error while reading a student's fields.
        while (cause instanceof JsonProcessingException jsonError) {
            if (jsonError instanceof JsonParseException) {
                JsonLocation location = jsonError.getLocation();
                if (location != null && location.getLineNr() > 0 && location.getColumnNr() > 0) {
                    return "The file is not valid JSON (line " + location.getLineNr()
                            + ", column " + location.getColumnNr() + ").";
                }
                return "The file is not valid JSON.";
            }
            cause = jsonError.getCause();
        }
        return "Check that the file is readable and contains valid student data.";
    }

    /** Returns the recovery warning prepared during model initialization, if loading failed. */
    Optional<String> getStartupWarning() {
        return Optional.ofNullable(startupWarning);
    }

    /** Returns the fatal startup explanation, if this instance could not lock its data folder. */
    Optional<String> getStartupFailure() {
        return Optional.ofNullable(startupFailure);
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting TutorLah " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping TutorLah ] =============================");
        try {
            if (storage != null && model != null) {
                storage.saveUserPrefs(model.getUserPrefs());
            }
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        } finally {
            releaseApplicationLock();
        }
    }

    private void releaseApplicationLock() {
        if (applicationLock == null) {
            return;
        }
        try {
            applicationLock.close();
        } catch (IOException e) {
            logger.warning("Failed to release the TutorLah data-folder lock " + StringUtil.getDetails(e));
        } finally {
            applicationLock = null;
        }
    }
}
