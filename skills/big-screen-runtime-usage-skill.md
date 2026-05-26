# Big Screen Runtime Usage Skill

Use this skill when a business project wants to reuse the common-capability big screen auto-scroll or fixed-height virtual-list runtime rules.

This skill is the preferred entrypoint for AI/Codex/GPT agents. If an agent only has time to read one file before implementing or reviewing a big-screen scrolling feature, read this file first.

## Current Supported Surface

Source files:

- `https://github.com/codeinsightlab/lixin-common-capability/blob/main/docs/big-screen-auto-scroll.md`
- `https://github.com/codeinsightlab/lixin-common-capability/blob/main/runtime/big-screen-runtime.mjs`

Current headless helpers:

- `createRafLoop`
- `normalizeLoopOffset`
- `createAutoScrollRuntime`
- `getFixedVirtualWindow`
- `chunkRows`
- `padRowsWithPlaceholder`

## When To Use This Skill

Use this skill for:

- big-screen auto-scroll design
- large-screen hanging display stability
- fixed-height virtual-list runtime
- multi-area dashboard scrolling
- loop offset retention
- two-copy seamless loops
- multi-column card row padding
- active/standby refresh strategy
- review of whether a business dashboard should reuse headless runtime rules

Do not use this skill for:

- backend Spring Boot starter design
- business API design
- dashboard UI component extraction
- routing, menu, permission, or page registration
- generic table components
- non-fixed-height waterfall virtualization
- business status mapping or business data normalization

## How AI Should Start

When a user asks to build or review a big-screen scrolling page, follow this order:

1. Read this skill.
2. Read `https://github.com/codeinsightlab/lixin-common-capability/blob/main/docs/big-screen-auto-scroll.md` when design rationale, acceptance criteria, or anti-patterns are needed.
3. Read `https://github.com/codeinsightlab/lixin-common-capability/blob/main/runtime/big-screen-runtime.mjs` only when code-level helper behavior is needed.
4. Inspect the target business page before editing.
5. Keep edits inside the target project and target page unless the user explicitly opens wider scope.

Recommended prompt for future AI agents:

```text
请先读取 https://github.com/codeinsightlab/lixin-common-capability/blob/main/skills/big-screen-runtime-usage-skill.md。
如需设计原则，再读取 https://github.com/codeinsightlab/lixin-common-capability/blob/main/docs/big-screen-auto-scroll.md。
如需 runtime helper，再参考 https://github.com/codeinsightlab/lixin-common-capability/blob/main/runtime/big-screen-runtime.mjs。
本轮只复用 headless runtime 思路，不抽 Vue 组件，不复制 DataPanel/InspectionBoard 业务代码。
```

## Boundary

This runtime area is only for front-end runtime capability extraction.

It is not:

- a Spring Boot starter
- a Vue component
- a dashboard page
- a UI library
- a business data adapter
- an npm package structure

Do not copy DataPanel or InspectionBoard business code into common-capability.

Do not add:

- `BigScreenAutoScrollList.vue`
- `VirtualAutoScrollList.vue`
- render components
- dashboard-specific fields
- API calls
- business sorting rules
- status color rules
- route or menu logic
- CSS from business pages

## Usage Rules

Use `createRafLoop` when a page needs a clamped `requestAnimationFrame` loop.

Use `createAutoScrollRuntime` when a page needs a headless loop offset runtime. The page must provide rendering and transform binding itself.

Use `normalizeLoopOffset` when data or loop height changes and the current visual offset must be retained inside `[0, loopHeight)`.

Use `getFixedVirtualWindow` only when item height is fixed and stable. It returns the visible range and spacer metrics, but it does not scroll by itself.

Use `chunkRows` and `padRowsWithPlaceholder` for stable multi-column card rows. Placeholder rows are layout-only and must not participate in business statistics.

## Recommended Reference Level

Choose the smallest reference level that solves the task:

| Task | Reference |
| --- | --- |
| Design or review a big-screen scroll strategy | `https://github.com/codeinsightlab/lixin-common-capability/blob/main/docs/big-screen-auto-scroll.md` |
| Tell AI how to implement without over-extracting | `https://github.com/codeinsightlab/lixin-common-capability/blob/main/skills/big-screen-runtime-usage-skill.md` |
| Reuse offset/window/chunk helper logic | `https://github.com/codeinsightlab/lixin-common-capability/blob/main/runtime/big-screen-runtime.mjs` |
| Build a business dashboard page | Target business project only; do not edit common-capability unless asked |
| Extract a Vue component | Do not do this in the current phase |

## Code Usage

If the target project vendors the runtime file or exposes it through its own package/build alias, import only the needed helpers:

```js
import {
  createAutoScrollRuntime,
  getFixedVirtualWindow,
  normalizeLoopOffset,
  chunkRows,
  padRowsWithPlaceholder
} from '@/utils/big-screen-runtime.mjs'
```

Canonical source:

```text
https://github.com/codeinsightlab/lixin-common-capability/blob/main/runtime/big-screen-runtime.mjs
```

Do not import from a developer's local filesystem path. If direct GitHub/raw imports do not fit the target build system, copy or vendor the helper into the target project's existing utility area only after confirming the user wants code reuse in that project. Keep the copied code headless and do not add business fields.

