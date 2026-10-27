package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

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

        // different student -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", ALICE)));
        CommandResult viewResult = new CommandResult("feedback", ALICE);
        assertTrue(viewResult.equals(new CommandResult("feedback", ALICE)));
        assertFalse(viewResult.equals(new CommandResult("feedback", BENSON)));
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

        CommandResult viewResult = new CommandResult("feedback", ALICE);
        assertEquals(viewResult.hashCode(), new CommandResult("feedback", ALICE).hashCode());
        assertNotEquals(viewResult.hashCode(), new CommandResult("feedback", BENSON).hashCode());
        assertNotEquals(commandResult.hashCode(), viewResult.hashCode());
    }

    @Test
    public void hashCode_equalDistinctStudents_worksInHashSet() {
        Person equalStudent = new PersonBuilder(ALICE).build();
        assertNotSame(ALICE, equalStudent);
        CommandResult firstResult = new CommandResult("feedback", ALICE);
        CommandResult equalResult = new CommandResult("feedback", equalStudent);
        assertEquals(firstResult, equalResult);
        assertEquals(firstResult.hashCode(), equalResult.hashCode());

        Set<CommandResult> results = new HashSet<>();
        results.add(firstResult);
        assertTrue(results.contains(equalResult));
        assertFalse(results.add(equalResult));
        assertEquals(1, results.size());
    }

    @Test
    public void getStudent_returnsOptionalStudent() {
        assertEquals(Optional.empty(), new CommandResult("feedback").getStudent());
        CommandResult result = new CommandResult("feedback", ALICE);
        assertEquals(Optional.of(ALICE), result.getStudent());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
    }

    @Test
    public void studentResult_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, ALICE));
    }

    @Test
    public void studentResult_toString_includesStudent() {
        CommandResult result = new CommandResult("feedback", ALICE);
        assertEquals(CommandResult.class.getCanonicalName()
                + "{feedbackToUser=feedback, showHelp=false, exit=false, student="
                + ALICE + "}", result.toString());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", student=null}";
        assertEquals(expected, commandResult.toString());
    }
}
