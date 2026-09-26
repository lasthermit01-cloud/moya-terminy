package com.customfolders.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

public class FolderWidgetProvider extends AppWidgetProvider {
    public static final String PREF = "widget_map";

    @Override public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) update(c, m, id);
    }

    @Override public void onAppWidgetOptionsChanged(Context c, AppWidgetManager m, int id, Bundle o) {
        update(c, m, id);
    }

    @Override public void onDeleted(Context c, int[] ids) {
        SharedPreferences.Editor e = c.getSharedPreferences(PREF, 0).edit();
        for (int id : ids) e.remove("w" + id);
        e.apply();
    }

    public static void bind(Context c, int widgetId, long folderId) {
        c.getSharedPreferences(PREF, 0).edit().putLong("w" + widgetId, folderId).apply();
        update(c, AppWidgetManager.getInstance(c), widgetId);
    }

    public static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        ComponentName n = new ComponentName(c, FolderWidgetProvider.class);
        for (int id : m.getAppWidgetIds(n)) update(c, m, id);
    }

    public static void update(Context c, AppWidgetManager m, int id) {
        FolderStore store = new FolderStore(c);
        SharedPreferences map = c.getSharedPreferences(PREF, 0);
        long fid = map.getLong("w" + id, -1);

        if (fid < 0) {
            SharedPreferences settings = c.getSharedPreferences("settings", 0);
            long pending = settings.getLong("pending_pin", -1);
            if (pending > 0 && store.get(pending) != null) {
                fid = pending;
                map.edit().putLong("w" + id, fid).apply();
                settings.edit().remove("pending_pin").apply();
            }
        }

        FolderStore.Folder f = store.get(fid);
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget_folder);

        if (f == null) {
            FolderStore.Folder placeholder = new FolderStore.Folder();
            placeholder.name = "Налаштувати";
            placeholder.showTitle = true;
            rv.setImageViewBitmap(R.id.widget_image, WidgetRenderer.render(c, placeholder, 180, 180));
            rv.setTextViewText(R.id.widget_title, "Налаштувати");
            rv.setViewVisibility(R.id.widget_title, View.VISIBLE);
            Intent open = new Intent(c, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(c, 0x70000000 + id, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            rv.setOnClickPendingIntent(R.id.widget_root, pi);
            rv.setOnClickPendingIntent(R.id.widget_image, pi);
            rv.setOnClickPendingIntent(R.id.widget_title, pi);
            m.updateAppWidget(id, rv);
            return;
        }

        Bundle op = m.getAppWidgetOptions(id);
        int minW = op.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110);
        int minH = op.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110);
        int labelDp = f.showTitle ? 24 : 0;
        int w = Math.min(700, Math.max(110, Ui.dp(c, minW)));
        int h = Math.min(700, Math.max(96, Ui.dp(c, Math.max(56, minH - labelDp))));

        rv.setImageViewBitmap(R.id.widget_image, WidgetRenderer.render(c, f, w, h));
        rv.setTextViewText(R.id.widget_title, f.name);
        rv.setViewVisibility(R.id.widget_title, f.showTitle ? View.VISIBLE : View.GONE);

        Intent i = new Intent(c, FolderPopupActivity.class)
                .putExtra("folderId", f.id)
                .setData(Uri.parse("customfolders://open/" + f.id + "/widget/" + id))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, (int) ((id * 31L) & 0x7fffffff), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.widget_root, pi);
        rv.setOnClickPendingIntent(R.id.widget_image, pi);
        rv.setOnClickPendingIntent(R.id.widget_title, pi);
        m.updateAppWidget(id, rv);
    }
}
