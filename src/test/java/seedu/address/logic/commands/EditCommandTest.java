package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EditCommand(null, new EditPersonDescriptor()));
        assertThrows(NullPointerException.class, () -> new EditCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void assertLessonsKept_lessonsChanged_throwsAssertionError() {
        assertThrows(AssertionError.class, () -> EditCommand.assertLessonsKept(ALICE,
                ALICE.withLessonAdded(new Lesson("Algebra"))));
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder().withLessons().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        Person expectedPerson = new PersonBuilder(editedPerson)
                .withLessons(contents(firstPerson.getLessons())).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_STUDENT_SUCCESS,
                Messages.format(expectedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPerson, expectedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_singleFieldSpecified_onlyThatFieldChanges() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEditChangesOnly(new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build(),
                new PersonBuilder(firstPerson).withName(VALID_NAME_BOB).build());
        assertEditChangesOnly(new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build(),
                new PersonBuilder(firstPerson).withPhone(VALID_PHONE_BOB).build());
        assertEditChangesOnly(new EditPersonDescriptorBuilder().withLevel(VALID_LEVEL_BOB).build(),
                new PersonBuilder(firstPerson).withLevel(VALID_LEVEL_BOB).build());
        assertEditChangesOnly(new EditPersonDescriptorBuilder().withSubject(VALID_SUBJECT_BOB).build(),
                new PersonBuilder(firstPerson).withSubject(VALID_SUBJECT_BOB).build());
    }

    @Test
    public void execute_studentWithLessons_lessonsKept() {
        Person withLessons = new PersonBuilder().withLessons("Quadratic inequalities", "Binomial theorem").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(withLessons);
        Model modelWithLessons = new ModelManager(addressBook, new UserPrefs());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withLevel(VALID_LEVEL_BOB).build();
        Person expectedPerson = new PersonBuilder(withLessons).withPhone(VALID_PHONE_BOB)
                .withLevel(VALID_LEVEL_BOB).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_STUDENT_SUCCESS,
                Messages.format(expectedPerson));

        Model expectedModel = new ModelManager(new AddressBook(addressBook), new UserPrefs());
        expectedModel.setPerson(withLessons, expectedPerson);

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), modelWithLessons, expectedMessage,
                expectedModel);
        assertEquals(List.of(new Lesson("Quadratic inequalities"), new Lesson("Binomial theorem")),
                modelWithLessons.getFilteredPersonList().get(0).getLessons());
    }

    @Test
    public void execute_sameIdentityDifferentCase_success() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String upperCaseName = firstPerson.getName().fullName.toUpperCase();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(upperCaseName).build();
        Person expectedPerson = new PersonBuilder(firstPerson).withName(upperCaseName).build();

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_STUDENT_SUCCESS,
                Messages.format(expectedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPerson, expectedPerson);

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_STUDENT_SUCCESS,
                Messages.format(editedPerson));
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personInFilteredList, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicateStudentUnfilteredList_failure() {
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(secondPerson.getName().fullName.toLowerCase())
                .withPhone(secondPerson.getPhone().value).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_STUDENT);
    }

    @Test
    public void execute_duplicateStudentFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit the student in the filtered list into a duplicate of a student outside it
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_STUDENT);
    }

    @Test
    public void execute_invalidStudentIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    /**
     * Edits a filtered list where the index is larger than the size of the filtered list,
     * but smaller than the size of the student list.
     */
    @Test
    public void execute_invalidStudentIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of the student list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        // same values -> returns true
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor(descriptor));
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ListCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, descriptor)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build())));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

    /**
     * Checks that editing the first student with {@code descriptor} results in exactly {@code expectedPerson}.
     */
    private void assertEditChangesOnly(EditPersonDescriptor descriptor, Person expectedPerson) {
        Model freshModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstPerson = freshModel.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_STUDENT_SUCCESS,
                Messages.format(expectedPerson));

        Model expectedModel = new ModelManager(new AddressBook(freshModel.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPerson, expectedPerson);

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), freshModel, expectedMessage,
                expectedModel);
    }

    private static String[] contents(List<Lesson> lessons) {
        return lessons.stream().map(lesson -> lesson.content).toArray(String[]::new);
    }
}
