package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;

public class CommandResultTest {
    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));

        // different viewed student -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", INDEX_FIRST_PERSON)));
        CommandResult viewResult = new CommandResult("feedback", INDEX_FIRST_PERSON);
        assertTrue(viewResult.equals(new CommandResult("feedback", INDEX_FIRST_PERSON)));
        assertFalse(viewResult.equals(new CommandResult("feedback", INDEX_SECOND_PERSON)));
        assertFalse(viewResult.equals(commandResult));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());

        CommandResult viewResult = new CommandResult("feedback", INDEX_FIRST_PERSON);
        assertEquals(viewResult.hashCode(), new CommandResult("feedback", INDEX_FIRST_PERSON).hashCode());
        assertNotEquals(viewResult.hashCode(), new CommandResult("feedback", INDEX_SECOND_PERSON).hashCode());
        assertNotEquals(commandResult.hashCode(), viewResult.hashCode());
    }

    @Test
    public void hashCode_equalDistinctIndexes_worksInHashSet() {
        for (int oneBasedIndex : new int[] {1, 2, 1001, Integer.MAX_VALUE}) {
            Index firstIndex = Index.fromOneBased(oneBasedIndex);
            Index equalIndex = Index.fromOneBased(oneBasedIndex);
            assertNotSame(firstIndex, equalIndex);
            CommandResult firstResult = new CommandResult("feedback", firstIndex);
            CommandResult equalResult = new CommandResult("feedback", equalIndex);
            assertEquals(firstResult, equalResult);
            assertEquals(firstResult.hashCode(), equalResult.hashCode());

            Set<CommandResult> results = new HashSet<>();
            results.add(firstResult);
            assertTrue(results.contains(equalResult));
            assertFalse(results.add(equalResult));
            assertEquals(1, results.size());
            assertTrue(results.remove(equalResult));
            assertTrue(results.isEmpty());
        }
    }

    @Test
    public void getViewedStudentIndex_returnsOptionalIndex() {
        assertEquals(Optional.empty(), new CommandResult("feedback").getViewedStudentIndex());
        CommandResult result = new CommandResult("feedback", INDEX_FIRST_PERSON);
        assertEquals(Optional.of(INDEX_FIRST_PERSON), result.getViewedStudentIndex());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void viewResult_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, INDEX_FIRST_PERSON));
    }

    @Test
    public void viewResult_toString_includesStudentIndex() {
        CommandResult result = new CommandResult("feedback", INDEX_FIRST_PERSON);
        assertEquals(CommandResult.class.getCanonicalName()
                + "{feedbackToUser=feedback, showHelp=false, exit=false, viewedStudentIndex="
                + INDEX_FIRST_PERSON + "}", result.toString());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", viewedStudentIndex=null}";
        assertEquals(expected, commandResult.toString());
    }
}
