---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TutorLah User Guide

TutorLah is a **desktop application for freelance tutors in Singapore to keep track of their secondary and junior college (JC) students and the lessons they have taught them, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). Store each student's contact details, level and subject, record what you covered after every lesson, and look back at a student's lesson history before the next one, without relying on scattered notes or memory. If you type quickly, TutorLah lets you do this faster than a typical mouse-driven application.

<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T11-1/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TutorLah. Your data will be saved in this folder.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar TutorLah.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. On first launch, the app contains sample students. The screenshot uses fictional student and lesson data after running `view 1`; your initial data may differ.<br>
   <a href="images/Ui.png"><img src="images/Ui.png" alt="Alex Tan highlighted with three dated lesson records after view 1" width="726"></a>

   **Command used:** `view 1`. Alex Tan's three lessons appear with the newest date first.

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all students.

   * `find wei` : Shows students with `wei` anywhere in their name. Use `list` to show all students again.

   * `flag 2 r/Missed homework twice` : Marks the 2nd displayed student for follow-up and records why.

   * `flagged` : Shows all students currently marked for follow-up.

   * `add n/Tan Wei Ming p/87654321 a/Blk 8 Bishan St 22, #05-13 l/JC1 sub/H2 Math` : Adds a JC1 student named `Tan Wei Ming` who is taking H2 Math.

   * `log 1 c/Quadratic inequalities` : Records a lesson on quadratic inequalities for the 1st student in the list.

   * `view 1` : Shows the lesson records of the 1st student in the list, most recent first.

   * `delete 3` : Deletes the 3rd student shown in the current list, together with their lesson records.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* The command box clears after a successful command. Screenshot captions state which commands produced the displayed results. Screenshots use fictional data; your student names and indexes may differ. Click a screenshot to open its full-size image.

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `Tan Wei Ming`.

* Prefixes such as `n/`, `p/` and `c/` are lowercase and must be typed exactly as shown.

* Parameters with prefixes can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

* `INDEX` refers to the number shown beside a student in the currently displayed student list. It must be a positive integer without leading zeros, such as 1, 2, 3, ...

* Extraneous parameters for `help` and `exit` are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a student: `add`

Adds a student to the end of the student list.

Format: `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT`

