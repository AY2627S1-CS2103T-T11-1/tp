package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.Lesson;

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
    private final List<Lesson> lessons;

    /**
     * Creates a student with no lesson records.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Address address, Level level, Subject subject) {
        this(name, phone, address, level, subject, List.of());
    }

    /**
     * Creates a student with the given lesson records, kept in the order given.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Address address, Level level, Subject subject, List<Lesson> lessons) {
        requireAllNonNull(name, phone, address, level, subject, lessons);
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.level = level;
        this.subject = subject;
        this.lessons = List.copyOf(lessons);
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
     * Returns an immutable list of this student's lesson records, in the order they were logged,
     * which throws {@code UnsupportedOperationException} if modification is attempted.
     */
    public List<Lesson> getLessons() {
        return lessons;
    }

    /**
     * Returns a copy of this student with {@code lesson} added after the existing lesson records.
     * This student is not modified.
     */
    public Person withLessonAdded(Lesson lesson) {
        requireNonNull(lesson);
        List<Lesson> updatedLessons = new ArrayList<>(lessons);
        updatedLessons.add(lesson);
        return new Person(name, phone, address, level, subject, updatedLessons);
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
                && subject.equals(otherPerson.subject)
                && lessons.equals(otherPerson.lessons);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, address, level, subject, lessons);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("address", address)
                .add("level", level)
                .add("subject", subject)
                .add("lessons", lessons)
                .toString();
    }

}
