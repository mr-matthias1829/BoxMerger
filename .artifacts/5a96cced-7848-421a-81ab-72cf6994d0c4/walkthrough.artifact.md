# Walkthrough - Auto-Merger Logic Update

We have successfully updated the auto-merger system to prioritize the most top-left slot, scanning from left-to-right, top-to-bottom across the grid.

## Summary of Changes

1. **Top-Left Priority Auto-Merger (`GameViewModel.kt`)**: Updated `performAutoMerge()` to scan grid cells (0 to 19) in reading order (top-left to bottom-right). For each box found, it searches for any subsequent matching tier box anywhere else on the grid and merges it into the earlier (top-left) slot, regardless of adjacency.

## Build Status
- **Build**: Successfully compiled (`BUILD SUCCESSFUL`).
