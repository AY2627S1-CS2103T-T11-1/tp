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
* `FindCommand`, `FindCommandParser` and `StudentContainsKeywordsPredicate` are adapted from AB3's find feature (`NameContainsKeywordsPredicate`), changed from any-keyword, whole-word name search to all-keyword substring matching on names, subjects and levels, with student-specific feedback.
* OpenAI Codex assisted with the implementation, tests and documentation for student-name search, deleting individual lesson records, Unicode blank-field validation, student follow-up flags and save-failure rollback, and with the Logic/parser diagrams and Developer Guide maintenance.
* OpenAI Codex, used by zikiai, assisted with the implementation, tests, documentation and screenshots for student summaries, per-student lesson counts and listing students without recorded lessons.
* Anthropic Claude Code, used by aerodart, assisted with the implementation, tests and documentation for dated lesson records, logging lessons with a past date and editing lesson records.
* Anthropic Claude Code, used by srivatsanj27, assisted with the implementation, tests and documentation for finding students by subject and level.
* OpenAI Codex, used by zikiai, assisted with unreadable-file recovery, its review fixes, tests and documentation.
* OpenAI Codex, used by cibichandar, assisted with the implementation, tests,
  documentation and screenshots for searching lesson histories.
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

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram. For example, `UiManager` holds a `Logic`, not a `LogicManager`, and `LogicManager` holds a `Model` and a `Storage`.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/ui/Ui.java).

<puml src="diagrams/UiClassDiagram.puml" alt="Partial UI class diagram showing the main window, callback fields, student cards, the student detail panel and feedback display" width="850" />

This partial class diagram focuses on command input, student cards, the details panel and feedback. `MainWindow` supplies a `CommandExecutor` callback to `CommandBox` and a `Consumer<Index>` callback to `PersonListPanel`. The fields shown on those classes hold the callbacks; neither input part needs a reference to the concrete `MainWindow` class. Solid arrows show held references, with `1` identifying a single referenced object; dashed arrows with `<<create>>` show creation dependencies. `PersonListPanel` creates `PersonCard` objects for visible students, and each card displays one `Person` from the Model component. `StudentDetailPanel` holds at most one `Person`, the student it currently shows. The creation dependency does not describe how many cards are visible. `Person` remains the Java class name for a student.

`UiManager` implements `Ui` and creates `MainWindow`. `MainWindow` and its visible UI parts inherit from `UiPart`; the window also creates `HelpWindow` and `StatusBarFooter`. These startup, shared-base and supporting classes are omitted from the focused diagram. The UI uses JavaFX, with layouts in `src/main/resources/view`, such as [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/resources/view/MainWindow.fxml).

The UI responsibilities are:

