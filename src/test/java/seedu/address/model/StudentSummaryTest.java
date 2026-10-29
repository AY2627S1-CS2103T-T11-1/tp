package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class StudentSummaryTest {
    @Test
    public void constructor_nullBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentSummary(null));
    }

    @Test
    public void constructor_emptyBook_countsZero() {
        assertTotals(new StudentSummary(new AddressBook()), 0, 0);
    }

    @Test
    public void constructor_mixedHistories_countsDatedUndatedAndRepeatedRecords() {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withName("Amy Tan").withLessons("Algebra", "Algebra")
                .build().withLessonAdded(new Lesson("Vectors", LocalDate.of(2020, 1, 2))));
        book.addPerson(new PersonBuilder().withName("Ben Lim").build());
        book.addPerson(new PersonBuilder().withName("Chloe Ng").withFlag().build());
        assertTotals(new StudentSummary(book), 3, 3);
    }

    @Test
    public void constructor_laterChanges_doNotChangeSnapshot() {
        AddressBook book = new AddressBook();
        Person amy = new PersonBuilder().withName("Amy Tan").build();
        book.addPerson(amy);
        StudentSummary before = new StudentSummary(book);
        assertTotals(before, 1, 0);
        book.setPerson(amy, amy.withLessonAdded(new Lesson("Algebra")));
        assertTotals(new StudentSummary(book), 1, 1);
        assertTotals(before, 1, 0);
    }

    private static void assertTotals(StudentSummary summary, int students, long lessons) {
        assertEquals(students, summary.getStudentCount());
        assertEquals(lessons, summary.getLessonCount());
    }
}
