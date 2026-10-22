package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_LEVEL = "Sec#4";
    private static final String INVALID_SUBJECT = "Math!";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "12345678";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_LEVEL = "Sec 4";
    private static final String VALID_SUBJECT = "E-Math";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        for (String input : new String[]{"0", "-1", "+1", "1.5", "10 a", "01", "001"}) {
            assertThrows(ParseException.class, () -> ParserUtil.parseIndex(input));
        }
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));

        // There is no arbitrary upper limit such as 1000
        assertEquals(Index.fromOneBased(1001), ParserUtil.parseIndex("1001"));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE),
                ParserUtil.parseIndex(Integer.toString(Integer.MAX_VALUE)));
    }

    @Test
    public void parseStudentIndex_invalidNumericInput_throwsStudentIndexMessage() {
        for (String input : new String[]{"0", "-1", "01", "001", "2147483648"}) {
            assertThrows(ParseException.class, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX, ()
                    -> ParserUtil.parseStudentIndex(input, "usage"));
        }
    }

    @Test
    public void parseStudentIndex_malformedInput_throwsCommandUsage() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, "usage");
        for (String input : new String[]{"", "+1", "1.5", "one", "1 2"}) {
            assertThrows(ParseException.class, expectedMessage, ()
                    -> ParserUtil.parseStudentIndex(input, "usage"));
        }
    }

    /**
     * Accepts a valid command index with surrounding whitespace and a caller-specific error message.
     */
    @Test
    public void parseCommandIndex_validInput_returnsIndex() throws Exception {
        assertEquals(Index.fromOneBased(2), ParserUtil.parseCommandIndex(" \t2 \t", "usage",
                MESSAGE_INVALID_LESSON_DISPLAYED_INDEX));
    }

    /**
     * Rejects missing index text, usage or error messages as programming errors.
     */
    @Test
    public void parseCommandIndex_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCommandIndex(null, "usage", "invalid"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCommandIndex("1", null, "invalid"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCommandIndex("1", "usage", null));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_blankUnicodeValue_reportsAddressConstraints() {
        for (String address : new String[]{"\u2003", "\u3000", "\u00a0", "\u200b", "\u2060", "\ufeff",
            " \t\u2003\u00a0\u200b\u2060 "}) {
            assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseAddress(address));
        }
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseName_repeatedInternalSpaces_returnsNormalisedName() throws Exception {
        Name expectedName = new Name("Tan Wei Ming");
        assertEquals(expectedName, ParserUtil.parseName("  Tan   Wei \t Ming  "));
    }

    @Test
    public void parsePhone_spacesBetweenDigits_returnsPhoneWithoutSpaces() throws Exception {
        Phone expectedPhone = new Phone("91234567");
        assertEquals(expectedPhone, ParserUtil.parsePhone("9123 4567"));
    }

    @Test
    public void parseLevel_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseLevel((String) null));
    }

    @Test
    public void parseLevel_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseLevel(INVALID_LEVEL));
    }

    @Test
    public void parseLevel_validValueWithoutWhitespace_returnsLevel() throws Exception {
        Level expectedLevel = new Level(VALID_LEVEL);
        assertEquals(expectedLevel, ParserUtil.parseLevel(VALID_LEVEL));
    }

    @Test
    public void parseLevel_validValueWithWhitespace_returnsTrimmedLevel() throws Exception {
        String levelWithWhitespace = WHITESPACE + VALID_LEVEL + WHITESPACE;
        Level expectedLevel = new Level(VALID_LEVEL);
        assertEquals(expectedLevel, ParserUtil.parseLevel(levelWithWhitespace));
    }

    @Test
    public void parseSubject_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseSubject((String) null));
    }

    @Test
    public void parseSubject_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseSubject(INVALID_SUBJECT));
    }

    @Test
    public void parseSubject_validValueWithoutWhitespace_returnsSubject() throws Exception {
        Subject expectedSubject = new Subject(VALID_SUBJECT);
        assertEquals(expectedSubject, ParserUtil.parseSubject(VALID_SUBJECT));
    }

    @Test
    public void parseSubject_validValueWithWhitespace_returnsTrimmedSubject() throws Exception {
        String subjectWithWhitespace = WHITESPACE + VALID_SUBJECT + WHITESPACE;
        Subject expectedSubject = new Subject(VALID_SUBJECT);
        assertEquals(expectedSubject, ParserUtil.parseSubject(subjectWithWhitespace));
    }

    @Test
    public void parseLessonContent_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseLessonContent(null));
    }

    @Test
    public void parseLessonContent_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseLessonContent(WHITESPACE));
        assertThrows(ParseException.class, () -> ParserUtil.parseLessonContent("a".repeat(Lesson.MAX_LENGTH + 1)));
    }

    @Test
    public void parseLessonDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseLessonDate(null));
    }

    @Test
    public void parseLessonDate_validValue_returnsDate() throws Exception {
        assertEquals(LocalDate.of(2026, 10, 21), ParserUtil.parseLessonDate("21/10/2026"));
        // single-digit day and month
        assertEquals(LocalDate.of(2026, 2, 1), ParserUtil.parseLessonDate("1/2/2026"));
        // leap day
        assertEquals(LocalDate.of(2024, 2, 29), ParserUtil.parseLessonDate("29/02/2024"));
        // surrounding whitespace
        assertEquals(LocalDate.of(2026, 10, 21), ParserUtil.parseLessonDate("  21/10/2026  "));
    }

    @Test
    public void parseLessonDate_invalidValue_throwsParseException() {
        for (String date : new String[]{"31/02/2026", "29/02/2025", "32/01/2026", "21/13/2026", "0/10/2026",
            "21/10/26", "2026-10-21", "21-10-2026", "21/10", "", "today", "+21/10/2026", "21 /10/2026"}) {
            assertThrows(ParseException.class, Lesson.MESSAGE_DATE_CONSTRAINTS, ()
                    -> ParserUtil.parseLessonDate(date));
        }
    }

    @Test
    public void parseLessonContent_blankUnicodeValue_reportsLessonConstraints() {
        for (String content : new String[]{"\u2003", "\u3000", "\u00a0", "\u200b", "\u2060", "\ufeff",
            " \t\u2003\u00a0\u200b\u2060 "}) {
            assertThrows(ParseException.class, Lesson.MESSAGE_CONSTRAINTS, ()
                    -> ParserUtil.parseLessonContent(content));
        }
    }

    @Test
    public void parseLessonContent_validValueWithWhitespace_returnsTrimmedLesson() throws Exception {
        Lesson expectedLesson = new Lesson("Surds  and  indices");
        assertEquals(expectedLesson, ParserUtil.parseLessonContent(WHITESPACE + "Surds  and  indices" + WHITESPACE));
    }
}