* `CommandBox` submits typed commands and clears its text only after successful execution. A failed typed command remains available for correction and receives error styling.
* `PersonListPanel` observes the filtered student list supplied by `Logic`. Compact cards show each student's displayed index, initials, name, level and subject, and lesson status. A flagged card also shows a red flag icon. The indexes follow the displayed list, including after filtering or deletion.
* `StudentDetailPanel` shows one student's level, subject, phone, address, flag reason and lesson records, numbered newest first as `view` numbers them. With no student selected, it shows guidance instead. It renders whichever immutable `Person` `MainWindow` gives it and keeps no other state. The level and subject chips sit side by side when both fit; otherwise the panel stacks them so that a long chip wraps its text within the panel width instead of being cut off.
* A primary mouse press on a populated card sends its displayed index to `MainWindow`. The callback runs before selection can rebuild the cell, so changing students after a typed `view` needs only one press. Repeated presses view the student again; empty cells and other mouse buttons do not execute the shortcut. Releasing a navigation key (arrows, Page Up/Down, Home, End) in the focused list sends the newly selected index through the same callback, so keyboard selection cannot leave the list and details panel showing different students. Programmatic selection and list changes fire no key events, so they never execute a command.
* `ResultDisplay` shows command feedback as wrapping, read-only text. Each update resets the caret and scroll position to the beginning, including when the feedback is unchanged. Error feedback uses a red style until the next command. Long histories remain accessible by scrolling, and the result area can be resized with the horizontal divider. The student list and details panel share the area below, separated by a vertical divider.
* After a successful command, `MainWindow` updates the details panel and the list highlight, as described in [Keeping the details panel in sync](#keeping-the-details-panel-in-sync). Programmatic selection and list changes do not execute a view command.

### Logic component

**API**: [`Logic.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

<!-- These PNG link targets are generated by the puml tags during the build. -->
<a href="diagrams/LogicClassDiagram.png" no-validation><puml src="diagrams/LogicClassDiagram.puml" alt="Partial Logic class diagram showing command execution, held references and command results" width="740" /></a>

[`LogicManager`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/LogicManager.java) implements `Logic` and holds one `AddressBookParser`, `Model` and `Storage`. Solid arrows show held references; dashed arrows show dependencies. Hollow triangles point to a superclass or interface. Lavender identifies Logic classes, pink the Model API and yellow the Storage API. The `Logic (partial)` boundary keeps those external APIs outside the component. `LogCommand` and `ViewCommand` are representative subclasses of `Command`. Other commands, parser internals and dependencies specific to individual commands are omitted; shared dependencies on `Model` and `CommandResult` are shown on `Command`. Click a class diagram in this section to open its full-size image.

How command execution works:

1. `AddressBookParser#parseCommand` returns a concrete `Command` for the input.
1. `LogicManager` calls `Command#execute(Model)`, which reads or updates the model and returns a `CommandResult`.
1. `LogicManager` compares the complete address book with its snapshot from before execution. If student data changed, it saves through `Storage` before returning the result. Otherwise it returns without saving, so `list`, `view`, `find`, `flagged`, `help` and `exit` work even when saving is unavailable. An edit that leaves the stored values unchanged also skips saving.

`CommandResult` contains feedback and the help/exit flags. Commands that view or change exactly one student (`view`, `findlesson`, `log`, `edit`, `editlesson`, `deletelesson`, `flag` and `unflag`) also supply that `Person` as it is after the command, through `getStudent()`, even when the student has no lessons. Other commands leave it absent (`0..1`). The UI shows that student in the details panel and highlights their card, as described in [Keeping the details panel in sync](#keeping-the-details-panel-in-sync).

Invalid input raises `ParseException`; execution or save failures raise `CommandException`. Before executing a parsed command, `LogicManager` copies the address book and captures the current filter through `Model#getFilteredPersonListPredicate`. Student, lesson and flag values are immutable, so the copied list preserves the previous values. If saving throws `IOException`, including `AccessDeniedException`, the manager restores changed data through `Model#setAddressBook` and reapplies the captured predicate before reporting the existing save error. Capturing the predicate, rather than only the visible students, preserves its meaning for later updates. The existing observable list remains connected to the UI, and a retry uses the original displayed indexes.

The sequence diagram below follows the successful execution of `deletelesson 1 2`. It shows how the command validates displayed indexes, converts the newest-first lesson number to the stored position, and replaces the student with an updated immutable value. Calls on the returned `Person` value are summarized in a note so that the diagram exposes only Logic internals and treats Model as a black box. Parser internals and persistence are omitted here; they are covered by the parser class diagram and architecture sequence diagram respectively.

<puml src="diagrams/DeleteLessonSequenceDiagram.puml" alt="Successful lesson-deletion sequence from command parsing through replacing the student in Model" width="950" />

Student deletion follows a related but distinct path. For a successful `delete 1`, `DeleteCommand` resolves the index against the currently displayed filtered list, validates it, and passes the selected `Person` rather than the numeric index to `Model#deletePerson`. The local validation and selection steps are summarized in a note because they are not calls to another participant. Removing that student also removes their owned lesson history. Parser internals and the common persistence path are omitted for the same reasons as above.

<puml src="diagrams/DeleteStudentSequenceDiagram.puml" alt="Successful student-deletion sequence using the displayed student index and the selected Person" width="950" />

The following diagram focuses on parser selection and command creation:

<a href="diagrams/ParserClasses.png" no-validation><puml src="diagrams/ParserClasses.puml" alt="Partial parser class diagram showing selection of lesson parsers and creation of their commands" width="520" /></a>

[`AddressBookParser`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/parser/AddressBookParser.java) separates the command word from its arguments and creates the matching parser. It lowercases only the command word with `Locale.ROOT`, so command selection ignores case without depending on the system locale. Arguments are unchanged, so prefixes remain case-sensitive and must use their documented lowercase forms. Command-specific parsers implement the generic `Parser` interface: for example, `LogCommandParser` implements `Parser<LogCommand>` and returns a `LogCommand`. The type parameter and common `Command` superclass are omitted from this diagram. Dashed arrows labelled `<<create>>` show object creation, not held references. Other command parsers are omitted; `help` and `exit` are created directly without separate parsers.

The helper dependencies are shown separately to keep the overview readable. Pale grey-blue boxes identify helpers within the same parser package.

<a href="diagrams/ParserHelpers.png" no-validation><puml src="diagrams/ParserHelpers.puml" alt="Partial parser helper diagram showing tokenization for logging and shared validation for both lesson parsers" width="500" /></a>

* `LogCommandParser` tokenizes `c/` and optional `date/` arguments using `ArgumentTokenizer` and the `Prefix` constants in `CliSyntax`. `ArgumentMultimap` stores the preamble and prefixed values and checks for duplicate prefixes. `ParserUtil` validates the student index, content and date.
* `DeleteLessonCommandParser` splits two positional indexes on whitespace, without using the tokenizer. It shares `ParserUtil`'s index validation, using the appropriate student- or lesson-index error message.

The helper diagram omits prefix classes, model values and exceptions to focus on tokenization, use of the returned multimap and shared validation.

Command words, positional indexes and prefix boundaries recognise Unicode
whitespace, including non-breaking spaces. `ArgumentTokenizer` trims field
boundaries without replacing internal spacing. In the UI, `CommandBox` uses a
`TextFormatter` to replace inserted tabs and line breaks with spaces before
JavaFX discards them, so pasted words do not join. Pasting does not execute a command.

### Model component

The **API** of this component is specified in [`Model.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/model/Model.java).

<puml src="diagrams/ModelClassDiagram.puml" alt="Partial Model class diagram showing students, value fields, optional follow-up flags and owned lesson records" width="950" />

This partial class diagram focuses on student and lesson data. `ModelManager` implements the `Model` API and owns one `AddressBook`; user-preference classes are omitted. The address book delegates uniqueness and list operations to `UniquePersonList`, while `ModelManager` exposes a filtered view of the same students for searches and displayed indexes.

`Person` is the Java class representing a student. It is an immutable value containing `Name`, `Phone`, `Address`, `Level`, `Subject`, an immutable list of zero or more `Lesson` values, and zero or one `Flag`. A `Flag` marks unresolved follow-up and may contain one validated reason. A `Lesson` contains content and an optional date: records saved by v1.3 have no date, while newer records have a `LocalDate`. Lessons and flags belong to their student, so deleting a student also deletes them.

Two students have the same identity only when their phone numbers match and their names match ignoring case. Other fields, including lessons and flags, contribute to full equality but not duplicate detection. `Person#withLessonAdded`, `Person#withLessonRemoved`, `Person#withFlag` and `Person#withoutFlag` return new `Person` objects rather than mutating the existing value. Any operation that rebuilds a student carries both the existing lesson list and flag across unless it is intentionally changing the flag.

Lesson records are stored oldest first by date. Undated legacy records remain at the oldest end, and records on the same date retain logging order. `view` reverses this stored order for display. The Model component has no dependency on UI, Logic or Storage, so its domain objects and rules remain meaningful independently of those components.


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" alt="Storage class diagram showing StorageManager, the two JSON storages and the student, lesson and flag adapters" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

`JsonSerializableAddressBook` holds a `persons` list of `JsonAdaptedPerson`, which stores each student's `name`, `phone`, `address`, `level` and `subject` as strings, a `lessons` list and, for a flagged student, a `flag`. Each `JsonAdaptedLesson` stores `content` and a `date` in `YYYY-MM-DD` form (`LocalDate#toString`), although users type and see `DD/MM/YYYY`. `JsonAdaptedFlag` stores an optional `reason`. Absent values are left out of the file rather than written as `null`: an undated lesson has no `date`, an unflagged student has no `flag`, and a flag without a reason has no `reason`. Saving writes lessons in the student's stored order.

Loading reverses this conversion. Each `toModelType` method checks that required fields are present and passes every value through the model's validators, such as `Name#isValidName` and `Lesson#isValidContent`, so a value that breaks any of them makes the whole file fail to load. Rules that are applied only when parsing commands are not checked on loading: lesson dates are not checked against 01/01/2000 or today's date, and names are not trimmed or their repeated spaces collapsed. So a file can contain values that no command could create, such as a lesson dated 2030 or a second `Amy  Tan` (with two spaces) alongside `Amy Tan`. `JsonSerializableAddressBook` also rejects duplicate students. Older files stay readable: a student without `lessons` loads with no lessons, a lesson without `date` loads as an undated `Lesson` (records saved by v1.3), and a student without `flag` loads unflagged. Lessons are rebuilt in file order and are not re-sorted on loading.

If `JsonAddressBookStorage#readAddressBook` fails with malformed JSON, a null root, null student entries, invalid stored values or a read error, the storage instance blocks subsequent saves for the rest of that session. Both save overloads check the guard before creating or writing a file. `MainApp` prepares an empty model and recovery instructions; `UiManager` shows these instructions in a warning after the main window is ready. The original data file remains untouched by later commands. Recovery requires closing the app, repairing or restoring the file and restarting with a new storage instance. Read-only commands continue to work because `LogicManager` skips saving when student data is unchanged; mutating commands are rejected by storage and rolled back. `MainApp` and `UiManager` hold a nullable warning string (null means no warning), while the startup-warning accessor returns an `Optional`. A genuinely missing file still loads sample students, which are first written when a command changes the data; a valid saved empty list remains empty.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Viewing lesson history

Both `view INDEX` and a primary press on a student card reach `MainWindow#executeCommand`. Typed input arrives through the `CommandBox` callback; a card press arrives through `MainWindow#handlePersonClick`, which builds `view INDEX` using the card's current displayed index. The click path leaves command-box text unchanged.

<puml src="diagrams/ViewHistorySequenceDiagram.puml" alt="Successful view sequence showing typed input and a student-card press joining the same UI execution path" width="950" />

The sequence shows a successful view of displayed student 1. The `User` actor represents the person entering a command or pressing a student card. Both entry paths share the same execution flow in `MainWindow`. The diagram expands only the UI component; `Logic` is shown through its interface. Parsing, model access, saving, JavaFX internals and error paths are omitted. Activation bars and the private self-call from `handlePersonClick` to `executeCommand` are omitted. Solid arrows show input events or calls; dashed arrows show returns. A return labelled `return` carries no value. The final alternatives complete the same entry path chosen at the top.

`MainWindow` passes the command to `Logic#execute`. A successful `ViewCommand` returns a `CommandResult` containing the feedback and the viewed `Person`. `MainWindow` displays the feedback, then finds the student's position in the displayed list, shows the student in `StudentDetailPanel` and passes the position to `PersonListPanel#selectPerson` to synchronise the highlight and scroll position. The student is present even when their history is empty. This selection does not invoke the click callback, so it cannot execute the command again.

`ViewCommand` reads the student from `Model#getFilteredPersonList`, leaving the current filter and student data unchanged. It traverses the stored lessons backwards: the most recent lesson date appears first, with the most recently logged record first among equal dates. Existing undated records appear at the oldest end when lessons have been added in date order. Each row shows its displayed lesson number, formatted date and content; undated records use `(undated)`. An empty history returns `No lessons logged yet for NAME.`.

If parsing or execution fails, `MainWindow` displays the error in the error style and leaves the details panel and highlight unchanged. For typed input, `CommandBox` keeps the failed command and marks it as an error. Card-click failures are displayed through the same execution path without clearing typed text. Viewing changes no student or lesson data, so `LogicManager` returns its result without attempting to save.

### Keeping the details panel in sync

`StudentDetailPanel` must reflect the latest data after any command, but student values are immutable: `log`, `edit`, `editlesson`, `deletelesson`, `flag` and `unflag` each replace the student in the Model with a new `Person`. A panel that kept the old object would show stale data.

Every command that views or changes exactly one student therefore returns that student, as it is after the command, through `CommandResult#getStudent`. These are `view`, `findlesson`, `log`, `edit`, `editlesson`, `deletelesson`, `flag` and `unflag`. After each successful command, `MainWindow#updateSelectedStudent` applies these rules:

1. If the result names a student, the panel shows that student. The list highlights the student's current position in `Logic#getFilteredPersonList`, or clears the highlight if a filter now hides them. For example, `unflag 1` in the `flagged` results removes the student from the list but keeps them in the panel.
1. Otherwise, the panel keeps its current student if the displayed list still contains that same value, and refreshes the highlight because positions may have shifted.
1. Otherwise, for example after `find` hides the student or `delete` removes them, the panel and the highlight are cleared.

Failed commands skip these rules, so an error never changes the panel. A failed save is a special case: `LogicManager` restores the previous address book, which replaces the list contents and clears the JavaFX selection. `MainWindow#restoreShownStudentHighlight` therefore highlights the panel's student again after any failed command, without scrolling if the highlight was not lost. Commands that affect no single student, such as `list`, `summary` and `add`, need no changes.

**Design considerations:**

* **Alternative 1 (current choice):** commands return the `Person` they acted on.
  * Pros: correct even when the displayed index changes during the command. `edit` resets the filter to all students, so the edited student's index afterwards can differ from the index the user typed.
  * Cons: each targeted command builds its result with the updated student, and its tests compare that student.
* **Alternative 2:** commands return the displayed index, as `view` did before.
  * Pros: smaller change, as only `view` and `findlesson` used it.
  * Cons: points at the wrong student after `edit` resets the filter, and has no value to show once `unflag` removes a student from the `flagged` list.
* **Alternative 3:** a "selected student" property in `Model` that commands set.
  * Pros: other UI parts could observe it.
  * Cons: adds UI state to the Model and a method that every Model stub in the tests must implement, for no current user benefit.

`PersonListPanelTest` covers the panel's contents and every rule above, including error feedback, filter changes, `edit` resetting the filter, `unflag` in the `flagged` results, deletion, keyboard navigation and failed saves after `log`, `edit`, `flag` and `delete`. The command tests check the student each targeted command returns.

### Rejecting blank Unicode fields

`Address#isValidAddress` and `Lesson#isValidContent` reject values composed entirely of Unicode whitespace (`\p{IsWhite_Space}`), format characters (`\p{Cf}`), or a mixture of the two. This includes non-breaking spaces, ZERO WIDTH SPACE (U+200B), WORD JOINER (U+2060) and a standalone BOM (U+FEFF). The check does not remove formatting characters inside real text: joiners in written languages and emoji sequences are preserved, along with internal spaces and the existing length and character rules. Both command parsing and the JSON adapters use these model validators, so add, edit, log and data loading share the same non-blank rule and existing field-specific messages.

### Deleting an individual lesson record

`DeleteLessonCommandParser` requires exactly two indexes. Both go through `ParserUtil#parseCommandIndex`, but student and lesson errors use their respective messages. `DeleteLessonCommand` obtains the student from `Model#getFilteredPersonList`, so the student index always refers to the list currently displayed after any search. It validates the student, the presence of lesson records and the lesson index before changing the Model.

`view` numbers lessons newest first, whereas `Person#getLessons` stores them oldest first. `Person#withLessonAdded` inserts each lesson in date order, keeping same-date records in logging order. The command therefore converts a one-based displayed lesson number to a zero-based stored position using `lessons.size() - lessonIndex.getOneBased()`. An assertion protects this internal conversion after the user-supplied index has been validated. Removing by position deletes only the selected occurrence even when two lessons have identical content and dates.

`Person#withLessonRemoved` returns a new student with the selected record omitted and every student field and remaining lesson preserved. `Model#setPerson` replaces the old value in the backing `UniquePersonList`; the active filtered view continues to show the same matching students. The command then returns a confirmation containing the displayed lesson number, student name and deleted content. `LogicManager` persists the updated address book through the normal save path shown in the architecture sequence diagram.

Deleting the final lesson therefore leaves the student present with an empty history. A later deletion attempt stops before mutation and reports `No lessons logged yet for NAME.`.

### Editing a student

`EditCommand` and `EditCommandParser` are adapted from AB3, with tags replaced by `Level` and `Subject`. `EditCommandParser` tokenizes `n/`, `p/`, `addr/`, `l/` and `sub/`. The preamble must be one student index, checked by `ParserUtil#parseStudentIndex`, and a prefix given twice is rejected with the shared duplicate-prefix message. Each value goes through the same `ParserUtil` method as `add`, so names are trimmed and their repeated spaces collapsed, and spaces in phone numbers are removed. The parsed values are stored in an `EditPersonDescriptor`, and a descriptor with no fields is rejected with `At least one field to edit must be provided.`.

`EditCommand` looks up the student in `Model#getFilteredPersonList`, so the index is the displayed one. `createEditedPerson` takes each field from the descriptor if present and from the existing student otherwise, then builds the new `Person` with the existing lesson list and flag. A `Person` is immutable and the `Lesson` list it returns is unmodifiable, so sharing that list is safe, and lesson numbers and dates are unchanged. `assertLessonsKept` and `assertFlagKept` check this. If the edit changes the student's identity (name ignoring case, or phone) to match another student, the command is rejected with `This student already exists in the list.`.

On success the command calls `Model#setPerson`, restores the full list as AB3 does, and returns the edited student in its `CommandResult`. The details panel and highlight then follow that student, as described in [Keeping the details panel in sync](#keeping-the-details-panel-in-sync). `LogicManager` saves the change. If the save fails, it restores the previous address book and filter.

### Editing a lesson record

`EditLessonCommandParser` tokenizes only `c/` and `date/`, so lesson content can contain text such as `a/level`. The text before the first prefix must be exactly two indexes, which are checked with the same `ParserUtil#parseCommandIndex` rules and messages as `deletelesson`. Content and dates go through `ParserUtil#parseLessonContent` and `ParserUtil#parseLessonDate`, the same rules as `log`. The parsed values are stored in an `EditLessonDescriptor`, modeled on `EditCommand.EditPersonDescriptor`, and a descriptor with no fields is rejected.

`EditLessonCommand` finds the student and converts the displayed lesson number to a stored position exactly as `DeleteLessonCommand` does, reusing `DeleteLessonCommand#toStoredLessonIndex`. It then checks the new date against today's date from an injected `Clock`, as `LogCommand` does, so tests can fix the date. An edit that would leave both content and date unchanged is rejected before the model changes.

The command keeps each student's lessons in date order with one of two operations:

* If the date is unchanged, `Person#withLessonReplaced` puts the edited lesson in the same position. Removing and adding it again would move it after the other lessons on its date, which would change its number even though only its content changed. `withLessonReplaced` asserts that the date is unchanged, so it cannot break date order.
* If the date changes, the command calls `Person#withLessonRemoved` and then `Person#withLessonAdded`, which inserts the lesson after every lesson dated on or before its new date. This is the same rule `log` uses for a backdated lesson.

`Lesson#withContent` keeps an undated legacy lesson undated, so a content-only edit never adds a date. After `Model#setPerson`, the command finds the edited lesson by identity, because another lesson can be equal to it, and reports its new displayed number. Assertions check that the student's identity and number of lessons are unchanged.

### Searching lesson histories

`FindLessonCommandParser` tokenizes `from/` and `to/` after normalizing Unicode whitespace.
The preamble holds the displayed student index and content keywords. Indexes and dates reuse
`ParserUtil`; duplicate prefixes use `ArgumentMultimap`. A valid date followed by extra
keywords gives an ordering hint; invalid dates keep the date-constraint message.
Future query boundaries are valid, but reversed ranges are rejected. Blank/invisible
checks share `StringUtil#isBlankOrInvisible` with student searches and field validators.
`LessonSearchPredicate` copies the keywords and combines literal, case-insensitive matching
through `StringUtil#containsIgnoreCase` with inclusive date boundaries. An undated lesson
matches only when neither boundary is supplied.

`FindLessonCommand` reads the selected student's complete history from the filtered student
list and iterates backwards. It derives each view number from the original stored position
before collecting matches, so gaps remain and repeated content is not merged. The result
identifies the student, so the UI shows their complete history in the details panel and
highlights their card. No lesson filter, model update
or storage field is introduced; `LogicManager` skips saving unchanged data.

### Student and lesson summaries

`AddressBookParser` routes `summary` to `SummaryCommandParser`, which accepts whitespace but rejects arguments with the summary-specific usage. `SummaryCommand` creates a `StudentSummary` from `Model#getAddressBook`, rather than the filtered list, and returns a heading and a single summary paragraph in a normal `CommandResult`. `Messages#formatSummaryParagraph` handles empty collections, singular/plural wording and whether any students lack lesson records. It never updates the active filter. The immutable snapshot counts each lesson occurrence, including undated legacy records; the total uses a `long` so adding the sizes of many histories cannot overflow an `int`. No summary values are added to JSON. Because the command changes no stored data, `LogicManager` returns its result without attempting to save, including during recovery.

`Person#getLessonCount` counts that student's records. `Person#getLatestLessonDate` selects the greatest present date, ignoring undated records and avoiding an assumption about list order. `Messages#formatLessonStatus` supplies empty, singular, plural and undated wording. `PersonCard` places this status below the level and subject using the existing wrapping label style. Each immutable student replacement already triggers the list cell to rebuild its card, so `log`, `editlesson` and `deletelesson` need no additional UI callbacks or cached counters. Flag display is independent of the lesson-status row.

`StudentSummaryTest`, `PersonTest`, `MessagesTest`, the summary command/parser tests and `PersonListPanelTest` cover zero/one/many totals, filtered views, legacy dates, persistence, unavailable saving, dynamic card updates and layout alongside flags. The class diagrams show representative classes and omit this derived snapshot and the summary command/parser.

### Listing students without recorded lessons

`AddressBookParser` routes `unlogged` to `UnloggedCommandParser`, which accepts only Unicode whitespace as arguments. `UnloggedCommand` applies an empty-history predicate through `Model#updateFilteredPersonList`, replacing the previous filter over the full backing collection. It does not mutate students or save data. An undated lesson still makes a history nonempty. The existing `FilteredList` responds to immutable student replacements, so logging a first lesson removes its student automatically; displayed indexes are recalculated by the existing list. The normal `CommandResult` panel rules retain a just-logged student outside the filter and clear the list highlight, as with `unflag` in a flagged view.

`UnloggedCommandTest`, `UnloggedCommandParserTest`, `UnloggedCommandIntegrationTest` and `PersonListPanelTest` cover empty/one/many results, dated and legacy history, filter replacement/restoration, read-only operation, invalid arguments, logging with displayed indexes, persistence and failed-save rollback. Summary totals continue to use the complete collection.

### Finding students by name, subject and level

`AddressBookParser` routes `find` input to `FindCommandParser`, which tokenizes the `n/`, `sub/` and `l/` prefixes. It first turns every Unicode whitespace character, such as a non-breaking space or tab, into a plain space, because `ArgumentTokenizer` only recognises a prefix after an ASCII space; otherwise a pasted `sub/math l/sec` with a non-breaking space would read `l/sec` as more subject keywords. Text before the first of these prefixes and the `n/` value become name keywords; the `sub/` and `l/` values become subject and level keywords. Each part is split on Unicode whitespace into literal keywords. A prefix given twice is rejected with the shared duplicate-prefix message, and a prefix whose value is blank, including Unicode spaces and invisible formatting characters, is rejected with that field's constraint message. A search with no keywords at all shows the usage. Other prefixes are not recognised, so a search for `a/l` stays part of the name keywords and cannot be read as an address.

`StudentContainsKeywordsPredicate` stores immutable copies of the name, subject and level keywords as typed, and needs at least one keyword in total. A student matches when each field contains every keyword given for it, in any order, and every field that has keywords matches. A field with no keywords is not searched, and `isNameSearchOnly()` reports when only name keywords were given, so that `FindCommand` can add a hint about `sub/` and `l/` when such a search finds no one. Because every word after `sub/` or `l/` belongs to that field, a name typed afterwards, as in `find sub/Physics ng`, makes the search find no one. `withTrailingFieldWordsAsNames()` therefore returns the searches that move the last one or more words of a multi-word subject or level search to the names, fewest words first (subject before level for the same number of words) and keeping at least one word in each field. When a search that used `sub/` or `l/` finds no one, `FindCommand` tests these alternatives against `Model#getAddressBook`, without changing the displayed list, and suggests the first one that matches, written with name keywords first. Moving words is only a suggestion, never applied automatically, because multi-word values such as `Sec 3` and `H2 Math` are valid. All three fields share `StringUtil#containsIgnoreCase`, which compares each candidate substring with `String#regionMatches(true, ...)`, avoiding context-sensitive lowercase conversion such as Greek final sigma. Matching is locale-independent and literal, without regular expressions or spelling correction. Predicate equality and hashing use all three keyword lists, so the same keyword in a different field is a different search.

`FindCommand` passes this predicate to `Model#updateFilteredPersonList`. The existing `FilteredList` applies it to the full address book, so a later search replaces the previous search instead of narrowing it further. The GUI observes this list and updates the cards and their displayed indexes automatically. An empty filtered list shows guidance to type `list`; it does not imply that stored students have been deleted.

`view`, `log`, `edit`, `delete`, `deletelesson` and student-card clicks continue to use displayed indexes. `list` restores all students. Adding or editing also restores the full list, while viewing, logging and deleting students or lessons retain the filter. The search changes no student or lesson data and is not persisted across restarts; read-only searches do not rewrite the student data file.

### Flagging students for follow-up

`FlagCommandParser` tokenizes only `r/`, leaving text such as `a/level` inside a reason. It parses the preamble with the shared student-index policy, rejects duplicate `r/` prefixes, trims surrounding Unicode whitespace and constructs an immutable `Flag`. A present flag may omit its reason, but a supplied reason must contain meaningful non-control text and be at most 100 characters.

`FlagCommand` and `UnflagCommand` obtain their target from `Model#getFilteredPersonList`, so they act on the displayed number after either `find` or `flagged`. They replace the immutable `Person` through `Model#setPerson` without changing its identity, details or lessons. Reflagging with a new reason replaces only the `Flag`; reflagging without a reason never clears an existing reason. `FlaggedCommand` applies `Person#isFlagged` to the full backing student list through `Model#updateFilteredPersonList`. Consequently, unflagging a student while that filter is active removes the updated value from the displayed list automatically.

`JsonAdaptedPerson` omits its nullable adapted flag for unflagged students. A present `JsonAdaptedFlag` omits its nullable reason, producing an empty JSON object for a reasonless flag. This distinguishes all three persisted states while keeping older data files compatible. The UI derives the card's flag icon and the details panel's flag banner directly from each immutable `Person`; no separate UI state is stored.

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

* is a freelance or private tutor in Singapore who teaches secondary and junior college (JC) students, from Sec 1 to JC2
* teaches about 10 to 20 students one-to-one, usually at the students' homes, in subjects such as A-Math, Physics and H2 Chemistry
* needs to keep student details and lesson histories in one place
* reviews previous lesson records when preparing for each student's next lesson
* prefers a desktop application with keyboard-driven interaction
* is comfortable entering text commands

Because the students are in Singapore, TutorLah expects 8-digit Singapore phone numbers and uses local level names such as `Sec 3` and `JC1`. No feature depends on the subject taught.

**Value proposition**: Manage student details and lesson records quickly in one keyboard-driven desktop application, so tutors can prepare for upcoming lessons without relying on scattered notes or memory.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

The stories marked `Yes` in the **MVP** column form the historical MVP scope. The separate **Status/version** column states what TutorLah actually implements; `Proposed` stories are not available in the current product.

| Priority | MVP | Status/version | As a … | I want to … | So that I can… |
|----------|-----|----------------|--------|-------------|----------------|
| `* * *` | Yes | Implemented (v1.2) | tutor | add a student's details | keep their information in one place |
| `* * *` | Yes | Implemented (v1.2) | tutor | see a list of my students | identify the student whose information or lesson records I need |
| `* * *` | Yes | Implemented (v1.2) | tutor | delete the student and their associated lesson records | ensure those details are no longer kept in TutorLah |
| `* * *` | Yes | Implemented (v1.4) | tutor | add a dated record of what I covered to that student | avoid relying on memory before the next lesson |
| `* * *` | Yes | Implemented (v1.4) | tutor | view their dated lesson records with the most recent first | see where we stopped and what we covered previously |
| `* *` | No | Implemented (v1.4) | tutor | edit a student's details | keep their information accurate when it changes |
| `* *` | No | Implemented (v1.2) | tutor | store the student's phone number and address | reach them and find their home when needed |
| `* *` | No | Implemented (v1.4) | tutor managing many students | find a student by name | access the correct student's information quickly |
| `* *` | No | Implemented (v1.5) | tutor | find students by subject and level | prepare common material for students with similar needs |
| `* *` | No | Proposed | tutor | record a student's regular weekly lesson slot | remember the schedule and identify clashes |
| `* *` | No | Implemented (v1.4) | tutor | delete an incorrect lesson record | keep the student's lesson history accurate |
| `* *` | No | Implemented (v1.5) | tutor | correct the content or date of a lesson record | fix a mistake without deleting the record and logging it again |
| `* *` | No | Implemented (v1.5) | tutor | search a student's lessons by topic or date range | find when a topic was covered and review work over a period |
| `* *` | No | Implemented (v1.5) | tutor | flag a student who needs follow-up and optionally note why | keep unresolved concerns visible and find those students again quickly |
| `* *` | No | Implemented (v1.5) | tutor | see total students and lesson records and each student's lesson count | review my recorded workload and spot students without lessons |
| `* *` | No | Implemented (v1.5) | tutor | list students with no recorded lessons | identify whose lesson records I need to follow up on |
| `* *` | No | Proposed | tutor | archive a former student | remove them from the active list without losing their history |
| `*` | No | Proposed | tutor | store multiple contact methods for a student | contact the appropriate person when needed |
| `*` | No | Proposed | tutor | manage multiple weekly slots, one-off lessons, and rescheduling | represent lesson arrangements more accurately |
| `*` | No | Proposed | tutor managing many students | add students in batches | avoid entering each student separately |
| `*` | No | Proposed | tutor | restore archived or deleted information | recover records removed accidentally |

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
3.  TutorLah records the lesson, dated today, and shows the student's name, the lesson date and what was covered.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The lesson content is missing, blank, or too long.

    * 2b1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2c. Tutor also provides the date the lesson was taught.

    * 2c1. TutorLah records the lesson on that date, placing it among the student's lessons by date.

      Use case ends.

* 2d. The given date is not a real date, is before 01/01/2000 or is in the future.

    * 2d1. TutorLah shows an error message describing the date rule.

      Use case resumes at step 2.

**Use case: UC05 - View a student's lesson records**

**MSS**

1.  Tutor <u>lists students (UC02)</u>.
2.  Tutor requests to view the lesson records of a specific student in the list, either by command or by clicking the student.
3.  TutorLah shows the student's details and lesson records, most recent first, and highlights the student in the list.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message, and the student shown before stays shown.

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

**Use case: UC08 - Flag a student for follow-up**

**Guarantees:** Flagging changes only the selected student's follow-up flag and optional reason. Their details and lesson records are kept.

**MSS**

1.  Tutor views a student list.
2.  Tutor requests to flag a specific displayed student and may provide a reason.
3.  TutorLah flags the student and shows a confirmation.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The reason is blank, too long or contains a control character.

    * 2b1. TutorLah shows an error message describing the reason restrictions.

      Use case resumes at step 2.

* 2c. The student is already flagged and no new reason was supplied.

    * 2c1. TutorLah informs Tutor that no change was made.

      Use case ends.

**Use case: UC09 - Archive a student (proposed)**

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

**Use case: UC10 - Find students**

**Guarantees:** No student or lesson data is changed. Later commands that take a student index use the numbering of the found students.

**MSS**

1.  Tutor requests to find students, giving words from their name, subject or level.
2.  TutorLah shows only the students matching every given word, numbered from 1, and the number found.

    Use case ends.

**Extensions**

* 1a. Tutor gives no search words, or leaves a subject or level search blank.

    * 1a1. TutorLah shows an error message.

      Use case resumes at step 1.

* 2a. No student matches.

    * 2a1. TutorLah shows an empty list and explains how to show all students again.

      Use case ends.

* \*a. At any time after step 2, Tutor wants to see all students again.

    * \*a1. Tutor <u>lists students (UC02)</u>.

      Use case ends.

**Use case: UC11 - Edit a lesson record**

**Guarantees:** Only the chosen lesson record is changed. The student, their other lesson records, other students and the current student filter are kept.

**MSS**

1.  Tutor <u>views a student's lesson records (UC05)</u> or <u>searches them (UC12)</u>.
2.  Tutor requests to edit a specific lesson record, providing new content, a new date or both.
3.  TutorLah updates the lesson, moves it to its place in date order if its date changed, and shows its old and current lesson numbers together with the updated record.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The student has no lesson records, or the given lesson index is invalid.

    * 2b1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2c. Tutor provides neither new content nor a new date.

    * 2c1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2d. The new content is blank or too long, or the new date is invalid or in the future.

    * 2d1. TutorLah shows an error message describing the relevant rule.

      Use case resumes at step 2.

* 2e. The supplied content and date are the same as the lesson's existing details.

    * 2e1. TutorLah informs Tutor that the lesson already has those details and makes no change.

      Use case ends.

**Use case: UC12 - Search a student's lesson records**

**Guarantees:** No student or lesson data is changed. The displayed student list and its numbering are kept, and matching lessons retain their numbers from the student's complete history.

**MSS**

1.  Tutor <u>lists students (UC02)</u> or <u>finds students (UC10)</u>.
2.  Tutor requests to search a specific displayed student's lesson records, providing content keywords, an inclusive date range or both.
3.  TutorLah shows the matching lesson records newest first with their complete-history lesson numbers, and opens the student's complete history in the details panel.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. Tutor provides no search criterion, provides a malformed date or duplicate date prefix, puts a keyword after a date, or gives a lower date after the upper date.

    * 2b1. TutorLah shows an error message describing the expected search format or date rule.

      Use case resumes at step 2.

* 3a. The student has no lesson records.

    * 3a1. TutorLah informs Tutor that no lessons have been logged for the student.

      Use case ends.

* 3b. The student has lesson records, but none matches every given content and date criterion.

    * 3b1. TutorLah informs Tutor that no matching lesson records were found.

      Use case ends.

**Use case: UC13 - View the student and lesson summary**

**Guarantees:** No student or lesson data is changed. The current student filter and displayed numbering are kept; totals cover the complete saved collection.

**MSS**

1.  Tutor requests to view the summary.
2.  TutorLah shows the total number of students, the total number of lesson records and how many students have no lesson records.

    Use case ends.

**Extensions**

* 1a. Tutor supplies an extra parameter.

    * 1a1. TutorLah shows an error message with the expected format.

      Use case resumes at step 1.

* 2a. There are no students.

    * 2a1. TutorLah explains that there are no students yet and suggests adding one.

      Use case ends.

**Use case: UC14 - Remove a student's follow-up flag**

**Guarantees:** Only the selected student's flag and its reason are removed. Their details and lesson records, other students and the current student filter are kept.

**MSS**

1.  Tutor <u>lists students (UC02)</u> or <u>finds students (UC10)</u>.
2.  Tutor requests to remove the follow-up flag from a specific displayed student.
3.  TutorLah removes the flag and reason, opens the student in the details panel and shows a confirmation.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The student is not flagged.

    * 2b1. TutorLah informs Tutor that no change was made.

      Use case ends.

* 3a. Tutor is viewing only flagged students.

    * 3a1. The unflagged student leaves the displayed list but remains open in the details panel.

      Use case ends.

**Use case: UC15 - List students flagged for follow-up**

**Guarantees:** No student or lesson data is changed. The results come from the complete saved collection and retain the students' original order.

**MSS**

1.  Tutor requests to view all students flagged for follow-up.
2.  TutorLah shows the flagged students, numbers the results from 1 and reports how many were found.

    Use case ends.

**Extensions**

* 1a. Tutor supplies an extra parameter.

    * 1a1. TutorLah shows an error message.

      Use case resumes at step 1.

* 2a. No students are flagged.

    * 2a1. TutorLah shows an empty list and explains how to show all students again.

      Use case ends.

* \*a. At any time after step 2, Tutor wants to see all students again.

    * \*a1. Tutor <u>lists students (UC02)</u>.

      Use case ends.

### Non-Functional Requirements

These are product requirements, not a claim that every target has been verified.

1.  **Platform support:** TutorLah should work on Windows, Linux and macOS.
2.  **Java version:** TutorLah should work with Java `25`.
3.  **Portable distribution:** TutorLah should be usable without an installer.
4.  **Single-file distribution:** TutorLah should be packaged in one JAR file that does not exceed `100 MB`.
5.  **Typing-first interaction:** Student and lesson management should be possible using typed commands. Adding a student and logging a lesson should each take one command.
6.  **Error feedback:** Rejected input should receive field- or command-specific feedback. Missing or malformed command parts should also show usage.
7.  **Data persistence:** After a successful data-changing command, TutorLah should save the complete student and lesson data locally before returning success feedback.
8.  **Data integrity:** A failed command should not add, delete or modify student or lesson records.

    If saving fails, `LogicManager` restores the preceding student data and active filter before reporting the error.

9.  **Human-editable storage:** Data should be stored locally in human-editable JSON, without a database management system.
10. **Offline operation:** Student and lesson management should work offline. Accessing the online User Guide requires an internet connection.
11. **Screen support:** The GUI should work well at `1920x1080` and higher with 100% and 125% scaling, and remain usable at `1280x720` and higher with 150% scaling.
12. **Single-user operation:** TutorLah should support one tutor using one local data file, not concurrent shared access by multiple users.


### Glossary

* **Active student (proposed)**: A student available for normal operations under the future archiving feature. Archiving is not implemented yet.

* **Archived student (proposed)**: A student hidden from the active list while their details and lesson records are retained.

* **Data file**: The JSON file on the user's computer where TutorLah stores all student and lesson data.

* **Duplicate student**: A student with the same phone number and the same name, ignoring case, as an existing student.

* **Flag**: A persistent marker that a student needs follow-up, optionally including one reason.

* **Flagged student**: A student who currently has a follow-up flag.

* **JC (junior college)**: The two-year pre-university stage in Singapore, written `JC1` and `JC2`.

* **Lesson**: A teaching session between the tutor and a student.

* **Lesson history**: All lesson records associated with a student.

* **Lesson index**: The lesson number shown by `view`, starting at 1 with the most recent record. `deletelesson` uses this displayed number.

* **Lesson record**: A record of what was covered in a completed lesson. New records have a lesson date; older undated records show `(undated)`.

* **Level**: The student's stage of education, such as `Sec 3` or `JC1`.

* **One-off lesson (proposed)**: A scheduled lesson outside a weekly slot.

* **Rescheduling (proposed)**: Changing the date or time of a scheduled lesson.

* **Scheduled lesson (proposed)**: A planned lesson that has not yet taken place, either in a weekly slot or as a one-off lesson.

* **Sec (secondary)**: Secondary school in Singapore, from `Sec 1` to `Sec 4` or `Sec 5`.

* **Student**: A learner whose student details and lesson records are managed in TutorLah.

* **Student details**: The information stored for a student, including their name, phone number, address, level and subject.

* **Student index**: The position of a student in the currently displayed student list, starting from 1. Commands such as `delete 3` and `view 3` use this index rather than a permanent identifier, so a student's index can change when the displayed list changes.

* **Student list**: Students matching the current filter. `list` restores the full list; filtering does not delete hidden students.

* **Tutor**: The freelance or private tutor who uses TutorLah to manage students and lessons.

* **Weekly lesson slot (proposed)**: A fixed day and time reserved each week for a student's regular lesson.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

Use Java `25` and a separate empty test folder. Copy `TutorLah.jar` there and open a terminal in that folder. Keep real student data out of this test folder.

1. Run `java -jar TutorLah.jar` with no existing data file. Expected: the GUI opens with sample students and a command box. Enter `list` to display the students.
1. Resize and move the window, then enter `exit`. Expected: the window closes. Run the same launch command from the same folder. The saved students, window size and window location are retained.

### Student and lesson summaries

Use a separate test folder with an empty student file and fictional data only.

1. Enter `summary`. Expected: the summary says `No students yet. Add a student to start recording lessons.`.
1. Add Amy Tan and Ben Lim, then log two dated lessons for Amy. Expected: Amy's card shows `2 lessons, last DATE` using the later date, and Ben's shows `No lessons yet`. `summary` reports 2 students, 2 lesson records and 1 student with no lessons: `You have 2 students and 2 lesson records in total. 1 student has no lessons recorded.`.
1. Enter `find ben`, then `summary`. Expected: the same totals, with only Ben still displayed as student 1. Repeat after flagging Ben and entering `flagged`, and after a search that finds no students.
1. Enter `summary 1`. Expected: the summary usage, with the preceding filter and student data unchanged.
1. Enter `list`, edit Amy's latest lesson to an earlier date, then remove her lessons one at a time using `deletelesson`. Expected: the card immediately follows the most recent remaining date, the count decreases, and removing the last record shows `No lessons yet`. `summary` then reports 2 students, 0 lesson records and 2 students with no lessons.
1. Restart with a valid legacy file containing undated lessons. Expected: these records count in the totals, and a student with only undated records shows `N lessons (undated)` (or `1 lesson (undated)`). Logging a dated lesson keeps those records and supplies the new last date.
1. Check the status alongside a flag icon, selected and hovered cards, long student names and the supported display sizes/scales. Expected: the status and flag icon remain readable without overlapping other fields.

### Searching lesson histories

Before launching, prepare an isolated test folder with `data/addressbook.json`
containing `{"persons": []}`. Add Amy Tan and Ben Lim, then log `Algebra` for Amy
on 02/01/2020, `Geometry` on 03/01/2020, `Algebra` on 04/01/2020 and `Vectors` on
05/01/2020. Amy is student 1 and Ben is student 2.

1. Enter `findlesson 1 algebra`. Expected: two matches numbered 2 and 4, newest first;
   Amy is highlighted. `view 1` and a card click still show all four lessons.
1. Enter `findlesson 1 from/02/01/2020 to/02/01/2020`. Expected: only lesson 4.
   Repeat with just `from/04/01/2020`, just `to/03/01/2020`, and an upper boundary
   of `31/12/9999`; endpoints are inclusive and future query limits are accepted.
1. Enter `find amy`, then `findlesson 1 algebra`. Expected: the same matches and only
   Amy remains in the student list. Repeat with a flagged Amy and `flagged`.
1. Enter `editlesson 1 4 c/Algebra corrected`, then `deletelesson 1 2`. Expected:
   the selected original records change; a new search shows one match, now numbered 3.
   Restart and check the saved history.
1. Enter `findlesson 1 missing`, then `list` and `findlesson 2 algebra`. Expected:
   distinct no-match and no-lessons messages. Try missing criteria, reversed ranges,
   invalid dates and repeated date prefixes; each reports an error without changing
   data or the student filter.
1. Enter `findlesson 1 from/02/01/2020 algebra`, then repeat with `to/04/01/2020`.
   Expected: a message to put keywords before date prefixes, with a corrected example.
   `findlesson 1 from/31/02/2020 algebra` still reports an invalid date.
1. Repeat a content-only search with valid undated legacy records, then supply a date
   boundary. Expected: undated matches appear only in the content-only search.
   Check a long search result by resizing the result area and scrolling; another
   command starts its feedback at the top.

### Adding students

Use a separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching. Start TutorLah with `java -jar /absolute/path/to/TutorLah.jar` so the test does not use real student data.

1. Enter `add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math`.<br>
   Expected: `New student added: Amy Tan; Phone: 81230011; Address: 11 Example Street; Level: Sec 3; Subject: E-Math`, and Amy appears as student 1.
1. Enter `add n/amy tan p/81230011 addr/99 Other Street l/Sec 4 sub/A-Math`.<br>
   Expected: `This student already exists in the list.` because the phone is identical and the name differs only by case. Amy's existing details remain unchanged.
1. Enter `add n/Amy Tan p/81230012 addr/12 Example Street l/JC1 sub/H2 Math`.<br>
   Expected: the second Amy is accepted because the phone differs, and both students remain visible.
1. Enter `add n/Muthu a/l Raju p/81230013 addr/13 Example Street l/Sec 2 sub/E-Math`, then `edit 3 n/Siti a/p Ahmad`, then `delete 3`.<br>
   Expected: the student is added with the full name `Muthu a/l Raju`; the edit changes only the name to `Siti a/p Ahmad`, and the address stays `13 Example Street`. `delete 3` then removes this student, so the two Amy Tan students remain for the following steps.
1. Enter `add n/Ben Lim p/81230014 a/14 Example Street l/Sec 2 sub/E-Math`, using the old `a/` prefix.<br>
   Expected: the add-command usage followed by `The address prefix is addr/, for example addr/1 Example Street.` No student is added.
1. Try omitting each required prefix in turn. Also try `p/123`, `l/`, `sub/`, a 101-character name and an address containing only spaces.<br>
   Expected: each command is rejected with either the add-command usage or the relevant field constraint. Neither existing student changes.

### Listing students

Continue with the two fictional students created above.

1. Enter `find amy`, then `list`.<br>
   Expected: the search shows both matching students, and `list` restores the full list in its original order with `Listed 2 students.`.
1. Enter `list extra`.<br>
   Expected: `The list command does not accept parameters.` and the displayed list remains unchanged.
1. In a separate test directory, prepare `data/addressbook.json` containing `{"persons": []}`, start TutorLah and enter `list`.<br>
   Expected: `No students found. Use add to create a student.` and the empty panel displays the current add-command guidance.

### Editing students

Use another isolated test folder. Before launching, create `data/addressbook.json` containing `{"persons": []}`. Launch with `java -jar /absolute/path/to/TutorLah.jar`, then enter these commands in order:

```text
add n/Amy Tan p/81230021 addr/21 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230022 addr/22 Example Street l/JC1 sub/H2 Math
log 1 c/Algebra date/20/10/2026
flag 1 r/Weak in algebra
```

1. Enter `find amy`, then `edit 1 p/91234567 l/Sec 4`.<br>
   Expected: `Edited student: Amy Tan; Phone: 91234567; Address: 21 Example Street; Level: Sec 4; Subject: E-Math`. The full list returns with Amy and Ben, and the details panel shows Amy with her flag banner and her `20/10/2026` Algebra record.
1. Enter `view 1`, then `flagged`.<br>
   Expected: `Lesson records for Amy Tan (1):` followed by `1. 20/10/2026: Algebra`, then `Listed 1 flagged student.`. The edit kept Amy's lesson and flag.
1. Enter `list`, then `edit 2 n/amy tan p/91234567`.<br>
   Expected: `This student already exists in the list.`, because the name differs from Amy's only by case and the phone is the same. Ben is unchanged.
1. Try `edit 1`, `edit 3 n/Cara Ong`, `edit 1 a/1 New Road` and `edit 1 p/123`.<br>
   Expected, in order: `At least one field to edit must be provided.`, `The student index provided is invalid.`, `Invalid command format!` followed by the edit usage, and `Phone numbers should be exactly 8 digits.`. No student changes.
1. Enter `edit 1 n/Amelia Tan addr/5 New Road`, then exit and restart from the same folder.<br>
   Expected: Amelia Tan is shown with `5 New Road`, phone `91234567`, level `Sec 4`, the flag and the Algebra record. The edit was saved.

### Deleting a student

Use another isolated test folder. Before launching, create `data/addressbook.json` containing `{"persons": []}`; a missing file would load sample students. Launch with `java -jar /absolute/path/to/TutorLah.jar`, then enter these commands in order:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
log 1 c/Algebra
log 2 c/Calculus
```

1. Try `delete 0`, `delete 01` and `delete 3`. Expected: `The student index provided is invalid.`, with both students and their lesson records unchanged.
1. Try `delete` and `delete abc`. Expected: `Invalid command format!` followed by the delete usage, with the data unchanged.
1. Enter `find ben`, then `delete 1`. Expected: confirmation beginning `Deleted student: Ben Lim;` with Ben's saved details. Ben and his Calculus record are deleted. The filter remains and shows no students; Amy and her Algebra record are kept.
1. Enter `list`, then `view 1`. Expected: `Listed 1 student.`, with Amy numbered 1, followed by her single dated Algebra record.
1. Exit and restart from the same folder. Confirm Amy and her record remain, then enter `delete 1` and `list`. Expected: Amy and her record are deleted, and the list reports `No students found. Use add to create a student.`.
1. Exit and restart again, then enter `list` and `delete 1`. Expected: the saved list is still empty (no sample students reappear), and deletion gives `The student index provided is invalid.`.

### Flagging students for follow-up

Use a separate test directory containing two fictional students. Enter the following commands in order:

1. Enter `flag 1`. Expected: student 1's card shows a red flag icon, the details panel opens student 1 with the banner `Flagged (no reason given)`, and the confirmation is `Flagged student: NAME.`.
1. Enter `flag 2 r/Missed homework twice`. Expected: student 2's card shows the flag icon, and the details panel opens student 2 with the banner `Missed homework twice`; the confirmation includes the same reason.
1. Enter `flag 2 r/Call parent about a/level worksheet`. Expected: only student 2's reason changes, including the literal `a/level` text. Enter the same command again; TutorLah reports that the student is already flagged with that reason and makes no change.
1. Enter `flagged`. Expected: both flagged students appear in their original order, renumbered from 1. Enter `unflag 1`; the first student disappears from the filtered list. Enter `list`; both saved students reappear, with only the second still flagged.
1. Try an empty `r/`, two `r/` prefixes, a 101-character reason, `flag 0`, `unflag 01`, and `flagged extra`. Expected: each command gives the corresponding reason, duplicate-prefix, index or unsupported-parameter error without changing any flag.
1. Edit the remaining flagged student, log and delete one lesson, then exit and restart. Expected: the student's flag icon and reason remain, while the active `flagged` filter is not restored. Enter `unflag INDEX`, exit and restart again; the student remains unflagged and their JSON object has no `flag` property.

Automated coverage is in `FlagTest`, the three flag command and parser test classes, `FlagPersistenceTest`, `JsonAdaptedFlagTest`, `JsonAdaptedPersonTest`, `PersonTest`, `AddressBookParserTest` and the flag UI cases in `PersonListPanelTest`.

### Listing students without recorded lessons

Use a separate test folder containing a saved empty address book, then add Amy Tan and Ben Lim with different phone numbers. Log one lesson for Amy.

1. Enter `find amy`, then `unlogged`. Expected: only Ben is displayed as student 1, with `Listed 1 student with no recorded lessons.`.
1. Enter `summary`. Expected: totals for both students and Amy's lesson, with only Ben still displayed. Try `unlogged 1`; the parameter error keeps this list.
1. Enter `view 1`, then `log 1 c/Algebra`. Expected: Ben's panel shows his new lesson, no card is highlighted, and the filtered student list becomes empty. Amy's lesson is unchanged.
1. Enter `list`. Expected: both students return. Restart: both remain visible, since the filter is not persisted. `unlogged` now reports no matches.

### Saving data

#### Recovering from a failed save

Use a separate test folder and fictional students only. Add Amy Tan and Ben Lim with different phone numbers, then enter `find amy`.

1. Keep the app running. Move `data/addressbook.json` to `data/addressbook.saved.json`, then create an empty directory named `data/addressbook.json` at the old file location.
1. Enter `delete 1`. Expected: a save error, with Amy still visible as student 1. The preserved JSON file still contains both students.
1. Remove the empty directory and move `data/addressbook.saved.json` back to `data/addressbook.json`. Enter `list`. Expected: both students remain; the failed deletion was not saved by this later command.
1. Enter `find amy`, then `delete 1`. Expected: Amy is deleted exactly once. `list` shows Ben, and a restart retains him.

`SaveFailureRollbackTest` also covers IO and permission failures for add, edit, log, delete, deletelesson, editlesson, flag and unflag, preservation of find/flagged filters and lesson data, subsequent commands and successful retries.

#### Missing or corrupted files

Use a separate test folder and fictional data only. Keep a valid copy of the student file before changing it.

1. Launch with no `data/addressbook.json`. Expected: sample students and no recovery warning. `list` works without creating the student file; logging a lesson creates it and saves the complete collection.
1. Save a fictional Amy Tan with an Algebra record, exit, and keep an exact copy of the JSON file. Remove a closing brace from the working copy and restart. Expected: a warning with the file location and repair/restart instructions, an empty list and saving disabled.
1. Dismiss the warning and try `list`, `find amy`, `flagged` and `help`. Expected: normal feedback for the empty collection, with the help window opening normally and the original file remaining byte-for-byte unchanged.
1. Try adding a fictional student. Expected: the saving-disabled error, with the model still empty and the original file unchanged. Enter `exit`. Expected: the app closes normally without overwriting that file.
1. Restore the valid file and restart. Expected: Amy and Algebra return, no recovery warning appears, and new changes save normally.
1. Repeat with valid JSON containing Amy's phone as `12`. Expected: the same protection and warning, because stored field validation also failed.
1. Prepare a valid `{"persons": []}` file and restart. Expected: an empty list with no warning and normal saving. Valid legacy lesson records without a date still load as undated.

`MainAppTest` and `JsonAddressBookStorageTest` exercise startup and subsequent commands with malformed JSON, a null root, null student entries and invalid values, exact byte preservation, missing/legacy data, repair and restart. `PersonListPanelTest` checks that the recovery warning is requested after the window is shown and is absent during a normal start.

### Deleting lesson records

Use a separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching. Start with `java -jar /absolute/path/to/TutorLah.jar`, then enter:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
log 1 c/Algebra
log 1 c/Geometry
log 1 c/Algebra
log 2 c/Calculus
```

1. Enter `view 1`.<br>
   Expected: Amy's three records appear as `1. DD/MM/YYYY: Algebra`, `2. DD/MM/YYYY: Geometry`, `3. DD/MM/YYYY: Algebra`, each with today's date.
1. Enter `deletelesson 1 1`, then `view 1`.<br>
   Expected: `Deleted lesson 1 for Amy Tan: Algebra`, followed by Geometry as lesson 1 and the older Algebra as lesson 2. This proves that only the newest duplicate was removed.
1. Enter `deletelesson 1 2`, then `view 1`.<br>
   Expected: `Deleted lesson 2 for Amy Tan: Algebra`, then only Geometry remains. Amy's details and Ben's Calculus record are unchanged.
1. Try `deletelesson 1 0`, `deletelesson 1 01`, `deletelesson 1 2`, `deletelesson 0 1`, `deletelesson`, `deletelesson 1` and `deletelesson 1 1 extra`.<br>
   Expected: the relevant lesson-index, student-index or usage error appears. Amy's remaining Geometry record is unchanged after every rejected command.
1. Exit and restart, enter `view 1`, then `deletelesson 1 1` and `view 1`.<br>
   Expected: Geometry survived the restart, its deletion succeeds, and Amy remains present with `No lessons logged yet for Amy Tan.`. Restart again to confirm the empty history was saved.
1. Enter `deletelesson 1 1` while Amy's history is empty.<br>
   Expected: `No lessons logged yet for Amy Tan.`, with both students and saved data unchanged.
1. Enter `find ben`, then `deletelesson 1 1` and `view 1`.<br>
   Expected: `Deleted lesson 1 for Ben Lim: Calculus`, while only Ben's card remains displayed. The view reports `No lessons logged yet for Ben Lim.`. Enter `list` to confirm both students remain; the search filter was retained during deletion and no unrelated student changed.

Automated coverage is in `DeleteLessonCommandParserTest`, `DeleteLessonCommandTest`, `PersonTest`, `LessonPersistenceTest`, `AddressBookParserTest` and the command-box deletion case in `PersonListPanelTest`. `FindCommandIntegrationTest` checks typed search followed by lesson deletion, retaining the filter and saving all students. Persistence tests use temporary directories.

### Editing lesson records

These steps need today's date to be 23/10/2026 or later. Use a separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching. Start with `java -jar /absolute/path/to/TutorLah.jar`, then enter:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
log 1 c/Algebra date/19/10/2026
log 1 c/Vectors date/21/10/2026
log 1 c/Trigonometry date/23/10/2026
```

1. Enter `view 1`.<br>
   Expected: `1. 23/10/2026: Trigonometry`, `2. 21/10/2026: Vectors` and `3. 19/10/2026: Algebra`.
1. Enter `editlesson 1 2 c/Vectors and scalars`, then `view 1`.<br>
   Expected: `Edited lesson 2 for Amy Tan (now lesson 2). 21/10/2026: Vectors and scalars`. The lesson keeps its number and date.
1. Enter `editlesson 1 3 date/22/10/2026`, then `view 1`.<br>
   Expected: `Edited lesson 3 for Amy Tan (now lesson 2). 22/10/2026: Algebra`. The view shows Trigonometry, Algebra, then Vectors and scalars.
1. Enter `editlesson 1 3 date/22/10/2026`.<br>
   Expected: `Edited lesson 3 for Amy Tan (now lesson 2). 22/10/2026: Vectors and scalars`. On a date that already has a lesson, the edited lesson goes after it in storage, so `view` shows it first.
1. Enter `editlesson 1 2 c/Vectors and scalars`.<br>
   Expected: `The lesson already has those details.`, and nothing changes.
1. Try `editlesson 1 1`, `editlesson 1 1 c/`, `editlesson 1 1 date/31/02/2026`, `editlesson 1 1 c/A c/B`, `editlesson 1 4 c/Algebra`, `editlesson 2 1 c/Algebra` and `editlesson 1 1 date/` followed by tomorrow's date.<br>
   Expected: in order, the missing-field, content, date, duplicate-prefix, lesson-index, student-index and future-date errors. Amy's records are unchanged after every rejected command.
1. Exit and restart from the same folder, then enter `view 1`.<br>
   Expected: the edited content, dates and order were saved.

Automated coverage is in `EditLessonCommandParserTest`, `EditLessonCommandTest`, `PersonTest`, `LessonTest` and `AddressBookParserTest`.

### Finding students and restoring the full list

Use a separate test directory and fictional data only. Before launching, create `data/addressbook.json` inside that directory containing `{"persons": []}`. A missing file loads sample students, so this valid empty file is needed for the indexes below. Start with `java -jar /absolute/path/to/TutorLah.jar`, then add these students in order:

```text
add n/Tan Wei Ming p/81230001 addr/10 Example Street l/Sec 3 sub/E-Math
add n/Tan Wei Jie p/81230002 addr/20 Example Street l/Sec 4 sub/A-Math
add n/Alicia Lim p/81230003 addr/30 Example Street l/JC1 sub/H2 Math
```

1. Enter `find WEI`. Expected: only the two Wei students, numbered 1 and 2, with `Found 2 students.`.
1. Enter `find ming tan`. Expected: only `Tan Wei Ming`, with `Found 1 student.`. Both keywords must occur in the name, ignoring case and keyword order.
1. Enter `find ali`. Expected: only `Alicia Lim`, numbered 1, even though the previous search hid her. Enter `view 1`, then press her card. Both show `No lessons logged yet for Alicia Lim.` and highlight her card. Enter `view 2`: the student-index error appears and Alicia's card remains displayed.
1. Enter `find` without keywords. Expected: usage feedback with Alicia still displayed. The failed command remains in the command box.
1. Enter `find unknown`. Expected: no cards and `No matching students found. Use list to show all students. To search by subject or level, use sub/ or l/, e.g. find sub/Physics.`. The hint appears because the search used names only; `find sub/chemistry` gives the message without it. The empty panel offers both `list` and the add-command format and example.
1. Enter `find sub/math`. Expected: all three students, with `Found 3 students.`, because `E-Math`, `A-Math` and `H2 Math` all contain `math`.
1. Enter `find sub/math l/sec`. Expected: only the two Wei students. Then enter `find tan l/4`. Expected: only `Tan Wei Jie`, combining a bare name keyword with a level.
1. Enter `find sub/math sub/h2`. Expected: `Multiple values specified for the following single-valued field(s): sub/` and the previous results stay displayed. Enter `find l/`. Expected: the level constraint message, also without changing the displayed list.
1. Enter `list`. Expected: `Listed 3 students.` and all three students in their original order, with Alicia numbered 3. Press Alicia's card once: her empty-history message appears and her card is highlighted.
1. Enter `find WEI`, then exit and restart. Expected: all three students return; searching did not remove hidden students or persist the filter.

### Lesson-history command sequences

Use another separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching as above. Start with `java -jar /absolute/path/to/TutorLah.jar`. The app stores data in the working directory; keep real student data out of this directory. Add these fictional students in order:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
```

1. Enter `view 1`. Expected: `No lessons logged yet for Amy Tan.` and Amy's card highlighted, even though her history is empty.
1. Enter `log 1 c/Algebra`, `log 1 c/Geometry`, then `log 1 c/Algebra`. Enter `view 1`. Expected: `Lesson records for Amy Tan (3):`, followed by Algebra, Geometry and Algebra, each with today's date. Both Algebra occurrences remain.
1. Enter `view 2`, then `view 1`. Expected: Ben's empty-history message and highlight first, then Amy's three records and highlight. Press Ben's card once, then Amy's once. Each first press updates both the feedback and highlight to the chosen student.
1. Enter `log 2 c/Calculus`. Type `log 2 c/unfinished` without pressing Enter, then press Ben's card twice. Expected: each press shows Ben's single Calculus record and keeps `log 2 c/unfinished` in the command box. Enter `view 2` instead: the same history appears and the successfully submitted command clears.
1. Enter `view 999`, then `view abc`. Expected: the student-index error, then usage feedback, with the failed command retained and styled as an error. The previous selection is not changed by either failed typed view. Enter `view 1`: Amy's unchanged history returns and the successful command clears.
1. Enter `find ben`, then `view 1` and press Ben's card. Expected: both show Ben's Calculus record while only Ben is displayed. Enter `list`, then `delete 1` to remove Amy. Ben becomes student 1; `view 1` and a single press on his card still show only his Calculus record. `view 2` reports an invalid student index.
1. Prepare a separate fictional student with 40 lesson records, including a 300-character record. View the student and scroll to the bottom. Expected: the header shows count 40, the records are newest first, the long content wraps, and row 40 is reachable. Press the same card again, then view another student: both updates reset the result area to its beginning without losing records.
1. Repeat the scrolling and selection checks in a smaller and a larger window, moving the horizontal divider both ways. The command box and student list must remain usable, and all records must remain reachable. Check the screen resolutions and scaling levels listed under Non-Functional Requirements as well.

### Student details panel

Start with an empty data folder and add two fictional students:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
```

1. Launch TutorLah. Expected: the details panel shows `No student selected.` and no card is highlighted.
1. Enter `view 2`. Expected: the panel shows Ben's initials, name, level and subject chips, phone and address, `Lessons (0)` and the hint `log 2 c/Topic covered`. Ben's card is highlighted.
1. Enter `log 1 c/Algebra date/1/10/2026`, then `log 1 c/Vectors date/8/10/2026`. Expected: the panel switches to Amy after the first command and lists `1. 08/10/2026 Vectors` and `2. 01/10/2026 Algebra` after the second, without another `view`.
1. Enter `editlesson 1 2 date/9/10/2026`, then `deletelesson 1 1`. Expected: Algebra first moves to lesson 1, and after the deletion only Vectors remains as lesson 1.
1. Enter `flag 2 r/Call parent`. Expected: the panel switches to Ben, with a red flag beside his name and the banner `Call parent`. His card shows a red flag icon.
1. Enter `flagged`, then `unflag 1`. Expected: the displayed list becomes empty, but the panel still shows Ben, now without the flag or banner.
1. Enter `list`, then `view 1`, then `find ben`. Expected: Amy is hidden by the search, so the panel returns to `No student selected.`.
1. Enter `list`, `view 1`, then `edit 1 n/Amelia Tan`. Expected: the panel shows `Amelia Tan` and keeps her lesson. Enter `view 9`: the result box turns red with the student-index error, and the panel still shows Amelia.
1. Click Amelia's card, then press the Down arrow. Expected: Ben's card is highlighted and the panel shows Ben. Press Up: both return to Amelia.
1. Enter `delete 1`. Expected: the panel returns to `No student selected.`.
1. Resize the window to its minimum width and drag the vertical divider both ways. Expected: long names and lesson content wrap, and nothing overlaps or is cut off.

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
