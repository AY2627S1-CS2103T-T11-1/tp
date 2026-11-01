---
  layout: default.md
  title: "User Guide"
  pageNav: 4
---

# TutorLah User Guide

TutorLah helps freelance tutors in Singapore keep student details and lesson notes in one place. Add your secondary or junior college (JC) students, record what you taught, and check their history before the next lesson. Type commands to work quickly, or click a student to view their details.

<page-nav-print />

## Quick start

1. Install Java **25**. To check, open a terminal and run `java -version`. The first line should show version 25. Mac users should follow the [Mac installation instructions](https://se-education.org/guides/tutorials/javaInstallationMac.html).
1. Download `TutorLah.jar` from the [latest release](https://github.com/AY2627S1-CS2103T-T11-1/tp/releases) and place it in an empty folder where you can create files, not a write-protected folder. TutorLah saves your students in this folder.
1. Open a terminal, change to that folder with `cd`, and run `java -jar "TutorLah.jar"`. Double-clicking the JAR file may not start TutorLah correctly on some computers, so use this command. Start TutorLah from this same folder each time so it finds your saved students.
1. Type a command in the top box and press **Enter**. Your first launch includes sample students to try these commands:

   | Command | What it does |
   |---|---|
   | `list` | Shows all students. |
   | `view 1` | Opens the first student's details and lesson history. |
   | `loglesson 1 c/Quadratic inequalities` | Records today's lesson for that student. |
   | `find tan` | Shows students whose names contain `tan`. Use `list` to return to everyone. |
   | `summary` | Shows your total students and lesson records. |

The screenshot below uses fictional data after `view 1`; your sample students may differ.

<a href="images/Ui.png"><img src="images/Ui.png" alt="Alex Tan highlighted in the student list, with his details and three dated lesson records in the details panel after view 1" width="726"></a>

Below the menu bar (**File** and **Help**), the window has four areas:

- **Command box** (top): type a command here. It clears after success; a failed command stays for correction.
- **Result box** (below it): shows what happened. Errors appear in red.
- **Student list** (left): shows each student's number, name, level, subject and lesson count. A red flag marks someone needing follow-up.
- **Details panel** (right): shows one student's contact details, flag reason and numbered lesson records.

The status bar at the bottom shows where your students are saved, for example `./data/addressbook.json`.

Choose a task below, or use the [command summary](#command-summary) for a quick reference.

## Using commands

- Replace capitalised words with your details: in `add n/NAME`, replace `NAME` with `Tan Wei Ming`.
- `[ ... ]` means optional; do not type the brackets. `KEYWORD...` means you may give more than one search word.
- Command words accept any case: `list`, `LIST` and `LiSt` work alike. Field markers such as `n/` (name) and `c/` (lesson content) must be lowercase.
- Give each field at most once. Fields can appear in any order unless a command says otherwise. Text belongs to the field before it until the next recognised marker.
- `INDEX` is the student's number in the **list currently shown**, starting at 1. After searching, use the new displayed numbers. Do not use leading zeros, such as `01`.
- Paste one command at a time, check it, then press Enter. Copied spaces work as separators; pasted tabs and line breaks become spaces.

<box type="warning" seamless>

**Slashes in names and notes:** names such as `Ramesh s/o Kumar` and `Muthu a/l Raju` work. However, a marker recognised by the command starts another field after a space, even inside your text. In lesson notes, write `care of` instead of `c/o`, and `date and time` instead of `date/time`. A repeated field gives an error, but a valid extra field may silently change the meaning: `edit 1 addr/Unit l/2` changes the address to `Unit` and the level to `2`.

</box>

Screenshots use fictional students. Captions give the commands used because successful commands disappear from the command box. Click any screenshot to enlarge it.

## Features

### Finding students and viewing totals

#### Listing all students: `list`

Format: `list`

Shows everyone, restores the full list after `find` or `flagged`, and reports the number of students. With none, it suggests adding a student. Extra text such as `list 3` is rejected.

**Screenshot:** after `find TAN`, enter `list` to restore all six students. Priya Ramesh is student 2 again.

<a href="images/list-students.png"><img src="images/list-students.png" alt="Listed 6 students after list restores the full list following find TAN" width="726"></a>

#### Finding students: `find`

Format: `find [KEYWORD]... [n/NAME] [sub/SUBJECT] [l/LEVEL]`

Use part of a name, subject or level to find students:

| Example | Matches |
|---|---|
| `find tan` or `find n/tan` | Students whose names contain `tan`. |
| `find alex tan` | Names containing both `alex` and `tan`, in any order. |
| `find sub/Physics l/Sec 3` | Students taking Physics whose level contains both `Sec` and `3`. |
| `find tan sub/math` | Students named Tan taking a subject such as E-Math or H2 Math. |

- Give at least one search word. Every word must match its own field; all supplied fields must match. Uppercase and lowercase match alike.
- Words before the first marker, and after `n/`, search names. Put name words **before** `sub/` or `l/`: `find ng sub/Physics` searches for Ng taking Physics; `find sub/Physics ng` searches for a subject containing both words.
- Each field must contain text and appear only once. Extra spaces are ignored. Punctuation and accents must match; wildcards and spelling correction are not supported. Other slash text, such as `a/l`, is ordinary search text.
- Each search checks everyone, replacing the previous search or flagged list. Matching students keep their original order and are numbered from 1.
- If nobody matches, TutorLah may suggest using `sub/` or `l/`, or moving name words before them. Suggestions do not run automatically.
- `find` changes no saved details. Use `list` to return to everyone. Searches are not remembered after restarting.

**Screenshot:** `find TAN` shows Alex Tan and Ethan Tan as students 1 and 2.

<a href="images/find-students.png"><img src="images/find-students.png" alt="Found 2 students after find TAN, showing Alex Tan and Ethan Tan" width="726"></a>

#### Viewing student and lesson totals: `summary`

Format: `summary`

Shows totals for **all students**, even while `find` or `flagged` shows only some:

```text
Summary:
You have 3 students and 3 lesson records in total.
```

Every lesson record counts, including repeated entries and older records without dates. These are recorded lessons, not scheduled lessons or teaching hours. Your current list and its numbers stay unchanged. With no students, the message suggests adding one. Extra text such as `summary 1` is rejected.

Student cards also show a quick lesson status:

| Recorded lessons | Card text |
|---|---|
| None | `No lessons yet` |
| One dated lesson | `1 lesson, last 21/10/2026` |
| Several dated lessons | `5 lessons, last 21/10/2026` |
| Only records without dates | `5 lessons (undated)` |

`last` means the latest **lesson date**, rather than when you entered the command. All records count when dated and undated lessons coexist. Counts update when you record, edit or delete a lesson.

### Managing student details

#### Adding a student: `add`

Format: `add n/NAME p/PHONE addr/ADDRESS l/LEVEL sub/SUBJECT`

All five fields are required:

| Field | What you can enter |
|---|---|
| `n/NAME` | 1–100 characters: letters, spaces, apostrophes, hyphens, periods and slashes. At least one letter; no digits. Letters from other languages are accepted. Extra spaces are reduced to one. |
| `p/PHONE` | Exactly 8 digits (`0`–`9`). Spaces, including copied non-breaking spaces, are removed: `9123 4567` becomes `91234567`. Do not include `+65`, country codes, hyphens or invisible zero-width characters. |
| `addr/ADDRESS` | 1–200 characters of meaningful text; not just spaces or invisible characters. Use `addr/`, **not** `a/`. |
| `l/LEVEL` | 1–40 characters: English letters, digits and spaces. For example, `Sec 3`, `JC1` or `Sec 4 Normal Academic`. Brackets and hyphens are not allowed. |
| `sub/SUBJECT` | 1–50 characters: English letters, digits, spaces, hyphens, `&`, `(`, `)`, `+` and commas. Start with an English letter or digit. For example, `E-Math` or `E-Math & A-Math`; slashes are not allowed. |

A student with both the same phone and the same name as another student is rejected, ignoring name case and extra spaces. Equal names with different phones are allowed.

Only the five listed markers start fields. Other slash text stays in the preceding value: `n/Amy t/friend` saves the name `Amy t/friend`, while `sub/Math t/friend` makes the subject invalid.

Examples:

- `add n/Tan Wei Ming p/87654321 addr/Blk 8 Bishan St 22, #05-13 l/JC1 sub/H2 Math`
- `add n/Ramesh s/o Kumar p/9123 4567 addr/12 Clementi Rd l/Sec 3 sub/Physics`

**Screenshot:** `add n/Jamie Lee p/81230007 addr/60 Example Street l/JC1 sub/Physics` adds Jamie at the end of the list.

<a href="images/add-student.png"><img src="images/add-student.png" alt="Add confirmation and Jamie Lee's new student card after add n/Jamie Lee p/81230007 addr/60 Example Street l/JC1 sub/Physics" width="726"></a>

#### Editing a student: `edit`

Format: `edit INDEX [n/NAME] [p/PHONE] [addr/ADDRESS] [l/LEVEL] [sub/SUBJECT]`

Give at least one field. Only supplied details change, following the [add rules](#adding-a-student-add), including duplicate checks. Lesson records and flags are kept. After success, everyone is listed again and the edited student opens on the right.

Examples:

- `edit 1 p/91234567` changes student 1's phone.
- `edit 1 n/Muthu a/l Raju` changes only the name, keeping the address.
- `edit 2 l/Sec 4 sub/A-Math` changes student 2's level and subject.

**Screenshot:** `edit 1 l/Sec 4 sub/A-Math` updates Alex Tan's card and details.

<a href="images/edit-student.png"><img src="images/edit-student.png" alt="Edit confirmation, with Alex Tan's card and details panel showing Sec 4 and A-Math" width="726"></a>

#### Deleting a student: `delete`

Format: `delete INDEX`

Removes the student at that number in the current list, **including all their lessons and their flag**. Deletion is saved automatically and cannot be undone in the app. Check the displayed list first; use `list` if you want everyone's original list order.

**Screenshot:** starting from the six-student list above, `delete 2` removes Priya Ramesh. Marcus Lim becomes student 2.

<a href="images/delete-student.png"><img src="images/delete-student.png" alt="Deletion confirmation for Priya Ramesh after delete 2, with Marcus Lim now student 2" width="726"></a>

### Lesson records

Student numbers always refer to the current student list. Lesson numbers come from `view STUDENT_INDEX`: **lesson 1 is the newest**, and records without dates appear last. Before changing a lesson, run `view` again if you have recorded, deleted or redated lessons since checking its number.

#### Logging a lesson: `loglesson`

Format: `loglesson INDEX c/CONTENT [date/DATE]`

Records what you taught. The date is today unless you supply `date/`:

- `CONTENT` is 1–300 characters of meaningful text, not just spaces or invisible characters. Internal spacing and characters used in real text are kept. Follow the [slash guidance](#using-commands) for `c/` and `date/` inside notes.
- Use `DD/MM/YYYY`, for example `21/10/2026`; single-digit days and months such as `1/2/2026` also work. The year must have four digits. Dates must exist and be from **01/01/2000 up to today** on your computer. Future dates are rejected.
- A backdated lesson appears in its date position. For the same date, the most recently logged appears first. Logging identical content twice creates two records.
- The confirmation shows the student, date and content. Use `editlesson` to correct it or `deletelesson` to remove it. The current student list and flags are kept.

Examples:

- `loglesson 1 c/Electric circuits: series and parallel, Q1-5 for homework`
- `loglesson 2 c/Surds and indices date/20/10/2026`

**Screenshot:** after `list`, `loglesson 1 c/Trigonometry: solved right-angled triangle problems. date/18/10/2026` records an earlier lesson for Alex Tan.

<a href="images/log-lesson-past-date.png"><img src="images/log-lesson-past-date.png" alt="Confirmation of a Trigonometry lesson logged for Alex Tan on 18/10/2026" width="726"></a>

#### Viewing a student's lesson records: `view`

Format: `view INDEX`

Opens the student's details and complete lesson history on the right, and lists their records in the result box. Viewing changes no saved details or student search.

- Records are numbered newest date first. For example, `1. 18/10/2026: Quadratic inequalities`. Older records without dates show `(undated)` after the dated records. Repeated entries remain separate.
- With no lessons, the message is `No lessons logged yet for NAME.`, and the details panel suggests a `loglesson` command.
- You can also click a student card. After clicking the list, use Up, Down, Page Up, Page Down, Home or End to view another student. This keeps any command you are typing.

**Screenshot:** `view 1` opens Alex Tan and highlights his card.

<a href="images/Ui.png"><img src="images/Ui.png" alt="Alex Tan highlighted in the student list, with his details and three dated lesson records in the details panel after view 1" width="726"></a>

##### The details panel

Commands that view or change one student open their latest details automatically. Other commands keep that student open while they remain in the displayed list. Filtering them out or deleting them clears the panel; failed commands keep it unchanged. After `unflag` in a flagged-only list, the unflagged student leaves the list but stays open so you can see the change.

At startup, nobody is selected. The panel suggests `view 1`, `list` or an `add` command according to whether students are visible, hidden by a search, or absent.

#### Searching a student's lessons: `findlesson`

Format: `findlesson INDEX [KEYWORDS] [from/DATE] [to/DATE]`

Find a topic, a date range, or both for one student:

- Give at least one keyword or date boundary. Put all keywords **before** `from/` and `to/`; give each boundary at most once, in either order.
- All keywords must occur in the lesson content, in any order. Part of a word works, ignoring case. Punctuation is matched as typed; wildcards and spelling correction are not supported.
- Dates use the format and earliest date described under `loglesson`. Future search dates are allowed. Both boundary dates are included; leave one out for an open-ended range. `from/` cannot be later than `to/`; equal dates search a single day.
- When both content and dates are supplied, all must match. Records without dates can match content searches, but are excluded whenever a date boundary is supplied.
- Matching lessons keep their numbers from the **complete history**: results 2 and 4 stay numbered 2 and 4. Use these with `editlesson` or `deletelesson`, and check again after any lesson changes.
- The matches appear only in the result box. The details panel shows the student's **complete** history, and the student list is unchanged. The search is not remembered.
- No matches gives `No matching lesson records found for NAME.`; no lessons gives `No lessons logged yet for NAME.`. Search content without `c/`: use `findlesson 1 algebra`. If a search using `c/` or `date/` finds no matches, TutorLah explains the supported format.

Examples:

- `findlesson 2 algebra` searches lesson content.
- `findlesson 2 from/01/10/2026 to/31/10/2026` searches October's dated records.
- `findlesson 2 revision from/01/10/2026` searches revision records from 1 October onwards.

**Screenshot:** `findlesson 1 algebra from/01/10/2026 to/31/10/2026` finds Amy Tan's lessons 2 and 4. Her full history remains on the right.

<a href="images/find-lessons.png"><img src="images/find-lessons.png"
  alt="Two matching Algebra lessons for Amy Tan, retaining full-history lesson numbers 2 and 4"
  width="726"></a>

#### Editing a lesson record: `editlesson`

Format: `editlesson STUDENT_INDEX LESSON_INDEX [c/CONTENT] [date/DATE]`

Give content, a date, or both, using the same limits as `loglesson`. The student, other lessons, flags and current student list are kept.

- Omitted fields keep their values. Give at least one field; unchanged content and date gives `The lesson already has those details.`
- A content-only edit keeps the lesson number. Changing its date moves it into date order and **may change its number**. The confirmation gives the new number; on a shared date, the moved lesson appears first among that date's records.
- An undated lesson stays undated unless you supply a date. You cannot remove a date from a dated lesson.

Examples:

- `editlesson 2 1 c/Quadratic inequalities and homework review` corrects lesson 1 for student 2.
- If `view 1` shows Trigonometry (23/10/2026), Vectors (21/10/2026) and Algebra (19/10/2026), `editlesson 1 3 date/22/10/2026` moves Algebra from lesson 3 to lesson 2.

#### Deleting a lesson record: `deletelesson`

Format: `deletelesson STUDENT_INDEX LESSON_INDEX`

Deletes just that lesson. The student, their flag, other records and the current student list are kept. Even identical lessons are separate records; only the chosen one is removed. Remaining lessons are renumbered from 1. Removing the final lesson keeps the student with no lessons.

Use exactly two positive whole numbers without leading zeros. Missing or extra values show the command format. Numbers such as `0`, `01` or an unavailable number give a student- or lesson-index error. With no lessons, the message is `No lessons logged yet for NAME.`. **Deletion cannot be undone in the app.**

**Screenshots:** after the logging example, `view 1` shows Alex's four lessons; Trigonometry is lesson 2.

<a href="images/lesson-history-before-delete.png"><img src="images/lesson-history-before-delete.png" alt="Four lessons for Alex Tan before deletion, with the 18/10/2026 Trigonometry lesson numbered 2" width="726"></a>

Enter `deletelesson 1 2` to remove it. Alex and the other three lessons remain.

<a href="images/lesson-history-after-delete.png"><img src="images/lesson-history-after-delete.png" alt="Deletion confirmation after deletelesson 1 2, with three remaining lessons for Alex Tan in the details panel and his details unchanged" width="726"></a>

### Follow-up flags

#### Flagging students for follow-up: `flag`

Format: `flag INDEX [r/REASON]`

Marks a student needing follow-up. Their card and details show a red flag; the details panel shows the reason or `Flagged (no reason given)`.

- A reason is optional. If given, use 1–100 characters after removing surrounding spaces; it cannot be blank, only invisible characters, or contain control characters such as tabs.
- Only `r/` is recognised as a field, so reasons can contain text such as `a/level`, `date/` or `c/homework`.
- Flagging again with a different reason replaces the reason. The same reason gives an already-flagged message. Omitting `r/` keeps an existing reason and reports that the student is already flagged.
- Flags are saved and kept when you edit student details or change lessons.

**Screenshot:** `flag 1 r/Missed homework twice` marks Alex Tan and shows the reason.

<a href="images/flag-student.png"><img src="images/flag-student.png" alt="Alex Tan's card with a red flag icon, and the details panel showing the reason Missed homework twice" width="726"></a>

#### Removing a flag: `unflag`

Format: `unflag INDEX`

Removes the flag and reason. If the student is not flagged, TutorLah says so. Both `flag` and `unflag` use the current displayed number and open the student on the right.

#### Listing flagged students: `flagged`

Format: `flagged`

Shows all flagged students, replacing any previous student search. Results keep their original order and are numbered from 1; use those displayed numbers in other commands. If none are flagged, TutorLah suggests `list`.

After `unflag` in this list, the student disappears from the list but stays open on the right. Use `list` to show everyone. Extra text is rejected, and the flagged-only view is not kept after restarting.

### Help and exit

#### Viewing help: `help`

Format: `help`

Shows a window with the User Guide link. Click **Copy URL** and paste it into your browser.

![Help window with the User Guide link and Copy URL button](images/helpMessage.png)

#### Exiting the program: `exit`

Format: `exit`

Closes TutorLah. There is no manual save step. Extra text after `help` or `exit` is ignored.

## Using the window

Drag the divider below the result box to give long results more room, or scroll inside it. Each command starts its feedback at the top. Drag the divider between the student list and details panel to adjust their widths; scroll inside the details panel for a long history.

The window can shrink to 700 × 420 pixels and opens no larger than your primary screen. If a saved position no longer fits on a connected screen, TutorLah opens in the centre of the primary screen.

## Keeping your data safe

### Saving and backups

TutorLah saves automatically after a successful change. Viewing, searching, showing totals, help and exit do not save or change student records.

Your students, lessons and flags are in `data/addressbook.json` inside the folder you launch TutorLah from. With Quick start, the `data` folder sits beside `TutorLah.jar`. **Close TutorLah before copying this file to a safe location as a backup.** Sample students are not saved until you first change data.

Only one copy can use the same data folder at a time. If a second copy reports a conflict, close the first before trying again. A leftover hidden `.tutorlah.lock` file is harmless and does not mean TutorLah is still running.

### If saving fails

The failed change is cancelled: your students, lessons and displayed list return to their previous state. Check the reported file location, folder permissions and available disk space, fix the problem, then retry. Do not assume the failed command was saved. The previous saved file is kept if writing or replacing it fails; a failed first save leaves no empty student file.

### If your saved students cannot be opened

TutorLah shows a warning with the file's location and a short reason, then opens with an empty student list. **Saving is disabled to protect the original file.** Commands that only read data still work, but changes cannot be saved.

1. Close TutorLah.
1. Make a backup of the file named in the warning.
1. Restore a valid backup at that location, or correct the file using the advanced notes below.
1. Restart TutorLah. Repairing the file while the app remains open does not enable saving.

### Advanced: editing the saved file

<panel header="Optional file-editing details" collapsed>

Use the commands above for normal updates. If you edit `data/addressbook.json` directly, close TutorLah and make a backup first. The file uses **JSON**, a text format with named fields and brackets. Keep it as UTF-8 text without a byte-order mark (BOM), which some editors add at the beginning.

- Keep one complete outer object with all students inside it. Do not join two data files together or add text after the closing `}`; only spaces or line breaks may follow it.
- Student fields follow the [add rules](#adding-a-student-add). Phones in the file must already be exactly eight digits, without spaces. Names are tidied before checking duplicates; duplicates make the whole file unreadable rather than being merged.
- Lesson dates are stored as `"date": "YYYY-MM-DD"`, for example `"date": "2026-10-18"`. Missing dates mean undated lessons. Dates before `2000-01-01`, impossible dates and other formats are rejected. Existing future dates still load in case your computer clock changed; recording or changing a date through commands still rejects future dates.
- Lessons load in date order and display newest first, with undated records last. For equal dates and undated records, their file order is reversed for display. Repeated entries are kept. Reading the file does not rewrite it; the next successful change saves the loaded order and tidied names.
- A flag with a reason uses `"flag": {"reason": "REASON"}`; one without a reason uses `"flag": {}`. An unflagged student has no `flag` field. Older files without flags still load.
- Blank or invisible-only addresses, lesson content and flag reasons are invalid. An invalid file produces the protected recovery state described above; the warning may mention JSON and a line and column to help locate the problem.

Saving requires the folder to support replacing a file in one step. If this is unavailable, saving reports an error. A forced shutdown may leave hidden `.tutorlah-*.tmp` files; TutorLah does not read them as student data or remove them on restart. After closing all copies, these temporary files can be deleted. Keep `addressbook.json` and `preferences.json`.

</panel>

## FAQ

**How do I transfer my data to another computer?**

1. Install Java and download TutorLah on the new computer, following Quick start.
1. Close TutorLah on both computers.
1. Copy the old `data/addressbook.json` to a `data` folder inside the new launch folder. Create the folder if needed; back up any existing destination file before replacing it. Do not combine the files.
1. Start TutorLah from the new folder. Use `list` and `view INDEX` to check that students, lessons and flags have loaded.

## Known issues

- **Minimized Help window:** if the Help window is minimized, running `help`, choosing Help from the menu or pressing F1 does not restore it. Restore the existing window manually.
- **Fullscreen on macOS:** if TutorLah is in fullscreen mode, the Help window opens in its own fullscreen space instead of over TutorLah. Pressing F1 again does not bring it back, and it keeps opening in fullscreen even after TutorLah leaves fullscreen mode. The Help window only links to this User Guide, so use this guide directly instead.

## Command summary

Groups and commands follow the same order as [Features](#features). Use current displayed student numbers; lesson numbers come from `view`. Brackets mean optional parts.

**Finding students and viewing totals**

| Action | Format | Example |
|---|---|---|
| [List everyone](#listing-all-students-list) | `list` | `list` |
| [Find students](#finding-students-find) | `find [KEYWORD]... [n/NAME] [sub/SUBJECT] [l/LEVEL]` | `find tan sub/Physics l/Sec 3` |
| [View totals](#viewing-student-and-lesson-totals-summary) | `summary` | `summary` |

**Managing student details**

| Action | Format | Example |
|---|---|---|
| [Add](#adding-a-student-add) | `add n/NAME p/PHONE addr/ADDRESS l/LEVEL sub/SUBJECT` | `add n/Tan Wei Ming p/87654321 addr/Blk 8 Bishan St 22 l/JC1 sub/H2 Math` |
| [Edit](#editing-a-student-edit) | `edit INDEX [n/NAME] [p/PHONE] [addr/ADDRESS] [l/LEVEL] [sub/SUBJECT]` | `edit 2 l/Sec 4 sub/A-Math` |
| [Delete](#deleting-a-student-delete) | `delete INDEX` | `delete 3` |

**Lesson records**

| Action | Format | Example |
|---|---|---|
| [Log a lesson](#logging-a-lesson-loglesson) | `loglesson INDEX c/CONTENT [date/DATE]` | `loglesson 1 c/Algebra date/21/10/2026` |
| [View history](#viewing-a-student-s-lesson-records-view) | `view INDEX` | `view 1` |
| [Find lessons](#searching-a-student-s-lessons-findlesson) | `findlesson INDEX [KEYWORDS] [from/DATE] [to/DATE]` | `findlesson 2 algebra from/01/10/2026` |
| [Edit a lesson](#editing-a-lesson-record-editlesson) | `editlesson STUDENT_INDEX LESSON_INDEX [c/CONTENT] [date/DATE]` | `editlesson 2 1 c/Vectors date/21/10/2026` |
| [Delete a lesson](#deleting-a-lesson-record-deletelesson) | `deletelesson STUDENT_INDEX LESSON_INDEX` | `view 2`, then `deletelesson 2 1` |

**Follow-up flags**

| Action | Format | Example |
|---|---|---|
| [Flag](#flagging-students-for-follow-up-flag) | `flag INDEX [r/REASON]` | `flag 2 r/Missed homework twice` |
| [Unflag](#removing-a-flag-unflag) | `unflag INDEX` | `unflag 2` |
| [Show flagged](#listing-flagged-students-flagged) | `flagged` | `flagged`, then `list` to show everyone |

**Help and exit**

| Action | Format | Example |
|---|---|---|
| [Help](#viewing-help-help) | `help` | `help` |
| [Exit](#exiting-the-program-exit) | `exit` | `exit` |
