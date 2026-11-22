package app.template.patches.example

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.template.patches.shared.Constants.COMPATIBILITY_BRAVE

private const val EXTENSION_CLASS =
    "Lapp/template/extension/extension/ExamplePatch;"

@Suppress("unused")
val braveCustomNtpPatch = patch(
    name = "Brave Custom New Tab",
    description = "Redirect regular New Tab requests to a saved custom URL.",
    default = true
) {
    compatibleWith(COMPATIBILITY_BRAVE)

    extendWith("extensions/extension.mpe")

    execute {
        NewTabUrlFingerprint.method.addInstructions(
            0,
            """
                iget-boolean v0, p0, Lmo2;->e:Z
                if-nez v0, :cond_brave_ntp_skip

                const-string v0, "chrome-native://newtab/"
                invoke-virtual {p2, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :cond_brave_ntp_skip

                iget-object v0, p0, Lmo2;->a:Ldn3;
                iget-boolean v1, p0, Lmo2;->e:Z
                invoke-static {v0, p2, v1}, $EXTENSION_CLASS;->rewriteNewTabUrl(Ljava/lang/Object;Ljava/lang/String;Z)Ljava/lang/String;
                move-result-object v1
                if-eqz v1, :cond_brave_ntp_skip
                move-object p2, v1

                :cond_brave_ntp_skip
            """
        )
    }
}
