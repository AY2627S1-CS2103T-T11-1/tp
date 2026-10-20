# TutorLah: context

Last updated: Oct 20, 2026, for issues #100 and #101 (edit and find restored).

## What it is

TutorLah is a desktop app for freelance maths tutors in Singapore. They use it
to keep student details and record what each lesson covered. It is built by
adapting AddressBook-Level3 (AB3), the SE-EDU sample app, and keeps AB3's
architecture and typing-first workflow while reshaping the data and commands
around tutoring.

## Who it's for

Jaren is a full-time freelance maths tutor with about 18 one-to-one students,
from Sec 1 E-Math to JC H2 Math. He teaches at their homes, travels between
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
| **Fields** | name, phone, email, address, tags | name, phone, address, **level**, **subject**. Email and tags are not in the spec. |
| **Name rule** | letters and digits only (`[\p{Alnum}][\p{Alnum} ]*`) | Unicode letters, spaces, `'` `-` `.` `/`, with no digits, so names like Mary-Anne, O'Brien and Ramesh s/o Kumar work |
| **Phone rule** | digits only, at least 3 | exactly 8 digits, since every Singapore number is 8 digits and a fixed length catches typos |
| **Duplicate** | same name (`isSamePerson`) | same phone **and** same name ignoring case, since two students can both be "Tan Wei Ming" |
| **`list`** | ignores any extra text | rejects parameters; reports `Listed {COUNT} students.` (`Listed 1 student.` for one) or `No students found. Use add to create a student.` |
| **New entity** | none | **Lesson**: a record of what was covered, owned by a student |
| **New commands** | none | `log INDEX c/CONTENT` and `view INDEX` |

## What stays from AB3

- **Architecture:** UI, Logic, Model and Storage, connected through interfaces.
- **Choosing by index:** `delete 3` targets the 3rd *displayed* student, the
  same as AB3's commands.
- **Command style:** a command word, an index, then `prefix/value` parameters
  in any order.
- **Storage:** JSON on disk. AB3 currently saves to `data/addressbook.json`.

## Where the changes land in the code

- **Model:** the `Person` fields change, `Level` and `Subject` are added, the
  duplicate rule changes, and a new `Lesson` list sits on each student.
- **Logic:** `AddCommandParser` gets new prefixes (`l/`, `sub/`), and there are
  new `LogCommand` and `ViewCommand` classes with their parsers. `list` gets
  a check that rejects parameters.
- **Storage:** `JsonAdaptedPerson` saves level, subject and lessons. This is the
  same pattern as the `remark` tutorial.
- **UI:** the person card shows level and subject. Lessons appear in the result
  box. Tutors can type `view INDEX` or left-click a student card to view that
  student's history. Repeated clicks refresh it, while command text is preserved.
  The click callback uses the current displayed index and the existing command path.
  Successful views from either input method highlight the matching card and scroll
  it into view. Card presses are handled before selection updates, so switching
  from typed commands needs only one click.

The README, website home page and User Guide share `docs/images/Ui.png`,
a screenshot of the current GUI using fictional student and lesson details.

## MVP commands

| Command | Format | Example |
|---|---|---|
| Add a student | `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT` | `add n/Tan Wei Ming p/87654321 a/Blk 8 Bishan St 22 #05-13 l/JC1 sub/H2 Math` |
| Delete a student | `delete INDEX` | `delete 3` |
| List students | `list` | `list` |
| Log a lesson | `log INDEX c/CONTENT` | `log 3 c/Quadratic inequalities` |
| View lessons | `view INDEX` | `view 3` |

Added after the MVP (v1.4):

| Command | Format | Example |
|---|---|---|
| Edit a student | `edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [l/LEVEL] [sub/SUBJECT]` | `edit 2 l/Sec 4 sub/A-Math` |
| Find students | `find KEYWORD [MORE_KEYWORDS]...` | `find tan wei` |

`edit` changes only the fields given and keeps the student's lesson records.

`find` shows students whose names contain every search keyword, ignoring case
and keyword order; partial names are accepted. It searches all students each
time and uses literal keywords without field prefixes or spelling correction.
Results keep their original order and are renumbered from 1. Commands and
card clicks use these displayed indexes. `list` restores the full list.
Adding or editing also restores it; viewing, logging and deleting retain
the filter. Student data and lesson records are unchanged by searching,
and filters are not kept across restarts.

## Scope

- **In the MVP:** add, delete, list, log a lesson, view lessons.
- **Added in v1.4:** edit a student's details (lessons are kept) and find students by partial names.
- **Planned next:** edit lessons, parent or guardian contact,
  weekly slots, homework tracking, rescheduling, monthly lesson counts,
  archiving.
- **Out of scope:** arranging lessons, messaging parents, grading, payments,
  exam planning, and moving data between machines.

## Decisions (Oct 8, 2026)

- **No lesson date** in the MVP. `log` takes content only.
- **View order:** most recently logged first (reverse logging order), as
  agreed by the team on Oct 8. Implemented in PR #63. This replaces the
  earlier order-logged proposal.
- **Email and tags are removed.** They may return post-MVP as optional
  fields.
- **`edit`, `find` and `clear` are removed for the MVP** and can be restored
  from Git history later. Removed from the code in issue #72 (Oct 14).
  `edit` was restored in issue #100 (Oct 20) for v1.4.
  `find` was restored in issue #101 (Oct 20), with all-keyword partial matching; `clear` remains removed.
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
