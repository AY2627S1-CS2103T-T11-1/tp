package seedu.address.model;

import static java.util.Objects.requireNonNull;

import seedu.address.model.person.Person;

/**
 * An immutable snapshot of student and lesson totals, derived without changing stored data.
 */
public class StudentSummary {
    private final int studentCount;
    private final long lessonCount;
    private final int studentsWithoutLessons;

    /**
     * Counts all students in {@code addressBook}, including their undated lesson records.
     * The snapshot is independent of subsequent changes to the address book.
     */
    public StudentSummary(ReadOnlyAddressBook addressBook) {
        requireNonNull(addressBook);
        studentCount = addressBook.getPersonList().size();
        long totalLessons = 0;
        int withoutLessons = 0;
        for (Person student : addressBook.getPersonList()) {
            int count = student.getLessonCount();
            totalLessons += count;
            if (count == 0) {
                withoutLessons++;
            }
        }
        lessonCount = totalLessons;
        studentsWithoutLessons = withoutLessons;
    }

    /** Returns the number of students in the complete collection. */
    public int getStudentCount() {
        return studentCount;
    }

    /** Returns the number of lesson records across all students. */
    public long getLessonCount() {
        return lessonCount;
    }

    /** Returns the number of students whose lesson history is empty. */
    public int getStudentsWithoutLessons() {
        return studentsWithoutLessons;
    }
}
