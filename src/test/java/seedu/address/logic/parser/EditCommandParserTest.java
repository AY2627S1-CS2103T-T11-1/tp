package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_LEVEL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_SUBJECT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.LEVEL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.SUBJECT_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_nameWithAlOrAp_editsNameOnly() {
        // a/l and a/p are kept in the name and must not change the address
        for (String name : new String[]{"Muthu a/l Raju", "Siti a/p Ahmad"}) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(name).build();
            assertParseSuccess(parser, "1 n/" + name,
                    new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_malformedPreamble_failure() {
        // words, decimals and signs are malformed
        assertParseFailure(parser, "a" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1.5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "+1" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // extra text after the index
        assertParseFailure(parser, "1 some random string" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as part of the preamble
        assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidNumericIndex_failure() {
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "01" + NAME_DESC_AMY, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
        assertParseFailure(parser, "2147483648" + NAME_DESC_AMY, MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_LEVEL_DESC, Level.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_SUBJECT_DESC, Subject.MESSAGE_CONSTRAINTS);

        // invalid phone followed by valid level
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + LEVEL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_LEVEL_DESC + VALID_ADDRESS_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + ADDRESS_DESC_AMY + NAME_DESC_AMY
                + LEVEL_DESC_AMY + SUBJECT_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withAddress(VALID_ADDRESS_AMY).withLevel(VALID_LEVEL_AMY)
                .withSubject(VALID_SUBJECT_AMY).build();

        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + LEVEL_DESC_BOB;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withLevel("JC1").build();

        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String base = targetIndex.getOneBased() + "";

        assertParseSuccess(parser, base + NAME_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build()));
        assertParseSuccess(parser, base + PHONE_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build()));
        assertParseSuccess(parser, base + ADDRESS_DESC_AMY, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build()));
        assertParseSuccess(parser, base + LEVEL_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withLevel(VALID_LEVEL_AMY).build()));
        assertParseSuccess(parser, base + SUBJECT_DESC_AMY, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withSubject(VALID_SUBJECT_AMY).build()));
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // repeated phone
        assertParseFailure(parser, "1" + PHONE_DESC_AMY + PHONE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // repeated level, together with a valid field
        assertParseFailure(parser, "1" + NAME_DESC_AMY + LEVEL_DESC_AMY + LEVEL_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));
    }
}
