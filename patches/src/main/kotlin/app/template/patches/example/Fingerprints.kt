package app.template.patches.example

import app.morphe.patcher.Fingerprint

/**
 * Brave 1.100.58 / ARM64 fingerprint verified against the supplied smali.
 */
internal object NewTabUrlFingerprint : Fingerprint(
    definingClass = "Lmo2;",
    name = "j",
    isStatic = false,
    returnType = "Lorg/chromium/chrome/browser/tab/Tab;",
    parameters = listOf("I", "Ljava/lang/String;")
)
