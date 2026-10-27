package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

/**
 * Tests the text helpers that the student card and detail panel share, without starting JavaFX.
 */
public class PersonCardTest {

    @Test
    public void initialsOf_variousNames_returnsUpToTwoUppercaseInitials() {
        assertEquals("PR", PersonCard.initialsOf("Priya Ramesh"));
        assertEquals("A", PersonCard.initialsOf("Alex"));
        assertEquals("TW", PersonCard.initialsOf("tan wei ming"));
        assertEquals("MA", PersonCard.initialsOf("Muthu A/L Raju"));
        assertEquals("MO", PersonCard.initialsOf("Mary-Anne O'Brien"));
        assertEquals("ÉT", PersonCard.initialsOf("Élodie Tan"));
        assertEquals("黄", PersonCard.initialsOf("黄小明"));
        // a letter outside the Basic Multilingual Plane is kept whole and uppercased
        assertEquals("𐐀T", PersonCard.initialsOf("𐐨an Tan"));
    }

    @Test
    public void initialsOf_noWordStartsWithLetter_returnsPlaceholder() {
        assertEquals("?", PersonCard.initialsOf(""));
        assertEquals("?", PersonCard.initialsOf("   "));
        assertEquals("?", PersonCard.initialsOf("-"));
        assertEquals("?", PersonCard.initialsOf("' ."));
        assertEquals("DT", PersonCard.initialsOf("- Dan  'x Tan"));
    }

    @Test
    public void formatLevelAndSubject_joinsWithMiddleDot() {
        assertEquals("Sec 3 · Physics", PersonCard.formatLevelAndSubject(
                new PersonBuilder().withLevel("Sec 3").withSubject("Physics").build()));
    }
}
