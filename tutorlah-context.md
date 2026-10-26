# TutorLah: context

Last updated: Oct 26, 2026, for issues #111 (target user), #142 (student follow-up flags), #145 (editing a lesson record), #116 (save-failure rollback) and #143 (finding students by subject and level); #146 adds student and lesson summaries.

## What it is

TutorLah is a desktop app for freelance tutors in Singapore who teach secondary
and junior college (JC) students one-to-one. They use it to keep student
details and record what each lesson covered. It is built by
adapting AddressBook-Level3 (AB3), the SE-EDU sample app, and keeps AB3's
architecture and typing-first workflow while reshaping the data and commands
around tutoring.

## Who it's for

Jaren is a full-time freelance tutor in Singapore with about 18 one-to-one
students from Sec 1 to JC2, in subjects such as A-Math, Physics and H2
Chemistry. He teaches at their homes, travels between
lessons on a tight schedule, and keeps everything on one laptop. His notes are
split between a spiral notebook and WhatsApp chats with parents, so each lesson
starts with working out where the last one stopped. He prefers typing to
clicking.

## Why AB3 is a good base

AB3 is already a typing-first desktop app for managing a list of people.
Jaren's core need has the same shape: a list of students he picks by number and
acts on with short commands. The work is adapting who is stored and what is
recorded about them, not building a new kind of app.

## What changes from AB3

| | AB3 | TutorLah |
|---|---|---|
| **Entity** | `Person` (a contact) | Student |
| **Fields** | name, phone, email, address, tags | name, phone, address, **level**, **subject**, and an optional follow-up **flag** with an optional reason. Email and tags are not in the spec. |
| **Name rule** | letters and digits only (`[\p{Alnum}][\p{Alnum} ]*`) | Unicode letters, spaces, `'` `-` `.` `/`, with no digits, so names like Mary-Anne, O'Brien and Ramesh s/o Kumar work |
| **Phone rule** | digits only, at least 3 | exactly 8 digits, since every Singapore number is 8 digits and a fixed length catches typos |
| **Duplicate** | same name (`isSamePerson`) | same phone **and** same name ignoring case, since two students can both be "Tan Wei Ming" |
| **`list`** | ignores any extra text | rejects parameters; reports `Listed {COUNT} students.` (`Listed 1 student.` for one) or `No students found. Use add to create a student.` |
| **New entity** | none | **Lesson**: a record of what was covered and the date it was logged, owned by a student |
| **New commands** | none | Lesson commands plus `flag INDEX [r/REASON]`, `unflag INDEX` and `flagged` |

## What stays from AB3

- **Architecture:** UI, Logic, Model and Storage, connected through interfaces.
- **Choosing by index:** `delete 3` targets the 3rd *displayed* student, the
  same as AB3's commands.
- **Command style:** a command word, an index, then `prefix/value` parameters
  in any order.
- **Storage:** JSON on disk. AB3 currently saves to `data/addressbook.json`.

If a command cannot be saved, TutorLah restores its preceding in-memory student data and active filter, then reports the save error. Retrying uses the original displayed indexes; a later command cannot silently persist a failed mutation.

## Where the changes land in the code

- **Model:** the `Person` fields change, `Level` and `Subject` are added, the
  duplicate rule changes, a `Lesson` list sits on each student, and an optional
  immutable `Flag` records follow-up state.
- **Logic:** `AddCommandParser` gets new prefixes (`l/`, `sub/`), and there are
  new `LogCommand` and `ViewCommand` classes with their parsers. `list` gets
  a check that rejects parameters.
- **Storage:** `JsonAdaptedPerson` saves level, subject, lessons and optional flags.
- **UI:** the person card shows level, subject and lesson count/latest date. Flagged cards show an amber
  `FLAGGED` badge and an optional `Flag reason:` row. Lessons appear in the result
  box. Tutors can type `view INDEX` or left-click a student card to view that
  student's history. Repeated clicks refresh it, while command text is preserved.
  The click callback uses the current displayed index and the existing command path.
  Successful views from either input method highlight the matching card and scroll
  it into view. Card presses are handled before selection updates, so switching
  from typed commands needs only one click.

The README, website home page and User Guide share `docs/images/Ui.png`,
a screenshot of the current GUI using fictional student and lesson details.
Main-window guide screenshots use a 1100 x 818 pixel canvas and a 726 pixel
display width. The help dialog keeps its natural proportions. Captures show
only application content, without mouse or assistant overlays.

Addresses and lesson content cannot consist only of Unicode whitespace or
formatting characters, such as non-breaking spaces and zero-width spaces.
Commands and JSON loading use the same validation. Real text keeps its
internal spaces and formatting, including joiners used in written languages
and emoji sequences.

## MVP commands

| Command | Format | Example |
|---|---|---|
| Add a student | `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT` | `add n/Tan Wei Ming p/87654321 a/Blk 8 Bishan St 22 #05-13 l/JC1 sub/H2 Math` |
| Delete a student | `delete INDEX` | `delete 3` |
| List students | `list` | `list` |
| Log a lesson | `log INDEX c/CONTENT [date/DATE]` | `log 3 c/Quadratic inequalities date/21/10/2026` |
| View lessons | `view INDEX` | `view 3` |

Added after the MVP (v1.4):

