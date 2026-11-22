package app.template.patches.example

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.COMPATIBILITY_BRAVE

private const val EXTENSION_CLASS =
    "Lapp/template/extension/extension/ExamplePatch;"

private fun escapeSmaliString(value: String): String =
    value.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

@Suppress("unused")
val braveCustomNtpPatch = patch(
    name = "Brave Custom New Tab",
    description = "Redirect regular New Tab requests to a URL configured in Morphe.",
    default = true
) {
    val customNtpUrl by stringOption(
        key = "Custom New Tab URL",
        default = "",
        title = "Custom New Tab URL",
        description = "Enter an HTTP or HTTPS URL. Leave blank to use Brave's default New Tab Page.",
        validator = { value ->
            value.isNullOrBlank() ||
                (value.startsWith("https://", ignoreCase = true) ||
                 value.startsWith("http://", ignoreCase = true))
        }
    )

    compatibleWith(COMPATIBILITY_BRAVE)
    extendWith("extensions/extension.mpe")

    execute {
        val configuredUrl = escapeSmaliString(customNtpUrl.orEmpty())

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
                const-string v2, "$configuredUrl"
                invoke-static {v0, p2, v1, v2}, ${EXTENSION_CLASS}->rewriteNewTabUrl(Ljava/lang/Object;Ljava/lang/String;ZLjava/lang/String;)Ljava/lang/String;
                move-result-object v1
                if-eqz v1, :cond_brave_ntp_skip
                move-object p2, v1

                :cond_brave_ntp_skip
            """
        )
    }
}
