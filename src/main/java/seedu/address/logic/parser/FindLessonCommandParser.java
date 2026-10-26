package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FROM;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FindLessonCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonSearchPredicate;

/**
 * Parses a displayed student index and content keywords before optional {@code from/} and {@code to/} dates.
 */
public class FindLessonCommandParser implements Parser<FindLessonCommand> {
    private static final String BLANK_KEYWORD_REGEX = "[\\p{IsWhite_Space}\\p{Cf}]*";

    /**
     * Parses a one-off lesson search, allowing future date boundaries but rejecting reversed ranges.
     *
     * @throws ParseException if the index, criteria, dates or prefix occurrences are invalid.
     */
    @Override
    public FindLessonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        // The tokenizer recognises prefixes after ASCII spaces; pasted Unicode whitespace must work too.
        String normalisedArgs = " " + args.replaceAll("(?U)\\s", " ");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalisedArgs, PREFIX_FROM, PREFIX_TO);
        String[] preamble = arguments.getPreamble().split("\\s+", 2);
        Index studentIndex = ParserUtil.parseStudentIndex(preamble[0], FindLessonCommand.MESSAGE_USAGE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_FROM, PREFIX_TO);

        List<String> keywords = preamble.length == 1 ? List.of() : Arrays.asList(preamble[1].split("\\s+"));
        LocalDate fromDate = parseBoundary(arguments, PREFIX_FROM);
        LocalDate toDate = parseBoundary(arguments, PREFIX_TO);
        if ((keywords.isEmpty() && fromDate == null && toDate == null)
                || keywords.stream().anyMatch(word -> word.matches(BLANK_KEYWORD_REGEX))) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindLessonCommand.MESSAGE_USAGE));
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ParseException(LessonSearchPredicate.MESSAGE_REVERSED_RANGE);
        }
        return new FindLessonCommand(studentIndex, new LessonSearchPredicate(keywords, fromDate, toDate));
    }

    /** Returns a parsed date boundary, or null if its prefix was not supplied. */
    private static LocalDate parseBoundary(ArgumentMultimap arguments, Prefix prefix) throws ParseException {
        if (arguments.getValue(prefix).isEmpty()) {
            return null;
        }
        return ParserUtil.parseLessonDate(arguments.getValue(prefix).get());
    }
}
