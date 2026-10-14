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

## Student-card layout checks

`PersonListPanelTest` renders the real panel with the application stylesheets. It checks address wrapping,
row-height recovery after resizing, maximum-length fields (including unbroken addresses), scrolling,
selection, and displayed indexes after additions and deletions. On Linux, run JavaFX tests with a virtual
display: `xvfb-run -a ./gradlew test`. The CI workflow uses this setup automatically.

To check the GUI manually, use a separate copy of the application with disposable data:

1. Add a short student followed by this wrapping example:

   ```text
   add n/Alicia Tan p/81234567 a/10 Short Street l/Sec 2 sub/E-Math
   add n/Charlotte O'Brien p/81234568 a/Block 1234567890 Sunset View Avenue Unit 999999999999 Singapore 999999 l/Upper Secondary Two sub/Higher Order Thinking in Additional Mathematics
   ```

2. Resize the window from wide to its minimum supported width and back several times. Charlotte's card
   should grow only when fields wrap, then return to its compact height. The short card should stay compact.
3. Add boundary-length data: a 100-character name, 200-character address (with and without spaces),
   20-character level, and 50-character subject. All text and the student index should remain readable.
4. Add enough students to scroll. Scroll down and back, select short and long cards, and resize again.
   Fields must not overlap, and the space below the subject should contain only the normal bottom padding.
5. Run `delete 1` and add another student. Check that the remaining cards and numbered indexes refresh.

Checked on 14 October 2026 with Java 25 on macOS: the real `MainWindow` was rendered using temporary
student data at window widths of 740 and 450 pixels. Charlotte's row changed from 98 to 115 pixels and
returned to 98 when widened. Bottom spacing remained 5 pixels, or 6 with the selection border.
Maximum-length fields and a `delete 1` command were also checked. These checks did not use the normal
saved student data. OS display-scaling checks still need to be repeated on the target platforms.
