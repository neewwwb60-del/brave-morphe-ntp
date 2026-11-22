package app.template.extension.extension;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Runtime preference bridge and settings UI for the Brave custom New Tab patch.
 * The inactivity interval is stored here; its scheduler is a later stage.
 */
@SuppressWarnings("unused")
public final class ExamplePatch {
    private static final String PREFS_NAME = "brave_custom_ntp";
    private static final String KEY_CUSTOM_URL = "custom_url";
    private static final String KEY_AUTO_OPEN_MINUTES = "auto_open_minutes";
    private static final String NATIVE_NTP = "chrome-native://newtab/";

    private static final int[] INTERVAL_VALUES = {0, 5, 10, 15, 30, 60, 120};
    private static final String[] INTERVAL_LABELS = {
            "Disabled", "5 minutes", "10 minutes", "15 minutes",
            "30 minutes", "1 hour", "2 hours"
    };

    private ExamplePatch() {
    }

    public static String rewriteNewTabUrl(Object host, String originalUrl, boolean incognito) {
        if (incognito || !NATIVE_NTP.equals(originalUrl)) return originalUrl;
        Context context = asContext(host);
        if (context == null) return originalUrl;

        try {
            String saved = preferences(context).getString(KEY_CUSTOM_URL, null);
            String normalized = normalizeWebUrl(saved);
            return normalized == null ? originalUrl : normalized;
        } catch (Throwable ignored) {
            return originalUrl;
        }
    }

    /** Opens the settings dialog from the long-pressed toolbar button. */
    public static boolean showSettingsFromView(View view) {
        if (view == null) return false;
        Context context = asContext(view.getContext());
        if (context == null) return false;

        try {
            showSettingsDialog(context);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void showSettingsDialog(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(context, 20);
        layout.setPadding(padding, dp(context, 8), padding, 0);

        TextView urlLabel = new TextView(context);
        urlLabel.setText("Custom New Tab URL");
        layout.addView(urlLabel);

        EditText urlInput = new EditText(context);
        urlInput.setSingleLine(true);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setHint("https://example.com");
        String currentUrl = getCustomUrl(context);
        if (currentUrl != null) urlInput.setText(currentUrl);
        layout.addView(urlInput);

        TextView intervalLabel = new TextView(context);
        intervalLabel.setText("Auto-open after inactivity");
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dp(context, 16);
        layout.addView(intervalLabel, labelParams);

        Spinner intervalSpinner = new Spinner(context);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, INTERVAL_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(adapter);

        int currentInterval = getAutoOpenMinutes(context);
        for (int i = 0; i < INTERVAL_VALUES.length; i++) {
            if (INTERVAL_VALUES[i] == currentInterval) {
                intervalSpinner.setSelection(i);
                break;
            }
        }
        layout.addView(intervalSpinner);

        TextView note = new TextView(context);
        note.setText("The inactivity timer will be activated in a later patch stage.");
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(context, 8);
        layout.addView(note, noteParams);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Brave New Tab Settings")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(button -> {
                    String url = urlInput.getText().toString().trim();
                    if (!url.isEmpty() && normalizeWebUrl(url) == null) {
                        urlInput.setError("Enter a valid HTTP or HTTPS URL");
                        return;
                    }

                    int position = intervalSpinner.getSelectedItemPosition();
                    if (position < 0 || position >= INTERVAL_VALUES.length) {
                        Toast.makeText(context, "Choose a valid inactivity interval",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean urlSaved = saveCustomUrl(context, url);
                    boolean intervalSaved = saveAutoOpenMinutes(
                            context, INTERVAL_VALUES[position]);

                    if (urlSaved && intervalSaved) {
                        Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Could not save all settings",
                                Toast.LENGTH_LONG).show();
                    }
                }));

        dialog.show();
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static String getCustomUrl(Context context) {
        if (context == null) return null;
        try {
            return normalizeWebUrl(preferences(context).getString(KEY_CUSTOM_URL, null));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Empty input clears the custom URL. */
    public static boolean saveCustomUrl(Context context, String url) {
        if (context == null) return false;
        String value = url == null ? "" : url.trim();

        try {
            if (value.isEmpty()) {
                return preferences(context).edit().remove(KEY_CUSTOM_URL).commit();
            }
            String normalized = normalizeWebUrl(value);
            if (normalized == null) return false;
            return preferences(context).edit().putString(KEY_CUSTOM_URL, normalized).commit();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int getAutoOpenMinutes(Context context) {
        if (context == null) return 0;
        try {
            int value = preferences(context).getInt(KEY_AUTO_OPEN_MINUTES, 0);
            return isSupportedInterval(value) ? value : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static boolean saveAutoOpenMinutes(Context context, int minutes) {
        if (context == null || !isSupportedInterval(minutes)) return false;
        try {
            return preferences(context).edit().putInt(KEY_AUTO_OPEN_MINUTES, minutes).commit();
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
                    || uri.getHost().trim().isEmpty() || scheme == null) return null;

            if (!"http".equalsIgnoreCase(scheme)
                    && !"https".equalsIgnoreCase(scheme)) return null;
            return candidate;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
