package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class FlagPersistenceTest {

    private static final Person ALEX = new PersonBuilder().withName("Alex Lim").withPhone("91234567")
            .withLevel("Sec 3").withSubject("E-Math").build();
    private static final Person BEA = new PersonBuilder().withName("Bea Tan").withPhone("98765432")
            .withLevel("JC1").withSubject("H2 Math").build();

    @TempDir
    public Path testFolder;

    private Path dataFile;
    private Model model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws IOException {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALEX);
        addressBook.addPerson(BEA);
        dataFile = testFolder.resolve("addressbook.json");
        model = new ModelManager(addressBook, new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        storage.saveAddressBook(addressBook);
        logic = logicOver(model, storage, testFolder);
    }

    @Test
    public void restart_flagsWithAndWithoutReasons_areKept() throws Exception {
        logic.execute("flag 1 r/Missed homework twice");
        logic.execute("flag 2");

        ReadOnlyAddressBook reloaded = restart();
        assertEquals(new Flag("Missed homework twice"), reloaded.getPersonList().get(0).getFlag().orElseThrow());
        assertEquals(new Flag(), reloaded.getPersonList().get(1).getFlag().orElseThrow());
        String saved = Files.readString(dataFile);
        assertTrue(saved.contains("\"flag\" : {"));
        assertTrue(saved.contains("\"reason\" : \"Missed homework twice\""));
    }

    @Test
    public void restart_unflaggedStudents_omitFlagProperty() throws Exception {
        logic.execute("list");
        assertFalse(Files.readString(dataFile).contains("\"flag\""));

        logic.execute("flag 1 r/Call parent");
        logic.execute("unflag 1");
        assertFalse(Files.readString(dataFile).contains("\"flag\""));
        assertEquals(List.of(ALEX, BEA), restart().getPersonList());
    }

    @Test
    public void restart_editLogAndDeleteLesson_keepFlag() throws Exception {
        logic.execute("flag 1 r/Needs follow-up");
        logic.execute("edit 1 l/Sec 4");
        logic.execute("loglesson 1 c/Algebra revision");
        logic.execute("deletelesson 1 1");

        Person reloaded = restart().getPersonList().get(0);
        assertEquals(new Flag("Needs follow-up"), reloaded.getFlag().orElseThrow());
        assertEquals("Sec 4", reloaded.getLevel().value);
        assertTrue(reloaded.getLessons().isEmpty());
    }

    @Test
    public void read_fileWithoutFlag_loadsUnflaggedStudents() throws Exception {
        logic.execute("list");
        ReadOnlyAddressBook reloaded = restart();
        assertFalse(reloaded.getPersonList().get(0).isFlagged());
        assertFalse(reloaded.getPersonList().get(1).isFlagged());
    }

    @Test
    public void read_invalidStoredReason_rejectsDataFile() throws Exception {
        Files.writeString(dataFile, """
                { "persons": [ {
                  "name": "Alex Lim", "phone": "91234567", "address": "123 Street",
                  "level": "Sec 3", "subject": "E-Math", "flag": { "reason": " " }
                } ] }
                """);
        assertThrows(DataLoadingException.class, () -> restart());
    }

    private ReadOnlyAddressBook restart() throws DataLoadingException {
        return new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow();
    }
}
