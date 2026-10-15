---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TutorLah User Guide

TutorLah is a **desktop application for freelance tutors to keep track of their students and the lessons they have taught them, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). Store each student's contact details, level and subject, record what you covered after every lesson, and look back at a student's lesson history before the next one, without relying on scattered notes or memory. If you type quickly, TutorLah lets you do this faster than a typical mouse-driven application.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/AY2627S1-CS2103T-T11-1/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TutorLah. Your data will be saved in this folder.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample students.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all students.

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

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `Tan Wei Ming`.

* Prefixes such as `n/`, `p/` and `c/` are lowercase and must be typed exactly as shown.

* Parameters with prefixes can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

* `INDEX` refers to the number shown beside a student in the currently displayed student list. It must be a positive integer such as 1, 2, 3, ...

* Extraneous parameters for `help` and `exit` are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... `

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.
</box>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all students: `list`

Shows all students and reports the number displayed.

Format: `list`

* With students: `Listed {COUNT} students.`, for example `Listed 3 students.`
  For one student, the message is `Listed 1 students.`.
* Without students: `No students found. Use add to create a student.`
* Extra arguments, such as `list 3`, are rejected with
  `The list command does not accept parameters.`. No student data is changed.

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.

### Viewing a student's lesson records: `view`

Shows all lesson records for one student in the command result area.

Format: `view INDEX`

* `INDEX` is a positive integer from the currently displayed student list.
* Records appear with the most recently logged first, numbered from 1. These numbers identify rows in the output, not student indexes.
* Lessons currently store content only, so ordering is by when records were logged, not by lesson date.
* Repeated lesson content is displayed as separate records. Viewing does not change records or the current student filter.
* A student with no records produces `No lessons logged yet for NAME.`
* The header is `Lesson records for NAME (COUNT):`, followed by one numbered line per record.
* Missing indexes, non-integers, and extra arguments produce an invalid-command-format message with usage. Zero, negative integers, integers too large to parse, and indexes outside the displayed list produce `The student index provided is invalid.`

Example: `log 1 c/Algebra` followed by `log 1 c/Geometry` and `view 1` displays Geometry before Algebra.

### Reading long command results

Drag the horizontal divider below the result box to give lesson histories more
space. Long text wraps automatically; scroll inside the result box to read older
records. Each command displays its feedback from the beginning. The command box
remains above the result area, and the student list remains below it.

### Logging a lesson: `log`

Records what you covered in a lesson for the specified student.

Format: `log INDEX c/CONTENT`

* Logs a lesson for the student at the specified `INDEX`.
* The index refers to the index number shown in the displayed student list.
* The index **must be a positive integer** 1, 2, 3, ...
* `CONTENT` can be any text from 1 to 300 characters, e.g. the topics covered or homework set. It cannot be blank.
* Lessons are kept in the order they are logged. Logging the same content twice records two lessons.
* There is currently no way to edit or delete a logged lesson, so check the student's name in the confirmation message.

Examples:
* `list` followed by `log 3 c/Quadratic inequalities` logs a lesson on quadratic inequalities for the 3rd student in the list.
* `log 1 c/Revision of trigonometric identities, Q1-5 for homework` logs a lesson for the 1st student in the list.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.
3. **Names with `a/` after a space**, such as `Muthu a/l Raju`, are rejected because ` a/` marks the start of the address. The remedy is to type it in capitals, e.g. `n/Muthu A/L Raju`, as prefixes are case-sensitive.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... ` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**List**   | `list`
**View**   | `view INDEX`<br> e.g., `view 1`
**Log**    | `log INDEX c/CONTENT`<br> e.g., `log 3 c/Quadratic inequalities`
**Help**   | `help`