| Command | Format | Example |
|---|---|---|
| Edit a student | `edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [l/LEVEL] [sub/SUBJECT]` | `edit 2 l/Sec 4 sub/A-Math` |
| Delete a lesson | `deletelesson STUDENT_INDEX LESSON_INDEX` | `deletelesson 2 1` |
| Find students | `find [KEYWORD]... [n/NAME] [sub/SUBJECT] [l/LEVEL]` | `find sub/Physics l/Sec 3` |

Added in v1.5:

| Command | Format | Example |
|---|---|---|
| Flag a student | `flag INDEX [r/REASON]` | `flag 2 r/Missed homework twice` |
| Remove a flag | `unflag INDEX` | `unflag 2` |
| List flagged students | `flagged` | `flagged` |
| Summarize students and lessons | `summary` | `summary` |

`summary` counts all students, all lesson occurrences and students with no lessons, even when `find` or `flagged` filters the visible list. It keeps that filter and shows a single short paragraph explaining the totals and any students without records. Cards show a lesson count and the most recent recorded date, or `No lessons yet` / `N lessons (undated)` where appropriate. The line refreshes when lesson records change. These values are derived from existing data and introduce no JSON fields.

Flags mark unresolved follow-up without changing student identity or lesson
history. The reason is optional and can be replaced by flagging again with a
different reason. `flagged` replaces the current filter and `list` restores all
students. Flag state is persisted; filter state is not.

`edit` changes only the fields given and keeps the student's lesson records.

`deletelesson` removes one record using the lesson number shown by `view`
(newest first). The student and their other lesson records are kept.
An empty history reports `No lessons logged yet for NAME.`.

Added in v1.5:

| Command | Format | Example |
|---|---|---|
| Edit a lesson | `editlesson STUDENT_INDEX LESSON_INDEX [c/CONTENT] [date/DATE]` | `editlesson 2 1 c/Vectors date/21/10/2026` |

`editlesson` corrects one lesson's content, date or both, using the lesson number shown by `view`. A content-only edit keeps the lesson's number. A new date moves the lesson into date order, so the confirmation gives its new number.

`find` shows students whose name, subject and level contain every keyword
given for that field, ignoring case and keyword order; partial words are
accepted. Bare keywords and `n/` search names, `sub/` subjects and `l/`
levels, and every given field must match, so a tutor can bring up all Sec 3
Physics students at once. It searches all students each time and uses literal
keywords without spelling correction.
Results keep their original order and are renumbered from 1. Commands and
card clicks use these displayed indexes. `list` restores the full list.
Adding or editing also restores it; viewing, logging and deleting students
or lessons retain the filter. Searching changes no student or lesson data,
and filters are not kept across restarts.

Case-insensitive search compares substrings directly rather than lowercasing
names and keywords separately, so Greek sigma variants also match. An empty
student panel offers both `list` and the add-command format and example,
covering searches with no matches as well as a genuinely empty address book.

`log` dates each lesson with today's date (v1.4, issue #98), or with
`date/DD/MM/YYYY` for a lesson taught earlier (issue #120). Future dates are
rejected. `view` shows each record as `NUMBER. DD/MM/YYYY: CONTENT`, most
recent date first. Lessons saved before v1.4 have no date and show
`(undated)`. The data file stores the date as `YYYY-MM-DD`.

## Scope

- **In the MVP:** add, delete, list, log a lesson, view lessons.
- **Added in v1.4:** edit a student's details (lessons are kept), find
  students by partial name, delete individual lesson records, and date
  each logged lesson.
- **Added in v1.5:** persistent follow-up flags with optional reasons and a
  filtered flagged-student view, editing a lesson record's content or date, and
  student/lesson totals with per-student lesson status.
- **Planned next:** parent or guardian contact,
  weekly slots, homework tracking, rescheduling, monthly lesson counts,
  archiving.
- **Out of scope:** arranging lessons, messaging parents, grading, payments,
  exam planning, and moving data between machines.

## Decisions (Oct 8, 2026)

- **No lesson date** in the MVP. `log` takes content only. Replaced in v1.4
  by issue #98: `log` dates each lesson with today's date.
- **View order:** most recently logged first (reverse logging order), as
  agreed by the team on Oct 8. Implemented in PR #63. This replaces the
  earlier order-logged proposal. Since issue #120, lessons are kept in date
  order, so `view` shows the most recent date first.
- **Email and tags are removed.** They may return post-MVP as optional
  fields.
- **`edit`, `find` and `clear` are removed for the MVP** and can be restored
  from Git history later. Removed from the code in issue #72 (Oct 14).
  `edit` was restored in issue #100 (Oct 20) for v1.4.
  `find` was restored in issue #101 (Oct 20), with all-keyword partial matching, and extended to subject and level in issue #143 (Oct 25); `clear` remains removed.
- **`view` replaces `lessons`** as the command for viewing a student's lessons.
- **`/` is allowed in names** so that s/o and d/o work. `a/l` and `a/p` still
  fail because ` a/` is the address prefix, a documented MVP limitation.
  With `edit`, such a name is accepted but changes the address (see the
  User Guide's Known issue 3); typing `A/L` in capitals avoids it.
- **Duplicates** are the same phone with the same name ignoring case.

## Shared index policy

- **Invalid index messages:** `delete`, `log`, and `view` accept positive
  integers without leading zeros. There is no arbitrary 1000 cap. Zero,
  negative integers, leading-zero integers, values too large to parse, and
  indexes outside the displayed list give "The student index provided is
  invalid." Missing or malformed command parts keep the command's invalid
  format message and usage.

For coding agents, use `tutorlah-agent-brief.md`, which has the full rules,
messages and interfaces.
