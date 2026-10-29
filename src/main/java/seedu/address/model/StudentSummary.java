package seedu.address.model;

import static java.util.Objects.requireNonNull;

import seedu.address.model.person.Person;

/**
 * An immutable snapshot of student and lesson totals, derived without changing stored data.
 */
public class StudentSummary {
    private final int studentCount;
    private final long lessonCount;

    /**
     * Counts all students in {@code addressBook}, including their undated lesson records.
     * The snapshot is independent of subsequent changes to the address book.
     */
    public StudentSummary(ReadOnlyAddressBook addressBook) {
        requireNonNull(addressBook);
        studentCount = addressBook.getPersonList().size();
        long totalLessons = 0;
        for (Person student : addressBook.getPersonList()) {
            totalLessons += student.getLessonCount();
        }
        lessonCount = totalLessons;
    }

    /** Returns the number of students in the complete collection. */
    public int getStudentCount() {
        return studentCount;
    }

    /** Returns the number of lesson records across all students. */
    public long getLessonCount() {
        return lessonCount;
    }
}
