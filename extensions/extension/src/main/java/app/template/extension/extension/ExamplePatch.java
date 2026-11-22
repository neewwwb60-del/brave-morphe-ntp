package app.template.extension.extension;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/**
 * Runtime preference bridge for the Brave custom New Tab patch.
 *
 * Preferences are stored in the patched Brave app's private storage and
 * survive app restarts. This class deliberately fails open: if anything
 * goes wrong, Brave receives its original New Tab URL.
 */
@SuppressWarnings("unused")
public final class ExamplePatch {
    private static final String PREFS_NAME = "brave_custom_ntp";
    private static final String KEY_CUSTOM_URL = "custom_url";
    private static final String KEY_AUTO_OPEN_MINUTES = "auto_open_minutes";
    private static final String NATIVE_NTP = "chrome-native://newtab/";

    private ExamplePatch() {
    }

    public static String rewriteNewTabUrl(
            Object host,
            String originalUrl,
            boolean incognito
    ) {
        if (incognito || !NATIVE_NTP.equals(originalUrl)) {
            return originalUrl;
        }

        Context context = asContext(host);
        if (context == null) {
            return originalUrl;
        }

        try {
            String saved = preferences(context).getString(KEY_CUSTOM_URL, null);
            String normalized = normalizeWebUrl(saved);
            return normalized == null ? originalUrl : normalized;
        } catch (Throwable ignored) {
            return originalUrl;
        }
    }

    /** Read the currently saved custom URL. Returns null when not configured. */
    public static String getCustomUrl(Context context) {
        if (context == null) return null;
        try {
            return normalizeWebUrl(preferences(context).getString(KEY_CUSTOM_URL, null));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Save an HTTP(S) URL. Empty input clears the custom URL. */
    public static boolean saveCustomUrl(Context context, String url) {
        if (context == null) return false;

        String value = url == null ? "" : url.trim();
        if (value.isEmpty()) {
            return preferences(context).edit().remove(KEY_CUSTOM_URL).commit();
        }

        String normalized = normalizeWebUrl(value);
        if (normalized == null) return false;

        return preferences(context).edit()
                .putString(KEY_CUSTOM_URL, normalized)
                .commit();
    }

    /** Returns 0 when inactivity auto-open is disabled. */
    public static int getAutoOpenMinutes(Context context) {
        if (context == null) return 0;
        try {
            int value = preferences(context).getInt(KEY_AUTO_OPEN_MINUTES, 0);
            return isSupportedInterval(value) ? value : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /** Supported values: 0, 5, 10, 15, 30, 60 and 120 minutes. */
    public static boolean saveAutoOpenMinutes(Context context, int minutes) {
        if (context == null || !isSupportedInterval(minutes)) return false;
        try {
            return preferences(context).edit()
                    .putInt(KEY_AUTO_OPEN_MINUTES, minutes)
                    .commit();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Context asContext(Object host) {
        if (!(host instanceof Context)) return null;
        Context context = (Context) host;
        Context appContext = context.getApplicationContext();
        return appContext != null ? appContext : context;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static boolean isSupportedInterval(int minutes) {
        return minutes == 0 || minutes == 5 || minutes == 10
                || minutes == 15 || minutes == 30
                || minutes == 60 || minutes == 120;
    }

    private static String normalizeWebUrl(String value) {
        if (value == null) return null;
        String candidate = value.trim();
        if (candidate.isEmpty()) return null;

        try {
            Uri uri = Uri.parse(candidate);
            String scheme = uri.getScheme();
            if (!uri.isHierarchical() || uri.getHost() == null
                    || uri.getHost().trim().isEmpty() || scheme == null) {
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
