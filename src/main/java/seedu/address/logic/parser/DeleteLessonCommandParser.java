package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteLessonCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed student index and lesson number for a {@code DeleteLessonCommand}.
 */
public class DeleteLessonCommandParser implements Parser<DeleteLessonCommand> {
    /**
     * Parses exactly two indexes, allowing surrounding and repeated whitespace.
     *
     * @throws ParseException if the arguments are malformed or either index is invalid
     */
    @Override
    public DeleteLessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] indexes = args.trim().split("\\s+");
        if (indexes.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteLessonCommand.MESSAGE_USAGE));
        }

        Index studentIndex = ParserUtil.parseStudentIndex(indexes[0], DeleteLessonCommand.MESSAGE_USAGE);
        Index lessonIndex = ParserUtil.parseCommandIndex(indexes[1], DeleteLessonCommand.MESSAGE_USAGE,
                MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        return new DeleteLessonCommand(studentIndex, lessonIndex);
    }
}
