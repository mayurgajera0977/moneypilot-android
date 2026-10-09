package com.moneypilot;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

public class MoneyPilotWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        String style = context.getSharedPreferences("moneypilot_widget", Context.MODE_PRIVATE).getString("style", "aurora");
        int bg = Color.rgb(240,243,255), title = Color.rgb(53,68,106), subtitle = Color.rgb(123,136,168), accent = Color.rgb(111,133,247);
        if ("mint".equals(style)) { bg=Color.rgb(230,250,244); title=Color.rgb(35,91,77); subtitle=Color.rgb(83,137,124); accent=Color.rgb(66,190,157); }
        else if ("peach".equals(style)) { bg=Color.rgb(255,241,235); title=Color.rgb(112,64,70); subtitle=Color.rgb(157,112,119); accent=Color.rgb(243,151,165); }
        else if ("minimal".equals(style)) { bg=Color.WHITE; title=Color.rgb(53,68,106); subtitle=Color.rgb(123,136,168); accent=Color.rgb(224,232,255); }
        else if ("glass".equals(style)) { bg=Color.rgb(238,243,255); title=Color.rgb(56,73,116); subtitle=Color.rgb(113,127,165); accent=Color.rgb(194,181,255); }
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_moneypilot);
            views.setInt(R.id.widget_root, "setBackgroundColor", bg);
            views.setTextColor(R.id.widget_title, title);
            views.setTextColor(R.id.widget_subtitle, subtitle);
            views.setInt(R.id.widget_expense, "setBackgroundColor", accent);
            views.setInt(R.id.widget_income, "setBackgroundColor", accent);
            views.setTextColor(R.id.widget_expense, title);
            views.setTextColor(R.id.widget_income, title);
            views.setOnClickPendingIntent(R.id.widget_expense, launch(context, "expense", id * 2 + 1));
            views.setOnClickPendingIntent(R.id.widget_income, launch(context, "income", id * 2 + 2));
            manager.updateAppWidget(id, views);
        }
    }
    private PendingIntent launch(Context context, String kind, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("quick_entry_kind", kind);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
