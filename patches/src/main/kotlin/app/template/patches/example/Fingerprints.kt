package app.template.patches.example

import app.morphe.patcher.Fingerprint

/**
 * Brave 1.100.58 / ARM64:
 * mo2.j(int, String) creates a tab from a URL.
 * The class and method signature were verified against the supplied smali.
 */
internal object NewTabUrlFingerprint : Fingerprint(
    definingClass = "Lmo2;",
    name = "j",
    isStatic = false,
    returnType = "Lorg/chromium/chrome/browser/tab/Tab;",
    parameters = listOf(
        "I",
        "Ljava/lang/String;"
    )
)
