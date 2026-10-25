package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Flag;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_LEVEL = "Sec#4";
    private static final String INVALID_SUBJECT = "Math!";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_LEVEL = BENSON.getLevel().toString();
    private static final String VALID_SUBJECT = BENSON.getSubject().toString();
    private static final List<JsonAdaptedLesson> VALID_LESSONS =
            List.of(new JsonAdaptedLesson("Quadratic inequalities", null),
                    new JsonAdaptedLesson("Binomial theorem", null));

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_personWithLessons_returnsPersonWithLessonsInOrder() throws Exception {
        Person personWithLessons = new PersonBuilder(BENSON)
                .withLessons("Quadratic inequalities", "Binomial theorem").build();
        Person loaded = new JsonAdaptedPerson(personWithLessons).toModelType();
        assertEquals(personWithLessons, loaded);
        assertEquals(List.of(new Lesson("Quadratic inequalities"), new Lesson("Binomial theorem")),
                loaded.getLessons());
    }

    @Test
    public void toModelType_nullLessons_returnsPersonWithNoLessons() throws Exception {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS, VALID_LEVEL, VALID_SUBJECT, null);
        assertEquals(List.of(), person.toModelType().getLessons());
    }

    @Test
    public void toModelType_personWithFlag_returnsFlagWithReason() throws Exception {
        Person flagged = new PersonBuilder(BENSON).withFlag("Call parent").build();
        assertEquals(flagged, new JsonAdaptedPerson(flagged).toModelType());

        JsonAdaptedPerson withoutReason = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS, new JsonAdaptedFlag((String) null));
        assertEquals(new Flag(), withoutReason.toModelType().getFlag().orElseThrow());
    }

    @Test
    public void toModelType_missingFlag_returnsUnflaggedPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS, null);
        assertEquals(java.util.Optional.empty(), person.toModelType().getFlag());
    }

    @Test
    public void toModelType_invalidFlagReason_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS, new JsonAdaptedFlag(" "));
        assertThrows(IllegalValueException.class, Flag.MESSAGE_REASON_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullLessonEntry_throwsIllegalValueException() {
        // a hand-edited data file such as "lessons": [ null ] must be rejected, not crash loading
        List<JsonAdaptedLesson> lessonsWithNull = java.util.Arrays.asList((JsonAdaptedLesson) null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                VALID_LEVEL, VALID_SUBJECT, lessonsWithNull);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Lesson.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidLesson_throwsIllegalValueException() {
        List<JsonAdaptedLesson> invalidLessons = List.of(new JsonAdaptedLesson(" ", null));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                        VALID_LEVEL, VALID_SUBJECT, invalidLessons);
        assertThrows(IllegalValueException.class, Lesson.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_ADDRESS,
                        VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(null, VALID_PHONE, VALID_ADDRESS,
                        VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_ADDRESS,
                        VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, null, VALID_ADDRESS,
                        VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_ADDRESS,
                        VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null,
                        VALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                        INVALID_LEVEL, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = Level.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullLevel_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                        null, VALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Level.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                        VALID_LEVEL, INVALID_SUBJECT, VALID_LESSONS);
        String expectedMessage = Subject.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullSubject_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_ADDRESS,
                        VALID_LEVEL, null, VALID_LESSONS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Subject.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

}
