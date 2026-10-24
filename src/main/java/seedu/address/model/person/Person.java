package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.Lesson;

/**
 * Represents a student in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    private static final Logger logger = LogsCenter.getLogger(Person.class);

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
     * Returns a copy of this student with {@code lesson} added in date order.
     * The lesson goes after every lesson that is undated or dated on or before its date,
     * so lessons on the same date keep the order they were logged in,
     * and undated lessons stay at the oldest end.
     * This student is not modified.
     */
    public Person withLessonAdded(Lesson lesson) {
        requireNonNull(lesson);
        List<Lesson> updatedLessons = new ArrayList<>(lessons);
        int insertAt = updatedLessons.size();
        while (insertAt > 0 && isDatedAfter(updatedLessons.get(insertAt - 1), lesson)) {
            insertAt--;
        }
        if (insertAt < updatedLessons.size()) {
            logger.fine("Inserted a backdated lesson at stored position " + (insertAt + 1) + " of "
                    + (updatedLessons.size() + 1) + ", before newer lessons");
        }
        updatedLessons.add(insertAt, lesson);
        return new Person(name, phone, address, level, subject, updatedLessons);
    }

    /**
     * Returns true if {@code existing} belongs after {@code added} in date order.
     * Undated lessons count as older than every dated lesson.
     */
    private static boolean isDatedAfter(Lesson existing, Lesson added) {
        if (existing.getDate().isEmpty()) {
            return false;
        }
        return added.getDate().map(addedDate -> existing.getDate().get().isAfter(addedDate)).orElse(true);
    }

    /**
     * Returns a copy of this student without the lesson at {@code zeroBasedIndex} in logging order.
     * This student and the order of the remaining lessons are unchanged.
     *
     * @throws IndexOutOfBoundsException if the index does not identify an existing lesson
     */
    public Person withLessonRemoved(int zeroBasedIndex) {
        List<Lesson> updatedLessons = new ArrayList<>(lessons);
        updatedLessons.remove(zeroBasedIndex);
        return new Person(name, phone, address, level, subject, updatedLessons);
    }

    /**
     * Returns a copy of this student with the lesson at {@code zeroBasedIndex} in logging order
     * replaced by {@code lesson}, which keeps that position.
     * The replacement must have the same date, so that the lessons stay in date order.
     * To change a lesson's date, remove it and add the changed lesson instead.
     * This student is not modified.
     *
     * @throws IndexOutOfBoundsException if the index does not identify an existing lesson
     */
    public Person withLessonReplaced(int zeroBasedIndex, Lesson lesson) {
        requireNonNull(lesson);
        List<Lesson> updatedLessons = new ArrayList<>(lessons);
        Lesson replacedLesson = updatedLessons.set(zeroBasedIndex, lesson);
        assert replacedLesson.getDate().equals(lesson.getDate())
                : "Replacing a lesson must keep its date, or the lessons fall out of date order";
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
