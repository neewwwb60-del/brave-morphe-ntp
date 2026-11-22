package app.template.patches.example

import app.morphe.patcher.Fingerprint

/**
 * Brave 1.100.58 / ARM64 fingerprints verified against the supplied smali.
 */
internal object NewTabUrlFingerprint : Fingerprint(
    definingClass = "Lmo2;",
    name = "j",
    isStatic = false,
    returnType = "Lorg/chromium/chrome/browser/tab/Tab;",
    parameters = listOf("I", "Ljava/lang/String;")
)

internal object BottomToolbarLongPressFingerprint : Fingerprint(
    definingClass = "Lw32;",
    name = "onLongClick",
    isStatic = false,
    returnType = "Z",
    parameters = listOf("Landroid/view/View;")
)
