# Graph Report - Test Fabric Mods  (2026-11-01)

## Corpus Check
- 60 files · ~12,160 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 12 file(s) not represented in the graph (top: .jar 6, (none) 3, .properties 2)

## Summary
- 489 nodes · 951 edges · 33 communities (10 shown, 23 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 17 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4249d232`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DisabilityManager.java
- VoiceFx.java
- net.minecraft.commands.CommandSourceStack
- net.minecraft.server.level.ServerPlayer
- .odds
- Hallucination.java
- DisabilityClient.java
- LactoseDisability.java
- DementiaDisability.java
- ModCheck.java
- DementiaDisability
- Projects Hub.md
- Disability
- .init
- Mors Disability
- VertigoDisability
- DiabetesDisability
- PollenDisability
- AnosognosiaDisability
- ColorBlindDisability
- DeafDisability
- ParkinsonDisability
- ProsopagnosiaDisability
- release.sh
- Project rules
- Mors Disability
- BlindDisability
- MuteDisability
- releases/README.md
- GLOBAL-CLAUDE-draft.md

## God Nodes (most connected - your core abstractions)
1. `Disability` - 46 edges
2. `DisabilityManager` - 24 edges
3. `DisabilityCommand` - 18 edges
4. `DisabilityMod` - 17 edges
5. `DisabilityClient` - 14 edges
6. `Disabilities` - 13 edges
7. `DementiaDisability` - 12 edges
8. `VoiceFx` - 12 edges
9. `Hallucination` - 11 edges
10. `DisabilityConfig` - 9 edges

## Surprising Connections (you probably didn't know these)
- `AdhdDisability` --inherits--> `Disability`  [EXTRACTED]
  src/main/java/com/ced/disabilities/disability/AdhdDisability.java → src/main/java/com/ced/disabilities/disability/Disability.java
- `AmputatedOffhandDisability` --inherits--> `Disability`  [EXTRACTED]
  src/main/java/com/ced/disabilities/disability/AmputatedOffhandDisability.java → src/main/java/com/ced/disabilities/disability/Disability.java
- `AnosognosiaDisability` --inherits--> `Disability`  [EXTRACTED]
  src/main/java/com/ced/disabilities/disability/AnosognosiaDisability.java → src/main/java/com/ced/disabilities/disability/Disability.java
- `AsthmaDisability` --inherits--> `Disability`  [EXTRACTED]
  src/main/java/com/ced/disabilities/disability/AsthmaDisability.java → src/main/java/com/ced/disabilities/disability/Disability.java
- `AttributeDisability` --inherits--> `Disability`  [EXTRACTED]
  src/main/java/com/ced/disabilities/disability/AttributeDisability.java → src/main/java/com/ced/disabilities/disability/Disability.java

## Import Cycles
- None detected.

## Communities (33 total, 23 thin omitted)

### Community 1 - "VoiceFx.java"
Cohesion: 0.07
Nodes (4): VoicechatIntegration, Biquad, Voice, VoiceFx

### Community 3 - "net.minecraft.server.level.ServerPlayer"
Cohesion: 0.11
Nodes (4): CancerDisability, CompositeDisability, LactoseDisability, DisabilityManager

### Community 4 - ".odds"
Cohesion: 0.07
Nodes (5): AdhdDisability, CerebralPalsyDisability, NarcolepsyDisability, OcdDisability, SeizuresDisability

### Community 5 - "Hallucination.java"
Cohesion: 0.10
Nodes (4): Fake, Hallucination, EntityRenderDispatcherMixin, PlayerModelMixin

### Community 8 - "DementiaDisability.java"
Cohesion: 0.08
Nodes (4): AttributeDisability, SchizophreniaDisability, TouretteDisability, DisabilityMod

### Community 10 - "DementiaDisability"
Cohesion: 0.26
Nodes (3): DementiaDisability, Lapse, Memory

### Community 11 - "Projects Hub.md"
Cohesion: 0.05
Nodes (33): Mors Disability, Notes, Tasks, Claude setup, Details, Notes, Pera App, Tasks (+25 more)

### Community 14 - "Mors Disability"
Cohesion: 0.18
Nodes (10): Commands (OP only), Config, Credits, Description, How it works, Modrinth listing (copy into the project page), Mors Disability, Support (+2 more)

### Community 27 - "Project rules"
Cohesion: 0.33
Nodes (5): Checkpoint every change, Dev test setup, Project rules, Test client audio, Verify it yourself. Never hand the user a "not verified" list.

### Community 28 - "Mors Disability"
Cohesion: 0.40
Nodes (4): Credits, License, Mors Disability, Support

## Knowledge Gaps
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Disability` connect `Disability` to `DisabilityManager.java`, `net.minecraft.commands.CommandSourceStack`, `net.minecraft.server.level.ServerPlayer`, `.odds`, `DisabilityClient.java`, `LactoseDisability.java`, `DementiaDisability.java`, `DementiaDisability`, `.init`, `VertigoDisability`, `DiabetesDisability`, `PollenDisability`, `AnosognosiaDisability`, `ColorBlindDisability`, `DeafDisability`, `ParkinsonDisability`, `ProsopagnosiaDisability`, `BlindDisability`, `MuteDisability`?**
  _High betweenness centrality (0.189) - this node is a cross-community bridge._
- **Should `DisabilityManager.java` be split into smaller, more focused modules?**
  _Cohesion score 0.05920444033302498 - nodes in this community are weakly interconnected._
- **Why does `DisabilityMod` connect `DementiaDisability.java` to `DisabilityManager.java`, `VoiceFx.java`, `net.minecraft.commands.CommandSourceStack`, `Hallucination.java`, `DisabilityClient.java`?**
  _High betweenness centrality (0.050) - this node is a cross-community bridge._
- **Should `VoiceFx.java` be split into smaller, more focused modules?**
  _Cohesion score 0.07198228128460686 - nodes in this community are weakly interconnected._
- **Why does `DisabilityManager` connect `net.minecraft.server.level.ServerPlayer` to `DisabilityManager.java`, `VoiceFx.java`, `net.minecraft.commands.CommandSourceStack`, `Hallucination.java`, `DisabilityClient.java`, `DementiaDisability.java`?**
  _High betweenness centrality (0.045) - this node is a cross-community bridge._
- **Should `net.minecraft.commands.CommandSourceStack` be split into smaller, more focused modules?**
  _Cohesion score 0.1365079365079365 - nodes in this community are weakly interconnected._
- **Should `net.minecraft.server.level.ServerPlayer` be split into smaller, more focused modules?**
  _Cohesion score 0.11219512195121951 - nodes in this community are weakly interconnected._