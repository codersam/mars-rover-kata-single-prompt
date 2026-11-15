# Implementation History

How this Mars Rover kata was planned and built, written for readers who did not see the working
session. The code was written by Claude (Claude Code) together with the repository owner, in three phases:

1. **Design**: interactive. Claude proposed a design and the owner answered clarifying questions. No code was written.
2. **Implementation**: one uninterrupted pass after the owner said "go", strictly test-first.
3. **History restoration**: rebuilding the git history after the original one was lost.

The four Rules of Simple Design from [`RULES.md`](RULES.md) guided every step:

| Tag | Rule |
|---|---|
| **R1** | Passes the tests |
| **R2** | Reveals intention |
| **R3** | No duplication |
| **R4** | Least elements |

Wherever a decision enforced a rule, it is marked with that rule's tag below.

---

## Phase 1: Design (no code allowed)

The owner's task was this: a Mars Rover on Mars is commanded by a Control Tower on Earth over a simple
protocol, and that protocol must be explicitly defined. The owner also set a working rule. Every
question must be asked up front. After "go", the whole solution is built in one pass with no stops
and no questions.

### Design iterations

**v0.1, first proposal.**
- A rover domain (`Direction`, `Position`, `Command`, `Rover`, `Planet`).
- A line-based text protocol with `CMD` and `RPT` messages and a `seq` number.
- A `Channel` interface with an in-memory implementation.
- An 11-step TDD roadmap, ending with an optional asynchronous delivery step.

**Questions, round 1.** These settled the domain and the transport:
- Grid edges wrap around.
- An obstacle stops the rover and is reported.
- The protocol is line-based text.
- Request/reply is synchronous.

**v0.2, simplified with the rules.**

| v0.1 element | Replaced by | Rule |
|---|---|---|
| `Channel` interface + `InMemoryChannel` | A plain function `(String) -> String`, the "link" | **R4** |
| Separate `Rover` and `RoverState` types | One immutable `Rover(position, direction)` | **R4**, **R3** |
| Optional async/latency step | Dropped. Synchronous replies need nothing more | **R4** |

**Questions, rounds 2 to 4.** These settled the protocol details. Several answers avoided a new
message type, keeping the protocol to two message kinds (**R4**):

| Question | Decision | Rule |
|---|---|---|
| How does Earth learn the landing state? | Mars sends `RPT 0 x y dir OK`. No `HELLO` message | **R4** |
| Reply to an unparseable line? | `RPT 0 … REJECTED MALFORMED`. No separate `ERR` message, so there is one reply shape | **R4** |
| Is an empty `CMD` valid? | Yes, it works as a status ping. No `PING` message | **R4** |
| Invalid command letter? | The whole message is rejected and the rover doesn't move | n/a |
| Sequence numbers? | Earth counts up from 1. Mars accepts only a seq strictly greater than the last one and uses up every well-formed seq. A replay gets `REJECTED DUPLICATE` | n/a |
| What does Earth keep? | The last known rover state plus the obstacles discovered so far | n/a |
| Tower API? | A typed `List<Command>`, so an invalid command can't be sent from Earth | **R2** |
| Bad reply on Earth? | A sealed `Outcome` with named cases: `Delivered`, `MalformedReply`, `SeqMismatch` | **R2** |
| Package layout? | `protocol` holds the vocabulary both sides share, so it is defined once. Then `mars` and `earth` | **R3** |
| Console demo? | Yes, and its output is checked by a test, so it isn't untested code | **R1** |
| Where is the protocol written down? | `PROTOCOL.md`, with the codec tests as its executable form | **R1** |

**v1.0, the final plan.** It was saved as [`DESIGN.MD`](DESIGN.MD) before "go".

---

## Phase 2: Implementation (one pass, test-first)

### Working method
- **Strict red → green → refactor.** Each cycle started with a failing test; a compile error counts
  as red. The cycle went green with the least code that passed, and refactoring happened only while
  green (**R1**).
