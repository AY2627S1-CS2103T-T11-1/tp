# TutorLah UML guide

How the team draws and checks UML diagrams for the Developer Guide (DG), so that every diagram meets the course's expectations and looks consistent. Read this before any DG diagram task.

Last updated: Oct 23, 2026, after #133 and #134 merged, for the v1.5 DG diagram tasks (#128 to #132).

**Owner:** aerodart. Sources are linked in each section. Where this guide and a course page disagree, the course page wins.

---

## 1. What the course expects

From the [Week 10 tP tasks][w10], the [DG deliverable][dg] and the [tP progress dashboard][dashboard].

* **Each member updates at least one UML diagram** to match their own changes. This is a regular weekly task, due Friday 16:00. Catching up within a few days still turns it green. Updating the DG text to match is optional at this stage.
* **The dashboard's "UML Updated" item** counts at least 3 changed lines in `*.puml` files during week 10.
* **PlantUML sources go in `docs/diagrams`**, and the explanations in `docs/DeveloperGuide.md`, so the grading scripts can attribute them.
* **Diagrams must match the latest release.** Remove or update anything that describes AB3 rather than TutorLah.
* **Use more than one UML diagram type.** The DG tips say to "try to include UML diagrams of multiple types" (see section 5).

### Keep diagrams simple

The course's DG tips say diagrams should be "comprehensible, not necessarily comprehensive".

* Omit minor classes, private members, self-calls and parameters. Write `foo(...)` when parameters are left out, or use pseudocode such as `save data in file`.
* Leave out exception handling, lambdas and anonymous methods. If an omission could mislead, say so in a UML note.
* Show a few representative classes instead of many similar ones, as AB3 does with `XYZCommand`.
* Give each diagram one purpose. A sequence diagram shows the internals of **at most one component**, and treats the others as black boxes.
* Split large diagrams: subcomponents as black boxes, `ref` frames for sequence diagrams, rakes for activity diagrams.
* Show associations as lines and arrows, not only as fields.
* Place each diagram next to the text that explains it, and size it so its text matches the body text.

### How diagrams are graded

From [Possible DG Bugs][grading] and the [PE guidelines][pe]:

* **DG bugs include:** notation that is incorrect or not as taught in the course, a non-UML diagram where UML would work, a diagram unsuited to its purpose, and a diagram that is too complicated. For architecture diagrams: unintuitive symbols, careless double-headed arrows, and low-level detail.
* **Notation errors can be `severity.Low` or higher,** because they hurt the diagram's credibility.
* **Tool variations are fine** if they do not contradict the textbook's notation. Leaving out optional notation is fine **if it is consistent.** For example, drawing activation bars in some places but not others is a bug.
* **Text too small to read** is usually a sign of too much detail. It counts as `severity.VeryLow`.

---

## 2. Notation checklist

From the textbook's [UML chapter][modeling]. Check every diagram against the parts that apply.

### Class diagrams

* **Classes:** attributes above operations, each in one compartment. Either compartment can be left out if it does not help.
* **Visibility:** `+` public, `-` private, `#` protected, `~` package private. UML has no default visibility or multiplicity, so leaving them out means "not specified".
* **Associations:** a solid line. An arrowhead shows navigability (who holds the reference). Show an association as a line **or** as an attribute, never both.
* **Multiplicity:** `0..1`, `1`, `*`, `n..m` or `1..*`, at the far end of the association.
* **Composition:** a filled diamond at the whole's end, e.g. `Person` owns its `Lesson`s.
* **Aggregation:** a hollow diamond. The textbook notes it often adds confusion, so prefer a plain association.
* **Dependency:** a dashed arrow, **only** when no association or inheritance already implies it.
* **Inheritance:** a solid line with a hollow triangle at the parent. **Interface implementation:** the same, with a dashed line. Mark interfaces `<<interface>>` and enumerations `<<enumeration>>`.
* **Abstract classes and methods:** `{abstract}` (preferred) or italics. **Class-level members:** underlined.
* **Notes:** can attach to an element or stand alone. Put constraints in `{ }`.

### Object diagrams

* Name objects `name:Class` (or `:Class`), **underlined**.
* Show attributes as `attribute = value`, with no types or visibility. No operations compartment.

### Sequence diagrams

* Participants are `:Class` or `name:Class`, **not underlined**.
* Calls are **solid** arrows with filled arrowheads. Returns are **dashed** arrows.
* An activation bar starts when the call arrives and ends at the return. It must not start early, run past the return, or break in the middle.
* A constructor arrow ends at the side of the new object's box. Mark the point an object is no longer referenced with an `X` at the end of its lifeline.
* Self-calls get a nested activation bar. Unroll chained calls such as `a.getB().doC()` into separate steps first.
* Frames: `alt` (at most one branch runs), `opt`, `loop`, `par`, and `ref` to move part of the interaction into its own diagram.
* A call to a static method goes to a participant marked `<<class>>`.
* Activation bars and return arrows may be left out to reduce clutter, if that causes no ambiguity and is done consistently across the diagram.

### Activity diagrams

See the textbook's [activity diagram sections][activity] for linear, alternate and parallel paths, rakes and swim lanes.

---

## 3. Our PlantUML setup

From the SE-EDU guide [Using PlantUML][plantuml] and the files in `docs/diagrams`.

### Every diagram

* Start with `@startuml`, then `!include style.puml`, and end with `@enduml`.
* **Use each component's color from `style.puml`:** UI green (`UI_COLOR`), Logic blue (`LOGIC_COLOR`), Model red (`MODEL_COLOR`), Storage gold (`STORAGE_COLOR`), and black for the user (`USER_COLOR`). `_T1` to `_T4` are lighter to darker shades.
* **Use the real Java class names**, such as `Person`, even where the DG text says "student".
* **Embed it in the DG** with `<puml src="diagrams/Name.puml" width="450" alt="..." />`. Adjust `width` so the diagram text is about the size of the body text, and write `alt` as a short description of what the diagram shows, as #134 does.

