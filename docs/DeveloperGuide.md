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
* Libraries used: [JavaFX](https://openjfx.io/) for the GUI, [Jackson](https://github.com/FasterXML/jackson) for reading and writing JSON data files, and [JUnit 5](https://github.com/junit-team/junit5) for automated tests.
* `FindCommand`, `FindCommandParser` and `StudentContainsKeywordsPredicate` are adapted from AB3's find feature (`NameContainsKeywordsPredicate`), changed from any-keyword, whole-word name search to all-keyword substring matching on names, subjects and levels, with student-specific feedback.
* OpenAI Codex assisted with the implementation, tests and documentation for student-name search, deleting individual lesson records, Unicode blank-field validation, student follow-up flags and save-failure rollback, and with the Logic/parser diagrams and Developer Guide maintenance.
* OpenAI Codex, used by zikiai, assisted with the implementation, tests, documentation and screenshots for student summaries and per-student lesson counts.
* Anthropic Claude Code, used by aerodart, assisted with the implementation, tests and documentation for dated lesson records, logging lessons with a past date, editing lesson records and name, level and subject validation, and with the Developer Guide's architecture, common classes and lesson-logging sections.
* Anthropic Claude Code, used by srivatsanj27, assisted with the implementation, tests and documentation for finding students by subject and level, handling null GUI settings, renaming `log` to `loglesson` and sharing test helpers, and with pull request reviews and Developer Guide updates.
* The TutorLah logo (`tutorlah_icon.png`) was created using Google Gemini.
* OpenAI Codex, used by zikiai, assisted with unreadable-file recovery, safe JSON replacement, their tests and documentation.
* OpenAI Codex, used by cibichandar, assisted with the implementation, tests,
  documentation and screenshots for searching lesson histories, and with Unicode
  whitespace normalization in command-entered phone numbers and its regression tests.
* The team's agent brief (`tutorlah-agent-brief.md`) and `AGENTS.md` were inspired by the `AGENTS.md` files of teams [CS2103T-T16-1](https://github.com/AY2627S1-CS2103T-T16-1/tp) and [CS2103T-T16-2](https://github.com/AY2627S1-CS2103T-T16-2/tp). The ideas were adapted and the text was written by our team.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" alt="Architecture diagram showing Main, UI, Logic, Model, Storage and Commons, the dependencies between them, the user and the data file" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

* **Solid arrows** point to a component whose objects the source keeps a reference to: Main holds all four components, Logic holds Model and Storage, and UI holds the `Person` objects it displays.
* **Dashed arrows** point to something the source only uses: Storage converts Model objects to and from the data file without keeping them, Storage reads and writes the file, and the user works through UI.
* Arrow colors only show which component an arrow starts from.

Logic, Model and Storage do not depend on UI. Every component also uses Commons; those arrows are left out to keep the diagram readable.

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

The *Sequence Diagram* below shows how the components interact with each other when the user successfully logs a lesson for an existing student with the command `loglesson 1 c/Vectors`. `Logic` updates the student in `Model`, then asks `Storage` to save all student data to the data file. Error paths, such as an invalid index or a failed save, are omitted.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="666" alt="Sequence diagram of loglesson 1 c/Vectors: the user types the command into UI, UI asks Logic to execute it, Logic updates the student in Model, then asks Storage to save, and the feedback returns to the user" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram. For example, `UiManager` holds a `Logic`, not a `LogicManager`, and `LogicManager` holds a `Model` and a `Storage`.

<puml src="diagrams/ComponentManagers.puml" width="300" alt="Partial class diagram showing the Ui, Logic, Model and Storage interfaces, the Manager class that implements each, and the references between them" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/ui/Ui.java).

<puml src="diagrams/UiClassDiagram.puml" alt="Partial UI class diagram showing the main window, callback fields, student cards, the student detail panel and feedback display" width="1000" />

This partial class diagram focuses on command input, student cards, the details panel and feedback. `MainWindow` supplies a `CommandExecutor` callback to `CommandBox` and a `Consumer<Index>` callback to `PersonListPanel`, so neither input part needs a reference to the concrete `MainWindow` class. `PersonListPanel` creates `PersonCard` objects for visible students, and each card displays one `Person` from the Model component. `StudentDetailPanel` holds at most one `Person`, the student it currently shows. `Person` remains the Java class name for a student.

`UiManager` implements `Ui` and creates `MainWindow`. `MainWindow` and its visible UI parts inherit from `UiPart`; the window also creates `HelpWindow` and `StatusBarFooter`. These startup, shared-base and supporting classes are omitted from the focused diagram. The UI uses JavaFX, with layouts in `src/main/resources/view`, such as [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/resources/view/MainWindow.fxml).

The UI responsibilities are:

