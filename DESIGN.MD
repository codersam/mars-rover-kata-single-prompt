# Mars Rover Kata — Design & Implementation Instructions

A Mars Rover on Mars is commanded by a Control Tower on Earth. The two sides talk over an
explicitly defined, line-based text protocol. Implemented in Kotlin, test-first (TDD).

## Guiding rules — 4 Rules of Simple Design (`prompt.md`)

1. **Passes the tests** — every behaviour is driven by a failing test first, including the console demo.
2. **Reveals intention** — domain names: `ControlTower`, `RoverStation`, `Outcome.Delivered`, `Reason.DUPLICATE`.
3. **No duplication** — the protocol vocabulary is defined once and shared by both sides.
4. **Least elements** — the link is a plain function `(String) -> String` (no `Channel` type), no async,
   no extra message types (a ping is an empty `CMD`, landing is `RPT 0`).

## Decisions

| Topic | Decision |
|---|---|
| Grid edges | Wrap around (torus) |
| Obstacles | Rover stops on last free cell, remaining commands skipped, reports `BLOCKED ox oy` |
| Wire format | Line-based text, one message per line |
| Communication | Synchronous request/reply |
| Bootstrap | Rover reports on landing with `RPT 0 x y dir OK`; Earth starts with unknown state |
| Invalid command letter | Whole message rejected (`REJECTED UNKNOWN_COMMAND`), rover does not move |
| Unparseable line | `RPT 0 x y dir REJECTED MALFORMED` |
| Empty command list | Valid — status ping |
| Seq numbers | Tower auto-increments from 1; station accepts only seq strictly greater than the last; any well-formed seq is consumed, even if rejected; replays get `REJECTED DUPLICATE` |
| Tower state | Last known rover state + set of discovered obstacles |
| Tower API | Typed `List<Command>` |
| Bad reply on Earth | Returned as a sealed `Outcome` value, tower state unchanged |
| Tests | `kotlin-test` only (JUnit Platform) |
| Package | `org.marsrover` with sub-packages `protocol`, `mars`, `earth` |
| Demo | Small console demo in `Main.kt`, its transcript covered by a test |
| Protocol spec | `PROTOCOL.md` + codec tests as executable spec |
| Git | `git init`, one commit per green cycle and per refactor |

## Protocol

```
command  := "CMD" SP seq [SP commands]                 ; Earth → Mars
commands := ("F" | "B" | "L" | "R")+                   ; absent = status ping
report   := "RPT" SP seq SP x SP y SP dir SP status    ; Mars → Earth
status   := "OK" | "BLOCKED" SP x SP y | "REJECTED" SP reason
reason   := "MALFORMED" | "UNKNOWN_COMMAND" | "DUPLICATE"
dir      := "N" | "E" | "S" | "W"
seq,x,y  := non-negative decimal integer
SP       := exactly one space; no leading/trailing whitespace; uppercase only
```

- Seq `0` is reserved for Mars (landing report, replies to malformed lines).
- Coordinates: `(0,0)` is bottom-left; `N` = y+1, `E` = x+1; positions wrap on both axes.
- Commands: `F` forward, `B` backward, `L` turn left, `R` turn right.

### Station processing order

1. Line does not match `CMD <seq>[ <token>]` (e.g. `GARBAGE`, `CMD -1`, `cmd 1 F`, `CMD 1 F F`)
   → `RPT 0 … REJECTED MALFORMED`; last seq unchanged.
2. `seq <= lastSeq` (lastSeq starts at 0, so `CMD 0` is always a duplicate)
   → `RPT seq … REJECTED DUPLICATE`; no move.
3. `lastSeq = seq` — the seq is consumed from here on.
4. Any character not in `F B L R` (e.g. `FFXL`, `ffr`) → `REJECTED UNKNOWN_COMMAND`; no move.
5. Execute: on an obstacle, stay on the last free cell, skip the rest → `BLOCKED ox oy`; otherwise `OK`.

Every report carries the rover's current position and heading.

## Packages & public API

### `org.marsrover.protocol` — shared vocabulary (Earth never depends on Mars)
- `Direction` (N, E, S, W) with `left()` / `right()`
- `Position(x, y)`, `Command` (F, B, L, R), `Rover(position, direction)` — immutable
- `sealed Message`: `CommandMessage(seq, commands)`, `Report(seq, rover, status)`
- `sealed Status`: `Ok`, `Blocked(obstacle)`, `Rejected(reason)`; `Reason` enum
- `ProtocolCodec`: `encode(Message)`, `decodeCommand(line)`, `decodeReport(line)`

### `org.marsrover.mars`
- `Planet(width, height, obstacles)` — wrapping, blocking, executing commands
- `RoverStation(planet, rover)` — `landingReport(): String`, `receive(line: String): String`;
  fails fast if the rover lands on an obstacle

### `org.marsrover.earth`
- `ControlTower(link: (String) -> String)`
  - `send(commands: List<Command>): Outcome`, `receive(line: String): Outcome`
  - `lastKnown: Rover?`, `obstacles: Set<Position>`
  - seq starts at 1 and increments on every `send` regardless of outcome; `receive` expects seq 0
  - state changes only on `Delivered`: any delivered report (even REJECTED) updates `lastKnown`;
    `BLOCKED` also records the obstacle
- `sealed Outcome`: `Delivered(report)`, `MalformedReply(line)`, `SeqMismatch(expected, actual)`

### `org.marsrover` — `Main.kt`
- `main()` only prints `demoTranscript()`, which wires a station and a tower over a 5×5 planet with
  one obstacle and returns the wire traffic. The transcript is asserted by a test.

## TDD roadmap

1. `Direction` — turns left and right from every heading
2. `Planet` — F and B per heading, wrapping at all four edges
3. `Planet` — executes a command sequence (turns and moves mixed)
4. `Planet` — obstacle stops execution, skips the rest, reports the obstacle
5. `ProtocolCodec` — encode/decode `CMD` (with commands, empty ping), malformed lines, unknown commands
6. `ProtocolCodec` — encode/decode `RPT` for OK, BLOCKED, each REJECTED reason; malformed lines
7. `RoverStation` — landing report, OK, BLOCKED, UNKNOWN_COMMAND, MALFORMED, DUPLICATE (incl. `CMD 0`),
   seq consumed on rejection; fails when landing on an obstacle
8. `ControlTower` — seq numbering, `receive` landing, state + obstacle tracking, `MalformedReply`,
   `SeqMismatch`, unchanged state on failure (stub link lambda)
9. End-to-end — tower ↔ station via `station::receive`, real codec on both sides
10. Demo transcript test → `Main.kt`
11. `PROTOCOL.md`, final refactoring pass against the 4 rules

## Implementation instructions

- The whole solution is implemented in a single go: no stopping, no questions during implementation.
- Strict red → green → refactor: write a failing test, run `./gradlew test` and see it fail
  (a compile error counts as red), write the minimal code to pass, run again, refactor while green.
- One git commit per green cycle (`green: <behaviour>`) and per refactor (`refactor: <what>`),
  each ending with the `Co-Authored-By` trailer.
- Housekeeping: `group = 'org.marsrover'` in `build.gradle`; replace the old `org.gameoflifekt` `Main.kt`;
  add `.claude/` to `.gitignore`.
- Finish with a summary of the cycles, the last test run, and the commit log.
