package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.AddressBook;
import seedu.address.model.StudentSummary;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void getErrorMessageForDuplicatePrefixes_addsRewordingHint() {
        assertEquals("Multiple values specified for the following single-valued field(s): c/\n"
                + "A recognised prefix after a space starts another field. "
                + "If it was part of your text, please reword it.",
                Messages.getErrorMessageForDuplicatePrefixes(new Prefix("c/")));
    }

    @Test
    public void lessonDateTextHint_suggestsRewording() {
        assertEquals("If \"date/\" was part of your lesson notes, please reword it, for example \"date and time\".",
                Messages.MESSAGE_LESSON_DATE_TEXT_HINT);
    }

    @Test
    public void formatSummaryParagraph_emptyCollection_suggestsAddingStudent() {
        assertEquals("No students yet. Add a student to start recording lessons.",
                Messages.formatSummaryParagraph(new StudentSummary(new AddressBook())));
    }

    @Test
    public void formatSummaryParagraph_singleStudentAndRecord_usesSingularWording() {
        AddressBook book = new AddressBook();
        Person student = new PersonBuilder().build();
        book.addPerson(student);
        assertEquals("You have 1 student and 0 lesson records in total. 1 student has no lessons recorded.",
                Messages.formatSummaryParagraph(new StudentSummary(book)));
        book.setPerson(student, student.withLessonAdded(new Lesson("Algebra")));
        assertEquals("You have 1 student and 1 lesson record in total. "
                + "Every student has at least one lesson recorded.",
                Messages.formatSummaryParagraph(new StudentSummary(book)));
    }

    @Test
    public void formatSummaryParagraph_multipleStudents_usesPluralAndCountsEmptyHistories() {
        AddressBook book = new AddressBook();
        Person amy = new PersonBuilder().withName("Amy Tan").build();
        Person ben = new PersonBuilder().withName("Ben Lim").build();
        book.addPerson(amy);
        book.addPerson(ben);
        assertEquals("You have 2 students and 0 lesson records in total. 2 students have no lessons recorded.",
                Messages.formatSummaryParagraph(new StudentSummary(book)));
        book.setPerson(amy, amy.withLessonAdded(new Lesson("Algebra")));
        assertEquals("You have 2 students and 1 lesson record in total. 1 student has no lessons recorded.",
                Messages.formatSummaryParagraph(new StudentSummary(book)));
        book.setPerson(ben, ben.withLessonAdded(new Lesson("Geometry")));
        assertEquals("You have 2 students and 2 lesson records in total. "
                + "Every student has at least one lesson recorded.",
                Messages.formatSummaryParagraph(new StudentSummary(book)));
    }

    @Test
    public void formatLessonStatus_emptyDatedAndUndatedHistories_usesCountAndLatestDate() {
        Person student = new PersonBuilder().build();
        assertEquals("No lessons yet", Messages.formatLessonStatus(student));
        student = student.withLessonAdded(new Lesson("Algebra"));
        assertEquals("1 lesson (undated)", Messages.formatLessonStatus(student));
        student = student.withLessonAdded(new Lesson("Geometry"));
        assertEquals("2 lessons (undated)", Messages.formatLessonStatus(student));
        student = student.withLessonAdded(new Lesson("Vectors", LocalDate.of(2020, 3, 5)));
        assertEquals("3 lessons, last 05/03/2020", Messages.formatLessonStatus(student));
        Person dated = new PersonBuilder().build().withLessonAdded(new Lesson("Algebra", LocalDate.of(2020, 1, 2)));
        assertEquals("1 lesson, last 02/01/2020", Messages.formatLessonStatus(dated));
    }

    @Test
    public void formatLessonDate_datedLesson_returnsDayMonthYear() {
        Lesson lesson = new Lesson("Algebra", LocalDate.of(2026, 10, 18));
        assertEquals("18/10/2026", Messages.formatLessonDate(lesson));
    }

    @Test
    public void formatLessonDate_singleDigitDayAndMonth_padsWithZeros() {
        Lesson lesson = new Lesson("Algebra", LocalDate.of(2026, 3, 5));
        assertEquals("05/03/2026", Messages.formatLessonDate(lesson));
    }

    @Test
    public void formatLessonDate_undatedLesson_returnsUndated() {
        assertEquals("(undated)", Messages.formatLessonDate(new Lesson("Algebra")));
    }
}
