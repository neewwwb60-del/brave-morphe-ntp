package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

object Constants {
    val COMPATIBILITY_BRAVE = Compatibility(
        name = "Brave",
        packageName = "com.brave.browser",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFFFF5000,
        targets = listOf(
            AppTarget(
                version = "1.100.58",
                versionCodes = mapOf(
                    SupportedAbi.ARM64_V8A to 430005804
                )
            )
        )
    )
}
