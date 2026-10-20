package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a student's name contains every keyword as a case-insensitive substring.
 * Adapted from AddressBook-Level3's {@code NameContainsKeywordsPredicate}
 * (https://github.com/se-edu/addressbook-level3), with all-keyword partial matching.
 */
public class NameContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Creates a predicate for a non-empty list of non-blank literal keywords.
     *
     * @throws NullPointerException if the list or a keyword is null.
     * @throws IllegalArgumentException if the list is empty or a keyword is blank.
     */
    public NameContainsKeywordsPredicate(List<String> keywords) {
        List<String> copiedKeywords = List.copyOf(keywords);
        checkArgument(!copiedKeywords.isEmpty(), "At least one search keyword is required.");
        checkArgument(copiedKeywords.stream().noneMatch(String::isBlank), "Search keywords must not be blank.");
        this.keywords = copiedKeywords.stream().map(keyword -> keyword.toLowerCase(Locale.ROOT)).toList();
    }

    @Override
    public boolean test(Person person) {
        String name = person.getName().fullName.toLowerCase(Locale.ROOT);
        return keywords.stream().allMatch(name::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NameContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return keywords.equals(otherPredicate.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
