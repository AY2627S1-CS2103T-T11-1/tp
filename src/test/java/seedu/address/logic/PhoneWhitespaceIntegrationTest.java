package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TestLogicUtil.logicOver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies command-side phone normalization, duplicate checks and persistence without relaxing stored values.
 */
public class PhoneWhitespaceIntegrationTest {
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withPhone("81230001")
            .withAddress("Block\u00a01 Road").withLessons("Algebra", "Vectors").withFlag("Call parent").build();
    private static final Person BEN = new PersonBuilder().withName("Ben Lim").withPhone("81230002").build();
    private static final List<String> SPACES = List.of(" ", "\u00a0", "\u202f", "\u2007", "\u2003",
            "\t\u00a0\u202f ");

    @TempDir
    public Path directory;

    private Model model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private Path dataFile;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        model.addPerson(AMY);
        model.addPerson(BEN);
        dataFile = directory.resolve("students.json");
        storage = new JsonAddressBookStorage(dataFile);
        storage.saveAddressBook(model.getAddressBook());
        logic = logicOver(model, storage, directory);
    }

    @Test
    public void execute_addUnicodeSpacedPhone_savesDigitsAndRestoresFullList() throws Exception {
        logic.execute("find amy");
        CommandResult result = logic.execute("add n/Cara Ong p/0123\u00a0\u202f4567 addr/Block\u00a03 Road"
                + " l/Sec 3 sub/Math");
        Person expected = new PersonBuilder().withName("Cara Ong").withPhone("01234567")
                .withAddress("Block\u00a03 Road").withLevel("Sec 3").withSubject("Math").build();

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expected)), result.getFeedbackToUser());
        assertEquals(List.of(AMY, BEN, expected), model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertTrue(Files.readString(dataFile).contains("\"phone\" : \"01234567\""));
    }

    @Test
    public void execute_editFilteredStudent_normalizesPhoneAndKeepsOtherFieldsAfterReload() throws Exception {
        for (String separator : SPACES) {
            logic.execute("find amy");
            CommandResult result = logic.execute("edit 1 p/9123" + separator + "0001");
            Person expected = new PersonBuilder(AMY).withPhone("91230001").build();

            assertEquals(String.format(EditCommand.MESSAGE_EDIT_STUDENT_SUCCESS, Messages.format(expected)),
                    result.getFeedbackToUser());
            assertEquals(expected, result.getStudent().orElseThrow());
            assertEquals(List.of(expected, BEN), model.getFilteredPersonList());
            Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            assertEquals(model.getAddressBook(), reloaded.getAddressBook());
            assertEquals(AMY.getLessons(), reloaded.getAddressBook().getPersonList().getFirst().getLessons());
            assertEquals(AMY.getFlag(), reloaded.getAddressBook().getPersonList().getFirst().getFlag());
        }
    }

    @Test
    public void execute_editDisplayedIndex_targetsFilteredStudentRatherThanStoredPosition() throws Exception {
        logic.execute("find ben");
        logic.execute("edit 1 p/9123\u202f0002");

        assertEquals(List.of(AMY, new PersonBuilder(BEN).withPhone("91230002").build()),
                model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_normalizedDuplicatePhone_rejectsAddAndEditWithoutChangingData() throws Exception {
        logic.execute("find amy");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(dataFile);
        for (String separator : SPACES) {
            assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(
                    "add n/amy tan p/8123" + separator + "0001 addr/Other Road l/JC1 sub/Physics"));
            assertEquals(before, model.getAddressBook());
            assertEquals(List.of(AMY), model.getFilteredPersonList());
            assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
        }

        logic.execute("find ben");
        assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_STUDENT, () ->
                logic.execute("edit 1 n/amy tan p/8123\u00a00001"));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(BEN), model.getFilteredPersonList());
        assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
    }

    @Test
    public void execute_invalidPhone_keepsStudentsFilterAndSavedBytes() throws Exception {
        logic.execute("find amy");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(dataFile);
        for (String phone : List.of("812\u00a00001", "81234\u202f0001", "+65\u00a081230001",
                "8123\u00a0O001", "8123-0001", "8123\u200b0001", "8123\u20600001", "８１２３０００１")) {
            for (String command : List.of("edit 1 p/" + phone,
                    "add n/Cara Ong p/" + phone + " addr/3 Road l/Sec 3 sub/Math")) {
                assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> logic.execute(command));
                assertEquals(before, model.getAddressBook());
                assertEquals(List.of(AMY), model.getFilteredPersonList());
                assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
            }
        }
    }

    @Test
    public void read_unicodeSpacedStoredPhone_rejectsFileAndBlocksSavingWithoutNormalizingIt() throws Exception {
        String original = Files.readString(dataFile);
        for (String separator : SPACES) {
            String invalid = original.replace(JsonUtil.toJsonString(AMY.getPhone().value),
                    JsonUtil.toJsonString("8123" + separator + "0001"));
            Files.writeString(dataFile, invalid);
            byte[] savedBefore = Files.readAllBytes(dataFile);
            JsonAddressBookStorage invalidStorage = new JsonAddressBookStorage(dataFile);

            assertThrows(DataLoadingException.class, invalidStorage::readAddressBook);
            assertThrows(IOException.class, JsonAddressBookStorage.MESSAGE_SAVE_BLOCKED, () ->
                    invalidStorage.saveAddressBook(new AddressBook()));
            assertArrayEquals(savedBefore, Files.readAllBytes(dataFile));
        }
    }
}