- **One commit per cycle**, prefixed `green:` or `refactor:`. A commit was made only when every test
  passed (**R1**).

### Environment constraints
- **No Gradle.** The session's sandbox blocked the local socket Gradle needs. Every cycle was compiled
  and run directly with the Kotlin 2.4.21 compiler and JUnit 6, using the jars the project's Gradle
  build uses.
- **No project `.git` at first.** Writing it was blocked, so the history was recorded elsewhere. That
  copy was later lost; see Phase 3.

### Cycle log

Test counts are cumulative and were confirmed again in Phase 3.

| # | Commit | Tests | What happened | Rules |
|---|---|---|---|---|
| 0 | `491d0c2` | n/a | Scaffold. Group renamed to `org.marsrover`, `DESIGN.MD` added | n/a |
| 1 | `84ce5ae` | 2 | `Direction` turns left and right, computed from the enum order instead of four `when` branches | **R4** |
| 2 | `6a69cd5` | 4 | `Planet` moves F/B. `Command` got only `F` and `B`, because that's all the tests asked for | **R4** |
| 3 | `3405331` | 6 | Wrapping at all four edges with `mod` | **R4** |
| 4 | `f0ffc8a` | 9 | `L`/`R` added. A mixed sequence of turns and moves works | n/a |
| 5 | `58214f7` | 13 | Obstacles. `execute` returns `Execution(rover, status)` using the protocol's own `Status`, so Mars needs no second "blocked" concept | **R3** |
| 6 | `2f3e9e5` | 13 | *Refactor:* planet width and height made private | **R4** |
| 7 | `e4203b0` | 15 | Codec encodes `CMD`. An empty list encodes as a ping | n/a |
| 8 | `ddf3050` | 17 | Codec decodes `CMD` | n/a |
| 9 | `3389058` | 19 | Codec tells `UnknownCommand(seq)` apart from `MalformedLine`. Both are cases of the named result `DecodedCommand` | **R2** |
| 10 | `4ef7d6c` | 19 | *Refactor:* a `trimEnd()` trick replaced by an explicit "a ping has no commands token" | **R2** |
| 11 | `000783d` | 22 | Codec encodes `RPT` for OK, BLOCKED and every REJECTED reason | n/a |
| 12 | `3ac5d20` | 24 | Codec decodes `RPT`. A malformed report decodes to `null` | **R4** |
| 13 | `5de5781` | 24 | *Refactor:* duplicated "x y" encoding extracted into one function, and both decoders now read in the same style | **R3**, **R2** |
| 14 | `cfd82b4` | 27 | `RoverStation`: landing report, execution, ping. Minimal first version (a plain cast) | **R1** |
| 15 | `396cff9` | 30 | Station: BLOCKED, UNKNOWN_COMMAND, MALFORMED. The cast became an exhaustive `when` | **R2** |
| 16 | `c0459bb` | 36 | Duplicate seq rejection. One `accept()` holds the rule "every seq is used once" and is shared by both command paths | **R3**, **R2** |
| 17 | `9b9154a` | 37 | The station refuses to land the rover on an obstacle. `Planet.isBlocked` is reused by the move logic and the landing check | **R3** |
| 18 | `efb0e7e` | 40 | `ControlTower` numbers commands and keeps the state. Tests stub the link with a lambda instead of a mocking library | **R4** |
| 19 | `8b279f9` | 43 | Tower `receive()` for the landing report, plus obstacle memory. `send` reuses the receive path | **R3** |
| 20 | `caa61e8` | 47 | `MalformedReply` and `SeqMismatch` outcomes, with state unchanged on failure. One `accept(line, expectedSeq)` path handles both | **R3**, **R2** |
| 21 | `bb7a284` | 47 | *Refactor:* the magic `0` used on both sides became `UNSOLICITED_SEQ` | **R2**, **R3** |
| 22 | `a88f0c6` | 50 | End-to-end test with the real wiring `ControlTower(station::receive)`. No transport class | **R4** |
| 23 | `4e33ef0` | 51 | Console demo. Its transcript is checked by `DemoTest` | **R1** |
| 24 | `b33b246` | 51 | *Refactor:* KDoc for command letters, planet wrapping, and the "stop at the first obstacle" rule | **R2** |
| 25 | `01143d3` | 52 | `PROTOCOL.md`. Its example exchange is replayed line by line by a test, so the document and the code can't disagree | **R1**, **R3** |
| 26 | `17e62bd` | 52 | `DESIGN.MD` updated with an "As built" section | n/a |

