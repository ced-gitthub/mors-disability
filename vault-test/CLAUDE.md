# Ced's Obsidian vault: rules for Claude

This vault is Ced's project dashboard. Start at [[Projects Hub]].

## Layout
- `Projects Hub.md`: every project, grouped by status.
- `Areas/<Root>/<Parent>/<Child>.md`: one note per project, grouped by area.
- Each note has `Status:`, `Root:` (and `Parent:` if any), `Folder:` (real code location), and a **Claude setup** section.

## Status meanings
- Deployed: finished and live; only maintenance and new features left.
- In Development: not deployed yet (includes waiting on review/approval).
- Archive: abandoned or taken down; kept for reference.

## When Ced mentions a project or starts a new chat
1. Find its note via the hub. Read it (status, next action, folder).
2. If it is new or unclear, ask: (a) status, (b) root area (and parent/child).
3. Then create or update the note and add it to the right hub section.
4. Never delete notes without asking. Never write empty files.

## Working on a project
The code, agents, skills and commands live in the project's own `Folder:`, not here.
Read that folder's `CLAUDE.md` and `.claude/` before working there.
