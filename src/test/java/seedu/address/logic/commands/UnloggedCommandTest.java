package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class UnloggedCommandTest {
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withPhone("81230001").build();
    private static final Person BEN = new PersonBuilder().withName("Ben Lim").withPhone("81230002").build();
    private static final Person CAROL = new PersonBuilder().withName("Carol Ng").withPhone("81230003").build();

    @Test
    public void execute_mixedHistories_filtersFullCollectionInOriginalOrder() {
        ModelManager model = new ModelManager();
        Person datedBen = BEN.withLessonAdded(new Lesson("Algebra", LocalDate.of(2020, 1, 1)));
        Person undatedCarol = CAROL.withLessonAdded(new Lesson("Legacy record"));
        Person dan = new PersonBuilder().withName("Dan Tan").withPhone("81230004").withFlag().build();
        model.addPerson(AMY);
        model.addPerson(datedBen);
        model.addPerson(undatedCarol);
        model.addPerson(dan);
        AddressBook original = new AddressBook(model.getAddressBook());
        model.updateFilteredPersonList(person -> person.equals(datedBen));

        CommandResult result = new UnloggedCommand().execute(model);
        assertEquals("Listed 2 students with no recorded lessons.", result.getFeedbackToUser());
        assertTrue(result.getStudent().isEmpty());
        assertEquals(List.of(AMY, dan), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
        new ListCommand().execute(model);
        assertEquals(original.getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void execute_oneMatch_reportsSingular() {
        ModelManager model = new ModelManager();
        model.addPerson(AMY);
        assertEquals("Listed 1 student with no recorded lessons.",
                new UnloggedCommand().execute(model).getFeedbackToUser());
        assertEquals(List.of(AMY), model.getFilteredPersonList());
    }

    @Test
    public void execute_noMatchesOrEmptyBook_reportsHowToRestoreList() {
        ModelManager model = new ModelManager();
        assertEquals(UnloggedCommand.MESSAGE_NO_MATCHES, new UnloggedCommand().execute(model).getFeedbackToUser());
        model.addPerson(AMY.withLessonAdded(new Lesson("Algebra")));
        assertEquals(UnloggedCommand.MESSAGE_NO_MATCHES, new UnloggedCommand().execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_nullModel_rejected() {
        assertThrows(NullPointerException.class, () -> new UnloggedCommand().execute(null));
    }
}
