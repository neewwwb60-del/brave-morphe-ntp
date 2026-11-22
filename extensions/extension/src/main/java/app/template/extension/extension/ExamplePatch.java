package app.template.extension.extension;

import android.net.Uri;

/**
 * Runtime URL validation and redirect bridge for the Brave custom New Tab patch.
 * The configured URL is supplied by Morphe when the patch is built.
 */
@SuppressWarnings("unused")
public final class ExamplePatch {
    private static final String NATIVE_NTP = "chrome-native://newtab/";

    private ExamplePatch() {
    }

    public static String rewriteNewTabUrl(
            Object host,
            String originalUrl,
            boolean incognito,
            String configuredUrl) {
        if (incognito || !NATIVE_NTP.equals(originalUrl)) return originalUrl;

        String normalized = normalizeWebUrl(configuredUrl);
        return normalized == null ? originalUrl : normalized;
    }

    private static String normalizeWebUrl(String value) {
        if (value == null) return null;
        String candidate = value.trim();
        if (candidate.isEmpty()) return null;

        try {
            Uri uri = Uri.parse(candidate);
            String scheme = uri.getScheme();

            if (!uri.isHierarchical()
                    || uri.getHost() == null
                    || uri.getHost().trim().isEmpty()
                    || scheme == null) {
                return null;
            }

            if (!"http".equalsIgnoreCase(scheme)
                    && !"https".equalsIgnoreCase(scheme)) {
                return null;
            }

            return candidate;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
