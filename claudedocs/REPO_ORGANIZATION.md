# Repo Organization & Naming Convention — chiggi_morphe_patches

Standard for folder layout and naming. Applies from 2026-08-14 onward. Read before adding files.
This is the law for *where things go and what they are called*; RULEBOOK.md links to it.

## 1. Canonical app slug

Every app has ONE lowercase slug, no spaces, matching its patch package dir under
`patches/src/main/kotlin/app/chiggi/<slug>/`. The slug is used everywhere (input, output, docs).

| App | Slug | Package |
|-----|------|---------|
| JioHotstar | `hotstar` | `com.hotstar` / `in.startv.hotstar` |
| Vi Movies & TV | `vimtv` | (Vi) |
| Does Not Commute | `commute` | `com.mediocre.commute` |
| AYA Baby AI | `ayababy` | (AYA) |
| Cry Analyzer | `cryanalyzer` | (Cry) |
| ChatterBaby | `chatterbaby` | (ChatterBaby) |
| ZEE5 | `zee5` | `com.graymatrix.did` |
| SonyLIV | `sonyliv` | `com.sonyliv` |
| Arrow Puzzle | `arrowpuzzle` | `com.easybrain.arrow.puzzle.game` |
| CrazyGames | `crazygames` | `com.crazygames.crazygamesapp` |
| Threads | `threads` | `com.instagram.barcelona` |
| Nutrilio | `nutrilio` | `net.nutrilio` |
| JioTV+ | `jiotv` | `com.jio.media.jiotvplus` |

New app → pick a slug, add the row here, use it for the package dir, input, output, and plan doc.

## 2. Directory layout

```
REPO ROOT
├── patches/            # Kotlin patch sources (TRACKED) — the product
├── extensions/         # Java extensions (TRACKED)
├── gradle/ scripts/    # build (TRACKED)
├── *.gradle.kts, gradle.properties, gradlew*      # build config (TRACKED)
├── package.json, .releaserc, patches-bundle.json, patches-list.json   # release (TRACKED)
├── README/CONTRIBUTING/LICENSE/NOTICE/CHANGELOG   # public docs (TRACKED)
├── CLAUDE.md, RULEBOOK.md                          # law files (TRACKED)
├── claudedocs/         # per-app patch plans + this file (TRACKED)
├── learn.md, Talk.md, Table_of_Contents.md        # AI workflow docs (IGNORED, stay at root)
│
└── workspace/          # ALL local build artifacts (IGNORED — never committed)
    ├── input/<slug>/    <slug>.apk | <slug>.apkm | <slug>-<variant>.apk
    └── output/<slug>/   <slug>-morphe.apk | <slug>-morphe-<variant>.apk
```

Rule: no per-app dir at repo root. Anything an app produces or consumes lives under `workspace/`.

## 3. Naming convention

| Thing | Pattern | Example |
|-------|---------|---------|
| Input APK | `workspace/input/<slug>/<slug>.<ext>` | `workspace/input/zee5/zee5.apk` |
| Input variant | `workspace/input/<slug>/<slug>-<variant>.<ext>` | `hotstar-chromecast.apk`, `hotstar-streamer.apk` |
| Output APK | `workspace/output/<slug>/<slug>-morphe.apk` | `workspace/output/zee5/zee5-morphe.apk` |
| Output variant | `workspace/output/<slug>/<slug>-morphe-<variant>.apk` | `hotstar-morphe-chromecast.apk` |
| Patch category dir | `patches/.../<slug>/patches/<category>/<feature>/` | `.../zee5/patches/player/autoskip/` |
| Fingerprints file | `Fingerprints.kt` (objects `<Name>Fingerprint`) | `OnMarkerHitFingerprint` |
| Patch file | `<Feature>Patch.kt` (val `<feature>Patch`) | `AutoSkipIntroPatch.kt` / `autoSkipIntroPatch` |
| Patch display name | Sentence case string in `name =` | `"Auto-skip intro and recap"` |
| Extension | `extensions/.../app/chiggi/<slug>/extension/<Name>.java` | `HsDeviceIdSpoof.java` |
| Patch plan doc | `claudedocs/<slug>-patch-plan.md` (lowercase slug) | `claudedocs/zee5-patch-plan.md` |

Fixed patch category vocabulary: `ads`, `player`, `misc`. Features nest under them
(`player/watermark`, `player/autoskip`, `player/buffer`, `misc/branding`, `misc/tv`,
`misc/upgrade`, `misc/popups`, `misc/analytics`, `misc/premium`, `misc/update`).

## 4. Rules that keep it neat

- Build output ALWAYS goes to `workspace/output/<slug>/<slug>-morphe.apk`; never leave APKs at root.
- Input APKs live under `workspace/input/<slug>/`; rename downloads to the slug on arrival.
- `workspace/` is git-ignored as a whole — one ignore rule, not per-app.
- Every app that gets patches also gets `claudedocs/<slug>-patch-plan.md`.
- Decompiles are scratch — keep them in the session scratchpad, not the repo.
- TV-only apps append `(Android TV)` to their `Compatibility(name)` in Constants.kt (e.g.
  `"SonyLIV (Android TV)"`) so Morphe Manager and aggregators show they are TV builds; phone apps
  get no suffix. Current TV apps: hotstar, jiotv, vimtv, zee5, sonyliv.
