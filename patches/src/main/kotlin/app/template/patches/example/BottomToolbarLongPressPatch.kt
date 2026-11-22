package app.template.patches.example

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.template.patches.shared.Constants.COMPATIBILITY_BRAVE

private const val EXTENSION_CLASS =
    "Lapp/template/extension/extension/ExamplePatch;"

@Suppress("unused")
val braveNewTabLongPressSettingsPatch = patch(
    name = "Brave NTP Settings Long Press",
    description = "Open custom New Tab settings when the bottom New Tab button is long-pressed.",
    default = true
) {
    compatibleWith(COMPATIBILITY_BRAVE)

    extendWith("extensions/extension.mpe")

    execute {
        BottomToolbarLongPressFingerprint.method.addInstructions(
            0,
            """
                iget-object v0, p0, Lw32;->D:Lcom/google/android/material/button/MaterialButton;
                if-ne p1, v0, :cond_brave_ntp_long_press_original

                invoke-static {p1}, $EXTENSION_CLASS;->showSettingsFromView(Landroid/view/View;)Z
                move-result v0
                if-eqz v0, :cond_brave_ntp_long_press_original

                const/4 v0, 0x1
                return v0

                :cond_brave_ntp_long_press_original
            """
        )
    }
}
