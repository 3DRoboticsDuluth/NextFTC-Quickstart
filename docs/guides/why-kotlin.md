# Why Kotlin?

Quickstart uses Kotlin for team-owned robot code while continuing to consume the
Java-based FTC SDK and third-party libraries normally. This is a deliberate choice:
Kotlin lets a subsystem express its hardware, state, commands, controls, and
lifecycle together with less ceremony, without abandoning the Android/JVM ecosystem
used by FTC.

Kotlin is not required by FTC, and it does not make robot behavior inherently more
correct. Its value comes from making the intended architecture easier to see and
giving students less repetitive code to maintain during a fast-moving season.

## What It Improves

### Robot Intent Stays Visible

A reusable command can be declared close to the hardware and state it changes:

```kotlin
object Gate : Subsystem() {
    val servo by ServoEx("gate")
    var position = CLOSED

    val open by instant { position = OPEN }
}
```

The declaration says that there is one Gate subsystem, one configured servo, and one
reusable `open` command. The project's delegates add hardware initialization,
subsystem ownership, inferred command naming, and execution logging without requiring
separate hardware, command, and binding classes.

Less code is useful only when the remaining code is still explicit. Quickstart avoids
short forms that conceal command requirements, lifecycle timing, hardware safety, or
units.

### The Language Fits the Robot Model

Quickstart uses a small set of Kotlin features repeatedly:

| Kotlin feature | Robot use |
|---|---|
| `object` | Represents the single physical instance of a subsystem. |
| Properties | Keep configurable values and observable state concise. |
| Delegated properties with `by` | Initialize hardware and infer stable command names such as `Gate.open`. |
| Extension functions and properties | Add typed units and readable operations without wrapping every upstream type. |
| Lambdas | Express small hardware updates and command actions next to their intent. |
| Null safety | Makes absent configuration or hardware-dependent values explicit. |
| Data classes | Define persisted configuration as visible, structured data. |
| Overloads | Let typed distance, angle, and path-progress values select the correct navigation behavior. |

These features support the repository's design; they are not a checklist of syntax
that every student must use. New code should follow an existing local example before
introducing another valid way to express the same idea.

### Java Interoperability Preserves the FTC Ecosystem

Kotlin compiles to JVM bytecode and can call Java directly. TeamCode can therefore use
the FTC SDK, NextFTC, Pedro Pathing, Panels, and vendor device classes without ports or
adapters merely to cross a language boundary. Java source can remain Java when that is
the clearest maintenance choice, especially for third-party code that should stay
recognizable against its upstream documentation.

This interoperability also permits an incremental adoption path. A team can retain a
proven Java class, add new Kotlin subsystems around it, and migrate only when the
change provides real value.

### Concision Helps During a Season

FTC software changes rapidly between mechanical revisions, driver testing, and match
strategy updates. Removing constructor boilerplate, static-holder patterns, and
one-use command classes reduces the number of places that must change together. It
also lets tests describe a subsystem through the same public vocabulary used by
Teleop and Auto.

The objective is not the fewest possible lines. The objective is a small, cohesive
implementation that students can read from top to bottom and safely modify.

## Tradeoffs and Guardrails

Kotlin is flexible: a function can use a block body or expression body, declarations
can span one line or several, and the language offers many advanced abstractions.
That flexibility can make a teaching codebase inconsistent if every contributor uses
a different style.

Quickstart manages that risk by establishing a limited local dialect:

- Keep declarations serving the same role visually consistent.
- Keep simple properties, functions, command lambdas, and argument lists compact.
- Expand code when branching, sequencing, or side effects would otherwise be hidden.
- Group hardware, state, commands, lifecycle methods, calculations, and telemetry.
- Prefer direct expressions over aliases and abstractions that are used only once.
- Add visibility, indirection, reflection, or clever syntax only for a demonstrated
  reason.
- Use typed units where a raw number would make navigation intent ambiguous.
- Keep tests beside the subsystem and treat them as executable examples.

These conventions are summarized in `AGENTS.md` and demonstrated throughout the
neutral Drive, Nav, Auto, and OpMode scaffold.

Kotlin also introduces a build plugin and runtime libraries. Quickstart owns that
setup in the verified reusable base, so a season repository should not need to repeat
the integration. Dependency versions and JVM targets remain recorded in
[Modules and Dependencies](../reference/modules-dependencies.md).

## What Students Need to Learn First

A student does not need the entire Kotlin language before contributing. The most
useful starting sequence is:

1. Read `val`, `var`, functions, classes, objects, and basic nullability.
2. Recognize a lambda such as `{ position = OPEN }` as behavior passed as a value.
3. Learn that `by` delegates part of a property's behavior to a reusable project
   helper.
4. Follow one existing subsystem and its test when adding a mechanism.
5. Learn command composition and typed navigation units when working on Auto.

The [Add a Subsystem](subsystem.md) guide provides the first practical pattern. The
[Commands](../architecture/commands.md) architecture page explains the delegated and
deferred command behavior that is intentionally hidden behind concise declarations.

## Why Not Java-Only?

Java remains capable, supported, and appropriate for FTC. A Java-only team can build
the same robot. Quickstart chooses Kotlin because its singleton objects, property
delegation, extension methods, null safety, lambdas, and data classes closely match
the architecture the project already wants.

The comparison is therefore not “Kotlin can do robotics and Java cannot.” It is that
Kotlin expresses this particular NextFTC architecture with less duplicated ceremony,
while preserving access to Java libraries and source whenever Java is the better
maintenance choice.

