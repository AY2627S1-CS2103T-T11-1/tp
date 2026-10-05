package seedu.address.model.util;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Level;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Tan"), new Phone("91234567"), new Address("Blk 8 Bishan St 22, #05-13"),
                new Level("Sec 3"), new Subject("E-Math")),
            new Person(new Name("Priya Ramesh"), new Phone("87652210"), new Address("12 Clementi Rd"),
                new Level("Sec 1"), new Subject("E-Math")),
            new Person(new Name("Marcus Lim"), new Phone("90341188"), new Address("Blk 221 Tampines St 23, #09-41"),
                new Level("JC1"), new Subject("H2 Math")),
            new Person(new Name("Nur Aisyah"), new Phone("82219054"), new Address("35 Jalan Kembangan"),
                new Level("Sec 4"), new Subject("A-Math")),
            new Person(new Name("Ethan Koh"), new Phone("96553021"), new Address("Blk 402 Ang Mo Kio Ave 10, #12-07"),
                new Level("JC2"), new Subject("H2 Math")),
            new Person(new Name("Chloe Ng"), new Phone("88124470"), new Address("7 Holland Grove Rd"),
                new Level("Sec 2"), new Subject("E-Math"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

}
