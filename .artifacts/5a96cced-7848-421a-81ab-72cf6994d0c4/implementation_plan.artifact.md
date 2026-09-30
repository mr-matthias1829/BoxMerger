# Game Expansion - Implementation Plan (Revised)

We will implement the 12 requested gameplay features, merge tracking distinction, and UI restructuring:
1. **Remove Offline Progress**: Remove offline calculation logic.
2. **Track Manual & Automatic Merges**: Track "manual merges" (player-initiated) and "automatic merges" separately. Total merges = manual + automatic.
3. **Track Playtime**: Add playtime tracker in seconds.
4. **Gem Drop on Merge**: 1% base chance (+ gem upgrades) to earn a gem on every merge (both manual and auto).
5. **Auto-Merger System**: Unlocked upon first prestige (starts at 6s interval, upgradable), toggleable on the main page.
6. **Page & Navigation Reorganization**:
   - Top row: Main Game, Prestige, Gem Upgrades.
   - Bottom row: Achievements, Stats, Settings.
7. **Prestige System & Page**: Explanation text, "+X Prestige" calculation button, resetting grid/boxes/upgrades while keeping persistent assets.
8. **Gem Upgrades**: 4 upgrades (More Boxes multiplier, More Prestige multiplier, Gem Chance +0.1% up to 5%, Auto Merge Speed).
9. **Prestige Upgrades**: 3 upgrades (More Boxes multiplier, More Gems multiplier, More Prestige multiplier).
10. **Prestige Passive Bonus**: +1% box generation per prestige held.
11. **Auto-Merger Toggle Button**: Placed below the grid on the main page once unlocked.
12. **Grid Size Reduction**: Changed from 5x5 (25 cells) to 5x4 (20 cells).

## Proposed Changes

### Core Game & ViewModel (`GameViewModel.kt`, `CurrencyManager.kt`, `UpgradeManager.kt`)
- Update grid size to 5x4 (20 cells).
- Add Prestige currency, gem drop chance calculation, manual merges counter, automatic merges counter, and playtime tracker.
- Implement Prestige reset logic, prestige calculation formula, and unlock tracking for Auto-Merger.
- Implement auto-merger ticking logic (merging lowest matching adjacent/available boxes every interval).

### Upgrades & Currencies
- Define Gem Upgrades and Prestige Upgrades with their respective multiplier effects.
- Apply multipliers to box generation, gem drop rate, and prestige gain.

### UI & Navigation (`MainLayout.kt`, `GameScreen.kt`, new Pages)
- Reorganize tabs into top row (Main Game, Prestige, Gem Upgrades) and bottom row (Achievements, Stats, Settings).
- Create Prestige screen with reset button and prestige upgrades.
- Create Gem Upgrades screen.
- Add Auto-Merger toggle button on Main Game screen when unlocked.