Basic loop-offset usage:

```js
const runtime = createAutoScrollRuntime({
  speed: 0.02,
  getLoopHeight: () => loopHeight,
  shouldRun: () => !paused && !document.hidden,
  onOffset({ offset }) {
    track.style.transform = `translate3d(0, ${-offset}px, 0)`
  }
})

runtime.start()
```

Fixed virtual window usage:

```js
const windowState = getFixedVirtualWindow({
  items: rows,
  offset,
  itemHeight: 42,
  viewportHeight: 420,
  bufferCount: 4,
  getKey: row => row.id
})

// Render windowState.visibleItems.
// Use windowState.totalHeight for spacer height.
// Use windowState.offsetY and offset to align the rendered content.
```

Multi-column card rows:

```js
const rows = padRowsWithPlaceholder(taskCards, 2)
```

Placeholder rules:

- placeholder is layout-only
- keep its size
- hide it visually
- do not count it in business totals
- do not attach business actions to it

## Recommended Runtime Composition

Use this composition for large-screen lists:

```text
requestAnimationFrame runtime
  -> clamped delta
  -> runtime offset
  -> optional normalizeLoopOffset
  -> optional getFixedVirtualWindow
  -> page-owned render
  -> translate3d
```

For two-copy seamless loops:

```text
rows
  -> render copy A + copy B
  -> measure copy A height as loopHeight
  -> createAutoScrollRuntime
  -> transform track by -offset
```

For fixed-height virtual lists:

```text
rows
  -> createAutoScrollRuntime or page-owned offset source
  -> getFixedVirtualWindow
  -> spacer height = totalHeight
  -> content transform = offsetY - offset
```

Do not combine automatic scrolling and virtual-window calculation into an inseparable component.

## Required Design Separation

Keep these responsibilities separate:

- Auto-scroll runtime: time, delta, speed, offset, pause/resume, cleanup.
- Virtual window: fixed item height, visible range, spacer height, offset alignment.
- Business page: data loading, sorting, status mapping, layout, DOM refs, styles.

Do not put automatic scrolling inside the virtual-list helper.

Do not put business refresh or API logic inside the runtime helper.

## Pause And Cleanup Rules

Business pages or integration layers must handle:

- hover pause
- manual wheel/touch/pointer pause
- `document.hidden` pause
- resume delay
- timestamp reset on resume
- `requestAnimationFrame` cancel on destroy
- timer cleanup on destroy
- event-listener cleanup on destroy

Recommended delays from the validated pages:

- hover resume: about `500ms`
- visibility resume: about `500ms`
- manual interaction resume: about `1500ms`

These values are not hard API defaults. They are starting points for business pages.

## Data Refresh Rule

For large-screen pages, prefer active/standby data buffers:

- active data keeps rendering during refresh
- standby data receives the new result
- switch only at a safe boundary or when the list is not looping
- preserve offset when possible
- avoid clearing rows before filling new rows

Recommended data-refresh flow:

```text
load new rows
  -> normalize/sort in business page
  -> write standby rows
  -> mark pending switch
  -> switch at loop boundary or when list is not looping
  -> preserve or normalize offset
```

Do not:

- clear active rows before new rows are ready
- reset offset on every refresh
- switch data while the user is interacting
- hide refresh errors by returning fake successful data

## Review Checklist

When reviewing a big-screen implementation, check:

- Is `scrollTop` used as a high-frequency auto-scroll driver? If yes, ask why `translate3d` is not used.
- Is `requestAnimationFrame` delta clamped?
- Is the timestamp reset after pause/resume?
- Does `document.hidden` pause runtime work?
- Are rAF, timers, and listeners cleaned up on destroy?
- Is virtual-list item height fixed and stable?
- Are automatic scrolling and virtual-window calculation separated?
- Is data refresh using active/standby or an equivalent no-flash strategy?
- Are placeholders layout-only?
- Does the page avoid per-frame reactive state writes on hot paths?
- Does the runtime avoid business fields and UI assumptions?

## Validation Checklist

Before using this runtime in a business page, confirm:

- The helper does not depend on Vue.
- The helper does not directly read or write DOM.
- The helper does not contain business fields.
- The page owns all UI, style, and API behavior.
- rAF delta is clamped.
- visibility/hover/destroy cleanup is handled by the page or integration layer.
- fixed virtual lists have stable item height.

## Common Mistakes To Reject

Reject or redesign these approaches:

- creating `BigScreenAutoScrollList.vue`
- creating `VirtualAutoScrollList.vue`
- copying DataPanel or InspectionBoard into common-capability
- adding business API calls to runtime helpers
- adding CSS or card/table UI into runtime helpers
- using `setInterval` as the main visual scrolling clock
- using high-frequency `scrollTop` writes as the main auto-scroll driver
- recalculating DOM layout every frame
- resetting offset on every data refresh
- mixing business DTO fields into helper return values
- treating common-capability as a dashboard page repository

## Current Evolution Stage

Current stage: capability sedimentation.

Allowed:

- docs
- headless helper functions
- design rules
- runtime principles
- review checklists

Not allowed unless the user explicitly opens a later phase:

- Vue components
- renderless components
- full npm package setup
- business page rewrites
- DataPanel or InspectionBoard refactors
- dashboard UI library extraction
