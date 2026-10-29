---
  layout: default.md
  title: "Setting up and getting started"
  pageNav: 3
---

# Setting up and getting started

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Setting up the project on your computer

<box type="warning" seamless>

**Caution:** Follow the steps below precisely. The setup may fail if you skip or change a step.
</box>

First, **fork** this repo, and **clone** the fork into your computer.

Install **JDK 25** before importing the project. TutorLah's build and CI use Java 25. The JavaFX dependency version in `build.gradle` is separate from this JDK requirement.

If you plan to use IntelliJ IDEA (highly recommended):

1. **Configure the JDK**: Follow the guide [_[se-edu/guides] IDEA: Configuring the JDK_](https://se-education.org/guides/tutorials/intellijJdk.html). Set the **Project SDK** to **JDK 25** and let the modules inherit that SDK.
1. **Import the project as a Gradle project**: Follow the guide [_[se-edu/guides] IDEA: Importing a Gradle project_](https://se-education.org/guides/tutorials/intellijImportGradleProject.html) to import the project into IDEA.
   <box type="warning" seamless>
   Note: Importing a Gradle project is slightly different from importing a normal Java project.
   </box>
1. **Check the Gradle JVM**: In **Settings > Build, Execution, Deployment > Build Tools > Gradle**, select **JDK 25** for the Gradle JVM, or the Project SDK if it resolves to JDK 25. Reload the Gradle project after changing it. See [IntelliJ's Gradle JVM guidance](https://www.jetbrains.com/help/idea/gradle-jvm-selection.html).

Verify the setup from a terminal in the project root, whether or not you use IntelliJ:

1. Run `java -version`, `javac -version` and `./gradlew --version` (Windows PowerShell: `.\gradlew.bat --version`). Confirm that Java, the compiler and Gradle's JVM use version 25. If they differ, set `JAVA_HOME` to your JDK 25 installation and put its `bin` directory on `PATH`; also recheck IntelliJ's Gradle JVM for builds started in the IDE.
1. Run `./gradlew clean check coverage` (Windows PowerShell: `.\gradlew.bat clean check coverage`). This runs the tests, Checkstyle and coverage report. See the [testing guide](Testing.md) for other test options.
1. Run `./gradlew run` (Windows PowerShell: `.\gradlew.bat run`) and try a few commands. To run from IntelliJ instead, use the `seedu.address.Main` entry point with `-ea` in its VM options to enable assertions.

If the build reports `invalid source release: 25`, check the Gradle JVM and terminal Java settings above before retrying.

--------------------------------------------------------------------------------------------------------------------

## Before writing code

1. **Configure the coding style**

   If using IDEA, follow the guide [_[se-edu/guides] IDEA: Configuring the code style_](https://se-education.org/guides/tutorials/intellijCodeStyle.html) to set up IDEA's coding style to match ours.

   <box type="tip" seamless>

   **Tip:**
    Optionally, follow the guide [_[se-edu/guides] Using Checkstyle_](https://se-education.org/guides/tutorials/checkstyle.html) to learn how to use Checkstyle in IDEA, for example to report problems _as_ you write code.
   </box>

1. **Set up CI**

   This project includes GitHub Actions configuration files in `.github/workflows`. GitHub runs CI automatically for every push and pull request. No setup is required.

1. **Learn the design**

   When you are ready to start coding, we recommend that you get some sense of the overall design by reading about [AddressBook’s architecture](DeveloperGuide.md#architecture).

1. **Do the tutorials**
   These tutorials will help you get acquainted with the codebase.

   * [Tracing code](https://se-education.org/guides/tutorials/ab3TracingCode.html)
   * [Adding a new command](https://se-education.org/guides/tutorials/ab3AddRemark.html)
   * [Removing fields](https://se-education.org/guides/tutorials/ab3RemovingFields.html)
