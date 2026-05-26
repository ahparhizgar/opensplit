# penpot — quick reference skill

Purpose
-------
Capture repository-specific notes and example code for interacting with the Penpot MCP plugin API so the agent (or any developer) can quickly inspect, export, and analyze Penpot designs.

Checklist (what this file contains)
- Short overview of useful Penpot APIs and objects
- Repro steps and a small, reusable JS snippet that returns a summary of the current open file
- Examples: list pages, enumerate shapes, read text content, export a file
- Troubleshooting notes and limitations observed while testing

Quick notes
-----------
- I tested this in the Penpot plugin environment (penpot global object available).
- Detected Penpot version during testing: 2.15.4.
- In the plugin context used, `penpot.currentUser` can be null depending on how the plugin is executed.

Useful objects
--------------
- `penpot` — top-level object for plugin scripts.
  - `penpot.version` — API/version string.
  - `penpot.currentFile` — File or null (the currently opened design file in the plugin context).
  - `penpot.currentPage` — currently active Page or null.
  - `penpot.root` — document root (top-level shapes).
  - `penpot.selection` — array of selected Shape objects.

- `File` (penpot.currentFile)
  - `.id`, `.name`, `.revn`
  - `.pages` — array of Page
  - `.export(type)` — export file as `.penpot` or `.zip` (returns ArrayBuffer/Uint8Array)

- `Page`
  - `.id`, `.name`, `.root` (root shape/board)
  - `.findShapes(criteria?)` — returns shapes matching optional criteria
  - `.createFlow()`, comment helpers, ruler guides, plugin data helpers

- `Shape` (union of board, text, rectangle, group, image, path, etc.)
  - Common props: `.id`, `.name`, `.type`, `.x`, `.y`, `.width`, `.height`, `.fills`, `.strokes`
  - Methods: `.getPluginData()`, `.setPluginData()`, `.export(config)`
  - Use `penpot.utils.types` helpers to test shape kinds (e.g. `penpot.utils.types.isText(shape)`).

Reusable: summarize current file
--------------------------------
This is the script I used to inspect the currently open file and produce a short JSON summary. Run it inside the Penpot plugin context (e.g., in a plugin action):

```js
(async () => {
  // Safe summary: gracefully handle missing values
  try {
    const out = { ok: true, penpotVersion: penpot.version, currentUser: null, file: null };

    try {
      const u = penpot.currentUser || null;
      if (u) out.currentUser = { id: u.id ?? null, name: u.name ?? null, email: u.email ?? null };
    } catch (e) { out.currentUser = null; }

    const file = penpot.currentFile;
    if (!file) {
      out.message = 'No file currently open in plugin context';
      console.log(JSON.stringify(out));
      return out;
    }

    const fileSummary = { id: file.id, name: file.name, revn: file.revn, pages: [] };

    for (const p of file.pages) {
      try {
        const shapes = (typeof p.findShapes === 'function') ? p.findShapes() : [];
        const top = (p.root && p.root.children && Array.isArray(p.root.children)) ? p.root.children : [];

        const topLevel = top.map(s => ({ id: s.id, name: s.name, type: s.type, x: s.x, y: s.y, width: s.width, height: s.height }));

        fileSummary.pages.push({ id: p.id, name: p.name, totalShapes: shapes ? shapes.length : null, topLevelCount: topLevel.length, topLevelShapes: topLevel });
      } catch (e) {
        fileSummary.pages.push({ id: p.id, name: p.name, error: String(e) });
      }
    }

    if (penpot.currentPage) fileSummary.currentPage = { id: penpot.currentPage.id, name: penpot.currentPage.name };

    out.file = fileSummary;
    out.root = (penpot.root && penpot.root.children) ? penpot.root.children.map(s => ({ id: s.id, name: s.name, type: s.type, x: s.x, y: s.y, width: s.width, height: s.height })) : [];
    out.selection = (penpot.selection || []).map(s => ({ id: s.id, name: s.name, type: s.type }));

    console.log(JSON.stringify(out, null, 2));
    return out;
  } catch (err) { console.error(err); return { ok: false, error: String(err) }; }
})();
```

Get full shape details (text extraction)
---------------------------------------
To read text from every Text shape (safe access):

```js
(async () => {
  const file = penpot.currentFile;
  if (!file) return [];
  const page = penpot.currentPage || file.pages[0];
  const shapes = page.findShapes();
  const texts = [];
  for (const s of shapes) {
    try {
      // Use type helper if available
      if (penpot.utils && penpot.utils.types && penpot.utils.types.isText && penpot.utils.types.isText(s)) {
        texts.push({ id: s.id, name: s.name, characters: s.characters ?? s.name });
      } else if ((s.type === 'text') || (s.constructor && s.constructor.name && /Text/i.test(s.constructor.name))) {
        texts.push({ id: s.id, name: s.name, characters: s.characters ?? s.name });
      }
    } catch (e) { /* ignore single-shape read errors */ }
  }
  console.log(texts);
  return texts;
})();
```

Exporting
---------
- Export whole file as `.penpot` or `.zip` using `file.export('penpot')` or `file.export('zip')` (returns ArrayBuffer/Uint8Array). Write the bytes to disk in your host environment or convert them to blob for download.
- Export individual shapes: most shape types support `.export(config)` which returns raw data for the requested export config. Example config varies by Penpot version; check the runtime API or experiment with a small export config object.

