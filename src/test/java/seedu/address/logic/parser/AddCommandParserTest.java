package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_LEVEL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_SUBJECT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_unsupportedPrefixInInvalidField_keepsErrorAndAddsHint() {
        String hint = "If t/ was intended as a field, add accepts only n/, p/, addr/, l/ and sub/.";
        for (String separator : new String[]{" ", "\u00a0", "\t", "\u2003"}) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                    + " sub/Math" + separator + "t/friend", Subject.MESSAGE_CONSTRAINTS + "\n" + hint);
        }
        assertParseFailure(parser, " sub/Math t/friend" + LEVEL_DESC_BOB + ADDRESS_DESC_BOB
                + PHONE_DESC_BOB + NAME_DESC_BOB, Subject.MESSAGE_CONSTRAINTS + "\n" + hint);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + " sub/x/extra", Subject.MESSAGE_CONSTRAINTS + "\n"
                + String.format(AddCommandParser.MESSAGE_UNSUPPORTED_PREFIX_HINT, "x/"));

        ParseException error = assertThrows(ParseException.class, () -> parser.parse(NAME_DESC_BOB
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB + " sub/Math t/friend"));
        assertEquals(Subject.MESSAGE_CONSTRAINTS, error.getCause().getMessage());
    }

    @Test
    public void parse_prefixLikeTextInOtherInvalidFields_addsHintToFirstError() {
        String hint = "\n" + String.format(AddCommandParser.MESSAGE_UNSUPPORTED_PREFIX_HINT, "x/");
        assertParseFailure(parser, " n/Bob x/1" + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, Name.MESSAGE_CONSTRAINTS + hint);
        assertParseFailure(parser, NAME_DESC_BOB + " p/22222222 x/extra" + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, Phone.MESSAGE_CONSTRAINTS + hint);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " addr/" + "A".repeat(Address.MAX_LENGTH)
                + " x/extra" + LEVEL_DESC_BOB + SUBJECT_DESC_BOB, Address.MESSAGE_CONSTRAINTS + hint);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + " l/Sec 3 x/extra"
                + SUBJECT_DESC_BOB, Level.MESSAGE_CONSTRAINTS + hint);
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + " sub/Math x/extra", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_slashTextInValidFields_preservesValues() {
        for (String address : new String[]{"Block 1 #02/03", "Home c/o Mum", "See https://example.com/a/b",
            "Home x/annex"}) {
            Person expected = new PersonBuilder(BOB).withName("Ramesh s/o Kumar").withAddress(address).build();
            assertParseSuccess(parser, " n/Ramesh s/o Kumar" + PHONE_DESC_BOB + " addr/" + address
                    + LEVEL_DESC_BOB + SUBJECT_DESC_BOB, new AddCommand(expected));
        }
    }

    @Test
    public void parse_invalidFieldWithoutUnsupportedToken_keepsOriginalError() {
        for (String subject : new String[]{"Math!", "Maths@Physics", "sub/Math", "Math (x/extra)"}) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                    + " sub/" + subject, Subject.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                + LEVEL_DESC_BOB + SUBJECT_DESC_BOB, new AddCommand(expectedPerson));

        // fields in a different order
        assertParseSuccess(parser, SUBJECT_DESC_BOB + LEVEL_DESC_BOB + ADDRESS_DESC_BOB + PHONE_DESC_BOB
                + NAME_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_specExamples_success() {
        Person tanWeiMing = new PersonBuilder().withName("Tan Wei Ming").withPhone("87654321")
                .withAddress("Blk 8 Bishan St 22 #05-13").withLevel("JC1").withSubject("H2 Math").build();
        assertParseSuccess(parser, " n/Tan Wei Ming p/87654321 addr/Blk 8 Bishan St 22 #05-13 l/JC1 sub/H2 Math",
                new AddCommand(tanWeiMing));

        Person maryAnneLim = new PersonBuilder().withName("Mary-Anne Lim").withPhone("91234567")
                .withAddress("12 Clementi Rd").withLevel("Sec 4").withSubject("E-Math").build();
        assertParseSuccess(parser, " n/Mary-Anne Lim p/91234567 addr/12 Clementi Rd l/Sec 4 sub/E-Math",
                new AddCommand(maryAnneLim));
    }

    @Test
    public void parse_nameWithAlOrAp_keepsFullName() {
        // a/l and a/p are part of Malay and Indian names, not an address prefix
        Person muthu = new PersonBuilder().withName("Muthu a/l Raju").withPhone("81234567")
                .withAddress("1 Example Street").withLevel("Sec 3").withSubject("E-Math").build();
        assertParseSuccess(parser, " n/Muthu a/l Raju p/81234567 addr/1 Example Street l/Sec 3 sub/E-Math",
                new AddCommand(muthu));

        Person siti = new PersonBuilder().withName("Siti a/p Ahmad").withPhone("81234568")
                .withAddress("2 Example Street").withLevel("JC1").withSubject("H2 Math").build();
        assertParseSuccess(parser, " addr/2 Example Street n/Siti a/p Ahmad p/81234568 l/JC1 sub/H2 Math",
                new AddCommand(siti));
    }

    @Test
    public void parse_oldAddressPrefixWithoutAddr_showsUsageWithHint() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE)
                + "\n" + AddCommandParser.MESSAGE_ADDRESS_PREFIX_HINT;
        assertParseFailure(parser, " n/Amy Tan p/81234567 a/1 Example Street l/Sec 3 sub/E-Math", expectedMessage);
        assertParseFailure(parser, " a/1 Example Street n/Amy Tan p/81234567 l/Sec 3 sub/E-Math", expectedMessage);
    }

    @Test
    public void parse_missingAddressWithoutOldPrefix_showsUsageOnly() {
        assertParseFailure(parser, " n/Amy Tan p/81234567 l/Sec 3 sub/E-Math",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_valuesNeedingNormalisation_success() {
        // repeated spaces in the name are reduced to one, spaces inside the phone number are removed
        Person expectedPerson = new PersonBuilder(BOB).build();
        assertParseSuccess(parser, " n/Bob    Choo p/2222 2222" + ADDRESS_DESC_BOB + LEVEL_DESC_BOB + SUBJECT_DESC_BOB,
                new AddCommand(expectedPerson));

        // names with s/o and d/o are accepted, as no prefix starts with "s/", "o/" or "d/"
        Person sonOf = new PersonBuilder(BOB).withName("Ramesh s/o Kumar").build();
        assertParseSuccess(parser, " n/Ramesh s/o Kumar" + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, new AddCommand(sonOf));
    }

    @Test
    public void parse_repeatedValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                + LEVEL_DESC_BOB + SUBJECT_DESC_BOB;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple addresses
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple levels
        assertParseFailure(parser, LEVEL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));

        // multiple subjects
        assertParseFailure(parser, SUBJECT_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + LEVEL_DESC_AMY + SUBJECT_DESC_AMY,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS,
                        PREFIX_LEVEL, PREFIX_SUBJECT));

        // invalid value followed by valid value
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
        assertParseFailure(parser, INVALID_LEVEL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));
        assertParseFailure(parser, INVALID_SUBJECT_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));

        // valid value followed by invalid value
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
        assertParseFailure(parser, validExpectedPersonString + INVALID_LEVEL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));
        assertParseFailure(parser, validExpectedPersonString + INVALID_SUBJECT_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, expectedMessage);

        // missing address prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_ADDRESS_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, expectedMessage);

        // missing level prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + VALID_LEVEL_BOB
                + SUBJECT_DESC_BOB, expectedMessage);

        // missing subject prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + VALID_SUBJECT_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_ADDRESS_BOB + VALID_LEVEL_BOB
                + VALID_SUBJECT_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_ADDRESS_DESC + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, Address.MESSAGE_CONSTRAINTS);

        // invalid level
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + INVALID_LEVEL_DESC
                + SUBJECT_DESC_BOB, Level.MESSAGE_CONSTRAINTS);

        // invalid subject
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB + LEVEL_DESC_BOB
                + INVALID_SUBJECT_DESC, Subject.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + INVALID_ADDRESS_DESC + LEVEL_DESC_BOB
                + SUBJECT_DESC_BOB, Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB
                + LEVEL_DESC_BOB + SUBJECT_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}
