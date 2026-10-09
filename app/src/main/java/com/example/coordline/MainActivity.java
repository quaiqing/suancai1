package com.example.coordline;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;
    private EditText x1, y1, x2, y2, width, alpha, color;
    private CheckBox visible;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(LineService.PREF, MODE_PRIVATE);
        buildUi();
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15);
        view.setTextColor(Color.rgb(35, 35, 35));
        view.setPadding(0, dp(8), 0, dp(4));
        return view;
    }

    private EditText field(String hint, String value) {
        EditText view = new EditText(this);
        view.setSingleLine(true);
        view.setText(value);
        view.setHint(hint);
        view.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        view.setPadding(dp(10), dp(7), dp(10), dp(7));
        return view;
    }

    private Button button(String text, Runnable action) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(v -> action.run());
        return button;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(18));
        root.setBackgroundColor(Color.WHITE);
        scroll.addView(root);
        setContentView(scroll);

        TextView title = label("坐标辅助线");
        title.setTextSize(24);
        title.setTypeface(null, 1);
        root.addView(title);
        root.addView(label("坐标单位为屏幕像素，原点位于左上角。所有操作均在本应用内完成。"));

        root.addView(label("起点 X1 / Y1"));
        LinearLayout row1 = row();
        x1 = field("X1", value("x1", 100));
        y1 = field("Y1", value("y1", 1600));
        row1.addView(x1, new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(space());
        row1.addView(y1, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row1);

        root.addView(label("终点 X2 / Y2"));
        LinearLayout row2 = row();
        x2 = field("X2", value("x2", 1340));
        y2 = field("Y2", value("y2", 1600));
        row2.addView(x2, new LinearLayout.LayoutParams(0, -2, 1));
        row2.addView(space());
        row2.addView(y2, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row2);

        root.addView(label("线条粗细（px）"));
        width = field("4", value("width", 4));
        root.addView(width);

        root.addView(label("线条颜色（例如 #FFFF3030）"));
        color = new EditText(this);
        color.setSingleLine(true);
        color.setText(String.format("#%08X", prefs.getInt("color", Color.RED)));
        color.setInputType(InputType.TYPE_CLASS_TEXT);
        root.addView(color);

        root.addView(label("线条透明度（0–100）"));
        alpha = field("85", value("alpha", 85));
        root.addView(alpha);

        visible = new CheckBox(this);
        visible.setText("显示线条");
        visible.setChecked(prefs.getBoolean("visible", true));
        root.addView(visible);

        root.addView(button("保存并显示", () -> {
            visible.setChecked(true);
            apply(true);
        }));
        root.addView(button("应用设置", () -> apply(visible.isChecked())));
        root.addView(button("隐藏线条", () -> {
            visible.setChecked(false);
            apply(false);
        }));
        root.addView(button("停止悬浮", this::stopOverlay));

        root.addView(label("首次使用需授权悬浮窗。部分系统或游戏可能限制触摸穿透。"));
        if (!Settings.canDrawOverlays(this)) {
            root.addView(button("授权悬浮窗", () -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }));
        }
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private View space() {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1));
        return view;
    }

    private String value(String key, int fallback) {
        return String.valueOf(prefs.getInt(key, fallback));
    }

    private int number(EditText field, int fallback) {
        try {
            return Integer.parseInt(field.getText().toString().trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void apply(boolean start) {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "请先授权悬浮窗", Toast.LENGTH_LONG).show();
            return;
        }

        int parsedColor;
        try {
            parsedColor = Color.parseColor(color.getText().toString().trim());
        } catch (Exception e) {
            Toast.makeText(this, "颜色格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putInt("x1", number(x1, 100))
                .putInt("y1", number(y1, 1600))
                .putInt("x2", number(x2, 1340))
                .putInt("y2", number(y2, 1600))
                .putInt("width", Math.max(1, Math.min(100, number(width, 4))))
                .putInt("alpha", Math.max(0, Math.min(100, number(alpha, 85))))
                .putInt("color", parsedColor)
                .putBoolean("visible", visible.isChecked())
                .apply();

        if (start) {
            Intent service = new Intent(this, LineService.class);
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(service);
            else startService(service);
        } else {
            LineService.refreshIfRunning();
        }
        Toast.makeText(this, "设置已应用", Toast.LENGTH_SHORT).show();
    }

    private void stopOverlay() {
        stopService(new Intent(this, LineService.class));
        Toast.makeText(this, "悬浮已停止", Toast.LENGTH_SHORT).show();
    }

}