### Where the code differs from the design

TDD showed that some planned elements weren't needed, or needed reshaping.

| Planned | Built | Rule |
|---|---|---|
| `sealed Message` supertype | Two `encode` overloads. Nothing ever needed the common supertype | **R4** |
| `decodeCommand` returns a message | `DecodedCommand` = `CommandMessage` / `UnknownCommand(seq)` / `MalformedLine`. The station must know the seq of an unknown command to use it up | **R2** |
| A separate decode error type for reports | `decodeReport` returns `Report?`. Earth only needs "report or not" | **R4** |
| Literal `0` for unsolicited reports | One named `UNSOLICITED_SEQ`, shared by Earth and Mars | **R2**, **R3** |

### Things that didn't follow textbook TDD
- **Cycle 1's commit was made while the test runner script was broken**, so no tests had run yet.
  The script was fixed and the commit re-verified green before moving on.
- **Some tests passed on their first run.** These are the end-to-end tests, the `PROTOCOL.md`
  example replay, seq gaps, and a malformed line not using up a seq. They pinned down behaviour
  earlier cycles had already produced, so they were acceptance or characterization tests, not drivers.

---

## Phase 3: History restoration

The original history was kept outside the project because of the sandbox, and it was lost. Once the
owner created an empty repository in the project, the history was rebuilt from the session record:

- Each earlier file version was worked backwards from the final files, using the exact edits made
  during implementation.
- All 27 commits were replayed in a temporary repository. Every code commit was compiled and tested,
  and all passed with the same test counts as the original run.
- The rebuilt final commit was confirmed byte-identical to the working tree (37 files) before it was
  brought into the project.

Commit messages, order and content match the original. Hashes and timestamps are new, and the
scaffold commit contains the IDE files as they were at restoration time.

---

## Where each rule was enforced

- **R1, passes the tests.** Red before green in every cycle. Commits only when green. The demo and the
  `PROTOCOL.md` example are both checked by tests. History restoration re-ran the tests at every commit.
- **R2, reveals intention.** Domain names throughout (`ControlTower`, `RoverStation`, `Outcome.Delivered`,
  `Reason.DUPLICATE`). Named results (`DecodedCommand`, `Outcome`) instead of exceptions or nulls where
  the cases matter. `UNSOLICITED_SEQ`. The explicit ping encoding. KDoc only where the rule isn't
  visible from the code.
- **R3, no duplication.** One shared `protocol` package. `Status` reused by the planet. A single
  `accept()` on each side. One position encoder. `isBlocked` reused. The protocol document checked
  against the code.
- **R4, least elements.** No `Channel`, no `Message`, and no extra message types (`HELLO`, `ERR`,
  `PING`). No async layer. No mocking library. Enum values added only when a test needed them. Planet
  dimensions kept private.

## Following along yourself

```bash
git log --reverse --oneline        # the cycles above, in order
git show 3389058                   # any single cycle: test and code change together
./gradlew test                     # 52 tests
# run main() in src/main/kotlin/org/marsrover/Main.kt from the IDE to print the demo wire traffic
```

See [`DESIGN.MD`](DESIGN.MD) for the full design, [`PROTOCOL.md`](PROTOCOL.md) for the wire protocol, and
[`RULES.md`](RULES.md) for the rules.
