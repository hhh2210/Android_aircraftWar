package edu.hitsz.rank;

import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.hitsz.R;

public final class RankSaveDialog {

    private RankSaveDialog() {
    }

    public static AlertDialog show(AppCompatActivity activity, RankDbHelper rankDbHelper,
                                   int score, String difficulty, OnRecordSavedListener listener) {
        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.dialog_username_hint);
        input.setSingleLine(true);

        FrameLayout container = new FrameLayout(activity);
        int paddingPx = (int) (20 * activity.getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, 0, paddingPx, 0);
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_username_title)
                .setMessage(String.format(Locale.getDefault(),
                        activity.getString(R.string.dialog_username_message), score))
                .setView(container)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_username_confirm, (dialogInterface, which) -> {
                    String username = input.getText().toString().trim();
                    if (username.isEmpty()) {
                        username = activity.getString(R.string.dialog_username_default);
                    }
                    rankDbHelper.insert(new RankRecord(score, difficulty,
                            getCurrentTimestamp(), username));
                    if (listener != null) {
                        listener.onRecordSaved();
                    }
                })
                .create();
        dialog.show();
        return dialog;
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }

    public interface OnRecordSavedListener {
        void onRecordSaved();
    }
}