### Things `style.puml` does that catch people out

* **`hide members`** hides every attribute and method. To show them for one class, add `show Lesson members`.
* **PlantUML shows visibility as colored icons by default.** Add `skinparam classAttributeIconSize 0` to get the textbook's `+`, `-`, `#` and `~` symbols.
* **`hide circle` and `hide footbox`** remove the class "C" circle and the repeated participant boxes at the bottom of sequence diagrams.
* **Known AB3 bug:** `STORAGE_COLOR_T2` is defined twice and `STORAGE_COLOR_T4` does not exist. Use `STORAGE_COLOR`, `_T1` or `_T3` until it is fixed.

### Sequence diagram house style

Follow `ViewHistorySequenceDiagram.puml` (#134), which the team's newer diagrams share:

* After `!include style.puml`, add these four lines so text size and colors match across diagrams:
    * `skinparam ArrowFontStyle plain`
    * `skinparam DefaultFontSize 16`
    * `skinparam ParticipantFontColor #FFFFFF`
    * `skinparam SequenceBoxFontColor #222222`
* Group participants by component in a lightly shaded box, and color each participant with its component color: `box Logic #F2F2FC` ... `end box`, then `participant ":LogicManager" as logicManager LOGIC_COLOR`.
* **Box tints, one per component:** UI `#F3F8F1` and Logic `#F2F2FC` are in use. Model and Storage need their own tints, proposed as Model `#FBF1F2` and Storage `#FBF7E8`. Never reuse another component's tint, or readers will misread which component a participant belongs to.
* Start from outside with `[-> logicManager : execute("...")` and end with `[<-- logicManager`, or start from an `actor User as user USER_COLOR`.
* Either draw activation bars everywhere in a diagram or nowhere. If you draw them, pair every `activate` with a `deactivate`.
* Use `create` before a constructor call and `destroy` after the object's last use. A hidden arrow (`A -[hidden]-> B`) places the `X` below the end of the activation bar.

### Fixing layout

* Arrow direction and length steer the layout: `-down->`, `-right->`, `--->`.
* Hidden links line elements up: `A -right[hidden]- B`.
* Declare classes in the order you want them laid out.
* If a diagram is still a tangle, split it rather than fighting the layout.

### Rendering and checking

* **Locally:** `cd docs`, `npm install`, then `npm run serve` to preview or `npm run build` to check. Class diagrams need Graphviz (`brew install graphviz` on macOS).
* **CI** installs Graphviz and builds the site with MarkBind 7.2.1. `docs/package.json` uses `markbind-cli` 5.1.0 locally, so also check the deployed site after merging.
* **For a quick look while editing,** use the PlantUML plugin in IntelliJ, as described in the SE-EDU guide.

---

## 4. Who owns which diagram (v1.5)

Each task owns its diagrams and the matching DG sections. Change another owner's diagram only after agreeing with them.

| Issue | Owner | Diagrams | Status (Oct 23, 14:30) |
|---|---|---|---|
| #128 | aerodart | `ArchitectureDiagram.puml`, `ComponentManagers.puml` | In progress |
| #129 | srivatsanj27 | `StorageClassDiagram.puml`, `ArchitectureSequenceDiagram.puml` | Merged (#133) |
| #130 | cibichandar | `LogicClassDiagram.puml`, `ParserClasses.puml`. Also removes the undo/redo diagrams and the tag-model example | Not started |
| #131 | fuhlie | `ModelClassDiagram.puml`, and `DeleteLessonSequenceDiagram.puml`, which replaces `DeleteSequenceDiagram.puml` | PR #135 open |
| #132 | zikiai | `UiClassDiagram.puml`, `ViewHistorySequenceDiagram.puml` | Merged (#134) |

Before deleting a `.puml` file, search `docs` for its name to make sure no page still uses it.

---

## 5. Diagram types after v1.5 (for the team to decide)

Removing undo/redo (#130) removes all of the DG's object diagrams (`UndoRedoState0` to `5`) and its only activity diagram (`CommitActivityDiagram`). The DG would then have only class and sequence diagrams, while the course asks for several types. Options:

* **An object diagram** of one `Person` and their `Lesson`s, before and after `deletelesson` or a backdated `log`. This shows date-order storage clearly.
* **An activity diagram** of `log`, from parsing the date through the future-date check to inserting in date order.

---

## 6. Checklist before opening a diagram PR

* [ ] The diagram matches the code on the latest `master`.
* [ ] The notation follows section 2, and optional notation is used consistently.
* [ ] A sequence diagram shows the internals of at most one component.
* [ ] It uses the component colors and the real Java class names.
* [ ] `npm run build` passes in `docs`, and you have looked at the rendered diagram.
* [ ] It sits next to the text that explains it, with a sensible `width` and an `alt` text.
* [ ] Deleted `.puml` files are no longer used anywhere.
* [ ] Reuse acknowledgements in the DG are still accurate.

[w10]: https://nus-cs2103-ay2627-s1.github.io/website/schedule/week10/project.html
[dg]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-deliverables.html
[dashboard]: https://nus-cs2103-ay2627-s1.github.io/dashboards/contents/tp-progress.html
[grading]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-grading.html
[pe]: https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-pe.html
[modeling]: https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html
[activity]: https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html#activity-diagrams-basic
[plantuml]: https://se-education.org/guides/tutorials/plantUml.html
