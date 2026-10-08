# TutorLah agent brief

Paste this whole file into your coding agent before giving it your task.
It replaces the "Shared rules" and "Shared conventions and agreed
interfaces" sections of the Oct 5 work plan, which are out of date.
Last updated: Oct 9, 2026, checked against `master` on that date.

**Owner:** aerodart (Git expert). Update this file and `tutorlah-context.md`
in the same PR that changes a decision, message or interface, and refresh
the date above.

If anything here disagrees with an older document, this file wins. If it
disagrees with the code on `master`, stop and ask the team.

---

## 1. Shared rules

- **Repo:** `AY2627S1-CS2103T-T11-1/tp`, a brownfield fork of AB3. Work in
  your own fork, on a new branch made from an up-to-date `master`
  (`git pull upstream master` first). Never commit on `master`.
- **One PR per task**, linked to its issue (`Fixes #N`), with the current
  milestone and one reviewer from the team.
- **Commit subjects** follow the course Git convention: imperative mood,
  capitalised, no full stop, **at most 50 characters** (72 is the hard
  limit). If a body is needed, leave a blank line and wrap at 72.
- **Merging:** use "Create a merge commit". Do not squash or rebase, and do
  not delete merged branches. To catch up with `master`, merge it into your
  branch. Do not rebase.
- **Keep the PR to the task.** Do not rename classes, packages or files
  outside the task. In particular, do not rename `Person` to `Student`.
- **Checks:** `./gradlew clean test checkstyleMain checkstyleTest` must pass.
  New code needs tests. Codecov's patch target is about 75%.
- **Style:** match the surrounding AB3 code. Javadoc on public classes and
  methods. Write `equals` in the same layout as `Address#equals`.
  Use `ToStringBuilder` for `toString`. No wildcard imports.
- **AI use:** the course expects AI use in the tP. Read and understand
  every line the agent writes before you commit it.

---

## 2. Commands (current)

| Command | Format | Success message |
|---|---|---|
| Add | `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT` | `New student added: {NAME}; Phone: {PHONE}; Address: {ADDRESS}; Level: {LEVEL}; Subject: {SUBJECT}` |
| Delete | `delete INDEX` | `Deleted student: {NAME}; Phone: {PHONE}; Address: {ADDRESS}; Level: {LEVEL}; Subject: {SUBJECT}` |
| List | `list` | `Listed {COUNT} students.` or `No students found. Use add to create a student.` |
| Log | `log INDEX c/CONTENT` | `Logged lesson for {NAME}: {CONTENT}` |
| **View** | **`view INDEX`** | Header `Lesson records for {NAME} ({COUNT}):` then one numbered line per lesson, or `No lessons logged yet for {NAME}.` |

**`view` replaces `lessons`.** The team renamed the command on Oct 8.
Any older document that says `lessons INDEX`, `LessonsCommand` or
`lessons-command` means `view`.

Other messages:

- Duplicate student: `This student already exists in the list.`
- `list` with extra text: `The list command does not accept parameters.`
- Invalid index (see section 4): `The student index provided is invalid.`

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

- `/` is allowed in names so that `s/o` and `d/o` work. No prefix starts
  with `s/`, `o/` or `d/`. Names containing `a/l` or `a/p` still fail,
  because ` a/` is the address prefix. This is a known MVP limitation and
  must be stated in the User Guide.
- **Duplicate student:** same phone, and names equal when case is ignored
  (`Person#isSamePerson`).

---

## 4. Decisions (0A) and their status

