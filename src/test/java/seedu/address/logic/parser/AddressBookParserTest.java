package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteLessonCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.EditLessonCommand;
import seedu.address.logic.commands.EditLessonCommand.EditLessonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FlagCommand;
import seedu.address.logic.commands.FlaggedCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.commands.UnflagCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Flag;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_view() throws Exception {
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON), parser.parseCommand("view 1"));
        assertThrows(ParseException.class, () -> parser.parseCommand("view"));
        assertThrows(ParseException.class, () -> parser.parseCommand("view 1 extra"));
    }

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_log() throws Exception {
        LogCommand command = (LogCommand) parser.parseCommand(LogCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_CONTENT + "Quadratic inequalities");
        assertEquals(new LogCommand(INDEX_FIRST_PERSON, new Lesson("Quadratic inequalities")), command);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    /**
     * Routes lesson deletion separately from deletion of an entire student.
     */
    @Test
    public void parseCommand_deleteLesson() throws Exception {
        assertEquals(new DeleteLessonCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON),
                parser.parseCommand("deletelesson 1 1"));
        assertThrows(ParseException.class, () -> parser.parseCommand("deletelesson 1"));
    }

    /**
     * Routes lesson editing separately from editing a student's details.
     */
    @Test
    public void parseCommand_editLesson() throws Exception {
        EditLessonDescriptor descriptor = new EditLessonDescriptor();
        descriptor.setContent("Vectors");
        assertEquals(new EditLessonCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, descriptor),
                parser.parseCommand(EditLessonCommand.COMMAND_WORD + " 1 1 " + PREFIX_CONTENT + "Vectors"));
        assertThrows(ParseException.class, () -> parser.parseCommand("editlesson 1 1"));
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
    }

    @Test
    public void parseCommand_listWithArguments_throwsParseException() {
        assertThrows(ParseException.class, ListCommand.MESSAGE_INVALID_ARGUMENTS, ()
                -> parser.parseCommand(ListCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getPersonDetails(person));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("Tan", "Wei"))),
                parser.parseCommand("find Tan Wei"));
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE), ()
                -> parser.parseCommand("find"));
    }

    @Test
    public void parseCommand_flagCommands() throws Exception {
        assertEquals(new FlagCommand(INDEX_FIRST_PERSON, new Flag("Call parent")),
                parser.parseCommand("flag 1 r/Call parent"));
        assertEquals(new UnflagCommand(INDEX_FIRST_PERSON), parser.parseCommand("unflag 1"));
        assertTrue(parser.parseCommand("flagged") instanceof FlaggedCommand);
        assertThrows(ParseException.class, FlaggedCommand.MESSAGE_INVALID_ARGUMENTS, () ->
                parser.parseCommand("flagged 1"));
    }

    @Test
    public void parseCommand_removedCommands_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("clear"));
    }
}
