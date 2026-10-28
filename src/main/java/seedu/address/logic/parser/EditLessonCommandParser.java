package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CONTENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditLessonCommand;
import seedu.address.logic.commands.EditLessonCommand.EditLessonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed student index, lesson number and new lesson details for an {@code EditLessonCommand}.
 */
public class EditLessonCommandParser implements Parser<EditLessonCommand> {

    /**
     * Parses exactly two indexes followed by at least one of {@code c/} and {@code date/}.
     *
     * @throws ParseException if the arguments are malformed, either index is invalid,
     *     a value is invalid or no field to edit is given
     */
    @Override
    public EditLessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_CONTENT, PREFIX_DATE);

        String[] indexes = argMultimap.getPreamble().split("(?U)\\s+");
        if (indexes.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditLessonCommand.MESSAGE_USAGE));
        }
        Index studentIndex = ParserUtil.parseStudentIndex(indexes[0], EditLessonCommand.MESSAGE_USAGE);
        Index lessonIndex = ParserUtil.parseCommandIndex(indexes[1], EditLessonCommand.MESSAGE_USAGE,
                MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_CONTENT, PREFIX_DATE);

        EditLessonDescriptor editLessonDescriptor = new EditLessonDescriptor();
        if (argMultimap.getValue(PREFIX_CONTENT).isPresent()) {
            editLessonDescriptor.setContent(
                    ParserUtil.parseLessonContent(argMultimap.getValue(PREFIX_CONTENT).get()).content);
        }
        if (argMultimap.getValue(PREFIX_DATE).isPresent()) {
            editLessonDescriptor.setDate(
                    ParserUtil.parseLessonDateWithNotesHint(argMultimap.getValue(PREFIX_DATE).get()));
        }

        if (!editLessonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditLessonCommand(studentIndex, lessonIndex, editLessonDescriptor);
    }
}
