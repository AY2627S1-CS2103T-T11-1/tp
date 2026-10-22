# TutorLah agent brief

Coding agents load this file through `AGENTS.md` in the repo root. Claude
Code users add a local `CLAUDE.md` containing `@AGENTS.md` (Git ignores
it). If your tool reads neither, paste this file in before your task. It
replaces the "Shared rules" and "Shared conventions and agreed interfaces"
sections of the Oct 5 work plan.

Last updated: Oct 22, 2026, for issue #120 (logging a lesson with a past date).

**Owner:** aerodart (Git expert). Update this file and `tutorlah-context.md`
in the same PR that changes a decision, message or interface, and refresh
the date above.

**Order of authority:** course rules (section 9) come first, then this
file, then older team documents, then the agent's own defaults. If this
file disagrees with the code on `master`, or two sources disagree, stop and
ask the team. Do not guess.

---

## Non-negotiables

- Changes reach `master` only through a reviewed PR. Never push to the
  team repo (`upstream`).
- **Agents do not commit, push, or post issues, PRs, comments, reviews or
  releases.** The human does that after reading the change.
- No `Co-Authored-By:` trailers or "Generated with" lines for AI tools in
  commits or PR text. Credit AI use in the DG Acknowledgements instead, as
  the course's [reuse policy][reuse] requires for widespread use.
- Credit reused or adapted code and text next to where it is used and in
  the DG Acknowledgements. This includes other teams' tP work, which the
  course checks for automatically.
- Use made-up student data in tests, screenshots and examples. Run manual
  tests from an empty folder, never against your own `data/` folder.
- Report checks that failed or were skipped. Never describe intended
  behaviour as tested.
- Check the source before stating a fact: open the file, page or command
  output instead of answering from memory, and confirm a path exists
  before giving it.
- Before drafting an issue, search the open issues for the same scope.
- Use straight quotes (`"` and `'`) in code, messages and docs. Curly
  quotes break exact-text tests and copied commands.

---

## 1. Shared rules

- **Repo:** `AY2627S1-CS2103T-T11-1/tp`, a brownfield fork of AB3. Work in
  your own fork, on a new branch made from an up-to-date `master`
  (`git pull upstream master` first). Never commit on `master`.
- **One PR per task**, linked to its issue (`Fixes #N`), with the current
  milestone and one reviewer from the team.
- **Commit subjects** follow the course [Git convention][git]: imperative
  mood, capitalised, no full stop, **at most 50 characters** (72 is the hard
  limit). If a body is needed, leave a blank line and wrap at 72.
- **Commit bodies** say what and why, not how. Describe the situation in the
  present tense, then introduce the change with "Let's ...". Avoid
  "currently" and "originally".
- **PR text** (descriptions, issues, comments) is not wrapped: write each
  paragraph or bullet as one line, because GitHub shows every line break.
- **PR description:** `Fixes #N`, or `Fixes part of #N` for a partial fix.
  Add before-and-after screenshots for UI changes. Open the PR as a draft
  until CI passes. See the [GitHub conventions][github-conv].
- **Before merging:** merge the latest `master` into the branch and wait for
  CI to pass on that result. Never merge while CI is failing or has not run.
- **Merging:** use "Create a merge commit". Do not squash or rebase, and do
  not delete merged branches. To catch up with `master`, merge it into your
  branch. Do not rebase. These protect the course's grading scripts
  ([Appendix E][appendix-e]).
- **Merge commit subject:** type `Issue title #N (#PR)` into GitHub's merge
  title box, within 72 characters. For example:
  `Update UG command summary to TutorLah formats #99 (#105)`
- **Keep the PR to the task.** Do not rename classes, packages or files
  outside the task. In particular, do not rename `Person` to `Student`.
  Do not reformat or tidy files the task does not need: it hides the real
  change and credits the lines to the wrong author in RepoSense.
