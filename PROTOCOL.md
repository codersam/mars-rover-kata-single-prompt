# Earth ↔ Mars Rover Protocol

Line-based text, one message per line, synchronous request/reply.
Earth sends a **command**, Mars answers with exactly one **report**. Mars may also send one
unsolicited report: the landing report.

## Grammar

```
command  := "CMD" SP seq [SP commands]                 ; Earth → Mars
commands := ("F" | "B" | "L" | "R")+                   ; absent = status ping
report   := "RPT" SP seq SP x SP y SP dir SP status    ; Mars → Earth
status   := "OK" | "BLOCKED" SP x SP y | "REJECTED" SP reason
reason   := "MALFORMED" | "UNKNOWN_COMMAND" | "DUPLICATE"
dir      := "N" | "E" | "S" | "W"
seq,x,y  := non-negative decimal integer (fits a 32-bit signed int)
SP       := exactly one space
```

No leading or trailing whitespace, keywords and letters are uppercase.

## Meaning

| Element | Meaning |
|---|---|
| `F` / `B` | Move one cell forward / backward along the heading |
| `L` / `R` | Turn left / right on the spot |
| `x y` in a report | The rover's current position; `(0,0)` is bottom-left, `N` = y+1, `E` = x+1 |
| `dir` | The rover's current heading |
| `OK` | All commands executed |
| `BLOCKED x y` | An obstacle at `x y` stopped the rover on its last free cell; remaining commands were skipped |
| `REJECTED reason` | Nothing was executed, the rover did not move |

The planet wraps around: leaving one edge re-enters at the opposite edge.
Every report carries the rover's current position and heading, whatever its status.

## Sequence numbers

- Earth numbers its commands `1, 2, 3, …`; each report echoes the seq of the command it answers.
- Seq `0` is reserved for Mars: the landing report and replies to malformed lines.
- Mars accepts a seq only if it is **strictly greater** than the last one it accepted (gaps are fine).
- Every well-formed seq is consumed once accepted — even if its commands are then rejected.

## How Mars handles a line (in this order)

1. Not `CMD <seq>[ <token>]` → `RPT 0 x y dir REJECTED MALFORMED` (no seq consumed)
2. `seq` ≤ last accepted seq → `RPT seq x y dir REJECTED DUPLICATE`
3. The seq is consumed.
4. A letter other than `F B L R` → `RPT seq x y dir REJECTED UNKNOWN_COMMAND` (whole message rejected)
5. Execute → `OK` or `BLOCKED ox oy`

## How Earth handles a reply

| Reply | Outcome | Tower state |
|---|---|---|
| Valid report with the expected seq | `Delivered(report)` | Last known rover updated; a `BLOCKED` obstacle is remembered |
| Not a valid report | `MalformedReply(line)` | Unchanged |
| Valid report with another seq | `SeqMismatch(expected, actual)` | Unchanged |

A Mars-side `REJECTED` report is still `Delivered`: the link worked, the status says why nothing moved.

## Example exchange

5×5 planet, obstacle at `(2,2)`, rover lands at `(0,0)` facing north:

```
Mars  -> Earth: RPT 0 0 0 N OK                      landing report
Earth -> Mars:  CMD 1 FFRFF
Mars  -> Earth: RPT 1 1 2 E BLOCKED 2 2              stopped in front of the rock
Earth -> Mars:  CMD 2 RFFF
Mars  -> Earth: RPT 2 1 4 S OK                      wrapped from y=0 to y=4
Earth -> Mars:  CMD 3                                status ping
Mars  -> Earth: RPT 3 1 4 S OK
Earth -> Mars:  CMD 3 F                              replay
Mars  -> Earth: RPT 3 1 4 S REJECTED DUPLICATE
Earth -> Mars:  CMD 4 FXF
Mars  -> Earth: RPT 4 1 4 S REJECTED UNKNOWN_COMMAND
Earth -> Mars:  HELLO
Mars  -> Earth: RPT 0 1 4 S REJECTED MALFORMED
```

The codec, station and end-to-end tests (`src/test/kotlin`) are the executable form of this document.
