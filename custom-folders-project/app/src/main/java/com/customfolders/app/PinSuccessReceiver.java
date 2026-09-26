package com.customfolders.app;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Receives the real widget ID after requestPinAppWidget succeeds. */
public class PinSuccessReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        long folderId = intent.getLongExtra("folderId", -1);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID && folderId > 0) {
            FolderWidgetProvider.bind(context, widgetId, folderId);
            context.getSharedPreferences("settings", 0).edit().remove("pending_pin").apply();
        }
    }
}
