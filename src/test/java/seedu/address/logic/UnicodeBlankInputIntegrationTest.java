package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies that blank Unicode fields are rejected consistently by typed commands and JSON loading.
 */
public class UnicodeBlankInputIntegrationTest {
    private static final List<String> BLANK_VALUES = List.of("", " ", "\t", "\u2003", "\u3000", "\u00a0",
            "\u2007", "\u202f", " \t\u2003\u00a0\u3000 ", "\u200b", "\u200c", "\u200d", "\u2060", "\ufeff",
            "\u200b\u200c\u200d\u2060\ufeff", " \u2003\u00a0\u200b\u2060 ");
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withPhone("81234567")
            .withAddress("10 Short Street").withLessons("Algebra").build();
    private static final Person BEN = new PersonBuilder().withName("Ben Lim").withPhone("81234568").build();

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
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(directory.resolve("preferences.json"))));
        logic.execute("find amy");
    }

    @Test
    public void execute_addBlankAddress_keepsStudentsFilterAndSavedData() throws Exception {
        for (String blank : BLANK_VALUES) {
            assertRejectedAndUnchanged("add n/Bea Tan p/81234569 addr/" + blank + " l/Sec 2 sub/E-Math",
                    Address.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void execute_editBlankAddress_keepsStudentsFilterAndSavedData() throws Exception {
        for (String blank : BLANK_VALUES) {
            assertRejectedAndUnchanged("edit 1 addr/" + blank, Address.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void execute_logBlankContent_keepsLessonsFilterAndSavedData() throws Exception {
        for (String blank : BLANK_VALUES) {
            assertRejectedAndUnchanged("log 1 c/" + blank, Lesson.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void execute_visibleUnicodeFields_preservesTextAndFormattingAfterReload() throws Exception {
        String address = "碧山街\u3000十号\u00a0#01-23";
        String content = "代数\u2003复习\u00a0Q1-5   完成 👩\u200d🏫";
        logic.execute("add n/小明 Tan p/81234569 addr/" + address + " l/Sec 2 sub/E-Math");
        logic.execute("log 3 c/" + content);
        String editedAddress = "خیابان\u200cشمالی\u300012\u00a0#02-34";
        logic.execute("edit 3 addr/" + editedAddress);
        List<Person> savedStudents = storage.readAddressBook().orElseThrow().getPersonList();
        assertEquals(model.getAddressBook().getPersonList(), savedStudents);
        assertEquals(List.of(AMY, BEN), savedStudents.subList(0, 2));
        assertEquals(editedAddress, savedStudents.get(2).getAddress().value);
        assertEquals(List.of(content), savedStudents.get(2).getLessons().stream()
                .map(lesson -> lesson.content).toList());
    }

    @Test
    public void read_blankUnicodeFields_rejectsFileWithoutRewritingIt() throws Exception {
        String original = Files.readString(dataFile);
        for (String blank : BLANK_VALUES) {
            for (String field : List.of("address", "content")) {
                String value = field.equals("address") ? AMY.getAddress().value : "Algebra";
                String invalid = original.replace("\"" + field + "\" : " + JsonUtil.toJsonString(value),
                        "\"" + field + "\" : " + JsonUtil.toJsonString(blank));
                assertNotEquals(original, invalid);
                Files.writeString(dataFile, invalid);
                assertThrows(DataLoadingException.class, storage::readAddressBook);
                assertEquals(invalid, Files.readString(dataFile));
            }
        }
    }

    private void assertRejectedAndUnchanged(String command, String expectedMessage) throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        String savedBefore = Files.readString(dataFile);
        ParseException exception = assertThrows(ParseException.class, () -> logic.execute(command));
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(savedBefore, Files.readString(dataFile));
        assertEquals(before, new AddressBook(storage.readAddressBook().orElseThrow()));
    }
}
