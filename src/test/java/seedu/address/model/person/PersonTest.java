package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name and phone, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB)
                .withLevel(VALID_LEVEL_BOB).withSubject(VALID_SUBJECT_BOB).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // same name, different phone -> returns false (two students can share a name)
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // different name, same phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs only in case, same phone -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));
        editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toUpperCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, same phone -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // name differs only in case -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(ALICE.getName().fullName.toUpperCase()).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different level -> returns false
        editedAlice = new PersonBuilder(ALICE).withLevel(VALID_LEVEL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different subject -> returns false
        editedAlice = new PersonBuilder(ALICE).withSubject(VALID_SUBJECT_BOB).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void hashCode_equalPersons_sameHashCode() {
        assertEquals(ALICE.hashCode(), new PersonBuilder(ALICE).build().hashCode());
    }

    @Test
    public void getLessons_newPerson_returnsEmptyList() {
        assertEquals(List.of(), new PersonBuilder().build().getLessons());
    }

    @Test
    public void getLessons_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().withLessons("Quadratic inequalities").build();
        assertThrows(UnsupportedOperationException.class, () -> person.getLessons().add(new Lesson("Surds")));
    }

    @Test
    public void withLessonAdded_validLesson_returnsNewPersonWithLessonAtEnd() {
        Person original = new PersonBuilder(ALICE).withLessons("Quadratic inequalities").build();
        Person updated = original.withLessonAdded(new Lesson("Binomial theorem"));

        assertEquals(List.of(new Lesson("Quadratic inequalities"), new Lesson("Binomial theorem")),
                updated.getLessons());
        // original student is not modified
        assertEquals(List.of(new Lesson("Quadratic inequalities")), original.getLessons());
        // other details are kept
        assertTrue(updated.isSamePerson(original));
        assertEquals(original.getAddress(), updated.getAddress());
        assertEquals(original.getLevel(), updated.getLevel());
        assertEquals(original.getSubject(), updated.getSubject());
    }

    @Test
    public void withLessonAdded_sameLessonTwice_keepsBoth() {
        Lesson lesson = new Lesson("Quadratic inequalities");
        Person updated = new PersonBuilder().build().withLessonAdded(lesson).withLessonAdded(lesson);
        assertEquals(List.of(lesson, lesson), updated.getLessons());
    }

    @Test
    public void withLessonAdded_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.withLessonAdded(null));
    }

    @Test
    public void equals_differentLessons_returnsFalse() {
        Person withLesson = new PersonBuilder(ALICE).withLessons("Quadratic inequalities").build();
        assertFalse(ALICE.equals(withLesson));
        // lessons do not affect identity
        assertTrue(ALICE.isSamePerson(withLesson));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", address=" + ALICE.getAddress() + ", level=" + ALICE.getLevel()
                + ", subject=" + ALICE.getSubject() + ", lessons=" + ALICE.getLessons() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
