# Google Play Protect and KirinMaL

A new sideloaded APK can be unknown to Google Play Protect. Its warnings come from Google's device/cloud service, not GitHub Releases, so changing an APK's name, signature or package ID cannot guarantee approval.

**KirinMaL v1.0.2 audits its actual signed APK** for a standard ZIP header, modern target SDK, `com.lanzmagick.kirinmal` app identity, no debuggable flag, no unnecessary SMS/Accessibility/Notification Listener/installation privileges, Android v2 signature and consistent signing certificate across ABI variants. SHA-256 digests are printed in GitHub Actions logs. Android dependency metadata is included.

The exact warning determines what action is needed:

- **App scan recommended / Send app for security check:** use the official Google Play Protect scan offered by Android. Unknown apps may be scanned before installing.
- **App blocked to protect your device:** identify the exact sensitive permission listed in the Play Protect notice and review the merged manifest.
- **Harmful app blocked:** audit malicious behavior and third-party dependencies and submit a [Play Protect appeal](https://support.google.com/googleplay/android-developer/contact/protectappeals) if it is a false positive.
- **App compatibility too low:** verify target SDK; the release workflow checks it.

Official documentation: https://developers.google.com/android/play-protect/warning-dev-guidance

Do not turn off Play Protect to bypass a block. If a block continues, collect the **exact full warning text or a screenshot**, APK variant and Android version so the developer can resolve the actual classification. This release audit cannot replace Google's review.