* `CommandBox` submits typed commands and clears its text only after successful execution. A failed typed command remains available for correction and receives error styling.
* `PersonListPanel` observes the filtered student list supplied by `Logic`. Compact cards show each student's displayed index, initials, name, level and subject, and lesson status. A flagged card also shows a red flag icon. The indexes follow the displayed list, including after filtering or deletion.
* `StudentDetailPanel` shows one student's level, subject, phone, address, flag reason and lesson records, numbered newest first as `view` numbers them. With no student selected, it shows guidance that works for the current list: `view 1` while students are displayed, `list` when a filter hides every saved student, or the `add` format when there are no students. It listens to `Logic#getFilteredPersonList` and the full student list, so the guidance follows `find`, `list`, `add` and `delete` without a restart. It renders whichever immutable `Person` `MainWindow` gives it and keeps no other state.
* The level and subject chips sit side by side when both fit; otherwise `StudentDetailPanel` stacks them so that a long chip wraps its text within the panel width instead of being cut off. Stacked chips return side by side only when they fit with 20 pixels to spare, more than the vertical scroll bar's width. Stacking can add the scroll bar and narrow the panel, so without this margin the two layouts would keep replacing each other.
* A primary mouse press on a populated card sends its displayed index to `MainWindow`. The callback runs before selection can rebuild the cell, so changing students after a typed `view` needs only one press. Repeated presses view the student again; empty cells and other mouse buttons do not execute the shortcut. Releasing a navigation key (arrows, Page Up/Down, Home, End) in the focused list sends the newly selected index through the same callback, so keyboard selection cannot leave the list and details panel showing different students. Programmatic selection and list changes fire no key events, so they never execute a command.
* `ResultDisplay` shows command feedback as wrapping, read-only text. Each update resets the caret and scroll position to the beginning, including when the feedback is unchanged. Error feedback uses a red style until the next command. Long histories remain accessible by scrolling, and the result area can be resized with the horizontal divider. The student list and details panel share the area below, separated by a vertical divider.
* `MainWindow` has a minimum size of 700 x 420 pixels. Its contents need about 350 pixels of height, leaving room for the title bar, so by calculation the window fits a `1280x720` screen at 150% scaling (about 853 x 480 logical pixels) with a taskbar.
* At startup, the saved or default window size is reduced to fit the primary screen's usable area. A saved position is restored when the complete window fits within any connected screen's usable bounds, including a secondary screen with negative coordinates. Otherwise, `MainWindow` centers it within the primary screen's usable bounds, which excludes areas reserved for taskbars and similar system UI.
* After a successful command, `MainWindow` updates the details panel and the list highlight, as described in [Keeping the details panel in sync](#keeping-the-details-panel-in-sync). Programmatic selection and list changes do not execute a view command.

### Logic component

**API**: [`Logic.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

<!-- These PNG link targets are generated by the puml tags during the build. -->
<a href="diagrams/LogicClassDiagram.png" no-validation><puml src="diagrams/LogicClassDiagram.puml" alt="Partial Logic class diagram showing command execution, held references and command results" width="740" /></a>

[`LogicManager`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/LogicManager.java) implements `Logic` and holds one `AddressBookParser`, `Model` and `Storage`. Lavender identifies Logic classes, pink the Model API and yellow the Storage API. The partial diagram uses `LogCommand` and `ViewCommand` as representative `Command` subclasses; command-specific parsers and dependencies are omitted. Click a class diagram in this section to open its full-size image.

How command execution works:

1. `AddressBookParser#parseCommand` returns a concrete `Command` for the input.
1. `LogicManager` calls `Command#execute(Model)`, which reads or updates the model and returns a `CommandResult`.
1. `LogicManager` compares the complete address book with its snapshot from before execution. If student data changed, it saves through `Storage` before returning the result. Otherwise it returns without saving, so `list`, `view`, `find`, `flagged`, `help` and `exit` work even when saving is unavailable. An edit that leaves the stored values unchanged also skips saving.

`CommandResult` contains feedback and the help/exit flags. Commands that view or change exactly one student (`view`, `findlesson`, `loglesson`, `edit`, `editlesson`, `deletelesson`, `flag` and `unflag`) also supply that `Person` as it is after the command, through `getStudent()`, even when the student has no lessons. Other commands leave it absent (`0..1`). The UI shows that student in the details panel and highlights their card, as described in [Keeping the details panel in sync](#keeping-the-details-panel-in-sync).

Invalid input raises `ParseException`; execution or save failures raise `CommandException`. Before executing a parsed command, `LogicManager` copies the address book and captures the current filter through `Model#getFilteredPersonListPredicate`. Student, lesson and flag values are immutable, so the copied list preserves the previous values. If saving throws `IOException`, including `AccessDeniedException`, the manager restores changed data through `Model#setAddressBook` and reapplies the captured predicate before reporting the existing save error. Capturing the predicate, rather than only the visible students, preserves its meaning for later updates. The existing observable list remains connected to the UI, and a retry uses the original displayed indexes.

The sequence diagram below follows the successful execution of `deletelesson 1 2`. `AddressBookParser` creates a `DeleteLessonCommandParser`, which parses both displayed indexes and creates the command. During execution, the command resolves student 1 against the filtered list. It converts lesson 2 from the newest-first display to stored position `lessons.size() - 2`, creates an immutable student without that lesson, and passes both student values to `Model#setPerson`. These data-object operations are described here rather than expanded in the diagram, which treats Model as a black box. The common persistence path is shown in the architecture sequence diagram.

<puml src="diagrams/DeleteLessonSequenceDiagram.puml" alt="Successful lesson-deletion sequence from parser and command creation through replacing the student in Model" width="950" />

Student deletion follows a related but distinct path. For a successful `delete 1`, `AddressBookParser` creates a `DeleteCommandParser`, which parses the displayed index and creates the command. `DeleteCommand` resolves that index against the filtered list, selects the corresponding `Person`, and passes the person rather than the numeric index to `Model#deletePerson`. Removing the student also removes their owned lesson records. The common persistence path is omitted as above.

<puml src="diagrams/DeleteStudentSequenceDiagram.puml" alt="Successful student-deletion sequence from parser and command creation through deleting the selected Person" width="950" />

The following diagram focuses on parser selection and command creation:

<a href="diagrams/ParserClasses.png" no-validation><puml src="diagrams/ParserClasses.puml" alt="Partial parser class diagram showing selection of lesson parsers and creation of their commands" width="520" /></a>

[`AddressBookParser`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/logic/parser/AddressBookParser.java) separates the command word from its arguments and creates the matching parser. It lowercases only the command word with `Locale.ROOT`, so command selection ignores case without depending on the system locale. Arguments are unchanged, so prefixes remain case-sensitive and must use their documented lowercase forms. Command-specific parsers implement the generic `Parser` interface: for example, `LogCommandParser` implements `Parser<LogCommand>` and returns a `LogCommand`. The diagram omits the type parameter, common `Command` superclass and other command parsers; `help` and `exit` are created directly without separate parsers.

The helper dependencies are shown separately to keep the overview readable. Pale grey-blue boxes identify helpers within the same parser package.

<a href="diagrams/ParserHelpers.png" no-validation><puml src="diagrams/ParserHelpers.puml" alt="Partial parser helper diagram showing tokenization for logging and shared validation for both lesson parsers" width="500" /></a>

* `LogCommandParser` tokenizes `c/` and optional `date/` arguments using `ArgumentTokenizer` and the `Prefix` constants in `CliSyntax`. `ArgumentMultimap` stores the preamble and prefixed values and checks for duplicate prefixes. `ParserUtil` validates the student index, content and date; `loglesson` and `editlesson` use `ParserUtil#parseLessonDateWithNotesHint`, described in [Prefix text inside values](#prefix-text-inside-values).
* `DeleteLessonCommandParser` splits two positional indexes on whitespace, without using the tokenizer. It shares `ParserUtil`'s index validation, using the appropriate student- or lesson-index error message.

The helper diagram omits prefix classes, model values and exceptions to focus on tokenization, use of the returned multimap and shared validation.

Command words, positional indexes and prefix boundaries recognize Unicode
whitespace, including non-breaking spaces. `ArgumentTokenizer` trims field
boundaries without replacing internal spacing. In the UI, `CommandBox` uses a
`TextFormatter` to replace inserted tabs and line breaks with spaces before
JavaFX discards them, so pasted words do not join. Pasting does not execute a command.

### Model component

The **API** of this component is specified in [`Model.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/model/Model.java).

<puml src="diagrams/ModelClassDiagram.puml" alt="Partial Model class diagram showing students, value fields, optional follow-up flags and owned lesson records" width="950" />

This partial class diagram focuses on student and lesson data. `ModelManager` implements the `Model` API and owns one `AddressBook`; user-preference classes are omitted. `AddressBook` delegates uniqueness and list operations to `UniquePersonList`, while `ModelManager` exposes a filtered view of the same students for searches and displayed indexes.

`Person` is the Java class representing a student. It is an immutable value containing `Name`, `Phone`, `Address`, `Level`, `Subject`, an immutable list of zero or more `Lesson` values, and zero or one `Flag`. A `Flag` marks unresolved follow-up and may contain one validated reason. A `Lesson` contains content and an optional date: records saved by v1.3 have no date, while newer records have a `LocalDate`. Lessons and flags belong to their student, so deleting a student also deletes them.

Two students have the same identity only when their phone numbers match and their names match ignoring case. Other fields, including lessons and flags, contribute to full equality but not duplicate detection. `Person#withLessonAdded`, `Person#withLessonRemoved`, `Person#withFlag` and `Person#withoutFlag` return new `Person` objects rather than mutating the existing value. Any operation that rebuilds a student carries both the existing lesson list and flag across unless it is intentionally changing the flag.

Lesson records are stored oldest first by date. Undated legacy records remain at the oldest end, and records on the same date retain logging order. `view` reverses this stored order for display. The Model component has no dependency on UI, Logic or Storage, so its domain objects and rules remain meaningful independently of those components.


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103T-T11-1/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" alt="Storage class diagram showing StorageManager, the two JSON storages and the student, lesson and flag adapters" width="550" />

`StorageManager` delegates student data to `JsonAddressBookStorage` and window preferences to `JsonUserPrefsStorage`. Both read and write JSON, using Model classes to validate and reconstruct values.

**Stored values**

* `JsonSerializableAddressBook` contains a `persons` list of `JsonAdaptedPerson`. Each adapter holds the student's five detail fields, lessons and optional flag.
* `JsonAdaptedLesson` stores content and an optional date as `YYYY-MM-DD` (`LocalDate#toString`); commands and the UI use `DD/MM/YYYY`. `JsonAdaptedFlag` stores an optional reason.
* Optional values are omitted rather than saved as `null`: undated lessons have no `date`, unflagged students have no `flag`, and reasonless flags have no `reason`. A legacy student without `lessons` loads with an empty history.

**Loading and ordering**

* `JsonAdaptedPerson` uses `Name#normalize`, shared with `ParserUtil#parseName`, to trim surrounding Unicode whitespace and collapse internal whitespace before validation. `JsonSerializableAddressBook` rejects normalized duplicates using `isSamePerson`; it never merges students or drops their records. Equal names with different phones remain separate students.
* Loaded lessons are stably sorted oldest first, with undated records first and saved order retained for ties. `view` and the details panel reverse that order. `editlesson` and `deletelesson` use the same displayed lesson numbers through `LessonCommandUtil#getStoredLessonIndex`; `loglesson` inserts new records by date.
* `JsonAdaptedLesson` rejects invalid dates and dates before `Lesson.EARLIEST_DATE`, reporting stored-date errors in `YYYY-MM-DD`. Undated legacy records and existing future dates still load, so a clock correction does not invalidate saved history. Lesson-writing commands retain their future-date checks.
* Each `toModelType` validates its values; an invalid student or lesson rejects the complete file. Loading and read-only commands do not rewrite it. Sorting and normalized names are first saved after a successful data change.

**Atomic saves**

`JsonUtil` serializes the complete value before `FileUtil#writeToFile` calls `AtomicFileWriter`. For both student data and preferences, the writer creates missing parent directories, stages UTF-8 content in a unique sibling file, then replaces the destination using `Files.move` with `ATOMIC_MOVE` and `REPLACE_EXISTING`. It never pre-creates or truncates the destination.

* A write or replacement failure preserves the original destination, including leaving no empty destination after a failed first save. Cleanup is attempted; a cleanup failure is suppressed on the original exception.
* With `ATOMIC_MOVE`, replacement of an existing target is provider-specific. Unsupported atomic replacement reports a save failure; there is no non-atomic fallback or power-loss guarantee.
* Abrupt termination can leave a `.tutorlah-*.tmp` file. Startup neither loads nor removes it; it may be deleted after all TutorLah instances close. Atomic replacement protects file writes; the separate `ApplicationLock` provides exclusive session access.

**Protected recovery**

If `JsonAddressBookStorage#readAddressBook` fails, that storage instance blocks both save overloads for the session before any file is created or written. `MainApp` uses an empty model, and `UiManager` shows recovery instructions after the window opens. The original file is preserved. Read-only commands continue to work; mutations fail saving and are rolled back. Recovery requires closing TutorLah, repairing or restoring the file, then restarting with a new storage instance.

`MainApp#getDataLoadingReason` selects a safe reason:

* Non-blank `IllegalValueException` messages retain the field-validation explanation.
* Syntax failures are unwrapped to `JsonParseException` and reported as `The file is not valid JSON (line N, column M).`, or without coordinates if unavailable.
* Invalid structures, such as a null `persons` list, and other read failures receive general readability/data-format guidance. Raw Jackson messages, source excerpts and internal class names are never shown.

A genuinely missing file loads sample students without a warning; the first data change saves them. A valid saved empty collection stays empty.

**Preferences and session locking**

Unreadable preferences, trailing JSON content or `"guiSettings": null` cause `DataLoadingException`. `MainApp#initPrefs` logs a warning and saves default `UserPrefs`, without a dialog or a student-data write. `MainWindow` limits restored dimensions and positions to usable screen bounds.

Before loading either file, `MainApp` obtains an operating-system lock through `ApplicationLock` on `data/.tutorlah.lock`. Failure or an existing lock stops initialization before Storage, Model, Logic or the main window are created; a failure-only UI explains the problem. The channel and lock are held until `MainApp#stop`. A leftover lock file is harmless after the operating-system lock is released. This prevents concurrent sessions from overwriting each other's changes.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package:

* `core`: `LogsCenter` gives each class its logger, `GuiSettings` holds the saved window size and position, and `Index` converts between the one-based indexes users type and the zero-based indexes the code uses.
* `exceptions`: `IllegalValueException` reports an invalid stored value, and `DataLoadingException` reports a data file that cannot be read or loaded.
* `util`: `StringUtil` has the text checks shared by commands and the data file, such as `isBlankOrInvisible`, `stripUnicodeWhitespace` and the case-insensitive `containsIgnoreCase` used by the searches. `AppUtil#checkArgument` and `CollectionUtil` check arguments, `JsonUtil` and `FileUtil` read and write JSON files, `AtomicFileWriter` replaces a file in one step so that a failed save cannot leave it half-written, and `ToStringBuilder` formats `toString` output.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Logging a lesson

`loglesson INDEX c/CONTENT [date/DATE]` adds one dated lesson record to a student. The activity diagram below shows its steps. A failed save is left out; `LogicManager` then restores the previous data, as described in the [Logic component](#logic-component).

<puml src="diagrams/LogActivityDiagram.puml" width="300" alt="Activity diagram of loglesson: parsing, checking the index, using the given date or today's date, rejecting a future date, inserting the lesson by date and saving" />

* **Parsing:** `LogCommandParser` tokenizes only `c/` and `date/`. It rejects a missing index or content, a repeated prefix, invalid content and an invalid date. `ParserUtil#parseLessonDate` accepts `D/M/YYYY` or `DD/MM/YYYY` with a four-digit year, and rejects dates that do not exist, such as `31/02/2026`, and dates before `Lesson.EARLIEST_DATE` (01/01/2000). `loglesson` and `editlesson` call it through `parseLessonDateWithNotesHint`, which adds a hint to reword `date/` if it was part of the lesson notes. `findlesson` gives the date error without the hint.
* **Today's date by default:** `LogCommand` gets today's date from a `Clock`: the system clock in the app, and a fixed clock in tests. A lesson without `date/` is dated today, and a date after today is rejected with `Lesson dates cannot be in the future. ...`. The parser does not check for future dates, so that every check uses the same clock.
* **Date order:** each student's lessons are stored oldest first. `Person#withLessonAdded` steps back from the newest end past every lesson dated after the new one, and inserts it there. A lesson logged for a date that already has lessons therefore goes after them, and `view`, which lists the stored order in reverse, shows it first among that day's lessons. `deletelesson` and `editlesson` use `view`'s numbering, so no command needs to sort.
* **Undated records:** lessons saved before v1.4 have no date, and `view` shows them as `(undated)`. They are kept at the oldest end, so `view` lists them last. When the data file is loaded, `JsonAdaptedPerson` stably sorts each student's lessons by date, with undated lessons first and lessons on the same date in their saved order, as described in the [Storage component](#storage-component). `withLessonAdded` never steps past an undated lesson, so a new lesson always goes after them. `JsonAdaptedLesson` stores a date as `"date" : "YYYY-MM-DD"` and leaves the field out for an undated lesson.

**Design considerations: when lessons are ordered**

* **Alternative 1 (current choice):** insert each lesson in date order when it is logged.
  * Pros: `view`, `deletelesson` and `editlesson` all number lessons by reversing the stored order, so they share one numbering without sorting, and the data file reads chronologically.
  * Cons: every command that adds or redates a lesson must insert it correctly.
* **Alternative 2:** store lessons in logging order and sort them by date when displaying.
  * Pros: adding a lesson is a plain append.
  * Cons: every command that takes a lesson number must repeat the same sort, or its numbers would not match `view`.

### Viewing lesson history

Both `view INDEX` and a primary press on a student card reach `MainWindow#executeCommand`. Typed input arrives through the `CommandBox` callback; a card press arrives through `MainWindow#handlePersonClick`, which builds `view INDEX` using the card's current displayed index. The click path leaves command-box text unchanged.

<puml src="diagrams/ViewHistorySequenceDiagram.puml" alt="Successful view sequence showing typed input and a student-card press joining the same UI execution path" width="950" />

The sequence shows both entry paths viewing displayed student 1 through `MainWindow`. It expands UI internals and treats `Logic` as an interface. Parsing, Model internals, saving, JavaFX internals and errors are outside its scope. Activation bars and the private self-call from `handlePersonClick` are omitted for clarity; the final alternatives return through the entry path chosen at the top.

`MainWindow` passes the command to `Logic#execute`. A successful `ViewCommand` returns a `CommandResult` containing the feedback and the viewed `Person`. `MainWindow` displays the feedback, then finds the student's position in the displayed list, shows the student in `StudentDetailPanel` and passes the position to `PersonListPanel#selectPerson` to synchronize the highlight and scroll position. The student is present even when their history is empty. This selection does not invoke the click callback, so it cannot execute the command again.

`ViewCommand` reads the student from `Model#getFilteredPersonList`, leaving the current filter and student data unchanged. It traverses the stored lessons backwards: the most recent lesson date appears first, with the most recently logged record first among equal dates. Existing undated records appear at the oldest end when lessons have been added in date order. Each row shows its displayed lesson number, formatted date and content; undated records use `(undated)`. An empty history returns `No lessons logged yet for NAME.`.

If parsing or execution fails, `MainWindow` displays the error in the error style and leaves the details panel and highlight unchanged. For typed input, `CommandBox` keeps the failed command and marks it as an error. Card-click failures are displayed through the same execution path without clearing typed text. Viewing changes no student or lesson data, so `LogicManager` returns its result without attempting to save.

**Design considerations: handling card clicks**

* **Current choice:** build `view INDEX` from the card's displayed index and use the existing Logic execution path. Typed commands and clicks share validation, feedback and selection behavior, while the callback preserves typed text.
* **Alternative:** update the panel directly from the clicked card. This avoids a command call but duplicates viewing behavior and can drift from command validation and feedback.

### Keeping the details panel in sync

`StudentDetailPanel` must reflect the latest data after any command, but student values are immutable: `loglesson`, `edit`, `editlesson`, `deletelesson`, `flag` and `unflag` each replace the student in the Model with a new `Person`. A panel that kept the old object would show stale data.

Every command that views or changes exactly one student therefore returns that student, as it is after the command, through `CommandResult#getStudent`. These are `view`, `findlesson`, `loglesson`, `edit`, `editlesson`, `deletelesson`, `flag` and `unflag`. After each successful command, `MainWindow#updateSelectedStudent` applies these rules:

1. If the result names a student, the panel shows that student. The list highlights the student's current position in `Logic#getFilteredPersonList`, or clears the highlight if a filter now hides them. For example, `unflag 1` in the `flagged` results removes the student from the list but keeps them in the panel.
1. Otherwise, the panel keeps its current student if the displayed list still contains that same value, and refreshes the highlight because positions may have shifted.
1. Otherwise, for example after `find` hides the student or `delete` removes them, the panel and the highlight are cleared.

Failed commands skip these rules, so an error never changes the panel. A failed save is a special case: `LogicManager` restores the previous student data, which replaces the list contents and clears the JavaFX selection. `MainWindow#restoreShownStudentHighlight` therefore highlights the panel's student again after any failed command, without scrolling if the highlight was not lost. Commands that affect no single student, such as `list`, `summary` and `add`, need no changes.

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

`PersonListPanelTest` covers the panel's contents and every rule above, including error feedback, filter changes, `edit` resetting the filter, `unflag` in the `flagged` results, deletion, keyboard navigation and failed saves after `loglesson`, `edit`, `flag` and `delete`. The command tests check the student each targeted command returns.

### Rejecting blank Unicode fields

`Address#isValidAddress` and `Lesson#isValidContent` reject values composed entirely of Unicode whitespace (`\p{IsWhite_Space}`), format characters (`\p{Cf}`), or a mixture of the two. This includes non-breaking spaces, ZERO WIDTH SPACE (U+200B), WORD JOINER (U+2060) and a standalone BOM (U+FEFF). The check does not remove formatting characters inside real text: joiners in written languages and emoji sequences are preserved, along with internal spaces and the existing length and character rules. Both command parsing and the JSON adapters use these model validators, so `add`, `edit`, `loglesson` and data loading share the same non-blank rule and existing field-specific messages.

### Deleting an individual lesson record

`DeleteLessonCommandParser` requires exactly two indexes. Both go through `ParserUtil#parseCommandIndex`, but student and lesson errors use their respective messages. `DeleteLessonCommand` obtains the student from `Model#getFilteredPersonList`, so the student index always refers to the list currently displayed after any search. It validates the student, the presence of lesson records and the lesson index before changing the Model.

`view` numbers lessons newest first, whereas `Person#getLessons` stores them oldest first. `Person#withLessonAdded` inserts each lesson in date order, keeping same-date records in logging order. The command therefore converts a one-based displayed lesson number to a zero-based stored position using `lessons.size() - lessonIndex.getOneBased()`. An assertion protects this internal conversion after the user-supplied index has been validated. Removing by position deletes only the selected occurrence even when two lessons have identical content and dates.

`Person#withLessonRemoved` returns a new student with the selected record omitted
and every student field and remaining lesson preserved. `Model#setPerson` replaces
the old value in the backing `UniquePersonList`; the active filtered view continues
to show the same matching students.
The confirmation includes the displayed lesson number, student name, lesson date
and deleted content. `Messages#formatLessonDate` supplies the date, shown in parentheses,
or `(undated)` for a legacy record. `LogicManager` persists the updated address book
through the normal save path shown in the architecture sequence diagram.

Deleting the final lesson therefore leaves the student present with an empty history. A later deletion attempt stops before mutation and reports `No lessons logged yet for NAME.`.

### Editing a student

`EditCommand` and `EditCommandParser` are adapted from AB3, with tags replaced by
`Level` and `Subject`. `EditCommandParser` tokenizes `n/`, `p/`, `addr/`, `l/` and
`sub/`. The preamble must be one student index, checked by `ParserUtil#parseStudentIndex`,
and a prefix given twice is rejected with the shared duplicate-prefix message.
Each value uses the same `ParserUtil` method as `add`: names have whitespace collapsed,
and `parsePhone` removes Unicode `White_Space` before checking eight ASCII digits.
Zero-width formatting characters remain invalid; stored phone values are validated
without this normalization. The parsed values are stored in an `EditPersonDescriptor`,
and a descriptor with no fields is rejected with `At least one field to edit must be provided.`.

`EditCommand` looks up the student in `Model#getFilteredPersonList`, so the index is the displayed one. `createEditedPerson` takes each field from the descriptor if present and from the existing student otherwise, then builds the new `Person` with the existing lesson list and flag. A `Person` is immutable and the `Lesson` list it returns is unmodifiable, so sharing that list is safe, and lesson numbers and dates are unchanged. `assertLessonsKept` and `assertFlagKept` check this. If the edit changes the student's identity (name ignoring case, or phone) to match another student, the command is rejected with `This student already exists in the list.`.

On success the command calls `Model#setPerson`, restores the full list as AB3 does, and returns the edited student in its `CommandResult`. The details panel and highlight then follow that student, as described in [Keeping the details panel in sync](#keeping-the-details-panel-in-sync). `LogicManager` saves the change. If the save fails, it restores the previous address book and filter.

### Editing a lesson record

`EditLessonCommandParser` tokenizes only `c/` and `date/`, so lesson content can contain text such as `a/level`. The text before the first prefix must be exactly two indexes, which are checked with the same `ParserUtil#parseCommandIndex` rules and messages as `deletelesson`. Content and dates go through `ParserUtil#parseLessonContent` and `ParserUtil#parseLessonDate`, the same rules as `loglesson`. The parsed values are stored in an `EditLessonDescriptor`, modeled on `EditCommand.EditPersonDescriptor`, and a descriptor with no fields is rejected.

`EditLessonCommand` finds the student and converts the displayed lesson number to a stored position exactly as `DeleteLessonCommand` does: both call `LessonCommandUtil#getStoredLessonIndex`, which also rejects a student with no lessons or a number beyond their history. Messages shared by several commands, such as the no-lessons and future-date messages, live in `Messages`. `EditLessonCommand` then checks the new date against today's date from an injected `Clock`, as `LogCommand` does, so tests can fix the date. An edit that would leave both content and date unchanged is rejected before the model changes.

**Design considerations: keeping date order when a lesson is edited**

* **Alternative 1 (current choice):** replace the lesson in place with `Person#withLessonReplaced` when its date is unchanged, and use `Person#withLessonRemoved` then `Person#withLessonAdded` only when the date changes. `withLessonAdded` inserts the lesson after every lesson dated on or before its new date, the same rule `loglesson` uses.
  * Pros: a content-only edit keeps the lesson's number, so the number the tutor saw in `view` stays valid.
  * Cons: two code paths. `withLessonReplaced` must assert that the date is unchanged so that it cannot break date order.
* **Alternative 2:** always remove the lesson and add it again.
  * Pros: one code path, shared with `loglesson`.
  * Cons: the re-added lesson goes after the other lessons on its date, so correcting only its content can change its number.

`Lesson#withContent` keeps an undated legacy lesson undated, so a content-only edit never adds a date. After `Model#setPerson`, the command finds the edited lesson by identity, because another lesson can be equal to it, and reports its new displayed number. Assertions check that the student's identity and number of lessons are unchanged.

### Prefix text inside values

`ArgumentTokenizer` starts a new field at every prefix the command recognizes when it follows whitespace, including non-breaking spaces and tabs. Free-text values can therefore not contain those prefixes after a space: `loglesson 1 c/Worksheet collected c/o Mum` has two `c/` fields, and `loglesson 1 c/Revised date/time conversions` gives `time conversions` as the date. Other slashes, such as `a/l` in a name or `d/dx` in notes, are ordinary text.

**Design considerations: prefix text in values**

* **Alternative 1 (current choice):** keep the same rule for every command, and explain it in error messages.
  * Pros: users learn one rule. Duplicate-field and strict date validation stay unchanged.
  * Cons: users must reword text such as `c/o`, as `care of`.
* **Alternative 2:** special-case likely text, such as `c/o` or `date/time` in lesson content.
  * Pros: these common phrases work as typed.
  * Cons: the same request would follow for `n/a` in addresses, `r/` in flag reasons and many other abbreviations. Guessing could also store a mistyped date or a pasted second field as notes without any error.

The error messages explain the limitation:

* `Messages#getErrorMessageForDuplicatePrefixes` adds `MESSAGE_PREFIX_TEXT_HINT` on a new line, so every command's duplicate-field error suggests rewording prefix text. The first line still names the repeated prefixes.
* `LogCommandParser` and `EditLessonCommandParser` call `ParserUtil#parseLessonDateWithNotesHint` for `date/`. It calls `parseLessonDate` and, on failure, throws a new `ParseException` with the original message, `MESSAGE_LESSON_DATE_TEXT_HINT` on a new line, and the original exception as its cause. Only the date call is wrapped, so index and content errors have no date hint, and `findlesson` keeps using `parseLessonDate` without the hint.

The UG asks users to reword such text, for example `care of` and `date and time`. Hints appear only when the input is invalid. A valid command with a different meaning, such as `edit 1 addr/Unit l/2`, which sets the level to `2`, cannot be detected; this is a documented limitation. A general quoting or escaping syntax for literal prefix text is a possible future enhancement.

`AddCommandParser` appends conditional guidance to invalid phones, levels and subjects only when an
unsupported ASCII-letter prefix follows Unicode whitespace and an otherwise valid field value.
The existing field parser validates that preceding value. Names and addresses permit slashes and keep
their normal errors. Parsing, validation and missing/duplicate-field handling remain unchanged; an added
hint preserves the original exception as its cause.

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

The predicate exposes its immutable keywords through `getKeywords`. If records exist but no results
match, `FindLessonCommand` adds a syntax hint when a keyword starts with lowercase `c/` or `date/`.
It does not reinterpret keywords, change matching or replace empty-history and invalid-date feedback.

`FindLessonCommand` reads the selected student's complete history from the filtered student
list and iterates backwards. It derives each view number from the original stored position
before collecting matches, so gaps remain and repeated content is not merged. The result
identifies the student, so the UI shows their complete history in the details panel and
highlights their card. No lesson filter, model update
or storage field is introduced; `LogicManager` skips saving unchanged data.

### Student and lesson summaries

`AddressBookParser` routes `summary` to `SummaryCommandParser`, which accepts whitespace but rejects arguments with the summary-specific usage. `SummaryCommand` creates a `StudentSummary` from `Model#getAddressBook`, rather than the filtered list, and returns a heading and a single summary paragraph in a normal `CommandResult`. `Messages#formatSummaryParagraph` handles empty collections and singular/plural wording for the totals. It never updates the active filter. The immutable snapshot counts each lesson occurrence, including undated legacy records; the total uses a `long` so adding the sizes of many histories cannot overflow an `int`. No summary values are added to JSON. Because the command changes no stored data, `LogicManager` returns its result without attempting to save, including during recovery.

`Person#getLessonCount` counts that student's records. `Person#getLatestLessonDate` selects the greatest present date, ignoring undated records and avoiding an assumption about list order. `Messages#formatLessonStatus` supplies empty, singular, plural and undated wording. `PersonCard` places this status below the level and subject using the existing wrapping label style. Each immutable student replacement already triggers the list cell to rebuild its card, so `loglesson`, `editlesson` and `deletelesson` need no additional UI callbacks or cached counters. Flag display is independent of the lesson-status row.

`StudentSummaryTest`, `PersonTest`, `MessagesTest`, the summary command/parser tests and `PersonListPanelTest` cover zero/one/many totals, filtered views, legacy dates, persistence, unavailable saving, dynamic card updates and layout alongside flags. The class diagrams show representative classes and omit this derived snapshot and the summary command/parser.

### Finding students by name, subject and level

`AddressBookParser` routes `find` input to `FindCommandParser`, which works in three steps:

1. It turns every Unicode whitespace character, such as a non-breaking space or tab, into a plain space, so that search keywords use a consistent separator. The shared tokenizer already recognizes a configured prefix after Unicode whitespace, as described in [Prefix text inside values](#prefix-text-inside-values), so this step is only about keywords.
1. It passes the normalized text to `ArgumentTokenizer` for the `n/`, `sub/` and `l/` prefixes. Text before the first of these prefixes and the `n/` value become name keywords; the `sub/` and `l/` values become subject and level keywords. Each part is split on whitespace into literal keywords.
1. It rejects invalid searches. A prefix given twice gets the shared duplicate-prefix message, a prefix whose value is blank (including Unicode spaces and invisible formatting characters) gets that field's constraint message, and a search with no keywords at all shows the usage.

Other prefixes are not recognized, so a search for `a/l` stays part of the name keywords and cannot be read as an address.

`StudentContainsKeywordsPredicate` stores immutable copies of the name, subject and level keywords as typed, and needs at least one keyword in total. A student matches when each field contains every keyword given for it, in any order, and every field that has keywords matches. A field with no keywords is not searched. Predicate equality and hashing use all three keyword lists, so the same keyword in a different field is a different search.

All three fields share `StringUtil#containsIgnoreCase`, so matching is locale-independent and literal, without regular expressions or spelling correction.

When a search finds no one, `FindCommand` may add guidance:

* `isNameSearchOnly()` reports when only name keywords were given, so `FindCommand` adds a hint about `sub/` and `l/`.
* Every word after `sub/` or `l/` belongs to that field, so a name typed afterwards, as in `find sub/Physics ng`, finds no one. `withTrailingFieldWordsAsNames()` returns the searches that move the last one or more words of a multi-word subject or level search to the names, fewest words first (subject before level for the same number of words), keeping at least one word in each field. `FindCommand` tests these against `Model#getAddressBook`, without changing the displayed list, and suggests the first one that matches, written with name keywords first.

`FindCommand` passes this predicate to `Model#updateFilteredPersonList`. The existing `FilteredList` applies it to all saved students, so a later search replaces the previous search instead of narrowing it further. The GUI observes this list and updates the cards and their displayed indexes automatically. An empty filtered list shows guidance to type `list`; it does not imply that stored students have been deleted.

`view`, `loglesson`, `edit`, `delete`, `deletelesson` and student-card clicks continue to use displayed indexes. `list` restores all students. Adding or editing also restores the full list, while viewing, logging and deleting students or lessons retain the filter. The search changes no student or lesson data and is not persisted across restarts; read-only searches do not rewrite the student data file.

**Design considerations: when keywords match**

* **Alternative 1 (current choice):** a student must match every keyword. A failed search may suggest a corrected one, which is never applied automatically.
  * Pros: each extra word narrows the results, and valid multi-word values such as `Sec 3` and `H2 Math` keep their meaning.
  * Cons: a name typed after `sub/` or `l/` finds no one until the user corrects it.
* **Alternative 2:** a student may match any keyword.
  * Pros: a misplaced or extra word still finds students.
  * Cons: `find sub/H2 Math` would also list every student whose subject contains `Math`, so results grow as the search gets more specific.

### Flagging students for follow-up

`FlagCommandParser` tokenizes only `r/`, leaving text such as `a/level` inside a reason. It parses the preamble with the shared student-index policy, rejects duplicate `r/` prefixes, trims surrounding Unicode whitespace and constructs an immutable `Flag`. A present flag may omit its reason, but a supplied reason must contain meaningful text, contain no ISO control characters and be at most 100 characters. The command box converts pasted tabs and line breaks to ordinary spaces before parsing, so they remain word separators instead of reaching this validation rule. The rule still protects other input paths and directly edited JSON data.

`FlagCommand` and `UnflagCommand` obtain their target from `Model#getFilteredPersonList`, so they act on the displayed number after either `find` or `flagged`. They replace the immutable `Person` through `Model#setPerson` without changing its identity, details or lessons. Reflagging with a new reason replaces only the `Flag`; reflagging without a reason never clears an existing reason. `FlaggedCommand` applies `Person#isFlagged` to the full backing student list through `Model#updateFilteredPersonList`. Consequently, unflagging a student while that filter is active removes the updated value from the displayed list automatically.

`JsonAdaptedPerson` omits its nullable adapted flag for unflagged students. A present `JsonAdaptedFlag` omits its nullable reason, producing an empty JSON object for a reasonless flag. This distinguishes all three persisted states while keeping older data files compatible. The UI derives the card's flag icon and the details panel's flag banner directly from each immutable `Person`; no separate UI state is stored.

**Design considerations: representing follow-up state**

* **Alternative 1 (current choice):** store an optional `Flag`, whose reason is also optional.
  * Pros: one field cannot contradict another.
  * Cons: callers must unwrap an `Optional`.
* **Alternative 2:** store a boolean and a nullable reason directly on `Person`.
  * Pros: callers can check the boolean directly.
  * Cons: the two fields can disagree, such as `isFlagged == false` with a non-null reason, and must change together.

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

| Priority | MVP | Status/version | As a ... | I want to ... | So that I can ... |
|----------|-----|----------------|--------|-------------|----------------|
| `* * *` | Yes | Implemented (v1.2) | tutor | add a student's details | keep their information in one place |
| `* * *` | Yes | Implemented (v1.2) | tutor | see a list of my students | identify the student whose information or lesson records I need |
| `* * *` | Yes | Implemented (v1.2) | tutor | delete a student and their associated lesson records | ensure those details are no longer kept in TutorLah |
| `* * *` | Yes | Implemented (v1.4) | tutor | add a dated lesson record to a student | avoid relying on memory before the next lesson |
| `* * *` | Yes | Implemented (v1.4) | tutor | view a student's dated lesson records with the most recent first | see where we stopped and what we covered previously |
| `* *` | No | Implemented (v1.4) | tutor | edit a student's details | keep their information accurate when it changes |
| `* *` | No | Implemented (v1.2) | tutor | store a student's phone number and address | reach that student and find their home when needed |
| `* *` | No | Implemented (v1.4) | tutor managing many students | find a student by name | access the correct student's information quickly |
| `* *` | No | Implemented (v1.5) | tutor | find students by subject and level | prepare common material for students with similar needs |
| `* *` | No | Proposed | tutor | record a student's regular weekly lesson slot | remember the schedule and identify clashes |
| `* *` | No | Implemented (v1.4) | tutor | delete an incorrect lesson record | keep the student's lesson history accurate |
| `* *` | No | Implemented (v1.5) | tutor | correct the content or date of a student's lesson record | fix a mistake without deleting the record and logging it again |
| `* *` | No | Implemented (v1.5) | tutor | search a student's lessons by topic or date range | find when a topic was covered and review work over a period |
| `* *` | No | Implemented (v1.5) | tutor | flag a student who needs follow-up and optionally note why | keep unresolved concerns visible and find those students again quickly |
| `* *` | No | Implemented (v1.5) | tutor | see total students and lesson records and each student's lesson count | review my recorded workload |
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

* 1a. Tutor supplies unwanted arguments.

    * 1a1. TutorLah informs Tutor that the command does not accept parameters.

      Use case resumes at step 1.

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
2.  Tutor requests to view the lesson records of a specific student in the list.
3.  TutorLah shows the student's details and lesson records, most recent first.

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

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The student has no lesson records.

    * 2b1. TutorLah informs Tutor that no lessons have been logged for the student.

      Use case ends.

* 2c. The given lesson record does not exist.

    * 2c1. TutorLah shows an error message.

      Use case resumes at step 2.

**Use case: UC08 - Flag a student for follow-up**

**Guarantees:** Flagging changes only the selected student's follow-up flag and optional reason. Their details and lesson records are kept.

**MSS**

1.  Tutor <u>lists students (UC02)</u> or <u>finds students (UC10)</u>.
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

* 2d. The student is already flagged with the supplied reason.

    * 2d1. TutorLah informs Tutor that no change was made.

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

* 1a. Tutor gives no search words, or leaves a name, subject or level search blank.

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
3.  TutorLah shows the matching lesson records newest first, with their lesson numbers from the student's complete history.

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
2.  TutorLah shows the total number of students and the total number of lesson records.

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
3.  TutorLah removes the flag and reason and shows a confirmation.

    Use case ends.

**Extensions**

* 2a. The given student index is invalid.

    * 2a1. TutorLah shows an error message.

      Use case resumes at step 2.

* 2b. The student is not flagged.

    * 2b1. TutorLah informs Tutor that no change was made.

      Use case ends.

* 3a. Tutor is viewing only flagged students.

    * 3a1. TutorLah removes the student from the displayed list of flagged students.

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

1.  **Platform support:** TutorLah should work on Windows, Linux and macOS.
2.  **Java version:** TutorLah should work with Java `25`.
3.  **Portable distribution:** TutorLah should be usable without an installer.
4.  **Single-file distribution:** TutorLah should be packaged in one JAR file that does not exceed `100 MB`.
5.  **Typing-first interaction:** A user with above-average typing speed for regular English text should be able to accomplish most tasks faster using commands than using the mouse.
6.  **Performance:** With up to 1,000 students and 10,000 lesson records, each command should show its result within 2 seconds on a typical laptop.
7.  **Durability:** A change that TutorLah reports as successful should already be saved, so closing the app straight afterwards loses no data.
8.  **Data integrity:** A failed command, including one whose changes cannot be saved, should leave the stored student and lesson records unchanged.

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

1. Run `java -jar TutorLah.jar` with no existing data file. Expected: sample students and a command box. Enter `list`, then `loglesson 1 c/Restart check` to make a change that saves the students and lesson.
1. Resize and move the window, then enter `exit`. Relaunch from the same folder and enter `view 1`. Expected: the students and Restart check lesson remain. The window size and location are retained when they fit the connected screens.
1. Close TutorLah and edit `preferences.json` to place the window wholly beyond the right/bottom of every screen. Restart: it is centered within the primary screen's usable bounds. Repeat beyond the left/top edges.
1. With a secondary screen connected, move the whole window onto it, exit and restart. Expected: its position is retained, including on a screen with negative coordinates. Disconnect that screen and restart: the window is centered on the primary screen.

### Student and lesson summaries

Use a separate test folder with `data/addressbook.json` containing `{"persons": []}`, prepared before launch.

1. Enter `summary`. Expected feedback: `No students yet. Add a student to start recording lessons.`
1. Enter these commands:

   ```text
   add n/Amy Tan p/81230001 addr/18 Example Street l/Sec 3 sub/Physics
   add n/Ben Lim p/81230002 addr/24 Example Street l/Sec 4 sub/Chemistry
   loglesson 1 c/Algebra date/10/10/2026
   loglesson 1 c/Geometry date/20/10/2026
   ```

   Expected: Amy's card shows `2 lessons, last 20/10/2026`; Ben's shows `No lessons yet`. Summary feedback: `You have 2 students and 2 lesson records in total.`
1. Enter `find ben`, then `summary`. Expected: the same totals, with only Ben displayed as student 1. Enter `flag 1`, `flagged` and `summary`: the totals still cover both students. `summary 1` gives the summary usage without changing this filter.
1. Enter `list`, then `editlesson 1 1 date/09/10/2026`. Expected: Amy's latest date becomes `10/10/2026`. Enter `deletelesson 1 1` twice: the count decreases to zero and her card shows `No lessons yet`. `summary` reports 2 students and 0 lesson records.
1. Repeat with a student whose records all lack dates, using the date-removal procedure in [Lesson dates and data saved by v1.3](#lesson-dates-and-data-saved-by-v1-3). Expected: the records count in the totals and the card shows `N lessons (undated)` (singular for one). Adding a dated lesson keeps the old records and supplies the last date.

### Searching lesson histories

Before launching, prepare an isolated test folder with `data/addressbook.json`
containing `{"persons": []}`. Add Amy Tan and Ben Lim, then use `loglesson` to record `Algebra` for Amy
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
loglesson 1 c/Algebra date/20/10/2026
flag 1 r/Weak in algebra
```

1. Enter `find amy`, then `edit 1 p/91234567 l/Sec 4`.<br>
   Expected: `Edited student: Amy Tan; Phone: 91234567; Address: 21 Example Street; Level: Sec 4; Subject: E-Math`. The full list returns with Amy and Ben, and the details panel shows Amy with her flag banner and her `20/10/2026` Algebra record.
1. Enter `edit 2 n/amy tan p/91234567`.<br>
   Expected: `This student already exists in the list.`, because the name differs from Amy's only by case and the phone is the same. Ben is unchanged.
1. Try `edit 1`, `edit 3 n/Cara Ong`, `edit 1 a/1 New Road` and `edit 1 p/123`.<br>
   Expected, in order: `At least one field to edit must be provided.`, `The student index provided is invalid.`, `Invalid command format!` followed by the edit usage, and `Phone numbers should be exactly 8 digits.`. No student changes.
1. Enter `edit 1 n/Amelia Tan addr/5 New Road`, then exit and restart from the same folder.<br>
   Expected: Amelia Tan is shown with `5 New Road`, phone `91234567`, level `Sec 4`, the flag and the Algebra record. The edit was saved.

### Phone whitespace in add and edit

Use a separate test folder with one fictional student, Amy Tan, at index 1.

1. Enter `edit 1 p/8123 0001`, then repeat with the internal space replaced by
   U+00A0 (non-breaking space) and U+202F (narrow non-breaking space). Paste the
   actual characters, not the literal `\u00a0` or `\u202f` text.
   Expected: each edit succeeds and the details panel shows `81230001`.
1. Try seven or nine digits, `+65`, a hyphen, a letter and U+200B (zero-width space)
   inside a phone, using both `add` and `edit`.
   Expected: the phone constraint message; students, lessons, flags and saved data
   are unchanged. Unicode whitespace does not make other non-digit characters valid.
1. Try adding `amy tan` with `8123` and `0001` separated by U+00A0.
   Expected: the duplicate-student error; changing whitespace does not create a new identity.

### Deleting a student

Use another isolated test folder. Before launching, create `data/addressbook.json` containing `{"persons": []}`; a missing file would load sample students. Launch with `java -jar /absolute/path/to/TutorLah.jar`, then enter these commands in order:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
loglesson 1 c/Algebra
loglesson 2 c/Calculus
```

1. Try `delete 0`, `delete 3` and `delete abc`. Expected: `The student index provided is invalid.` for the first two, then `Invalid command format!` followed by the delete usage. Both students and their lesson records are unchanged.
1. Enter `find ben`, then `delete 1`. Expected: confirmation beginning `Deleted student: Ben Lim;` with Ben's saved details. Ben and his Calculus record are deleted. The filter remains and shows no students; Amy and her Algebra record are kept.
1. Enter `list`, then `view 1`. Expected: `Listed 1 student.`, with Amy numbered 1, followed by her single dated Algebra record.
1. Exit and restart from the same folder. Confirm Amy and her record remain, then enter `delete 1` and `list`. Expected: Amy and her record are deleted, and the list reports `No students found. Use add to create a student.`.
1. Exit and restart again, then enter `list` and `delete 1`. Expected: the saved list is still empty (no sample students reappear), and deletion gives `The student index provided is invalid.`.

### Flagging students for follow-up

Use a separate test directory containing two fictional students. Enter the following commands in order:

1. Enter `flag 1`, then `flag 2 r/Missed homework twice`. Expected: both cards show a red flag. After each command, the details panel opens that student and shows `Flagged (no reason given)` for student 1 or `Missed homework twice` for student 2.
1. Enter `flag 2 r/Call parent about a/level worksheet`, then repeat the command. Expected: the literal `a/level` reason replaces the old reason; the repeated command reports that the student is already flagged with that reason and makes no change.
1. Enter `flagged`, then `unflag 1`. Expected: both flagged students initially appear in their original order; after unflagging, the first student leaves the filtered list but remains open without a flag. Enter `list` to confirm both students still exist and only the second is flagged.
1. Try an empty `r/`, two `r/` prefixes, a 101-character reason, `flag 0`, `unflag 01`, and `flagged extra`. Expected: each command reports the corresponding error without changing any flag.
1. Enter `edit 2 l/Sec 4`, `loglesson 2 c/Algebra`, `deletelesson 2 1`, and `flagged`, then exit and restart. Expected: before exiting, only student 2 is displayed. After restarting, both students appear, with only student 2 still flagged with the same reason. Enter `unflag 2` and restart again; the student remains unflagged.

### Saving data

Use a separate disposable folder for each scenario. For the concurrent-copy and failed-save checks, prepare `data/addressbook.json` containing `{"persons": []}`, launch TutorLah, and add Amy Tan and Ben Lim using the two add commands in [Student and lesson summaries](#student-and-lesson-summaries-2). Their successful additions create the saved fixture.

#### Preventing concurrent copies

1. Leave the first copy open and launch the same JAR again from that working folder. Expected: the second copy reports that another copy is using the data folder, exits without a main window, and leaves the student file unchanged.
1. In the first copy, enter `loglesson 1 c/Concurrent copy check` and `exit`. Relaunch and use `list` and `view 1`. Expected: both students and Amy's lesson remain. A leftover `data/.tutorlah.lock` file does not block a new session once the first process releases its lock.

#### Recovering from a failed save

1. Enter `find amy`. While the app runs, move `data/addressbook.json` to `data/addressbook.saved.json` and create an empty directory named `data/addressbook.json` at the old location.
1. Enter `delete 1`. Expected: a save error, with Amy still displayed as student 1 and the backup file unchanged with both students.
1. Remove the empty directory and restore the saved file to its original name. Enter `list`: both students remain; the read-only command has not persisted the failed deletion.
1. Enter `find amy`, `delete 1` and `list`. Expected: Amy is deleted once, only Ben remains, and restarting retains him.

Where folder permissions can be enforced (for example on a POSIX filesystem), make this disposable `data/` directory read-only and enter `loglesson 1 c/Permission test`. Expected: the error identifies `addressbook.json`, not a `.tutorlah-` path; the student data, selection and saved bytes remain unchanged, with no temporary files left behind. Restore permissions before retrying or deleting the fixture. Record this check as skipped if the account can bypass those permissions.

#### Missing or corrupted files

1. Start in a fresh folder without `data/addressbook.json`. Expected: sample students and no warning. `list` leaves the student file absent; `loglesson 1 c/Recovery check` creates it. Exit and back up the complete file.
1. Remove a closing brace from the working file and restart. Expected: a warning naming the file, a short JSON syntax reason with line/column and repair/restart instructions. The list is empty and saving is disabled. No JSON contents, `[Source: ...]` text or internal class names appear.
1. Try `list`, `find amy`, `flagged` and `help`. Expected: read-only commands work, Help opens, and the file remains byte-for-byte unchanged. Try the Amy add command from the summary fixture: it gives the saving-disabled error, the model remains empty, and `exit` leaves the file untouched.
1. Restore the valid backup and restart. Expected: the saved students and Recovery check lesson return without a warning, and a new lesson saves normally.
1. With TutorLah closed, change one stored phone to `12` and restart. Expected: the same protection, with the warning containing `Reason: Phone numbers should be exactly 8 digits.` Restore the backup before continuing.
1. Restart with a valid `{"persons": []}` file. Expected: an empty list without a warning, and successful saving when the Amy add command is entered.

### Importing and repairing student data

Work on disposable copies of `data/addressbook.json` with TutorLah closed; keep a valid backup for restoring each invalid-file scenario.

1. Give a fictional student lessons dated `2026-10-20` then `2026-10-10`, plus undated and repeated same-day records. Restart and enter `view 1`. Expected: newest dates first, undated records last and no lost occurrences. For ties, the last saved occurrence appears first. Read-only commands leave the file unchanged.
1. Enter `loglesson 1 c/Mid date/15/10/2026` and `view 1`: Mid appears between the dated records. Use `editlesson` and `deletelesson` with the displayed lesson numbers, then restart. Expected: the remaining lessons and flag are preserved.
1. Give two students the same phone and names `Amy Tan` and ` Amy  Tan `. Restart: the duplicate-person warning opens an empty protected session without rewriting the file. Restore the fixture and give them different phones: both load with normalized names.
1. Change a saved date to `1999-12-31` and restart. Expected: protected recovery with a reason mentioning `2000-01-01` and `YYYY-MM-DD`. A date of `2000-01-01` loads; undated records and existing future dates also load. Lesson-writing commands still reject future dates.
1. Close both copies of TutorLah. Copy a valid file into `data/addressbook.json` in a fresh destination folder, backing up any destination file first. Launch there and use `list` and `view` to verify students, lessons and flags.

### Deleting lesson records

Use a separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching. Start with `java -jar /absolute/path/to/TutorLah.jar`, then enter:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
loglesson 1 c/Algebra date/18/10/2026
loglesson 1 c/Geometry date/19/10/2026
loglesson 1 c/Algebra date/20/10/2026
loglesson 2 c/Calculus date/20/10/2026
```

1. Enter `view 1`.<br>
   Expected: Amy's records appear as `1. 20/10/2026: Algebra`,
   `2. 19/10/2026: Geometry`, `3. 18/10/2026: Algebra`.
1. Enter `deletelesson 1 1`, then `view 1`.<br>
   Expected: `Deleted lesson 1 for Amy Tan (20/10/2026): Algebra`, followed by
   Geometry as lesson 1 and the older Algebra as lesson 2. Only the newest duplicate
   was removed, and the confirmation identifies its date.
1. Enter `deletelesson 1 2`, then `view 1`.<br>
   Expected: `Deleted lesson 2 for Amy Tan (18/10/2026): Algebra`, then only Geometry
   remains. Amy's details and Ben's Calculus record are unchanged.
1. Try `deletelesson 1 0`, `deletelesson 1 01`, `deletelesson 1 2`, `deletelesson 0 1`, `deletelesson`, `deletelesson 1` and `deletelesson 1 1 extra`.<br>
   Expected: the relevant lesson-index, student-index or usage error appears. Amy's remaining Geometry record is unchanged after every rejected command.
1. Exit and restart, enter `view 1`, then `deletelesson 1 1` and `view 1`.<br>
   Expected: Geometry survived the restart, its deletion succeeds, and Amy remains present with `No lessons logged yet for Amy Tan.`. Restart again to confirm the empty history was saved.
1. Enter `deletelesson 1 1` while Amy's history is empty.<br>
   Expected: `No lessons logged yet for Amy Tan.`, with both students and saved data unchanged.
1. Enter `find ben`, then `deletelesson 1 1` and `view 1`.<br>
   Expected: `Deleted lesson 1 for Ben Lim (20/10/2026): Calculus`, while only Ben's
   card remains displayed. The view reports `No lessons logged yet for Ben Lim.`.
   Enter `list` to confirm both students remain; deletion retained the filter and
   no unrelated student changed.

For a legacy record without a date, the confirmation uses `(undated)` once, for
example `Deleted lesson 1 for Amy Tan (undated): Algebra`.

Automated coverage is in `DeleteLessonCommandParserTest`, `DeleteLessonCommandTest`, `PersonTest`, `LessonPersistenceTest`, `AddressBookParserTest` and the command-box deletion case in `PersonListPanelTest`. `FindCommandIntegrationTest` checks typed search followed by lesson deletion, retaining the filter and saving all students. Persistence tests use temporary directories.

### Editing lesson records

These steps need today's date to be 23/10/2026 or later. Use a separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching. Start with `java -jar /absolute/path/to/TutorLah.jar`, then enter:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
loglesson 1 c/Algebra date/19/10/2026
loglesson 1 c/Vectors date/21/10/2026
loglesson 1 c/Trigonometry date/23/10/2026
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
1. Try `editlesson 1 1`, `editlesson 1 4 c/Algebra` and `editlesson 1 1 date/` followed by tomorrow's date.<br>
   Expected: in order, `At least one field to edit must be provided.`, `The lesson index provided is invalid.` and `Lesson dates cannot be in the future. Log a lesson on or after the day it is taught.` Amy's records are unchanged.
1. Exit and restart from the same folder, then enter `view 1`.<br>
   Expected: the edited content, dates and order were saved.

### Finding students and restoring the full list

Use a separate test directory and fictional data only. Before launching, create `data/addressbook.json` inside that directory containing `{"persons": []}`. A missing file loads sample students, so this valid empty file is needed for the indexes below. Start with `java -jar /absolute/path/to/TutorLah.jar`, then add these students in order:

```text
add n/Tan Wei Ming p/81230001 addr/10 Example Street l/Sec 3 sub/E-Math
add n/Tan Wei Jie p/81230002 addr/20 Example Street l/Sec 4 sub/A-Math
add n/Alicia Lim p/81230003 addr/30 Example Street l/JC1 sub/H2 Math
```

1. Enter `find WEI`. Expected: only the two Wei students, numbered 1 and 2, with `Found 2 students.`.
1. Enter `find ming tan`. Expected: only `Tan Wei Ming`, with `Found 1 student.`. Both keywords must occur in the name, ignoring case and keyword order.
1. Enter `find ali`. Expected: only `Alicia Lim`, numbered 1, even though the previous search hid her.
1. Enter `find` without keywords. Expected: usage feedback with the displayed list unchanged. The failed command remains in the command box.
1. Enter `find unknown`. Expected: no cards and `No matching students found. Use list to show all students. To search by subject or level, use sub/ or l/, e.g. find sub/Physics.`. The hint appears because the search used names only; `find sub/chemistry` gives the message without it. The empty panel offers `list`.
1. Enter `find sub/math`. Expected: all three students, with `Found 3 students.`, because `E-Math`, `A-Math` and `H2 Math` all contain `math`.
1. Enter `find tan l/4`. Expected: only `Tan Wei Jie`, combining a bare name keyword with a level.
1. Enter `find sub/math sub/h2`. Expected: `Multiple values specified for the following single-valued field(s): sub/`, then the hint to reword prefix text, and the previous results stay displayed. Enter `find l/`. Expected: the level constraint message, also without changing the displayed list.
1. Enter `list`. Expected: `Listed 3 students.` and all three students in their original order, with Alicia numbered 3.
1. Enter `find WEI`, then exit and restart. Expected: all three students return; searching did not remove hidden students or persist the filter.

### Lesson-history command sequences

Use another separate test directory with `data/addressbook.json` containing `{"persons": []}`, prepared before launching as above. Start with `java -jar /absolute/path/to/TutorLah.jar`. The app stores data in the working directory; keep real student data out of this directory. Add these fictional students in order:

```text
add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
```

1. Enter `view 1`. Expected: `No lessons logged yet for Amy Tan.` and Amy's card highlighted, even though her history is empty.
1. Enter `loglesson 1 c/Algebra`, `loglesson 1 c/Geometry`, then `loglesson 1 c/Algebra`. Enter `view 1`. Expected: `Lesson records for Amy Tan (3):`, followed by Algebra, Geometry and Algebra, each with today's date. Both Algebra occurrences remain.
1. Enter `loglesson 2 c/Calculus`. Type `loglesson 2 c/unfinished` without pressing Enter, then press Ben's card. Expected: Ben's single Calculus record appears and `loglesson 2 c/unfinished` stays in the command box. Press Amy's card: her three records appear.
1. Enter `view 999`, then `view abc`. Expected: the student-index error, then usage feedback, each shown as an error with the command kept for correction. The details panel still shows the previous student.
1. Enter `find ben`, then `view 1` and press Ben's card. Expected: both show Ben's Calculus record while only Ben is displayed. Enter `list`, then `delete 1` to remove Amy. Ben becomes student 1; `view 1` and a single press on his card still show only his Calculus record. `view 2` reports an invalid student index.
1. Prepare a separate fictional student with 40 lesson records, including a 300-character record. View the student and scroll to the bottom. Expected: the header shows count 40, the records are newest first, the long content wraps, and row 40 is reachable.

### Student details panel

Use a separate test folder and fictional students only.

1. Before launching, create `data/addressbook.json` containing `{"persons": []}`, so that TutorLah starts with no students instead of the sample data. Launch TutorLah. Expected: no card is highlighted, and the details panel shows `No student selected.`, the hint `Add a student first. Their details and lessons will show here.` and `add n/NAME p/PHONE addr/ADDRESS l/LEVEL sub/SUBJECT`.
1. Add two students in this order, so that Amy is student 1 and Ben is student 2:

   ```text
   add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math
   add n/Ben Lim p/81230012 addr/12 Example Street l/JC1 sub/H2 Math
   ```

   Expected: after the first `add`, the hint changes to `Click a student, or type view INDEX, to see their details and lessons here.` with `view 1`. No student opens in the panel.
1. Enter `view 2`. Expected: the panel shows Ben's initials, name, level and subject chips, phone and address, `Lessons (0)` and the hint `loglesson 2 c/Topic covered`. Ben's card is highlighted.
1. Enter `loglesson 1 c/Algebra date/1/10/2026`, then `loglesson 1 c/Vectors date/8/10/2026`. Expected: the panel switches to Amy after the first command and lists `1. 08/10/2026 Vectors` and `2. 01/10/2026 Algebra` after the second, without another `view`.
1. Enter `flag 2 r/Call parent`. Expected: the panel switches to Ben, with a red flag beside his name and the banner `Call parent`. His card shows a red flag icon.
1. Enter `flagged`, then `unflag 1`. Expected: the displayed list becomes empty, but the panel still shows Ben, now without the flag or banner.
1. Enter `list`, then `view 1`, then `find ben`. Expected: Amy is hidden by the search, so the panel returns to `No student selected.`.
1. Enter `find nobody`. Expected: the list is empty, and the panel's hint is `No students are displayed. Type list to show all students.` with `list`, not `view 1`. Enter `flagged` while no student is flagged: the same hint appears.
1. Enter `list`, `view 1`, then `edit 1 n/Amelia Tan`. Expected: the panel shows `Amelia Tan` and keeps her lessons. Enter `view 9`: the result box turns red with the student-index error, and the panel still shows Amelia.
1. Click Amelia's card, then press the Down arrow. Expected: Ben's card is highlighted and the panel shows Ben. Press Up: both return to Amelia.
1. Resize the window to its minimum width and drag the vertical divider both ways. Expected: long names and lesson content wrap, and nothing overlaps or is cut off.
1. Shrink the window to its minimum height of 420 pixels. Expected: the command box, result box, student list, details panel and status bar all remain visible, and the result box, list and panel scroll instead of cutting off text.
1. Enter `view 1`, then `delete 1`. Expected: the panel returns to `No student selected.` with `view 1`, because Ben is now student 1.
1. Enter `delete 1` again. Expected: the list is empty, and the panel's hint asks you to add a student first, with the `add` format.

### Lesson dates and data saved by v1.3

Use a separate test folder whose `data/addressbook.json` contains `{"persons": []}`, and add one fictional student with `add n/Amy Tan p/81230011 addr/11 Example Street l/Sec 3 sub/E-Math`. `INDEX` below is 1.

1. Enter `loglesson INDEX c/Vectors`, then `loglesson INDEX c/Surds date/DD/MM/YYYY` and `loglesson INDEX c/Matrices date/DD/MM/YYYY`, both with yesterday's date, and view the student.
   Expected: `Vectors` is listed first with today's date, then `Matrices` and `Surds` with yesterday's date. `Matrices` comes before `Surds` because it was logged later on the same date. Enter `deletelesson INDEX 3`: `Surds` is deleted.
1. Try `date/` with tomorrow's date, `31/02/2026`, `2026-10-20`, `1/1/-2026`, `31/12/1999` and an empty value.
   Expected: tomorrow gives `Lesson dates cannot be in the future. ...`, and the others give `Lesson dates should be real dates from 01/01/2000 onwards, ...` followed by `If "date/" was part of your lesson notes, please reword it, for example "date and time".` No lesson is added.
1. Enter `loglesson INDEX c/Worksheet collected c/o Mum` and `loglesson INDEX c/Revised date/time conversions`.
   Expected: the first gives the repeated `c/` error with the hint to reword prefix text, and the second gives the date error with the `date/` hint. No lesson is added. Then enter `loglesson INDEX c/Worksheet collected care of Mum`: the lesson is logged as typed.
1. Enter `findlesson INDEX from/31/02/2026`.
   Expected: only the lesson-date message, without the `date/` notes hint.
1. Log a lesson for a test student, exit and restart.
   Expected: `view` shows the lesson with the same date, and the data file stores it as `"date" : "YYYY-MM-DD"`.
1. Exit, keep a copy of the data file, and remove the `date` field from a lesson that is not already the oldest, as in a file saved by v1.3. Ensure that the student also has multiple dated lessons, including two with the same date, and at least one other lesson without a date. Start the app and view the student.
   Expected: every undated lesson appears after all dated lessons. Dated lessons are newest first; among lessons on the same date and among undated lessons, the record saved later appears first. The lesson whose date was removed appears within the undated group according to that order, rather than necessarily last. No lesson is lost.
1. Exit, and change a lesson's date to `18/10/2026` in the data file. Start the app.
   Expected: a warning says `The student data file could not be loaded:` with the file's location and `Reason: Lesson dates should be real dates from 2000-01-01 onwards, in the format YYYY-MM-DD.` TutorLah starts with no students and saving is disabled. Close TutorLah, restore your copy and restart.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Effort**

**Difficulty compared with AB3**

* **Two linked entity types.** AB3 manages one entity type, a contact with tags. Each student owns a dated, ordered history of lesson records with its own commands (`loglesson`, `view`, `editlesson`, `deletelesson`, `findlesson`). Lessons are addressed by a second, newest-first index while being stored oldest first, so every lesson command, search result and the details panel must use the same numbering, including after a lesson is edited onto another date.
* **Dates.** Lesson dates are parsed strictly, cannot be in the future when entered through `loglesson` or `editlesson`, default to today through an injected `Clock`, and must stay ordered when logged, edited or loaded. Records saved by v1.3 have no date and still load.
* **Data safety.** AB3 overwrites its data file directly. TutorLah writes to a temporary file and replaces the original atomically, rolls back the student data and filter if a save fails, refuses to save after a file fails to load so the original is preserved, and uses an operating-system lock so that two copies cannot use the same data folder.
* **Input from real users.** Commands accept Unicode spaces copied from documents, command words ignore case, names such as `Muthu a/l Raju` work because the address prefix became `addr/`, and errors explain prefix-like text inside values.
* **A richer GUI.** A details panel follows the student that each command acts on, card clicks run `view` through Logic, and the window can shrink to fit a 1280x720 screen at 150% scaling.

**Effort**

* Production Java: 4,016 lines added and 687 removed across 78 files under `src/main/java` since the fork (`git diff --shortstat`, including comments and blank lines).
* Test code: 10,516 lines added across 105 files under `src/test`. `@Test` methods grew from 239 to 769, plus 14 `@TestFactory` methods.
* Commands: 9 new commands, 1 AB3 command (`clear`) removed, and `add`, `edit`, `find`, `list` and `delete` adapted to students.
* Process: 112 merged pull requests, most reviewed by a teammate, and an alpha test that led to further fixes before v1.5.

**Reuse**

`find` was adapted from AB3, and AI tools assisted parts of the implementation, tests and documentation, as listed under [Acknowledgements](#acknowledgements). The lesson, flag, summary, recovery and locking features are new.

**Achievements**

* About 97% line coverage, with save failures, file recovery and concurrent launches tested automatically.
