# KirinMaL release notes

Copy `_TEMPLATE.md` to `<version>.md` for every release (for example, `1.0.14.md`).

The canonical version and Android version code live in `buildSrc/src/main/kotlin/Version.kt`. The signed Android release workflow checks that the matching note file exists and publishes it with the APKs. Updating a note for an already-published version also synchronizes the GitHub Release description.

Older release notes remain in `docs/releases/` for history; new releases should use this folder.
