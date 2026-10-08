# TutorLah: context

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
| **`list`** | ignores any extra text | rejects parameters, and gives a clear message when the list is empty |
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
  box in the MVP (Feature 5).

## MVP commands

| Command | Format | Example |
|---|---|---|
| Add a student | `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT` | `add n/Tan Wei Ming p/87654321 a/Blk 8 Bishan St 22 #05-13 l/JC1 sub/H2 Math` |
| Delete a student | `delete INDEX` | `delete 3` |
| List students | `list` | `list` |
| Log a lesson | `log INDEX c/CONTENT` | `log 3 c/Quadratic inequalities` |
| View lessons | `view INDEX` | `view 3` |

## Scope

- **In the MVP:** add, delete, list, log a lesson, view lessons.
- **Planned next:** edit students and lessons, parent or guardian contact,
  weekly slots, homework tracking, rescheduling, monthly lesson counts,
  archiving.
- **Out of scope:** arranging lessons, messaging parents, grading, payments,
  exam planning, and moving data between machines.

## Decisions (Oct 8, 2026)

- **No lesson date** in the MVP. `log` takes content only.
- **View order:** most recently logged first (reverse logging order), as
  confirmed by zikiai on Oct 8 for issue #12 and PR #63. This supersedes
  the earlier order-logged proposal.
- **Email and tags are removed.** They may return post-MVP as optional
  fields.
- **`edit`, `find` and `clear` are removed for the MVP** and can be restored
  from Git history later. On Oct 8 they were still in the code, waiting for
  task 2A.
- **`view` replaces `lessons`** as the command for viewing a student's lessons.
- **`/` is allowed in names** so that s/o and d/o work. `a/l` and `a/p` still
  fail because ` a/` is the address prefix, a documented MVP limitation.
- **Duplicates** are the same phone with the same name ignoring case.

## Still open

- **Invalid index messages:** `delete` on `master` follows the W6 spec (zero,
  negative and out-of-range numbers give "The student index provided is
  invalid."). The 0A proposal kept AB3's split instead. Issue #59 will apply
  one rule to every index command once the team picks one.

For coding agents, use `tutorlah-agent-brief.md`, which has the full rules,
messages and interfaces.
