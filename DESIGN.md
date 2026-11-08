# Stopify design direction: "Oxide"

Concept: a late-night listening desk. Deep teal-black ground, one coral signal color for the thing playing or the thing to press, sand for secondary marks.

- Ground `#0A1112`, raised `#142021`/`#1B2A2C`, text `#E8EEEC` (15:1), muted `#9FB0AE` (7.5:1).
- Accent coral `#FF6F61` (on `#3A0A05`, 6.9:1): only for play, current track, selected tab. Sand `#E9C46A` secondary. Mint `#7FD6C2` tertiary.
- Light mode: bone `#F2F4F1`, coral deepened to `#B8321F`.
- Type: Bricolage Grotesque for titles and numerals, Hanken Grotesk for text.
- Radius 8 / 12 / 20 / 28. Edges (1dp outline) instead of shadows. One elevation language.
- Structure: two floating objects above the bottom edge, 16dp from the sides: a mini player card (22dp radius, hairline progress along its bottom) and a navigation pill (32dp radius). Both have a 1dp edge plus a soft shadow so they read as lifted over scrolling content. The selected tab shows a short coral bar above its icon.
- Home: headline greeting, a "resume" slab for the last track, a numbered recents ledger, then shelves. Album and playlist cards open their screen; only songs play on tap.
- Motion: short fades and a bar slide; no bounce.
- Full player: one column (header, artwork, title and favourite, status line for lyric or error, scrubber, transport with a 76dp coral key, a tool row of Lyrics / Save / Queue). Swipe down closes, swipe artwork skips, Back closes. Every control does something.
- Avoided: filter chips that do nothing, circular play badge on every card.
