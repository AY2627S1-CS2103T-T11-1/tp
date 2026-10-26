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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void lessonStatistics_emptyOrUndatedHistory_countsWithoutDate() {
        Person student = new PersonBuilder().build();
        assertEquals(0, student.getLessonCount());
        assertTrue(student.getLatestLessonDate().isEmpty());
        student = new PersonBuilder(student).withLessons("Algebra", "Algebra").build();
        assertEquals(2, student.getLessonCount());
        assertTrue(student.getLatestLessonDate().isEmpty());
    }

    @Test
    public void getLatestLessonDate_mixedHistory_usesMaximumRecordedDate() {
        LocalDate latest = LocalDate.of(2020, 3, 5);
        Person student = new PersonBuilder().withLessons("Legacy record").build()
                .withLessonAdded(new Lesson("Vectors", latest))
                .withLessonAdded(new Lesson("Algebra", LocalDate.of(2020, 1, 2)))
                .withLessonAdded(new Lesson("Geometry", latest));
        assertEquals(4, student.getLessonCount());
        assertEquals(Optional.of(latest), student.getLatestLessonDate());
        Person withoutLatest = student.withLessonRemoved(3).withLessonRemoved(2);
        assertEquals(Optional.of(LocalDate.of(2020, 1, 2)), withoutLatest.getLatestLessonDate());
        assertEquals(Optional.of(latest), student.getLatestLessonDate());
    }

    @Test
    public void getLatestLessonDate_unsortedConstructorHistory_doesNotAssumeListOrder() {
        LocalDate latest = LocalDate.of(2020, 3, 5);
        Person student = new PersonBuilder().build();
        Person unsorted = new Person(student.getName(), student.getPhone(), student.getAddress(),
                student.getLevel(), student.getSubject(), List.of(new Lesson("Vectors", latest),
                    new Lesson("Legacy record"), new Lesson("Algebra", LocalDate.of(2020, 1, 2))));
        assertEquals(Optional.of(latest), unsorted.getLatestLessonDate());
    }

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

        // different flag -> returns false, but identity is unchanged
        Person flaggedAlice = new PersonBuilder(ALICE).withFlag("Call parent").build();
        assertFalse(ALICE.equals(flaggedAlice));
        assertTrue(ALICE.isSamePerson(flaggedAlice));

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
    public void withFlagAndWithoutFlag_keepStudentDetailsAndLessons() {
        Person original = new PersonBuilder(ALICE).withLessons("Algebra", "Geometry").build();
        Flag flag = new Flag("Call parent");
        Person flagged = original.withFlag(flag);
        assertEquals(flag, flagged.getFlag().orElseThrow());
        assertEquals(original.getLessons(), flagged.getLessons());
        assertTrue(flagged.isSamePerson(original));
        assertFalse(original.isFlagged());
        assertEquals(original, flagged.withoutFlag());
    }

    @Test
    public void lessonChanges_keepFlag() {
        Flag flag = new Flag("Review homework");
        Person flagged = new PersonBuilder(ALICE).withLessons("Algebra").withFlag("Review homework").build();
        Person added = flagged.withLessonAdded(new Lesson("Geometry"));
        assertEquals(flag, added.getFlag().orElseThrow());
        assertEquals(flag, added.withLessonRemoved(0).getFlag().orElseThrow());
    }

    @Test
    public void withFlag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.withFlag(null));
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
    public void withLessonAdded_earlierDate_insertedBeforeLaterLessons() {
        Lesson monday = new Lesson("Algebra", LocalDate.of(2026, 10, 19));
        Lesson wednesday = new Lesson("Vectors", LocalDate.of(2026, 10, 21));
        Lesson tuesday = new Lesson("Surds", LocalDate.of(2026, 10, 20));
        Person updated = new PersonBuilder().build().withLessonAdded(monday).withLessonAdded(wednesday)
                .withLessonAdded(tuesday);
        assertEquals(List.of(monday, tuesday, wednesday), updated.getLessons());
    }

    @Test
    public void withLessonAdded_sameDate_keepsLoggingOrder() {
        LocalDate date = LocalDate.of(2026, 10, 21);
        Lesson first = new Lesson("Algebra", date);
        Lesson later = new Lesson("Surds", LocalDate.of(2026, 10, 22));
        Lesson second = new Lesson("Vectors", date);
        Person updated = new PersonBuilder().build().withLessonAdded(first).withLessonAdded(later)
                .withLessonAdded(second);
        assertEquals(List.of(first, second, later), updated.getLessons());
    }

    @Test
    public void withLessonAdded_oldestDatedLesson_staysAfterUndatedLessons() {
        Lesson undated = new Lesson("Quadratic inequalities");
        Lesson recent = new Lesson("Vectors", LocalDate.of(2026, 10, 21));
        Lesson oldest = new Lesson("Surds", LocalDate.of(2020, 1, 1));
        Person updated = new PersonBuilder().build().withLessonAdded(undated).withLessonAdded(recent)
                .withLessonAdded(oldest);
        assertEquals(List.of(undated, oldest, recent), updated.getLessons());
    }

    @Test
    public void withLessonAdded_undatedLesson_insertedBeforeDatedLessons() {
        Lesson undated = new Lesson("Quadratic inequalities");
        Lesson dated = new Lesson("Vectors", LocalDate.of(2026, 10, 21));
        Lesson anotherUndated = new Lesson("Binomial theorem");
        Person updated = new PersonBuilder().build().withLessonAdded(undated).withLessonAdded(dated)
                .withLessonAdded(anotherUndated);
        assertEquals(List.of(undated, anotherUndated, dated), updated.getLessons());
    }

    /**
     * Removes exactly one stored occurrence and preserves all other student data and immutability.
     */
    @Test
    public void withLessonRemoved_validIndex_preservesOtherData() {
        Person original = new PersonBuilder(ALICE).withLessons("Algebra", "Geometry", "Algebra").build();
        Person updated = original.withLessonRemoved(2);
        assertEquals(new PersonBuilder(ALICE).withLessons("Algebra", "Geometry").build(), updated);
        assertEquals(new PersonBuilder(ALICE).withLessons("Algebra", "Geometry", "Algebra").build(), original);
        assertThrows(UnsupportedOperationException.class, () -> updated.getLessons().clear());
        assertEquals(ALICE, new PersonBuilder(ALICE).withLessons("Algebra").build().withLessonRemoved(0));
    }

    /**
     * Rejects positions outside the stored lesson list.
     */
    @Test
    public void withLessonRemoved_invalidIndex_throwsIndexOutOfBoundsException() {
        Person student = new PersonBuilder(ALICE).withLessons("Algebra").build();
        assertThrows(IndexOutOfBoundsException.class, () -> student.withLessonRemoved(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> student.withLessonRemoved(1));
        assertThrows(IndexOutOfBoundsException.class, () -> ALICE.withLessonRemoved(0));
    }

    /**
     * Replaces exactly one stored occurrence in place and preserves all other student data and immutability.
     */
    @Test
    public void withLessonReplaced_sameDate_keepsPositionAndOtherData() {
        LocalDate date = LocalDate.of(2026, 10, 21);
        Lesson first = new Lesson("Algebra", date);
        Lesson second = new Lesson("Vectors", date);
        Lesson later = new Lesson("Surds", LocalDate.of(2026, 10, 22));
        Person original = new PersonBuilder(ALICE).build().withLessonAdded(first).withLessonAdded(second)
                .withLessonAdded(later);

        Lesson edited = second.withContent("Vectors and scalars");
        Person updated = original.withLessonReplaced(1, edited);

        assertEquals(List.of(first, edited, later), updated.getLessons());
        assertEquals(List.of(first, second, later), original.getLessons());
        assertEquals(new PersonBuilder(ALICE).build(), new PersonBuilder(updated).withLessons().build());
        assertThrows(UnsupportedOperationException.class, () -> updated.getLessons().clear());
    }

    /**
     * Keeps the student's follow-up flag when a lesson is replaced.
     */
    @Test
    public void withLessonReplaced_flaggedStudent_keepsFlag() {
        Person flagged = new PersonBuilder(ALICE).withLessons("Algebra").withFlag("Review homework").build();
        Person updated = flagged.withLessonReplaced(0, new Lesson("Linear algebra"));
        assertEquals(flagged.getFlag(), updated.getFlag());
        assertTrue(updated.isFlagged());
    }

    /**
     * Keeps an undated legacy lesson undated when only its content is replaced.
     */
    @Test
    public void withLessonReplaced_undatedLesson_staysUndated() {
        Person original = new PersonBuilder(ALICE).withLessons("Algebra", "Geometry").build();
        Person updated = original.withLessonReplaced(0, new Lesson("Linear algebra"));
        assertEquals(new PersonBuilder(ALICE).withLessons("Linear algebra", "Geometry").build(), updated);
    }

    /**
     * Rejects positions outside the stored lesson list and a missing lesson.
     */
    @Test
    public void withLessonReplaced_invalidArguments_throwsException() {
        Person student = new PersonBuilder(ALICE).withLessons("Algebra").build();
        Lesson lesson = new Lesson("Geometry");
        assertThrows(IndexOutOfBoundsException.class, () -> student.withLessonReplaced(-1, lesson));
        assertThrows(IndexOutOfBoundsException.class, () -> student.withLessonReplaced(1, lesson));
        assertThrows(IndexOutOfBoundsException.class, () -> ALICE.withLessonReplaced(0, lesson));
        assertThrows(NullPointerException.class, () -> student.withLessonReplaced(0, null));
    }

    /**
     * Rejects a replacement with a different date, which would break date order.
     */
    @Test
    public void withLessonReplaced_differentDate_throwsAssertionError() {
        Person student = new PersonBuilder(ALICE).build()
                .withLessonAdded(new Lesson("Algebra", LocalDate.of(2026, 10, 21)));
        assertThrows(AssertionError.class, () ->
                student.withLessonReplaced(0, new Lesson("Algebra", LocalDate.of(2026, 10, 22))));
        assertThrows(AssertionError.class, () -> student.withLessonReplaced(0, new Lesson("Algebra")));
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
                + ", subject=" + ALICE.getSubject() + ", lessons=" + ALICE.getLessons()
                + ", flag=Optional.empty}";
        assertEquals(expected, ALICE.toString());
    }
}