- **Checks:** `./gradlew clean check coverage` must pass. This is what CI
  runs (`.github/workflows/gradle.yml`), and it includes the tests and
  Checkstyle. New code needs tests. Codecov's patch target is the current
  coverage of `master` (about 85% on Oct 18), so new and changed lines must
  be covered at least that well.
- **Java 25:** the build needs JDK 25. If Gradle fails with
  `invalid source release: 25`, the shell is using an older JDK. Agent
  shells often skip SDKMAN, so set it first, for example on macOS:
  `export JAVA_HOME="$HOME/.sdkman/candidates/java/current"`.
- **Style:** match the surrounding AB3 code. Javadoc on public classes and
  methods. Write `equals` in the same layout as `Address#equals`.
  Use `ToStringBuilder` for `toString`, except `Lesson#toString` (section 5).
  No wildcard imports.
- **AI use:** the course expects AI use in the tP. Read and understand
  every line the agent writes before you commit it.

---

## 2. Commands (current)

| Command | Format | Success message |
|---|---|---|
| Add | `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT` | `New student added: {NAME}; Phone: {PHONE}; Address: {ADDRESS}; Level: {LEVEL}; Subject: {SUBJECT}` |
| Delete | `delete INDEX` | `Deleted student: {NAME}; Phone: {PHONE}; Address: {ADDRESS}; Level: {LEVEL}; Subject: {SUBJECT}` |
| Delete lesson | `deletelesson STUDENT_INDEX LESSON_INDEX` | `Deleted lesson {LESSON_INDEX} for {NAME}: {CONTENT}` |
| List | `list` | `Listed {COUNT} students.` (`Listed 1 student.` for one) or `No students found. Use add to create a student.` |
| Edit | `edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [l/LEVEL] [sub/SUBJECT]` | `Edited student: {NAME}; Phone: {PHONE}; Address: {ADDRESS}; Level: {LEVEL}; Subject: {SUBJECT}` |
| Find | `find KEYWORD [MORE_KEYWORDS]...` | `Found {COUNT} students.` (`Found 1 student.` for one) or `No matching students found. Use list to show all students.` |
| Log | `log INDEX c/CONTENT [date/DATE]` | `Logged lesson for {NAME} on {DATE}: {CONTENT}` |
| **View** | **`view INDEX`** | Header `Lesson records for {NAME} ({COUNT}):` then one line per lesson, `{NUMBER}. {DATE}: {CONTENT}`, or `No lessons logged yet for {NAME}.` |

