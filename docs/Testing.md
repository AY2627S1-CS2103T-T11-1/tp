---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

Java assertions are enabled for `./gradlew run`. For direct IntelliJ runs, add
`-ea` to the VM options; for a JAR, use `java -ea -jar TutorLah.jar`.

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

## Command whitespace and paste checks

Use a disposable folder and fictional students, not your normal saved data.
Start with an empty student list and add
`add n/Amy Tan p/81230001 addr/1 Example Street l/Sec 3 sub/Math`.

1. Enter `edit 1 addr/New Rd[NBSP]l/Sec 4`, replacing `[NBSP]` with a
   non-breaking space (Option+Space on macOS). Check that the address is
   `New Rd` and the level is `Sec 4`, including after restarting.
2. Enter `loglesson 1[NBSP]c/Algebra` using the same separator. It should log
   `Algebra`, just like `loglesson 1 c/Algebra` with an ordinary space.
3. Copy `loglesson 1 c/Quadratic` and `inequalities` as two lines, then paste into
   the command box. Check that it reads `loglesson 1 c/Quadratic inequalities` and
   nothing is logged until Enter is pressed. Repeat with a tab between the words.
4. Paste `Amy` and `Tan` separated by a tab over the name in `find old name`.
   Check the result is `find Amy Tan`; undo and redo should restore each version.
5. Check `view 01` still fails and repeated prefixes still report an error.
   Addresses and lesson notes with repeated internal spaces or joiners should
   keep those characters after saving and restarting.

`CommandWhitespaceTest` covers parsing and validation; `LogicManagerTest`
checks the saved fields. `PersonListPanelTest` exercises the real command box
and system clipboard using temporary storage, restoring clipboard contents afterwards.

## Student-card layout checks

`PersonListPanelTest` renders the real panel with the application stylesheets. It checks card and detail
text wrapping, row-height recovery after resizing, maximum-length fields, scrolling, selection, and
displayed indexes after additions and deletions. On Linux, run JavaFX tests with a virtual
display: `xvfb-run -a ./gradlew test`. The CI workflow uses this setup automatically.

To check the GUI manually, launch from a separate writable folder containing
`data/addressbook.json` with `{"persons": []}`. Do not use your normal student data.

1. Add a short student followed by this wrapping example:

   ```text
   add n/Alicia Tan p/81234567 addr/10 Short Street l/Sec 2 sub/E-Math
   add n/Charlotte O'Brien p/81234568 addr/Block 1234567890 Sunset View Avenue Unit 999999999999 Singapore 999999 l/Upper Secondary Two sub/Higher Order Thinking in Additional Mathematics
   ```

2. Enter `view 2`. Resize the window between 1000 and its minimum width of 700 pixels several times,
   at any height down to the minimum of 420 pixels. Charlotte's card should grow when its name or level/subject
   wraps, then return to its compact height when widened. Alicia's short card should stay compact.
   Phone numbers and addresses appear in the details panel, not the cards; check Charlotte's full address
   there, scrolling the details panel if needed.
3. Add boundary-length names, addresses (with and without spaces), levels and subjects using the current
   limits in the [User Guide](UserGuide.md#adding-a-student-add). Give each added student a different phone
   number. Check card text and indexes, then `view INDEX` to check the full address in the details panel.
4. Add enough students to scroll. Scroll down and back, select short and long cards, and resize again.
   Fields must not overlap, and the space below the lesson-status line should contain only normal padding.
5. Run `delete 1` and add another student. Check that the remaining cards and numbered indexes refresh.

Rechecked on 28 October 2026 with Java 25 on macOS using a displayed `MainWindow`, the command box and
fictional data in a temporary folder. Both example commands passed. Repeated 1000/700/1000-pixel resizes
preserved full card text, wrapping recovery and bottom padding. Maximum-length fields, spaced/unbroken
addresses in the details panel, scrolling, selection, and indexes after `delete 1` and another add passed.
OS display-scaling and Windows/Linux native-window checks were not repeated.

Historical result (14 October 2026, earlier GUI): at widths of 740 and 450 pixels, Charlotte's row changed
from 98 to 115 pixels and back, with 5-pixel bottom spacing (6 when selected). The current main window
cannot reach 450 pixels; those measurements are not evidence for the current layout.
