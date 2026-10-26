package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSearchPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Tests feedback, complete-history numbering and student-filter preservation for lesson searches. */
public class FindLessonCommandTest {
    private static final Index FIRST = Index.fromOneBased(1);
    private static final LocalDate DAY = LocalDate.of(2020, 1, 2);
    private static final Person AMY = new PersonBuilder().withName("Amy Tan").withFlag("Call parent")
            .withLessons("Algebra legacy").build()
            .withLessonAdded(new Lesson("Algebra", DAY))
            .withLessonAdded(new Lesson("Geometry", DAY))
            .withLessonAdded(new Lesson("Algebra", DAY.plusDays(1)))
            .withLessonAdded(new Lesson("Vectors", DAY.plusDays(2)));

    @Test
    public void execute_matchesWithGaps_keepsViewNumbersAndOriginalData() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(AMY);
        AddressBook before = new AddressBook(model.getAddressBook());
        String expected = "Matching lesson records for Amy Tan (3):\n2. 03/01/2020: Algebra"
                + "\n4. 02/01/2020: Algebra\n5. (undated): Algebra legacy";
        assertEquals(new CommandResult(expected, FIRST), search("alg").execute(model));
        assertEquals(before, model.getAddressBook());
        assertSame(AMY, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_dateSearch_excludesUndatedAndRetainsSameDateOrder() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(AMY);
        FindLessonCommand command = new FindLessonCommand(
                FIRST, new LessonSearchPredicate(List.of(), DAY, DAY));
        assertEquals(new CommandResult("Matching lesson records for Amy Tan (2):"
                + "\n3. 02/01/2020: Geometry\n4. 02/01/2020: Algebra", FIRST), command.execute(model));
    }

    @Test
    public void execute_emptyHistoryAndNoMatches_returnsDistinctMessages() throws Exception {
        ModelManager model = new ModelManager();
        Person student = new PersonBuilder().withName("Ben Lim").build();
        model.addPerson(student);
        assertEquals(new CommandResult("No lessons logged yet for Ben Lim.", FIRST),
                search("alg").execute(model));
        model.setPerson(student, student.withLessonAdded(new Lesson("Geometry")));
        assertEquals(new CommandResult("No matching lesson records found for Ben Lim.", FIRST),
                search("alg").execute(model));
    }

    @Test
    public void execute_filteredStudents_targetsDisplayedIndexWithoutReplacingFilter() throws Exception {
        ModelManager model = new ModelManager();
        Person ben = new PersonBuilder().withName("Ben Lim").build();
        model.addPerson(ben);
        model.addPerson(AMY);
        model.updateFilteredPersonList(Person::isFlagged);
        var list = model.getFilteredPersonList();
        assertEquals(FIRST, search("alg").execute(model).getViewedStudentIndex().orElseThrow());
        assertSame(list, model.getFilteredPersonList());
        assertEquals(List.of(AMY), list);
        Person flaggedStudent = new PersonBuilder(ben).withFlag().build();
        model.setPerson(ben, flaggedStudent);
        assertEquals(List.of(flaggedStudent, AMY), list);
        model.setPerson(AMY, AMY.withoutFlag());
        assertEquals(List.of(flaggedStudent), list);
    }

    @Test
    public void execute_invalidDisplayedIndex_reportsStudentIndexError() {
        ModelManager model = new ModelManager();
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, ()
                -> search("alg").execute(model));
        model.addPerson(AMY);
        FindLessonCommand outside = new FindLessonCommand(Index.fromOneBased(Integer.MAX_VALUE),
                new LessonSearchPredicate(List.of("alg"), null, null));
        assertThrows(CommandException.class, Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, ()
                -> outside.execute(model));
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindLessonCommand(null,
                new LessonSearchPredicate(List.of("alg"), null, null)));
        assertThrows(NullPointerException.class, () -> new FindLessonCommand(FIRST, null));
        assertThrows(NullPointerException.class, () -> search("alg").execute(null));
    }

    @Test
    public void equalsAndHashCode_compareStudentIndexAndCriteria() {
        FindLessonCommand command = search("alg");
        assertTrue(command.equals(command));
        assertTrue(command.equals(search("alg")));
        assertEquals(command.hashCode(), search("alg").hashCode());
        assertFalse(command.equals(null));
        assertFalse(command.equals("findlesson"));
        assertFalse(command.equals(search("geometry")));
        assertFalse(command.equals(new FindLessonCommand(Index.fromOneBased(2),
                new LessonSearchPredicate(List.of("alg"), null, null))));
    }

    @Test
    public void toStringMethod_describesIndexAndCriteria() {
        LessonSearchPredicate predicate = new LessonSearchPredicate(List.of("alg"), null, null);
        assertEquals(FindLessonCommand.class.getCanonicalName() + "{studentIndex=" + FIRST
                + ", predicate=" + predicate + "}", search("alg").toString());
    }

    private static FindLessonCommand search(String keyword) {
        return new FindLessonCommand(FIRST, new LessonSearchPredicate(List.of(keyword), null, null));
    }
}