**Lesson dates (#98, #120):** `log` dates each lesson with `date/DATE` if
given, otherwise today's date from the system clock. `{DATE}` is
`DD/MM/YYYY`, for example `18/10/2026`, and typed dates use the same format
(`1/2/2026` is also accepted). A future date gives `Lesson dates cannot be
in the future. Log a lesson on or after the day it is taught.` Lessons
saved before v1.4 have no date, and `view` shows `(undated)` in place of
`{DATE}`.

**Lesson order (#120):** each student's lessons are stored in date order.
A lesson logged for an earlier date is inserted by date, after lessons on
the same date. Undated lessons stay at the oldest end. `view` shows the
stored order reversed, and `deletelesson` uses the same numbers.

**`view` replaces `lessons`.** The team renamed the command on Oct 8.
Any older document that says `lessons INDEX`, `LessonsCommand` or
`lessons-command` means `view`.

**GUI shortcut:** left-clicking a populated student card runs `view INDEX` for
its current displayed index. Clicking it again refreshes the output. Existing
history messages and newest-first ordering apply, and typed command text is kept.
Successful views also highlight and scroll to that student's card. The shortcut
runs on the first primary mouse press, before selection can rebuild the cell.
List updates and programmatic selection changes do not execute this shortcut.

Other messages:

- Duplicate student: `This student already exists in the list.` (used by `add` and `edit`)
- `edit` with no field: `At least one field to edit must be provided.`
- `list` with extra text: `The list command does not accept parameters.`
- `find` without keywords: invalid command format with the find usage; the current filter is kept.
- Invalid index (see section 4): `The student index provided is invalid.`
- Invalid lesson index: `The lesson index provided is invalid.`

**Lesson deletion:** `deletelesson` uses the displayed student index and
the lesson number from `view` (newest first). Remove only that occurrence,
even if contents repeat. Keep the student, remaining lessons and current
filter. Deleting the last lesson leaves an empty history. Show confirmation;
use `view` or a card click to see the remaining records.

**Find policy (#101):** every whitespace-separated keyword must occur somewhere
in the student's name, ignoring case and keyword order. Partial names are
supported. Leading, trailing and repeated whitespace is ignored. Keywords
are literal text, not field prefixes, wildcards or spelling corrections.
Each find searches the full address book and keeps the original list order.
Displayed indexes restart at 1 and are used by commands and card clicks.
`list` restores all students; add and edit also restore the full list. View,
log, delete and deletelesson retain the filter. Find changes no stored
student or lesson data, and filters are not persisted. The empty-list panel says
`No students to display.` and `Type list to show all students, or add one:`,
followed by the add-command format and example. This offers useful guidance
both when a search hides saved students and when no students are saved.

---

## 3. Fields and validation

| Field | Prefix | Accepted | Invalid-value message |
|---|---|---|---|
| Name | `n/` | 1 to 100 characters after collapsing repeated spaces. Unicode letters, spaces, `'`, `-`, `.` and `/`. No digits. | `Names should contain only letters, spaces, apostrophes, hyphens, periods and slashes, and must not be blank.` |
| Phone | `p/` | Exactly 8 digits. Spaces inside are removed first. No `+` or country code. | `Phone numbers should be exactly 8 digits.` |
| Address | `a/` | 1 to 200 printable characters | `Addresses must not be blank and must be at most 200 characters.` |
| Level | `l/` | 1 to 20 characters: letters, digits, spaces | `Levels should contain only letters, digits and spaces, and must not be blank.` |
| Subject | `sub/` | 1 to 50 characters: letters, digits, spaces, hyphens | `Subjects should contain only letters, digits, spaces and hyphens, and must not be blank.` |
| Lesson content | `c/` | 1 to 300 printable characters, internal spaces kept | `Lesson content must not be blank and must be at most 300 characters.` |
| Lesson date | `date/` | Optional. A real date as `D/M/YYYY` or `DD/MM/YYYY` (digits only, four-digit year), from 01/01/2000 (`Lesson.EARLIEST_DATE`), not in the future | `Lesson dates should be real dates from 01/01/2000 onwards, in the format DD/MM/YYYY, for example 21/10/2026.` |

- Addresses and lesson content cannot consist entirely of Unicode whitespace
  or format characters, including non-breaking spaces, ZERO WIDTH SPACE
  (U+200B), WORD JOINER (U+2060) and BOM (U+FEFF). The model validators enforce
  this for typed commands and JSON loading, with the same messages above.
  Real text keeps internal spaces and formatting, including joiners used by
  written languages and emoji sequences.
- `/` is allowed in names so that `s/o` and `d/o` work. No prefix starts
  with `s/`, `o/` or `d/`. `date/` is used only by `log`, so lesson content
  such as `d/dx` and names such as `d/o` are unaffected. Names containing `a/l` or `a/p` still fail,
  because ` a/` is the address prefix. This is a known MVP limitation and
  must be stated in the User Guide.
- **`a/` in names with `edit`:** because every `edit` field is optional, `edit 1 n/Muthu a/l Raju` is accepted and changes both the name (`Muthu`) and the address (`l Raju`), instead of being rejected as with `add`. Typing `A/L` in capitals avoids it. The User Guide warns about this in the `edit` section and Known issue 3. A parser fix belongs in its own issue.
- **Duplicate student:** same phone, and names equal when case is ignored
  (`Person#isSamePerson`).

---

## 4. Decisions (0A) and their status

| # | Decision | Status on `master` |
|---|---|---|
| 1 | No lesson date in the MVP. `log` takes content only. | Done for the MVP. Replaced in v1.4 by issue #98: `log` dates each lesson with today's date. |
| 2 | **Order shown by `view`: most recent first.** Most recent date first, then most recently logged among lessons on the same date. | Agreed by the team on Oct 8 as reverse logging order (PR #63). Updated by issue #120: lessons are stored in date order when logged, so `view` and `deletelesson` need no sorting. |
| 3 | Email and tags removed from `Person`. They may return post-MVP as optional fields. | Done (PR #51) |
| 4 | `edit`, `find` and `clear` removed for the MVP. Delete the commands, parsers, tests, `AddressBookParser` cases, help and UG entries. Restore from Git history post-MVP. | Done (issue #72). `edit` restored in issue #100 and `find` in issue #101 for v1.4; `clear` remains removed. |
| 5 | `/` allowed in names | Done (PR #51) |
| 6 | Case-insensitive duplicate names | Done (PR #51) |
| 7 | **Invalid index behaviour.** See below. | Applied consistently to `delete`, `log`, and `view` in issue #59. |

**On decision 7.** An index is a positive integer without leading zeros.
There is no arbitrary limit of 1000; any value representable by `Index` is
accepted by the parser. Commands apply these errors consistently:

- Zero, negative integers, leading-zero integers, values too large for
  `Index`, and indexes outside the displayed list give
  `The student index provided is invalid.`
- Missing indexes, words, decimals, signs such as `+1`, and extra command
  parts give the invalid command format message with that command's usage.
- `deletelesson` applies these rules to both indexes. An invalid lesson
  number gives `The lesson index provided is invalid.`. For a valid positive
  lesson number but no lessons, show `No lessons logged yet for {NAME}.`.
  Check indexes and history before changing data.

---

## 5. Agreed Java interfaces (as on `master`)

- `seedu.address.model.person.Level` and `Subject`: same shape as
  `Address` (public `value`, `isValidLevel` / `isValidSubject`,
  `MESSAGE_CONSTRAINTS`, `MAX_LENGTH`).
- `Person(Name, Phone, Address, Level, Subject)` creates a student with no
  lessons. `Person(Name, Phone, Address, Level, Subject, List<Lesson>)`
  creates one with lessons.
- `Person#getLessons()` returns an unmodifiable list, oldest first.
  `Person#withLessonAdded(Lesson)` returns a new `Person` with the lesson
  inserted in date order (see "Lesson order" in section 2).
- `seedu.address.model.lesson.Lesson`: immutable. `Lesson(String content)`
  is undated and `Lesson(String content, LocalDate date)` is dated. Public
  `content`, `getDate()` (an `Optional<LocalDate>`), `withDate(LocalDate)`,
  `isValidContent`, `MESSAGE_CONSTRAINTS`, `MAX_LENGTH`. Two lessons are
  equal only if both content and date are equal.
- `Lesson#toString` returns the content only, because command confirmations
  print lessons through it. Keep it that way and do not switch it to
  `ToStringBuilder`.
- `LogCommand(Index, Lesson)` keeps a dated lesson's date and dates an
  undated lesson with the system clock. `LogCommand(Index, Lesson, Clock)`
  lets tests fix today's date. It rejects dates after today.
- `ParserUtil.parseLessonDate(String)` parses `DD/MM/YYYY` strictly and
  throws `ParseException` with `Lesson.MESSAGE_DATE_CONSTRAINTS`.
- `Messages.formatLessonDate(Lesson)` returns the `DD/MM/YYYY` date, or
  `(undated)`, using `Messages.LESSON_DATE_FORMATTER`. Use it wherever a
  lesson date is shown.
- `JsonAdaptedLesson` stores the date as `"date" : "YYYY-MM-DD"` and leaves
  the field out for undated lessons. A missing date loads as undated, and an
  invalid date makes the data file invalid.
- **Testing with dates:** `log` run through `Logic` uses today's date, so
  build expected text with `LessonDateUtil.todayText()` and expected lessons
  with `LessonDateUtil.loggedToday(...)` (in `testutil`). Unit tests of
  `LogCommand` pass a fixed `Clock` instead. `PersonBuilder#withLessons`
  creates undated lessons, which `view` shows as `(undated)`.
- `Person#withLessonRemoved(int zeroBasedIndex)` returns a new `Person`
  without the selected lesson in stored order. Other fields are kept.
- Logging a lesson is `model.setPerson(target, target.withLessonAdded(lesson))`.
  Deleting a student removes their lessons, because lessons live inside
  `Person`.
- `Messages.format(Person)` prints the add and delete format above.
- `PersonListPanel(ObservableList<Person>, Consumer<Index>)` invokes the callback
  for primary-button clicks on populated cards. `MainWindow` routes that index
  through `Logic.execute("view " + index.getOneBased())` and displays its result.
- `ViewCommand` returns `CommandResult(feedback, Index)`. The optional
  `getViewedStudentIndex()` identifies the displayed student to highlight.
  Other commands leave it empty. `MainWindow` passes successful view indexes to
  `PersonListPanel#selectPerson(Index)`, which highlights and scrolls without
  executing another command or taking focus from the command box.
- Prefixes in `CliSyntax`: `n/`, `p/`, `a/`, `l/`, `sub/`, `c/`, `date/`.
- `ParserUtil#parseCommandIndex(text, usage, invalidIndexMessage)` shares
  student and lesson index validation. `parseStudentIndex` delegates to it
  with the student-index error message.
- `EditCommand(Index, EditPersonDescriptor)` edits name, phone, address, level and subject. It rebuilds the student with the 6-argument `Person` constructor, carrying `getLessons()` across, so lessons are never lost on edit.
- `FindCommand(NameContainsKeywordsPredicate)` replaces the displayed filter. The predicate requires a non-empty list of non-blank keywords and keeps an immutable copy as typed. Each keyword matches a literal name substring using locale-independent `String#regionMatches(true, ...)`, without lowercasing the name or keywords. Predicate equality and hashing use the stored keyword list. `FindCommandParser` splits on whitespace without interpreting prefixes.
- **Any code that rebuilds a `Person`** (for example an edit command) must
  carry `getLessons()` across. The 5-argument constructor starts with no
  lessons and would silently delete them.

---

## 6. Known gaps on `master` (Oct 15)

These are mismatches with this brief. Fix them only if your task covers
them.

No known mismatches recorded. `docs/images/Ui.png` shows the current TutorLah
GUI with `view 1` and fictional student and lesson details (issue #93).

---

## 7. Verification checklist (run before every PR)

Ask the agent to run each check and report the result. Open the PR only
when all pass.

- [ ] Branch made from an up-to-date `master`. No commits on `master`.
- [ ] `./gradlew clean check coverage` passes (tests and Checkstyle, as in CI).
- [ ] Every acceptance criterion in the task is met. Quote each one and say
      how it was checked.
- [ ] Every user-facing message matches this brief character for character.
- [ ] New or changed code has tests, including the boundary values in
      section 3.
- [ ] Only the files the task needs changed (`git diff --stat master`).
- [ ] The app starts with `./gradlew run` and the feature works by hand.
      For a release, test the JAR as in section 11.
- [ ] Commit subjects: imperative, capitalised, no full stop, at most 50
      characters.
- [ ] PR: base `AY2627S1-CS2103T-T11-1/tp` `master`, `Fixes #N`, current
      milestone, one reviewer.
- [ ] After merge: `git switch master`, `git pull upstream master`,
      `git push origin master`.

When the agent finishes, it reports in four parts:

- **Changed:** the files, and why each one changed.
- **Verified:** the exact commands or steps, with their output.
- **Not verified:** what was not checked, and why.
- **Risks:** anything the team needs to decide or approve.

---

## 8. Designing user-facing behaviour

From the course guidance on [functionality bugs][bugs] and
[feature flaws][flaws]. A PE tester judges the app by what a reasonable
tutor expects, not by what was easy to parse.

- Plan for likely mistakes, not only the normal case. A typo must not crash
  the app, corrupt the data file or leave the app unusable.
- Do not restrict input just to make parsing easier. Real names, addresses
  and levels must be possible to enter. Known gap: names with `a/l` or
  `a/p` (UG Known issue 3).
- Prefer a warning to a rejection for unusual but harmless input. Reject
  only when accepting would make the data ambiguous or wrong.
- Error messages name the field that is wrong and say why.
- Duplicate checks compare normalised values (case, repeated spaces). The
  UG states what the check does not catch.
- Long values must not break the student cards or hide the result box.
- The data file stays editable by hand. After a format change, files saved
  by the previous release must still load, with a test that proves it.
- Every restriction a user can run into is in the UG.
- Change validation rules in their own issue, not inside an unrelated PR.
- **Open question (#106):** is "exactly 8 digits" too strict for a phone
  number, for example an overseas parent's number? The rule stands until
  the team decides.

---

## 9. Course constraints

From the [tP constraints][constraints] page. These are course rules, not
team choices.

- Typing is the main way to use the app. It is for a single user.
- Data is stored locally in a human-editable text file. No database
  (DBMS) and no remote server run by the team.
- It runs on Windows, macOS and Linux with only Java 25 installed, without
  an installer.
- Third-party libraries need approval from the teaching team first.
- The GUI works well at 1920x1080 and above at 100% and 125% scale, and is
  usable at 1280x720 and above at 150% scale.
- The product is one JAR file of at most 100 MB.

---

## 10. Reviewing PRs

From [Appendix E][appendix-e] and the [SE-EDU review guide][reviewing].

- Put most feedback in inline comments on the relevant lines, plus one
  overall comment. The course values location-specific comments more in
  grading.
- Ask questions rather than give orders, and include the input that fails
  and a possible fix. Say what works well and why ("I like how ...").
- Run anything you call a bug before you post it, or say that you did not.
- Do not repeat a comment at every occurrence. Note that it appears in
  other places too. Do not repeat another reviewer's comment either: reply
  to it with new evidence instead.
- Check that the PR is one complete change, with tests and docs updated, a
  correct title and description, and no unrelated edits.
- Point out overlaps with other open PRs, and say whether a conflict was
  actually checked.
- Approve only after your earlier comments are addressed.
- Use plain English with no slang. Agents may draft review comments, but
  the reviewer reads, edits and posts them.

---

## 11. Iterations and releases

- Before closing a milestone, compare it with what actually shipped, and
  move unfinished issues to the next milestone.
- A release has the version tag, the JAR from `./gradlew shadowJar`
  (`build/libs/TutorLah.jar`), and notes listing what changed since the
  previous release, with screenshots committed under `docs/images/`.
- Before publishing, copy the JAR to an empty folder and run it with
  `java -jar TutorLah.jar` under Java 25. Add, log, view and exit, then
  start it again and check that the data was kept.
- For the feature freeze and final submission, read the live
  [Week 12 page][w12]. Do not rely on remembered limits.

[reuse]: https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixB-policies.html#policy-on-reuse
[appendix-e]: https://nus-cs2103-ay2627-s1.github.io/website/admin/appendixE-gitHub.html
[constraints]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html
[bugs]: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/project.html#triaging-functionality-bugs
[flaws]: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week8/project.html#triaging-feature-flaw-bugs
[w12]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-w12.html
[git]: https://se-education.org/guides/conventions/git.html
[github-conv]: https://se-education.org/guides/conventions/github.html
[reviewing]: https://se-education.org/guides/guidelines/PRs-reviewing.html
