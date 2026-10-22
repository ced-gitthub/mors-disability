# Modrinth listing (copy into the project page)
Name: Mors Disability

Slug: mors-disability
Summary: 30 real-life disabilities that rotate on a timer, with a HUD list, OP commands and a join check so every player must have the mod.
Categories: Game Mechanics, Adventure
Loader: Fabric. Game version: 26.2. Client and server: required on both.
Dependency: Fabric API (required).
License: MIT

Credits: inspired by the [Disabilities](https://modrinth.com/mod/disabilities) mod by OddSense. Mors Disability is an independent implementation with no code or assets copied from it.


## Description
(paste everything below this line into Modrinth > Settings > Description)

---

# Mors Disability

Every player randomly gains real-life inspired disabilities on a timer. Blindness, Parkinson's tremors, dementia that wipes your chests, asthma attacks while sprinting, and 25 more. **30 disabilities in total.**

**Warning: includes screen shaking and seizure effects. Not suitable for players sensitive to flashing or shaking visuals.**

Inspired by the [Disabilities](https://modrinth.com/mod/disabilities) mod by OddSense. This is an independent implementation, with no code or assets copied from it.

**Must be installed on both the server and every client.** Players without the mod are refused at login. Requires Fabric API.

## How it works
- A player with no disability gets a hidden countdown, shown on the HUD as "Next disability". When it ends they get a random disability.
- Each disability has its own timer based on its severity: **mild 10 min, moderate 5 min, severe 3 min**.
- When every timed disability has run out, they are all removed together and replaced by 1 new one (25% chance of 2, 7% chance of 3).
- Press **H** in game to see your disabilities and their timers.

## Commands (OP only)
| Command | What it does |
|---|---|
| `/disability give <player> <id>` | Give a specific disability |
| `/disability remove <player> <id>` | Remove one disability |
| `/disability list [player]` | Show a player's disabilities |
| `/disability next [player]` | Add another random disability and restart the timers |
| `/disability reset [player]` | Remove all disabilities and restart the countdown |
| `/disability reload` | Reload the config file without restarting |
| `/disability timer <minutes>` | Set one shared timer for everything |
| `/disability timer <mild\|moderate\|severe> <minutes>` | Set the timer for one severity |
| `/disability timer off` | Turn the timer off (also works per severity) |
| `/disability timer shared` | Go back to one shared timer |
| `/disability mode <stack\|replace>` | Switch the timer mode |

## Config
File: `config/mors-disability.json`. Edit it, then run `/disability reload` (or restart the server).

```json
{
  "intervalMinutes": 10.0,
  "severityMinutes": { "mild": 10.0, "moderate": 5.0, "severe": 3.0 },
  "mode": "replace",
  "chanceTwo": 0.25,
  "chanceThree": 0.07,
  "disabledDisabilities": []
}
```

| Option | Meaning |
|---|---|
| `intervalMinutes` | Minutes between new disabilities when one shared timer is used. 0 or less turns the timer off. |
| `severityMinutes` | Timer per severity (mild, moderate, severe). Leave it empty `{}` to use only `intervalMinutes`. |
| `mode` | `replace` or `stack` (see `/disability mode`). |
| `chanceTwo` | Chance (0 to 1) of getting 2 disabilities at once when the timer fires. |
| `chanceThree` | Chance (0 to 1) of getting 3 at once. Otherwise you get 1. |
| `disabledDisabilities` | List of ids that must never be handed out, for example `["blind", "cancer"]`. |

## The 30 disabilities
**Mild:** short, kleptomania, lactose_intolerance, pollen_allergy, insomnia, adhd, ocd, color_blind, dyslexia, diabetes, anosognosia, prosopagnosia

**Moderate:** brittle_bones, arthritis, asthma, mute, tourette, cerebral_palsy, parkinsons, vertigo, scoliosis, dementia, schizophrenia, deaf

**Severe:** blind, cancer, amputated_leg, amputated_offhand, narcolepsy, seizures

Use these ids with `/disability give` and in `disabledDisabilities`.

## Credits
Inspired by [Disabilities](https://modrinth.com/mod/disabilities) by OddSense. Licensed MIT.

---

## Versions
- release: 0.2.3+26.2 (releases/release/mors-disability-0.2.3+26.2.jar), changelog: HUD shows countdown to first disability
- release: 0.2.2+26.2, real-life descriptions, diabetes low-blood-sugar, seizures cleanup, mod-required login check
