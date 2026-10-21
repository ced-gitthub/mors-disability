# Project rules

## Verify it yourself. Never hand the user a "not verified" list.
- If something can be tested, test it in this session. Do not report "unverified because it needs player input" and stop.
- If direct testing is blocked (needs real input, a GUI, etc.), build the test hook yourself (for example a dev-only client/server command driven by `-Ddisabilitystack.selftest=true`, a script, or a GameTest) and run it. Do not offer the hook as a "next step" or ask permission first.
- Only list something as unverified after you actually tried and could not make it testable. Say what you tried.
- If you say you have another way to verify, do it in the same turn.
- Fix failures yourself using systematic debugging, then re-run. Do not wait for the user between steps.

## Dev test setup
- Java 25: `$env:JAVA_HOME="C:\Users\Ced\jdk25-dl\x\jdk-25.0.4.1+1"`
- Server: `gradlew runServer` (RCON 25575, password `test`). Client: `gradlew runClient --args="--quickPlayMultiplayer localhost:25565 --username Tester"`.
- Check results in `run/logs/latest.log` and via RCON.
- Keep `run/config/mors-disability.json` at the default (`intervalMinutes` 10) when finished, and stop all test java processes.

## Test client audio
Before every dev client launch, set `soundCategory_music:0.0` (and keep master 1.0) in `run/options.txt` so no music plays while testing (user request).

## Checkpoint every change
- After ANY change, however small, make a git commit (checkpoint) before moving on: `git add -A && git commit -m "<what changed>"`.
- Commit before risky work too, so it can be rolled back with `git revert`/`git checkout`.
