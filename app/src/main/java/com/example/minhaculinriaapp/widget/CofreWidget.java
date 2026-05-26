package com.example.minhaculinriaapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import com.example.minhaculinriaapp.MainActivity;
import com.example.minhaculinriaapp.R;

import java.util.Locale;

public class CofreWidget extends AppWidgetProvider {

    public static final String PREFS_WIDGET = "widget_prefs";
    public static final String KEY_TIMER_LABEL = "timer_label";
    public static final String KEY_TIMER_REMAINING = "timer_remaining";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(Context ctx, AppWidgetManager mgr, int[] ids) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_WIDGET, Context.MODE_PRIVATE);
        String label = prefs.getString(KEY_TIMER_LABEL, null);
        int remaining = prefs.getInt(KEY_TIMER_REMAINING, 0);

        // PendingIntent para abrir o app ao tocar no widget
        Intent launchIntent = new Intent(ctx, MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        for (int id : ids) {
            RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_cofre);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

            if (label != null) {
                views.setTextViewText(R.id.widget_timer_label, label);
                views.setTextViewText(R.id.widget_timer_remaining,
                        String.format(Locale.getDefault(), "%02d:%02d",
                                remaining / 60, remaining % 60));
                views.setViewVisibility(R.id.widget_active_layout, View.VISIBLE);
                views.setViewVisibility(R.id.widget_idle_text, View.GONE);
            } else {
                views.setViewVisibility(R.id.widget_active_layout, View.GONE);
                views.setViewVisibility(R.id.widget_idle_text, View.VISIBLE);
            }
            mgr.updateAppWidget(id, views);
        }
    }
}