Common helpers & actions I used while testing
---------------------------------------------
- `penpot.utils.types` — type tests (isBoard/isText/isRectangle/etc.)
- `penpot.generateMarkup(shapes, {type:'svg'|'html'})` — generate markup from shapes
- `penpot.generateStyle(shapes, { type: 'css', withPrelude: true })`
- `penpot.uploadMediaData(name, data, mimeType)` — upload binary image data into Penpot

Observed behavior & troubleshooting
----------------------------------
- `penpot.currentUser` can be null in plugin contexts depending on plugin privileges and environment.
- `page.findShapes()` returns shapes but some shapes may throw when accessing certain properties — always wrap per-shape reads in try/catch.
- The plugin environment may not persist exported bytes to the host filesystem; export returns a binary buffer which you must pass back to the host for saving.

Example: export file and trigger download (in a browser plugin environment)
```js
// file is penpot.currentFile
const bytes = await file.export('penpot');
// bytes is an ArrayBuffer/Uint8Array: convert to Blob and createObjectURL for download in the plugin UI
const blob = new Blob([bytes], { type: 'application/octet-stream' });
const url = URL.createObjectURL(blob);
// then open url or create anchor link to download
```

Designing / writing to Penpot (create or modify shapes)
------------------------------------------------------
This repository required not only reading but also writing to Penpot. Below is the safe pattern I used to add a new Text shape under an existing (anchor) text. It prefers the current selection as the anchor, falls back to the first text on the page, computes a position below that anchor, creates the Text shape and sets basic properties.

```js
(async () => {
  const out = { ok: true };
  const file = penpot.currentFile;
  if (!file) { out.ok = false; out.message = 'No file open'; console.log(out); return out; }
  const page = penpot.currentPage || file.pages[0];
  if (!page) { out.ok = false; out.message = 'No page'; console.log(out); return out; }

  // 1) find anchor text (selected or first text on page)
  let anchor = null;
  try {
    const sel = (penpot.selection && penpot.selection.length) ? penpot.selection[0] : null;
    if (sel && penpot.utils && penpot.utils.types && penpot.utils.types.isText && penpot.utils.types.isText(sel)) anchor = sel;
  } catch (e) {}
  if (!anchor) {
    try { const texts = page.findShapes ? page.findShapes({ type: 'text' }) : []; if (texts && texts.length) anchor = texts[0]; } catch (e) {}
  }
  if (!anchor) { out.ok = false; out.message = 'No text anchor found'; console.log(out); return out; }

  // 2) compute position below anchor
  const margin = 8;
  const anchorX = (typeof anchor.x === 'number') ? anchor.x : (anchor.boardX || 0);
  const anchorY = (typeof anchor.y === 'number') ? anchor.y : (anchor.boardY || 0);
  const anchorH = (typeof anchor.height === 'number') ? anchor.height : (anchor.bounds && anchor.bounds.height) || 0;
  const newX = anchorX;
  const newY = anchorY + anchorH + margin;

  // 3) create text
  const textContent = 'This text was added by the agent.';
  let created = null;
  try { created = penpot.createText ? penpot.createText(textContent) : null; } catch (e) { created = null; }
  if (!created) { try { created = penpot.createText(''); } catch (e) { created = null; } }
  if (!created) { out.ok = false; out.message = 'Could not create text'; console.log(out); return out; }

  // 4) set properties safely
  try {
    if (typeof created.characters !== 'undefined') created.characters = textContent;
    created.name = textContent;
    created.x = newX;
    created.y = newY;
    if (typeof created.bringToFront === 'function') created.bringToFront();
  } catch (e) { /* ignore per-property failures */ }

  // 5) optional: nest the created shape inside the same parent as anchor
  try {
    if (anchor.parent && anchor.parent.appendChild && typeof anchor.parent.appendChild === 'function') {
      // appendChild will reparent the shape under the same group/board
      anchor.parent.appendChild(created);
    }
  } catch (e) { /* non-fatal */ }

  out.created = { id: created.id ?? null, name: created.name ?? null, x: created.x ?? null, y: created.y ?? null };
  console.log(JSON.stringify(out, null, 2));
  return out;
})();
```

Notes about write operations
- Use try/catch liberally: some shapes or properties are read-only depending on context.
- After creating text, sizes may be reported as 1 until the editor executes layout. You can explicitly call `created.resize(width, height)` or set typography tokens if available.
- To keep shapes organized, reparent / append to the anchor's parent (board/group) after creation.
- If you need specific typography (font family, size, weight), set those properties when available; properties differ by Penpot version.


What I learned from the recent inspection
----------------------------------------
- The repository's single design file `OpenSplit` contains one page (`Page 1`) with 3 shapes total. At top level there is a `Board` and a selected `Text` shape whose visible name is the text content.
- The global `penpot` API is feature rich and exposes enough methods to enumerate pages/shapes, export, and read/write plugin data keys.

Recommended next additions to this skill file
--------------------------------------------
- Add a short snippet to batch-export selected shapes as SVG/PNG and push them to the host.
- Add a set of typed utility wrappers (TypeScript) to normalize shape reads and conversions.
- Add standard diagnostics to capture plugin environment (user info, permissions) when a script is run.

Location
--------
This file is stored at `.agents/skills/penpot/SKILL.md` for quick reference by other agents.

Last tested
-----------
May 26, 2026 — test run observed Penpot v2.15.4 and produced a small JSON summary of the `OpenSplit` design (board + selected text). See repo activity for the log run.

-- end

