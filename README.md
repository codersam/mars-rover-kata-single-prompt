# Mars Rover Kata (Kotlin, TDD)

A rover on Mars is driven by a Control Tower on Earth. The two talk over a small, explicitly defined
text protocol: Earth sends `CMD` lines and Mars answers each one with an `RPT` line. The planet wraps
around at its edges, rocks stop the rover, and every reply tells Earth where the rover is and what happened.

```
EARTH  ControlTower  ──── CMD 1 FFRFF ────▶  RoverStation  MARS
                     ◀─ RPT 1 1 2 E BLOCKED 2 2 ─
```

The kata was built test-first and follows the [4 Rules of Simple Design](RULES.md).

## Quick start

You need **JDK 25**. The Gradle wrapper is included, so you don't need to install Gradle.

```bash
./gradlew test     # run the 52 tests
./gradlew run      # run the demo mission and print the wire traffic
```

`./gradlew run` prints:

```
Mars  -> Earth: RPT 0 0 0 N OK
Earth -> Mars:  CMD 1 FFRFF
Mars  -> Earth: RPT 1 1 2 E BLOCKED 2 2
Earth -> Mars:  CMD 2 RFFF
Mars  -> Earth: RPT 2 1 4 S OK
Earth -> Mars:  CMD 3
Mars  -> Earth: RPT 3 1 4 S OK
```

In this run, the rover lands at `(0,0)` facing north on a 5×5 planet. It hits a rock at `(2,2)`,
drives south off the bottom edge and comes back in at the top, and finally answers a status ping
(an empty `CMD`).

The HTML test report is written to `build/reports/tests/test/index.html`.

## Where things are

| Package | Contents |
|---|---|
| [`protocol`](src/main/kotlin/org/marsrover/protocol) | Shared vocabulary for both sides: commands, positions, messages, `ProtocolCodec` |
| [`mars`](src/main/kotlin/org/marsrover/mars) | `Planet` (wrapping and obstacles) and `RoverStation` (the rover's end of the link) |
| [`earth`](src/main/kotlin/org/marsrover/earth) | `ControlTower` (mission control) and `Outcome` (the result of each exchange) |
| [`Main.kt`](src/main/kotlin/org/marsrover/Main.kt) | The demo mission shown above |

Tests mirror this layout under [`src/test/kotlin`](src/test/kotlin/org/marsrover).

## Further reading

- [PROTOCOL.md](PROTOCOL.md): the wire protocol, covering grammar, sequence numbers, error handling and a full example exchange.
- [DESIGN.MD](DESIGN.MD): the design decisions, public API and TDD roadmap.
- [HISTORY.md](HISTORY.md): how it was built, cycle by cycle, and where each rule of simple design was applied.
- [RULES.md](RULES.md): the 4 Rules of Simple Design.
