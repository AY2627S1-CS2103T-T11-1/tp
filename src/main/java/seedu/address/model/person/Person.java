package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a student in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;

    // Data fields
    private final Address address;
    private final Level level;
    private final Subject subject;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Address address, Level level, Subject subject) {
        requireAllNonNull(name, phone, address, level, subject);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.level = level;
        this.subject = subject;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public Level getLevel() {
        return level;
    }

    public Subject getSubject() {
        return subject;
    }

    /**
     * Returns true if both students have the same phone number and the same name, ignoring case.
     * Two students may share a name, so the name alone does not identify a student.
     * This defines a weaker notion of equality between two students.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().fullName.equalsIgnoreCase(getName().fullName)
                && otherPerson.getPhone().equals(getPhone());
    }

    /**
     * Returns true if both students have the same identity and data fields.
     * This defines a stronger notion of equality between two students.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && address.equals(otherPerson.address)
                && level.equals(otherPerson.level)
                && subject.equals(otherPerson.subject);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, address, level, subject);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("address", address)
                .add("level", level)
                .add("subject", subject)
                .toString();
    }

}