| # | Decision | Status on `master` |
|---|---|---|
| 1 | No lesson date in the MVP. `log` takes content only. | Done |
| 2 | **Order shown by `view`: most recently logged first.** Lessons have no dates, so this means reverse logging order. | Agreed by the team on Oct 8. Done (PR #63). This replaces the earlier order-logged proposal. |
| 3 | Email and tags removed from `Person`. They may return post-MVP as optional fields. | Done (PR #51) |
| 4 | `edit`, `find` and `clear` removed for the MVP. Delete the commands, parsers, tests, `AddressBookParser` cases, help and UG entries. Restore from Git history post-MVP. | **Not done yet.** All three still exist (task 2A). Do not extend them. |
| 5 | `/` allowed in names | Done (PR #51) |
| 6 | Case-insensitive duplicate names | Done (PR #51) |
| 7 | **Invalid index behaviour.** See below. | `delete` and `view` follow the W6 version (PRs #60 and #63). The final rule for every command is still open in #59. |

**On decision 7.** Two versions exist:

- **W6 spec, as `delete` implements it on `master` (PR #60):** zero,
  negative and out-of-range numbers give `The student index provided is
  invalid.` Words give the invalid command format message with usage.
- **0A proposal (AB3's split):** zero, negative, leading zeros and values
  above 1000 give the invalid command format message. Only a well-formed
  index past the end of the list gives the invalid index message.

Issue #59 will apply one rule to every index command. Until the team
picks one, new commands should copy `delete`'s current behaviour, and
tests must not assert that indexes above 1000 are accepted.

---

## 5. Agreed Java interfaces (as on `master`)

- `seedu.address.model.person.Level` and `Subject`: same shape as
  `Address` (public `value`, `isValidLevel` / `isValidSubject`,
  `MESSAGE_CONSTRAINTS`, `MAX_LENGTH`).
- `Person(Name, Phone, Address, Level, Subject)` creates a student with no
  lessons. `Person(Name, Phone, Address, Level, Subject, List<Lesson>)`
  creates one with lessons.
- `Person#getLessons()` returns an unmodifiable list in the order logged.
  `Person#withLessonAdded(Lesson)` returns a new `Person`.
- `seedu.address.model.lesson.Lesson`: immutable. `Lesson(String content)`,
  public `content`, `isValidContent`, `MESSAGE_CONSTRAINTS`, `MAX_LENGTH`.
- Logging a lesson is `model.setPerson(target, target.withLessonAdded(lesson))`.
  Deleting a student removes their lessons, because lessons live inside
  `Person`.
- `Messages.format(Person)` prints the add and delete format above.
- Prefixes in `CliSyntax`: `n/`, `p/`, `a/`, `l/`, `sub/`, `c/`.
- **Any code that rebuilds a `Person`** (for example an edit command) must
  carry `getLessons()` across. The 5-argument constructor starts with no
  lessons and would silently delete them.

---

## 6. Known gaps on `master` (Oct 8)

These are mismatches with this brief. Fix them only if your task covers
them.

- `list` still says `Listed all persons.` and has no empty-list message.
- `log` still uses `The person index provided is invalid.` (issue #59).
- `MESSAGE_INVALID_PERSON_DISPLAYED_INDEX` and
  `MESSAGE_INVALID_STUDENT_DISPLAYED_INDEX` both exist in `Messages`.
- `edit`, `find` and `clear` still exist (decision 4, task 2A).
- Ui.png still shows `lessons 1` in the command box.

---

## 7. Verification checklist (run before every PR)

Ask the agent to run each check and report the result. Open the PR only
when all pass.

- [ ] Branch made from an up-to-date `master`. No commits on `master`.
- [ ] `./gradlew clean test` passes.
- [ ] `./gradlew checkstyleMain checkstyleTest` passes.
- [ ] Every acceptance criterion in the task is met. Quote each one and say
      how it was checked.
- [ ] Every user-facing message matches this brief character for character.
- [ ] New or changed code has tests, including the boundary values in
      section 3.
- [ ] Only the files the task needs changed (`git diff --stat master`).
- [ ] The app starts with `./gradlew run` and the feature works by hand.
- [ ] Commit subjects: imperative, capitalised, no full stop, at most 50
      characters.
- [ ] PR: base `AY2627S1-CS2103T-T11-1/tp` `master`, `Fixes #N`, current
      milestone, one reviewer.
- [ ] After merge: `git switch master`, `git pull upstream master`,
      `git push origin master`.
