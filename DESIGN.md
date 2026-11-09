# Stopify design direction: "Aura × Spectrum" (ported from Prism)

Concept: Prism's 1.7 look, carried over to Stopify. A near-black ground with a hint of violet, soft glass panes with a lit top edge, very light large titles, and one *split light* (red to violet) that marks only what is playing, picked or in progress.

Source of truth: `Yigder/Prism` (`ui/theme/Theme.kt`, `Tokens.kt`, `Spectrum.kt`, `Glass.kt`, `components/Design.kt`, `components/Controls.kt`, `MainActivity.kt` TabBar, `player/MiniPlayer.kt`).

## Tokens (code: `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `Shape.kt`, `PrismTokens.kt`)
- Seed `#7C5CFF` (Prism violet). `schemeFromSeed` grows the full Material scheme from one hue: ground L 5.5 % dark / 98.5 % light, containers in small steps, accent run through `readableOn` (4.5:1). Artwork colour can replace the seed. "Prism" = seed, "White light" = greys only, "AMOLED" = pure black.
- Ink: text 93 %, secondary 60 % (`ink2`), quiet 38 % (`ink3`), hairline 9 %.
- Split light: `#FF4D5E #FF9F43 #FFD84D #3DDC97 #36B8FF #7C5CFF` (deeper set on light pages). Used for: mini-player progress, scrubber, current tab line, current song ring, selected chip ring, switch track, Liked tile, play ring.
- Type: Outfit (variable, one file). Display 200, page titles 36/200, hero titles 28/300, section titles 22/300, rows 16/500, body 14.5/400, labels 11.5–14/500. Thin weights only from 21 sp up.
- Shape: 8 / 12 / 16 / 22 / 28. Cover on shelf 14, cover in a row 10, pane 22, bars 28, buttons and chips are pills.
- Spacing: gutter 22, section gap 32, row art 48.
- Motion: 120 / 240 / 380 ms, no bounce.

## Components (`ui/components/PrismKit.kt`)
`PrismButton` (solid ink pill), `PrismOutlinedButton` / `PrismTonalButton` (glass pills), `PrismSwitch`, `PrismChip`, `Segmented`, `RoundAction` (44 dp glass disc), `ScreenHeader`, `SubPageHeader`, `Eyebrow`, `SectionHeader`, `SettingsGroup`, `PrismScrubber`, `SpectrumLine`, `Hairline`; modifiers `pane`, `spectrumRing`, `glassDisc`.

## Structure
- Dock (`Navbar.kt`): two separate floating objects. The mini player is its own rounded card (cover, title, play pill, next, split-light progress); beneath it a compact navigation pill only as wide as its three tabs. The current tab shows icon + label, the others icon only.
- Glass (`GlassSurface.kt`, Settings → Appearance → Glass): `Solid` / `Blurred` / `Liquid`. `Modifier.glassSurface` backs the dock and mini player with the live backdrop (blur, or blur plus a refracting lens rim). Bottom sheets use `PrismSheet`: tinted pane, light falling from the top edge, fine grain, lit rim, short handle.
- Mini player (`BottomMiniPlayer.kt`): no progress bar, no favourite button (it lives in the full player). When another Connect device is playing, the artist line becomes "speaker icon + device name" in the accent colour. Tap or swipe up opens, swipe sideways skips.
- Connect (`ConnectDevicesSheet.kt`, `data/SpotifyConnect*.kt`): the device menu is a bottom sheet from the full player's tool capsule. Playback on other devices is mirrored live in both players and controlled remotely.
- Home: large light title with the greeting beneath, round glass actions, a "resume" pane, a recents ledger, shelves with a quiet strapline over a light title. Cards have no borders.
- Library / Search: page header, glass search pill, chips (picked chip carries the split-light ring), Liked tile in split light.
- Settings: sub-page header with glass back button, small-caps group labels over lifted panes, hairline row dividers, split-light switches, segmented glass capsule for display mode.
- Full player: cover blurred into the ground, 28 dp cover lifted with a shadow, light 28 sp title with a glass heart, split-light scrubber, transport of glass prev/next discs round a wide ink play pill (ringed in split light while playing), a lyrics pane (line before, now, after), glass tool capsule (devices, save, queue).
- Album / playlist / artist: sub-screens stack by opening order (an album opened from an artist covers it); glass back disc, 28 dp hero cover (artist: round portrait), every top-track row has a thumbnail, solid ink play pill.

## Avoided
Coral/teal "Oxide" palette, Bricolage/Hanken pair, hard 1 dp outlines on every card, bold headings, accent-filled primary buttons.