* All five fields are required.
* `NAME` is 1 to 100 characters of letters, spaces, apostrophes, hyphens, periods and slashes, with no digits. Repeated spaces are reduced to one. Names such as `Ramesh s/o Kumar` work, but see [Known issues](#known-issues) for names containing `a/l` or `a/p`.
* `PHONE` must be exactly 8 digits. Spaces between digits are removed, so `9123 4567` is saved as `91234567`. Do not add `+65` or another country code.
* `ADDRESS` is 1 to 200 characters. It cannot contain only whitespace or invisible formatting characters, such as non-breaking spaces and zero-width spaces.
* `LEVEL` is 1 to 20 characters of letters, digits and spaces, such as `Sec 3` or `JC1`.
* `SUBJECT` is 1 to 50 characters of letters, digits, spaces and hyphens, such as `E-Math`, `Physics` or `H2 Chemistry`.
* A student with the same phone number and the same name as an existing student, ignoring case, is a duplicate and is rejected with `This student already exists in the list.`

Examples:
* `add n/Tan Wei Ming p/87654321 a/Blk 8 Bishan St 22, #05-13 l/JC1 sub/H2 Math`
* `add n/Ramesh s/o Kumar p/9123 4567 a/12 Clementi Rd l/Sec 3 sub/Physics`

**Screenshot example:** `add n/Jamie Lee p/81230007 a/60 Example Street l/JC1 sub/H2 Math` adds a JC1 student taking H2 Math. The confirmation and Jamie's card show the saved details. The student list has been scrolled down to show the new card:

<a href="images/add-student.png"><img src="images/add-student.png" alt="Add confirmation and Jamie Lee's new student card after add n/Jamie Lee p/81230007 a/60 Example Street l/JC1 sub/H2 Math" width="726"></a>

### Listing all students: `list`

Shows all students and reports the number displayed.

Format: `list`

* Restores the full student list after a `find` or `flagged` command.
* With students: `Listed {COUNT} students.`, for example `Listed 3 students.`
* With one student: `Listed 1 student.`
* Without students: `No students found. Use add to create a student.`
* Extra arguments, such as `list 3`, are rejected with
  `The list command does not accept parameters.`. No student data is changed.

**Screenshot example:** after `find TAN`, run `list`. The confirmation `Listed 6 students.` shows that the full list has been restored. Priya Ramesh is again student 2:

<a href="images/list-students.png"><img src="images/list-students.png" alt="Listed 6 students after list restores the full list following find TAN" width="726"></a>

### Finding students by name: `find`

Shows students whose names contain all the search keywords. You can use part of a name instead of typing the full name.

Format: `find KEYWORD [MORE_KEYWORDS]...`

* At least one keyword is required. `find` without keywords shows the invalid command format message and usage; the displayed list is unchanged.
* Matching ignores uppercase/lowercase. `find TAN` and `find tan` give the same results.
* Every keyword must occur somewhere in the name, in any order. With `Alex Tan` and `Ethan Tan` saved, `find tan` shows both students, while `find alex tan` shows only `Alex Tan`.
* Leading, trailing and repeated whitespace is ignored. Keywords are literal text, with no field prefixes, wildcard matching or spelling correction. Punctuation and accented characters must match as entered in the name, ignoring case.
* Each search checks all saved students, replacing the previous `find` or `flagged` filter. The matches keep their original list order.
* With one match: `Found 1 student.` With multiple matches: `Found {COUNT} students.` With no matches: `No matching students found. Use list to show all students.`
* Matches are numbered from 1. Use these displayed numbers for `view`, `log`, `edit` and `delete`, or click a matching card to view its history.
* `find` does not change or delete student details or lesson records. Type `list` to restore all students. Search filters are not saved across restarts.
* Adding or editing a student also restores the full list. Viewing, logging and deleting use the current filter.

Examples:
* `find ali` matches `Alice Tan` and `Alicia Lim`.
* `find tan ming` matches `Tan Wei Ming` and `Ming Tan`.
* `find Muthu a/l Raju` matches a saved name of `Muthu A/L Raju`; unlike `add` and `edit`, `find` does not interpret `a/` as an address prefix.

**Screenshot example:** after `find TAN`, only Alex Tan and Ethan Tan are displayed, numbered 1 and 2:

<a href="images/find-students.png"><img src="images/find-students.png" alt="Found 2 students after find TAN, showing Alex Tan and Ethan Tan" width="726"></a>

### Flagging students for follow-up: `flag`, `unflag`, `flagged`

Use a flag to mark a student who needs follow-up. A reason is optional. Flagged student cards show an amber `FLAGGED` badge, and cards with a reason show `Flag reason: REASON` below the name.

Formats:

* `flag INDEX [r/REASON]`
* `unflag INDEX`
* `flagged`

`flag` and `unflag` use the student's number in the currently displayed list. `flagged` searches the complete saved student collection, displays all flagged students in their original order, and numbers the results from 1. The displayed numbers can be used directly with other index-based commands. Type `list` to restore all students.

The optional `REASON`:

* is 1 to 100 characters after surrounding whitespace is removed;
* cannot be blank or contain control characters; and
* may contain punctuation and text that resembles another prefix, such as `a/level`, `date/` or `c/homework`, because only lowercase `r/` is parsed by `flag`.

Flagging an unflagged student adds the flag. Flagging an already flagged student with a different reason replaces the old reason. Repeating the same reason is rejected with `NAME is already flagged with that reason.` Omitting `r/` for an already flagged student keeps any existing reason and reports `NAME is already flagged.`

`unflag` removes both the flag and its reason. Trying to unflag an unflagged student reports `NAME is not flagged.` When `unflag` is run from the `flagged` results, that student disappears from the displayed list because they no longer match the filter.

Examples:

* `flag 2`
* `flag 2 r/Missed homework twice`
* `unflag 2`
* `flagged`

Flags are saved automatically. Editing a student's details, logging a lesson or deleting one lesson does not change their flag. The active `find` or `flagged` filter itself is not saved across restarts.

**Screenshot example:** `flag 1 r/Missed homework twice` marks Alex Tan and shows the confirmation, amber badge and reason on his card:

<a href="images/flag-student.png"><img src="images/flag-student.png" alt="Alex Tan's card with an amber FLAGGED badge and the reason Missed homework twice" width="726"></a>

### Editing a student: `edit`

Changes the details of an existing student. The student's lesson records and follow-up flag are kept.

Format: `edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [l/LEVEL] [sub/SUBJECT]`

* Edits the student at the specified `INDEX`. The index refers to the index number shown in the displayed student list, and **must be a positive integer** 1, 2, 3, ...
* At least one field must be given. Fields can be given in any order, and each field can be given at most once.
* Only the fields given are changed. Each field follows the same rules as in [`add`](#adding-a-student-add).
* Lesson records and the follow-up flag are not changed, and `view` shows the lessons as before.
* Editing a student so that they have the same phone number and the same name as another student, ignoring case, is rejected with `This student already exists in the list.`
* If no field is given, for example `edit 1`, the edit is rejected with `At least one field to edit must be provided.`
* **Caution:** a new name containing ` a/` after a space, such as `Muthu a/l Raju`, is not rejected by `edit`. It changes the student's address instead. Type it in capitals, for example `n/Muthu A/L Raju`. See [Known issues](#known-issues).

Examples:
* `edit 1 p/91234567` changes the phone number of the 1st student.
* `edit 2 l/Sec 4 sub/A-Math` changes the level and subject of the 2nd student.

**Screenshot example:** `edit 1 l/Sec 4 sub/A-Math` updates Alex Tan's level and subject. The confirmation and student card show the new values:

<a href="images/edit-student.png"><img src="images/edit-student.png" alt="Edit confirmation and Alex Tan's card showing Sec 4 and A-Math" width="726"></a>

### Deleting a student: `delete`

Deletes the specified student, together with their lesson records.

Format: `delete INDEX`

* Deletes the student at the specified `INDEX`.
* The index refers to the index number shown in the displayed student list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd student in the list.

**Screenshot example:** starting from the six-student list shown in the `list` example, `delete 2` removes Priya Ramesh. The confirmation identifies the deleted student, and Marcus Lim becomes student 2:

<a href="images/delete-student.png"><img src="images/delete-student.png" alt="Deletion confirmation for Priya Ramesh after delete 2, with Marcus Lim now student 2" width="726"></a>

### Viewing a student's lesson records: `view`

Shows all lesson records for one student in the command result area.

Format: `view INDEX`

You can also left-click a student's card to view the same lesson history.
Clicking the same card again refreshes its history, for example after logging a
lesson. Viewing by either method highlights the student's card and scrolls it
into view. A single left-click switches histories directly from the command box.
Any text you are typing in the command box is preserved.

* `INDEX` is a positive integer from the currently displayed student list.
* Records appear with the most recent date first, numbered from 1. Records on the same date appear with the most recently logged first. These numbers identify rows in the output, not student indexes.
* Each record shows the lesson date, as `DD/MM/YYYY`, then what was covered, for example `1. 18/10/2026: Quadratic inequalities`.
* Lessons logged before v1.4 have no date and show `(undated)` in place of the date, for example `2. (undated): Binomial theorem`.
* Repeated lesson content is displayed as separate records. Viewing does not change records or the current student filter.
* A student with no records produces `No lessons logged yet for NAME.`
* The header is `Lesson records for NAME (COUNT):`, followed by one line per record in the form `NUMBER. DATE: CONTENT`.
* Missing indexes, non-integers, and extra arguments produce an invalid-command-format message with usage. Zero, negative integers, leading-zero integers, integers too large to parse, and indexes outside the displayed list produce `The student index provided is invalid.`

Example: `log 1 c/Algebra` followed by `log 1 c/Geometry` and `view 1` displays Geometry before Algebra, each with today's date.

**Screenshot example:** `view 1` displays Alex Tan's three dated lessons, newest date first, and highlights his student card:

<a href="images/Ui.png"><img src="images/Ui.png" alt="Three dated lesson records for Alex Tan after view 1, with his student card highlighted" width="726"></a>

### Reading long command results

Drag the horizontal divider below the result box to give lesson histories more
space. Long text wraps automatically; scroll inside the result box to read older
records. Each command displays its feedback from the beginning. The command box
remains above the result area, and the student list remains below it.

### Logging a lesson: `log`

Records what you covered in a lesson for the specified student.

Format: `log INDEX c/CONTENT [date/DATE]`

* Logs a lesson for the student at the specified `INDEX`.
* The index refers to the index number shown in the displayed student list.
* The index **must be a positive integer** 1, 2, 3, ...
* `CONTENT` can be any text from 1 to 300 characters, e.g. the topics covered or homework set. It cannot contain only whitespace or invisible formatting characters, such as non-breaking spaces and zero-width spaces. Internal spaces and formatting characters used in real text are kept.
* `DATE` is the day the lesson was taught, as `DD/MM/YYYY`, for example `21/10/2026`. Single-digit days and months such as `1/2/2026` are also accepted. Without `date/`, the lesson is dated with today's date on your computer.
* `DATE` must be a real date from 01/01/2000 up to today: future dates and dates such as `31/02/2026` are rejected. The day and month have one or two digits, and the year has exactly four.
* Confirmation: `Logged lesson for NAME on DD/MM/YYYY: CONTENT`, for example `Logged lesson for Tan Wei Ming on 18/10/2026: Quadratic inequalities`.
* Lessons are kept in date order, so a lesson logged later for an earlier date takes its place by date. Lessons on the same date keep the order they were logged in. Logging the same content twice records two lessons.
* To correct a logged lesson's content or date, use `editlesson`. To remove an incorrect record, use `deletelesson`. Both are described below.

Examples:
* `list` followed by `log 3 c/Quadratic inequalities` logs a lesson on quadratic inequalities for the 3rd student in the list.
* `log 1 c/Electric circuits: series and parallel, Q1-5 for homework` logs a lesson for the 1st student in the list.
* `log 2 c/Surds and indices date/20/10/2026` logs a lesson taught on 20 October 2026 for the 2nd student, placed among their records by that date.

**Screenshot example:** after `list`, the command `log 1 c/Trigonometry: solved right-angled triangle problems. date/18/10/2026` records an earlier lesson for Alex Tan. The confirmation includes the lesson date and content:

<a href="images/log-lesson-past-date.png"><img src="images/log-lesson-past-date.png" alt="Confirmation of a Trigonometry lesson logged for Alex Tan on 18/10/2026" width="726"></a>

### Deleting a lesson record: `deletelesson`

Deletes one lesson without deleting the student or their other lesson records.

Format: `deletelesson STUDENT_INDEX LESSON_INDEX`

* `STUDENT_INDEX` is the student's number in the currently displayed student list.
* `LESSON_INDEX` is the lesson's current number shown by `view STUDENT_INDEX`. Lesson 1 is the most recent lesson, not the oldest.
* Both indexes must be positive whole numbers without leading zeros. Exactly two arguments are required, in the order shown.
* Use `view STUDENT_INDEX` before deleting to check the lesson number. Numbers are positions, not permanent IDs: logging or deleting a lesson, or changing a lesson's date with `editlesson`, can change them. Running `view` first is recommended, not required.
* Only the selected occurrence is deleted, even if several lessons have identical content. Remaining lessons keep their order and are renumbered from 1 on the next view.
* Confirmation: `Deleted lesson LESSON_INDEX for NAME: CONTENT`. Run `view STUDENT_INDEX` again or click the student's card to see the remaining records.
* Student details, other students' records and the current student filter are unchanged. Deleting the final lesson keeps the student with an empty history.
* Missing arguments, non-integers such as `abc`, `+1` or `1.5`, and extra
  arguments produce an invalid-command-format message with usage. Zero,
  negative integers, leading-zero integers, overflowing integers and
  out-of-range indexes produce `The student index provided is invalid.` or
  `The lesson index provided is invalid.`, depending on the index.
* If the student's history is empty, a valid positive lesson number produces
  `No lessons logged yet for NAME.`. Nothing is deleted.
* Deletion is saved automatically and cannot be undone in the app. `delete INDEX` still deletes the entire student and all their lessons; it is a different command.

Example: after logging `Algebra`, then `Geometry` for student 2 on 18/10/2026, `view 2` shows `1. 18/10/2026: Geometry` and `2. 18/10/2026: Algebra`. `deletelesson 2 1` deletes Geometry and confirms `Deleted lesson 1 for NAME: Geometry`. A subsequent `view 2` shows only `1. 18/10/2026: Algebra`.

**Screenshot example:** after the past-date logging example above, `view 1` shows Alex Tan's four lessons in date order. The Trigonometry lesson dated 18/10/2026 is lesson 2:

<a href="images/lesson-history-before-delete.png"><img src="images/lesson-history-before-delete.png" alt="Four lessons for Alex Tan before deletion, with the 18/10/2026 Trigonometry lesson numbered 2" width="726"></a>

Run `deletelesson 1 2`, then `view 1`. Three lessons remain, numbered 1 to 3. Alex's student details are kept:

<a href="images/lesson-history-after-delete.png"><img src="images/lesson-history-after-delete.png" alt="Three remaining lessons after deletelesson 1 2 followed by view 1; Alex Tan's details are unchanged" width="726"></a>

### Editing a lesson record: `editlesson`

Corrects the content or date of one lesson record, without deleting it and logging it again.

Format: `editlesson STUDENT_INDEX LESSON_INDEX [c/CONTENT] [date/DATE]`

* `STUDENT_INDEX` and `LESSON_INDEX` work as in `deletelesson`: the student's number in the displayed student list, and the lesson's current number shown by `view STUDENT_INDEX`, where lesson 1 is the most recent.
* Give at least one of `c/` and `date/`. Without either, the command reports `At least one field to edit must be provided.`
* `c/CONTENT` replaces the lesson's content and `date/DATE` replaces its date. A field you leave out keeps its current value. Both follow the same rules as in `log`: content of 1 to 300 characters, and a real date from 01/01/2000 up to today.
* Changing only the content keeps the lesson's number.
* Changing the date moves the lesson to its place in date order, so its number can change. On a date that already has lessons, the edited lesson goes after them, so `view` shows it first among that date's lessons. The confirmation gives the new number to use in later `editlesson` and `deletelesson` commands.
* Lessons saved before v1.4 have no date and show `(undated)`. Editing only their content keeps them undated, and giving `date/` dates them. A date cannot be removed from a dated lesson.
* If the new content and date are the same as the lesson's current ones, the command reports `The lesson already has those details.` and nothing changes.
* Confirmation: `Edited lesson LESSON_INDEX for NAME (now lesson NEW_INDEX). DATE: CONTENT`.
* Student details, other lessons, other students and the current student filter are unchanged. Index errors and an empty history give the same messages as `deletelesson`, and a future date gives the same message as `log`.

Examples:
* `editlesson 2 1 c/Quadratic inequalities and homework review` corrects the content of student 2's most recent lesson. It stays lesson 1.
* Suppose `view 1` shows `1. 23/10/2026: Trigonometry`, `2. 21/10/2026: Vectors` and `3. 19/10/2026: Algebra`. `editlesson 1 3 date/22/10/2026` moves Algebra to 22/10/2026 and confirms `Edited lesson 3 for NAME (now lesson 2). 22/10/2026: Algebra`. A following `view 1` lists Trigonometry, Algebra and Vectors, in that order.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TutorLah automatically saves data after each successful command. You do not need to save manually.

### Editing the data file

TutorLah data is saved automatically as a JSON file `data/addressbook.json`, relative to the folder from which you launch the app. If you follow the Quick start steps, this is the folder containing the JAR. Advanced users are welcome to update data directly by editing that data file.

In the data file, a lesson's date is stored as `"date" : "YYYY-MM-DD"`, for example `"date" : "2026-10-18"`. A lesson without a `date` field is shown as `(undated)`. A date in any other format makes the data file invalid.

A flagged student with a reason stores `"flag" : { "reason" : "REASON" }`; a flag without a reason stores `"flag" : { }`. An unflagged student has no `flag` property. Data files created before flags were introduced therefore continue to load every student as unflagged.

Addresses and lesson content containing only whitespace or invisible formatting characters are invalid in data files too. If an older file contains these values, back up the file and correct them before opening it.

<box type="warning" seamless>

**Caution:**
If the data file cannot be loaded, TutorLah shows a startup warning with its location and starts with an empty student list. Saving is disabled for that session, so ordinary commands cannot overwrite the original file. Close TutorLah using the window's close button, make a backup of the file, then repair it or restore a valid copy at the same location. Restart TutorLah to load the recovered students and enable saving again. Repairing the file while the app remains open does not enable saving; a restart is required. Back up the file before editing it.<br>
Furthermore, certain edits can cause TutorLah to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous TutorLah home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.
3. **Names with `a/` after a space**, such as `Muthu a/l Raju` or `Siti a/p Ahmad`, are read as two fields, because ` a/` marks the start of the address:
   * With `add`, the command is rejected, because the address is then given twice.
   * With `edit`, the command succeeds but **changes the address**. For example, `edit 1 n/Muthu a/l Raju` sets the name to `Muthu` and replaces the student's address with `l Raju`.
   * The remedy is to type it in capitals, for example `n/Muthu A/L Raju`, as prefixes are case-sensitive. This keeps the full name and leaves the address unchanged.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE a/ADDRESS l/LEVEL sub/SUBJECT` <br> e.g., `add n/Tan Wei Ming p/87654321 a/Blk 8 Bishan St 22, #05-13 l/JC1 sub/H2 Math`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Delete lesson** | `deletelesson STUDENT_INDEX LESSON_INDEX`<br> e.g., `view 2`, then `deletelesson 2 1`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE] [a/ADDRESS] [l/LEVEL] [sub/SUBJECT]`<br> e.g., `edit 2 l/Sec 4 sub/A-Math`
**Edit lesson** | `editlesson STUDENT_INDEX LESSON_INDEX [c/CONTENT] [date/DATE]`<br> e.g., `editlesson 2 1 c/Vectors date/21/10/2026`
**Find**   | `find KEYWORD [MORE_KEYWORDS]...`<br> e.g., `find tan wei` (use `list` to show all students again)
**Flag**   | `flag INDEX [r/REASON]`<br> e.g., `flag 2 r/Missed homework twice`
**Flagged** | `flagged` (use `list` to show all students again)
**List**   | `list`
**View**   | `view INDEX`<br> e.g., `view 1`
**Log**    | `log INDEX c/CONTENT [date/DATE]`<br> e.g., `log 3 c/Quadratic inequalities date/21/10/2026`
**Unflag** | `unflag INDEX`<br> e.g., `unflag 2`
**Help**   | `help`
**Exit**   | `exit`
