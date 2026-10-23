---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TutorLah Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* This project is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) by the [SE-EDU initiative](https://se-education.org).
* `FindCommand`, `FindCommandParser` and `NameContainsKeywordsPredicate` are adapted from AB3's find feature, with all-keyword substring matching and student-specific feedback.
* OpenAI Codex assisted with the implementation, tests and documentation for student-name search, deleting individual lesson records and Unicode blank-field validation, and with the Logic/parser diagrams and Developer Guide maintenance.
* Anthropic Claude Code, used by aerodart, assisted with the implementation, tests and documentation for dated lesson records and logging lessons with a past date.
* The team's agent brief (`tutorlah-agent-brief.md`) and `AGENTS.md` were inspired by the `AGENTS.md` files of teams [CS2103T-T16-1](https://github.com/AY2627S1-CS2103T-T16-1/tp) and [CS2103T-T16-2](https://github.com/AY2627S1-CS2103T-T16-2/tp). The ideas were adapted and the text was written by our team.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other when the user successfully logs a lesson for an existing student with the command `log 1 c/Vectors`. `Logic` updates the student in `Model`, then asks `Storage` to save the whole address book to the data file. Error paths, such as an invalid index or a failed save, are omitted.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="666" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/ui/Ui.java).

<puml src="diagrams/UiClassDiagram.puml" alt="Partial UI class diagram showing the main window, callback fields, student cards and feedback display" width="850" />

This partial class diagram focuses on command input, student cards and feedback. `MainWindow` supplies a `CommandExecutor` callback to `CommandBox` and a `Consumer<Index>` callback to `PersonListPanel`. The fields shown on those classes hold the callbacks; neither input part needs a reference to the concrete `MainWindow` class. Solid arrows show held references, with `1` identifying a single referenced object; dashed arrows with `<<create>>` show creation dependencies. `PersonListPanel` creates `PersonCard` objects for visible students, and each card displays one `Person` from the Model component. The creation dependency does not describe how many cards are visible. `Person` remains the Java class name for a student.

`UiManager` implements `Ui` and creates `MainWindow`. `MainWindow` and its visible UI parts inherit from `UiPart`; the window also creates `HelpWindow` and `StatusBarFooter`. These startup, shared-base and supporting classes are omitted from the focused diagram. The UI uses JavaFX, with layouts in `src/main/resources/view`, such as [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/resources/view/MainWindow.fxml).

The UI responsibilities are:

* `CommandBox` submits typed commands and clears its text only after successful execution. A failed typed command remains available for correction and receives error styling.
* `PersonListPanel` observes the filtered student list supplied by `Logic`. Cards show each student's displayed index, name, phone, address, level and subject. The indexes follow the displayed list, including after a search or deletion.
* A primary mouse press on a populated card sends its displayed index to `MainWindow`. The callback runs before selection can rebuild the cell, so changing students after a typed `view` needs only one press. Repeated presses refresh the history; empty cells and other mouse buttons do not execute the shortcut.
* `ResultDisplay` shows command feedback as wrapping, read-only text. Each update resets the caret and scroll position to the beginning, including when the feedback is unchanged. Long histories remain accessible by scrolling, and the result area can be resized with the horizontal divider.
* After a successful `view`, `MainWindow` asks `PersonListPanel` to highlight and scroll to the viewed student. Programmatic selection and list changes do not execute a view command.

### Logic component

**API**: [`Logic.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

<!-- These PNG link targets are generated by the puml tags during the build. -->
<a href="diagrams/LogicClassDiagram.png" no-validation><puml src="diagrams/LogicClassDiagram.puml" alt="Partial Logic class diagram showing command execution, held references and command results" width="740" /></a>

[`LogicManager`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/LogicManager.java)
implements `Logic` and holds one `AddressBookParser`, `Model` and `Storage`.
Solid arrows show held references; dashed arrows show dependencies. Hollow
triangles point to a superclass or interface. Lavender identifies Logic classes,
pink the Model API and yellow the Storage API. The `Logic (partial)` boundary
keeps those external APIs outside the component.
`LogCommand` and `ViewCommand` are representative subclasses of `Command`.
Other commands, parser internals and individual commands' dependencies are
omitted; the command's dependency on its result type is shown once on `Command`.
Click any diagram in this section to open its full-size image.

How command execution works:

1. `AddressBookParser#parseCommand` returns a concrete `Command` for the input.
1. `LogicManager` calls `Command#execute(Model)`, which reads or updates the
   model and returns a `CommandResult`.
1. `LogicManager` saves the complete address book through `Storage`, then
   returns the result to the UI. This save also runs for non-mutating commands
   such as `list` and `view`.

`CommandResult` contains feedback and the help/exit flags. A successful `view`
also supplies a displayed student `Index`, even for an empty history; other
commands leave it absent (`0..1`). The UI uses it to highlight the student, as
shown in [Viewing lesson history](#viewing-lesson-history).

Invalid input raises `ParseException`; execution or save failures raise
`CommandException`. A failed save prevents the normal result from reaching the
UI, but does not roll back a change already made to the model.

The following diagram focuses on parser selection and command creation:

<a href="diagrams/ParserClasses.png" no-validation><puml src="diagrams/ParserClasses.puml" alt="Partial parser class diagram showing selection of lesson parsers and creation of their commands" width="520" /></a>

[`AddressBookParser`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/parser/AddressBookParser.java)
separates the command word from its arguments and creates the matching parser.
Command-specific parsers implement the generic `Parser` interface: for example,
`LogCommandParser` implements `Parser<LogCommand>` and returns a `LogCommand`.
The type parameter and common `Command` superclass are omitted from this
diagram. Dashed arrows labelled `<<create>>` show object creation, not held
references. Other command parsers are omitted; `help` and `exit` are created
directly without separate parsers.

The helper dependencies are shown separately to keep the overview readable.
Pale grey-blue boxes identify helpers within the same parser package.

<a href="diagrams/ParserHelpers.png" no-validation><puml src="diagrams/ParserHelpers.puml" alt="Partial parser helper diagram showing tokenization for logging and shared validation for both lesson parsers" width="500" /></a>

* `LogCommandParser` tokenizes `c/` and optional `date/` arguments using
  `ArgumentTokenizer` and the `Prefix` constants in `CliSyntax`.
  `ArgumentMultimap` stores the preamble and prefixed values and checks for
  duplicate prefixes. `ParserUtil` validates the student index, content and date.
* `DeleteLessonCommandParser` splits two positional indexes on whitespace,
  without using the tokenizer. It shares `ParserUtil`'s index validation, using
  the appropriate student- or lesson-index error message.

The helper diagram omits prefix classes, model values and exceptions to focus
on tokenization, use of the returned multimap and shared validation.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Viewing lesson history

Both `view INDEX` and a primary press on a student card reach `MainWindow#executeCommand`. Typed input arrives through the `CommandBox` callback; a card press arrives through `MainWindow#handlePersonClick`, which builds `view INDEX` using the card's current displayed index. The click path leaves command-box text unchanged.

<puml src="diagrams/ViewHistorySequenceDiagram.puml" alt="Successful view sequence showing typed input and a student-card press joining the same UI execution path" width="950" />

The sequence shows a successful view of displayed student 1. The `User` actor represents the person entering a command or pressing a student card. Both entry paths share the same execution flow in `MainWindow`. The diagram expands only the UI component; `Logic` is shown through its interface. Parsing, model access, saving, JavaFX internals and error paths are omitted. Activation bars and the private self-call from `handlePersonClick` to `executeCommand` are omitted. Solid arrows show input events or calls; dashed arrows show returns. A return labelled `return` carries no value. The final alternatives complete the same entry path chosen at the top.

`MainWindow` passes the command to `Logic#execute`. A successful `ViewCommand` returns a `CommandResult` containing the feedback and the viewed student's displayed index. `MainWindow` displays the feedback, then passes the index to `PersonListPanel#selectPerson` to synchronise the highlight and scroll position. The index is present even when the student's history is empty. This selection does not invoke the click callback, so it cannot execute the command again.

`ViewCommand` reads the student from `Model#getFilteredPersonList`, leaving the current filter and student data unchanged. It traverses the stored lessons backwards: the most recent lesson date appears first, with the most recently logged record first among equal dates. Existing undated records appear at the oldest end when lessons have been added in date order. Each row shows its displayed lesson number, formatted date and content; undated records use `(undated)`. An empty history returns `No lessons logged yet for NAME.`.

If parsing or execution fails, `MainWindow` displays the error instead of the history and does not apply a viewed-student selection. For typed input, `CommandBox` keeps the failed command and marks it as an error. Card-click failures are displayed through the same execution path without clearing typed text. Although viewing does not modify students or lessons, it still goes through `LogicManager`'s existing save-after-success path.

### Rejecting blank Unicode fields

`Address#isValidAddress` and `Lesson#isValidContent` reject values composed entirely of Unicode whitespace (`\p{IsWhite_Space}`), format characters (`\p{Cf}`), or a mixture of the two. This includes non-breaking spaces, ZERO WIDTH SPACE (U+200B), WORD JOINER (U+2060) and a standalone BOM (U+FEFF). The check does not remove formatting characters inside real text: joiners in written languages and emoji sequences are preserved, along with internal spaces and the existing length and character rules. Both command parsing and the JSON adapters use these model validators, so add, edit, log and data loading share the same non-blank rule and existing field-specific messages.

### Deleting an individual lesson record

`DeleteLessonCommandParser` parses the student and lesson indexes.
Both use `ParserUtil#parseCommandIndex`, with index-specific error messages.
`DeleteLessonCommand` selects the student from `Model#getFilteredPersonList`
and validates the lesson index before changing data.

`view` numbers lessons newest first, but `Person#getLessons` stores them
oldest first. `Person#withLessonAdded` inserts each lesson in date order, so
the stored order and the displayed numbers stay in step even when a lesson is
logged with a past date. The command converts the displayed number using
`lessons.size() - lessonIndex.getOneBased()`, with an assertion checking the
resulting index. Removing by position ensures that only the selected occurrence
is deleted when lesson content repeats.

`Person#withLessonRemoved` returns a copy with the remaining lessons and all
student details preserved. `Model#setPerson` updates the student without
resetting the filter; the existing `LogicManager` save path persists the change.
Deleting the last lesson keeps the student with an empty history.
Attempting another deletion reports `No lessons logged yet for NAME.`.

### Finding students by name

`AddressBookParser` routes `find` input to `FindCommandParser`, which splits it into whitespace-separated literal keywords and rejects an empty search. It does not use field prefixes, so a search for `a/l` cannot be interpreted as an address edit.

`NameContainsKeywordsPredicate` stores an immutable copy of the keywords as typed. A name matches when it contains every keyword as a substring, regardless of keyword order. Each candidate substring is compared with `String#regionMatches(true, ...)`, avoiding context-sensitive lowercase conversion such as Greek final sigma. Matching is locale-independent and literal, without regular expressions or spelling correction. Predicate equality and hashing use the stored keyword list; matching ignores case without changing the keywords.

`FindCommand` passes this predicate to `Model#updateFilteredPersonList`. The existing `FilteredList` applies it to the full address book, so a later search replaces the previous search instead of narrowing it further. The GUI observes this list and updates the cards and their displayed indexes automatically. An empty filtered list shows guidance to type `list`; it does not imply that stored students have been deleted.

`view`, `log`, `edit`, `delete`, `deletelesson` and student-card clicks continue to use displayed indexes. `list` restores all students. Adding or editing also restores the full list, while viewing, logging and deleting students or lessons retain the filter. The search changes no student or lesson data and is not persisted across restarts; the existing storage path saves the complete address book.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a freelance or private tutor who manages multiple students and recurring lessons
* needs to keep student details and lesson histories in one place
* reviews previous lesson records when preparing for each student's next lesson
* prefers a desktop application with keyboard-driven interaction
* is comfortable entering text commands

**Value proposition**: Manage student details and lesson records quickly in one keyboard-driven desktop application, so tutors can prepare for upcoming lessons without relying on scattered notes or memory.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

The stories marked `Yes` in the **MVP** column form the proposed MVP.

| Priority | MVP | As a … | I want to … | So that I can… |
|----------|-----|--------|-------------|----------------|
| `* * *` | Yes | tutor | add a student's details | keep their information in one place |
| `* * *` | Yes | tutor | see a list of my students | identify the student whose information or lesson records I need |
| `* * *` | Yes | tutor | delete the student and their associated lesson records | ensure those details are no longer kept in TutorLah |
| `* * *` | Yes | tutor | add a dated record of what I covered to that student | avoid relying on memory before the next lesson |
| `* * *` | Yes | tutor | view their lesson records with the most recent first | see where we stopped and what we covered previously |
| `* *` | No | tutor | edit a student's details | keep their information accurate when it changes |
| `* *` | No | tutor | store the student's phone number and address | reach them and find their home when needed |
| `* *` | No | tutor managing many students | find a student by name | access the correct student's information quickly |
| `* *` | No | tutor | record a student's regular weekly lesson slot | remember the schedule and identify clashes |
| `* *` | No | tutor | delete an incorrect lesson record | keep the student's lesson history accurate |
| `* *` | No | tutor | archive a former student | remove them from the active list without losing their history |
| `*` | No | tutor | store multiple contact methods for a student | contact the appropriate person when needed |
| `*` | No | tutor | manage multiple weekly slots, one-off lessons, and rescheduling | represent lesson arrangements more accurately |
| `*` | No | tutor managing many students | add students in batches | avoid entering each student separately |
| `*` | No | tutor | restore archived or deleted information | recover records removed accidentally |

### Use cases

(For all use cases below, the **System** is `TutorLah` and the **Actor** is the `Tutor`, unless specified otherwise)

**Use case: UC01 - Add a student**

**Guarantees:** The student is added only if all required details are valid and no student with the same name and phone number already exists in the system.

**MSS**

1.  Tutor requests to add a student by providing the necessary details.
2.  TutorLah adds the student and shows the details of the added student.

    Use case ends.

**Extensions**

* 1a. Tutor leaves out one or more required details.

    * 1a1. TutorLah shows an error message with the expected format.

      Use case resumes at step 1.

* 1b. One or more of the given details are invalid.

    * 1b1. TutorLah shows an error message describing the invalid detail.

      Use case resumes at step 1.

* 1c. A student with the same name and phone number already exists.

    * 1c1. TutorLah informs Tutor that this student already exists.

      Use case ends.

**Use case: UC02 - List all students**

**MSS**

1.  Tutor requests to view all students.
2.  TutorLah shows the list of all students.

    Use case ends.

**Extensions**

* 2a. There are no students.

    * 2a1. TutorLah informs Tutor that there are no students.

      Use case ends.

**Use case: UC03 - Delete a student**

**Guarantees:** The student's lesson records are deleted together with the student. Other students and their respective lesson records are not affected.

**MSS**

1.  Tutor <u>lists students (UC02)</u>.
2.  Tutor requests to delete a specific student in the list.
3.  TutorLah deletes the student and their lesson records, and shows the details of the deleted student.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

**Use case: UC04 - Log a lesson for a student**

**Guarantees:** The lesson is recorded only against the chosen student. The student's existing lesson records are not changed.

**MSS**

1.  Tutor <u>lists students (UC02)</u>.
2.  Tutor requests to log a lesson for a specific student in the list, providing what was covered.
3.  TutorLah records the lesson and shows the student's name together with what was covered.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The lesson content is missing, blank, or too long.

    * 2b1. TutorLah shows an error message.

      Use case resumes at step 2.

**Use case: UC05 - View a student's lesson records**

**MSS**

1.  Tutor <u>lists students (UC02)</u>.
2.  Tutor requests to view the lesson records of a specific student in the list.
3.  TutorLah shows the student's lesson records.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 3a. The student has no lesson records.

    * 3a1. TutorLah informs Tutor that no lessons have been logged for the student.

      Use case ends.

**Use case: UC06 - Edit a student's details**

**Guarantees:** The student's lesson records are kept after the edit.

**MSS**

1.  Tutor <u>lists students (UC02)</u>.
2.  Tutor requests to edit a specific student in the list, providing the new details.
3.  TutorLah updates the student and shows the updated details.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. Tutor does not provide any new details.

    * 2b1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2c. One or more of the new details are invalid.

    * 2c1. TutorLah shows an error message describing the invalid detail.

      Use case resumes at step 2.

* 2d. The edited student would have the same name and phone number as another existing student.

    * 2d1. TutorLah informs Tutor that a student with the same particulars already exists.

      Use case resumes at step 2.

**Use case: UC07 - Delete a lesson record**

**Guarantees:** Only the chosen lesson record is deleted. The student and their other lesson records are not affected.

**MSS**

1.  Tutor <u>views a student's lesson records (UC05)</u>.
2.  Tutor requests to delete a specific lesson record of that student.
3.  TutorLah deletes the lesson record and shows the details of the deleted record.

    Use case ends.

**Extensions**

* 2a. The given lesson record does not exist.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

**Use case: UC08 - Archive a student (proposed)**

**Guarantees:** The archived student no longer appears in the active student list, but their details and lesson records are retained, unlike <u>UC03</u>.

**MSS**

1.  Tutor <u>lists students (UC02)</u>.
2.  Tutor requests to archive a specific student in the list.
3.  TutorLah archives the student and shows the details of the archived student.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

### Non-Functional Requirements

These are product requirements, not a claim that every target has been
verified. The save-failure limitation below remains a gap in the intended
persistence and data-integrity guarantees.

1.  **Platform support:** TutorLah should work on Windows, Linux and macOS.
2.  **Java version:** TutorLah should work with Java `25`.
3.  **Portable distribution:** TutorLah should be usable without an installer.
4.  **Single-file distribution:** TutorLah should be packaged in one JAR file
    that does not exceed `100 MB`.
5.  **Typing-first interaction:** Student and lesson management should be
    possible using typed commands. Adding a student and logging a lesson
    should each take one command.
6.  **Error feedback:** Rejected input should receive field- or command-specific
    feedback. Missing or malformed command parts should also show usage.
7.  **Data persistence:** After a successful command, TutorLah should save the
    complete student and lesson data locally before returning success feedback.
8.  **Data integrity:** A failed command should not add, delete or modify
    student or lesson records.
9.  **Human-editable storage:** Data should be stored locally in human-editable
    JSON, without a database management system.
10. **Offline operation:** Student and lesson management should work offline.
    Accessing the online User Guide requires an internet connection.
11. **Screen support:** The GUI should work well at `1920x1080` and higher
    with 100% and 125% scaling, and remain usable at `1280x720` and higher
    with 150% scaling.
12. **Single-user operation:** TutorLah should support one tutor using one
    local data file, not concurrent shared access by multiple users.

**Known limitation:** `LogicManager` updates the model before saving. If saving
fails, it reports an error but does not roll back the in-memory change.

### Glossary

* **Active student (proposed)**: A student available for normal operations
  under the future archiving feature. Archiving is not implemented yet.

* **Archived student (proposed)**: A student hidden from the active list while
  their details and lesson records are retained.

* **Data file**: The JSON file on the user's computer where TutorLah stores all
  student and lesson data.

* **Duplicate student**: A student with the same phone number and the same
  name, ignoring case, as an existing student.

* **Lesson**: A teaching session between the tutor and a student.

* **Lesson history**: All lesson records associated with a student.

* **Lesson index**: The lesson number shown by `view`, starting at 1 with the
  most recent record. `deletelesson` uses this displayed number.

* **Lesson record**: A record of what was covered in a completed lesson. New
  records have a lesson date; older undated records show `(undated)`.

* **Level**: The student's stage of education, such as `Sec 3` or `JC1`.

* **One-off lesson (proposed)**: A scheduled lesson outside a weekly slot.

* **Rescheduling (proposed)**: Changing the date or time of a scheduled lesson.

* **Scheduled lesson (proposed)**: A planned lesson that has not yet taken
  place, either in a weekly slot or as a one-off lesson.

* **Student**: A learner whose student details and lesson records are managed
  in TutorLah.

* **Student details**: The information stored for a student, including their
  name, phone number, address, level and subject.

* **Student index**: The position of a student in the currently displayed
  student list, starting from 1. Commands such as `delete 3` and `view 3` use
  this index rather than a permanent identifier, so a student's index can
  change when the displayed list changes.

* **Student list**: Students matching the current filter. `list` restores the
  full list; filtering does not delete hidden students.

* **Tutor**: The freelance or private tutor who uses TutorLah to manage
  students and lessons.

* **Weekly lesson slot (proposed)**: A fixed day and time reserved each week
  for a student's regular lesson.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

Use Java `25` and a separate empty test folder. Copy `TutorLah.jar` there and
open a terminal in that folder. Keep real student data out of this test folder.

1. Run `java -jar TutorLah.jar` with no existing data file.
   Expected: the GUI opens with sample students and a command box. Enter
   `list` to display the students.
1. Resize and move the window, then enter `exit`.
   Expected: the window closes. Run the same launch command from the same
   folder. The saved students, window size and window location are retained.

### Deleting a student

Use another isolated test folder. Before launching, create
`data/addressbook.json` containing `{"persons": []}`; a missing file would
load sample students. Launch with `java -jar /absolute/path/to/TutorLah.jar`,
then enter these commands in order:

```text
add n/Amy Tan p/81230011 a/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 a/12 Example Street l/JC1 sub/H2 Math
log 1 c/Algebra
log 2 c/Calculus
```

1. Try `delete 0`, `delete 01` and `delete 3`.
   Expected: `The student index provided is invalid.`, with both students
   and their lesson records unchanged.
1. Try `delete` and `delete abc`.
   Expected: `Invalid command format!` followed by the delete usage, with
   the data unchanged.
1. Enter `find ben`, then `delete 1`.
   Expected: confirmation beginning `Deleted student: Ben Lim;` with Ben's
   saved details. Ben and his Calculus record are deleted. The filter remains
   and shows no students; Amy and her Algebra record are kept.
1. Enter `list`, then `view 1`.
   Expected: `Listed 1 student.`, with Amy numbered 1, followed by her single
   dated Algebra record.
1. Exit and restart from the same folder. Confirm Amy and her record remain,
   then enter `delete 1` and `list`.
   Expected: Amy and her record are deleted, and the list reports
   `No students found. Use add to create a student.`.
1. Exit and restart again, then enter `list` and `delete 1`.
   Expected: the saved list is still empty (no sample students reappear),
   and deletion gives `The student index provided is invalid.`.

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_

### Deleting lesson records

Run in a separate empty directory using `java -jar /absolute/path/to/TutorLah.jar`, with fictional students only. Add a student called Amy, note her displayed index, and use that index wherever `INDEX` appears below. Add another student, Ben, with a distinct phone number and a Calculus record.

1. Log `Algebra`, `Geometry`, then `Algebra` for Amy. `view INDEX` must show `1. DD/MM/YYYY: Algebra`, `2. DD/MM/YYYY: Geometry`, `3. DD/MM/YYYY: Algebra`, each with today's date.
1. Enter `deletelesson INDEX 1`. Expected: `Deleted lesson 1 for Amy: Algebra`. A subsequent view or card click must show `1. DD/MM/YYYY: Geometry`, `2. DD/MM/YYYY: Algebra`, proving the newest duplicate was deleted rather than the oldest.
1. Enter `deletelesson INDEX 2`, then view Amy. Expected: only `1. DD/MM/YYYY: Geometry`. Amy's details and Ben's Calculus record must remain unchanged.
1. Try `deletelesson INDEX 0`, `deletelesson INDEX 01`, `deletelesson INDEX 2`, missing arguments and extra arguments. Expected: the appropriate index error or usage message, with Amy's remaining lesson unchanged.
1. Exit and restart. Expected: Amy still has only Geometry and Ben still has Calculus. Delete Amy's last lesson, then view her: `No lessons logged yet for Amy.`. Amy's student card must remain present. Restart again to confirm the empty history was saved.
1. Try `deletelesson INDEX 1` again while Amy's history is empty. Expected:
   `No lessons logged yet for Amy.`, with both students and saved data unchanged.
1. Enter `find ben`, then `deletelesson 1 1`. Expected: `Deleted lesson 1 for Ben: Calculus`, with only Ben's card still displayed. `view 1` must show `No lessons logged yet for Ben.`. Enter `list` to confirm both students remain and Amy's history is unchanged. Restart to check that both students and their empty histories were saved.

Automated coverage is in `DeleteLessonCommandParserTest`, `DeleteLessonCommandTest`, `PersonTest`, `LessonPersistenceTest`, `AddressBookParserTest` and the command-box deletion case in `PersonListPanelTest`. `FindCommandIntegrationTest` checks typed search followed by lesson deletion, retaining the filter and saving all students. Persistence tests use temporary directories.

### Finding students and restoring the full list

Use a separate test directory and fictional data only. Before launching, create `data/addressbook.json` inside that directory containing `{"persons": []}`. A missing file loads sample students, so this valid empty file is needed for the indexes below. Start with `java -jar /absolute/path/to/TutorLah.jar`, then add these students in order:

```text
add n/Tan Wei Ming p/81230001 a/10 Example Street l/Sec 3 sub/E-Math
add n/Tan Wei Jie p/81230002 a/20 Example Street l/Sec 4 sub/A-Math
add n/Alicia Lim p/81230003 a/30 Example Street l/JC1 sub/H2 Math
```

1. Enter `find WEI`. Expected: only the two Wei students, numbered 1 and 2, with `Found 2 students.`.
1. Enter `find ming tan`. Expected: only `Tan Wei Ming`, with `Found 1 student.`. Both keywords must occur in the name, ignoring case and keyword order.
1. Enter `find ali`. Expected: only `Alicia Lim`, numbered 1, even though the previous search hid her. Enter `view 1`, then press her card. Both show `No lessons logged yet for Alicia Lim.` and highlight her card. Enter `view 2`: the student-index error appears and Alicia's card remains displayed.
1. Enter `find` without keywords. Expected: usage feedback with Alicia still displayed. The failed command remains in the command box.
1. Enter `find unknown`. Expected: no cards and `No matching students found. Use list to show all students.`. The empty panel offers both `list` and the add-command format and example.
1. Enter `list`. Expected: `Listed 3 students.` and all three students in their original order, with Alicia numbered 3. Press Alicia's card once: her empty-history message appears and her card is highlighted.
1. Enter `find WEI`, then exit and restart. Expected: all three students return; searching did not remove hidden students or persist the filter.

### Lesson-history command sequences

Use another separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching as above. Start with `java -jar /absolute/path/to/TutorLah.jar`. The app stores data in the working directory; keep real student data out of this directory. Add these fictional students in order:

```text
add n/Amy Tan p/81230011 a/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 a/12 Example Street l/JC1 sub/H2 Math
```

1. Enter `view 1`. Expected: `No lessons logged yet for Amy Tan.` and Amy's card highlighted, even though her history is empty.
1. Enter `log 1 c/Algebra`, `log 1 c/Geometry`, then `log 1 c/Algebra`. Enter `view 1`. Expected: `Lesson records for Amy Tan (3):`, followed by Algebra, Geometry and Algebra, each with today's date. Both Algebra occurrences remain.
1. Enter `view 2`, then `view 1`. Expected: Ben's empty-history message and highlight first, then Amy's three records and highlight. Press Ben's card once, then Amy's once. Each first press updates both the feedback and highlight to the chosen student.
1. Enter `log 2 c/Calculus`. Type `log 2 c/unfinished` without pressing Enter, then press Ben's card twice. Expected: each press shows Ben's single Calculus record and keeps `log 2 c/unfinished` in the command box. Enter `view 2` instead: the same history appears and the successfully submitted command clears.
1. Enter `view 999`, then `view abc`. Expected: the student-index error, then usage feedback, with the failed command retained and styled as an error. The previous selection is not changed by either failed typed view. Enter `view 1`: Amy's unchanged history returns and the successful command clears.
1. Enter `find ben`, then `view 1` and press Ben's card. Expected: both show Ben's Calculus record while only Ben is displayed. Enter `list`, then `delete 1` to remove Amy. Ben becomes student 1; `view 1` and a single press on his card still show only his Calculus record. `view 2` reports an invalid student index.
1. Prepare a separate fictional student with 40 lesson records, including a 300-character record. View the student and scroll to the bottom. Expected: the header shows count 40, the records are newest first, the long content wraps, and row 40 is reachable. Press the same card again, then view another student: both updates reset the result area to its beginning without losing records.
1. Repeat the scrolling and selection checks in a smaller and a larger window, moving the horizontal divider both ways. The command box and student list must remain usable, and all records must remain reachable. Check the screen resolutions and scaling levels listed under Non-Functional Requirements as well.

### Lesson dates and data saved by v1.3

1. Log `Vectors` for a test student, then `log INDEX c/Surds date/DD/MM/YYYY` with yesterday's date, and view the student.
   Expected: `Vectors` is listed first with today's date, then `Surds` with yesterday's date. Enter `deletelesson INDEX 2`: `Surds` is deleted.
1. Try `date/` with tomorrow's date, `31/02/2026`, `2026-10-20`, `1/1/-2026`, `31/12/1999` and an empty value.
   Expected: tomorrow gives `Lesson dates cannot be in the future. ...`, and the others give `Lesson dates should be real dates from 01/01/2000 onwards, ...`. No lesson is added.

1. Log a lesson for a test student, exit and restart.
   Expected: `view` shows the lesson with the same date, and the data file stores it as `"date" : "YYYY-MM-DD"`.
1. Exit, keep a copy of the data file, and remove the `date` field from one lesson, as in a file saved by v1.3. Start the app and view the student.
   Expected: that lesson shows `(undated)` in place of the date, in its original position.
1. Exit, and change a lesson's date to `18/10/2026` in the data file. Start the app.
   Expected: the data file is treated as invalid and the app starts with no students, as for other invalid fields. Restore your copy afterwards.
