package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a student's name, subject and level each contain every keyword given for that field,
 * as case-insensitive substrings. A field with no keywords is not searched.
 * Adapted from AddressBook-Level3's {@code NameContainsKeywordsPredicate}
 * (https://github.com/se-edu/addressbook-level3), with all-keyword partial matching on more fields.
 */
public class StudentContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> nameKeywords;
    private final List<String> subjectKeywords;
    private final List<String> levelKeywords;

    /**
     * Creates a predicate from the keywords for each field. At least one keyword must be given in total.
     *
     * @throws NullPointerException if a list or a keyword is null.
     * @throws IllegalArgumentException if no keyword is given or a keyword is blank.
     */
    public StudentContainsKeywordsPredicate(List<String> nameKeywords, List<String> subjectKeywords,
            List<String> levelKeywords) {
        this.nameKeywords = List.copyOf(nameKeywords);
        this.subjectKeywords = List.copyOf(subjectKeywords);
        this.levelKeywords = List.copyOf(levelKeywords);
        checkArgument(!this.nameKeywords.isEmpty() || !this.subjectKeywords.isEmpty()
                || !this.levelKeywords.isEmpty(), "At least one search keyword is required.");
        checkArgument(isNoneBlank(this.nameKeywords) && isNoneBlank(this.subjectKeywords)
                && isNoneBlank(this.levelKeywords), "Search keywords must not be blank.");
    }

    @Override
    public boolean test(Person person) {
        return containsAll(person.getName().fullName, nameKeywords)
                && containsAll(person.getSubject().value, subjectKeywords)
                && containsAll(person.getLevel().value, levelKeywords);
    }

    /**
     * Returns true if only name keywords were given, so subjects and levels are not searched.
     */
    public boolean isNameSearchOnly() {
        return subjectKeywords.isEmpty() && levelKeywords.isEmpty();
    }

    public List<String> getNameKeywords() {
        return nameKeywords;
    }

    public List<String> getSubjectKeywords() {
        return subjectKeywords;
    }

    public List<String> getLevelKeywords() {
        return levelKeywords;
    }

    /**
     * Returns searches in which the last words of a multi-word subject or level search are name keywords
     * instead, for example a name typed after {@code sub/}. Each field keeps at least one keyword. Searches
     * moving fewer words come first, and subject searches come before level searches.
     */
    public List<StudentContainsKeywordsPredicate> withTrailingFieldWordsAsNames() {
        List<StudentContainsKeywordsPredicate> alternatives = new ArrayList<>();
        for (int movedCount = 1; movedCount < subjectKeywords.size(); movedCount++) {
            alternatives.add(new StudentContainsKeywordsPredicate(
                    withNames(lastWords(subjectKeywords, movedCount)),
                    firstWords(subjectKeywords, movedCount), levelKeywords));
        }
        for (int movedCount = 1; movedCount < levelKeywords.size(); movedCount++) {
            alternatives.add(new StudentContainsKeywordsPredicate(
                    withNames(lastWords(levelKeywords, movedCount)),
                    subjectKeywords, firstWords(levelKeywords, movedCount)));
        }
        return alternatives;
    }

    private List<String> withNames(List<String> movedKeywords) {
        List<String> names = new ArrayList<>(nameKeywords);
        names.addAll(movedKeywords);
        return names;
    }

    private static List<String> firstWords(List<String> keywords, int movedCount) {
        return keywords.subList(0, keywords.size() - movedCount);
    }

    private static List<String> lastWords(List<String> keywords, int movedCount) {
        return keywords.subList(keywords.size() - movedCount, keywords.size());
    }

    /**
     * Returns true if {@code text} contains every keyword, in any order. An empty list matches any text.
     */
    private static boolean containsAll(String text, List<String> keywords) {
        return keywords.stream().allMatch(keyword -> StringUtil.containsIgnoreCase(text, keyword));
    }

    private static boolean isNoneBlank(List<String> keywords) {
        return keywords.stream().noneMatch(String::isBlank);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof StudentContainsKeywordsPredicate otherPredicate)) {
            return false;
        }

        return nameKeywords.equals(otherPredicate.nameKeywords)
                && subjectKeywords.equals(otherPredicate.subjectKeywords)
                && levelKeywords.equals(otherPredicate.levelKeywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameKeywords, subjectKeywords, levelKeywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("nameKeywords", nameKeywords)
                .add("subjectKeywords", subjectKeywords)
                .add("levelKeywords", levelKeywords)
                .toString();
    }
}
