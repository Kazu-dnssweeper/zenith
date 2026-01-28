package com.iterio.app.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class IterioWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = IterioWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            ACTION_DATA_CHANGED -> {
                IterioWidgetStateHelper.updateWidget(context)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.iterio.app.action.UPDATE_WIDGET"
        const val ACTION_DATA_CHANGED = "com.iterio.app.action.DATA_CHANGED"

        fun sendUpdateBroadcast(context: Context) {
            val intent = Intent(context, IterioWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }

        fun sendDataChangedBroadcast(context: Context) {
            val intent = Intent(context, IterioWidgetReceiver::class.java).apply {
                action = ACTION_DATA_CHANGED
            }
            context.sendBroadcast(intent)
        }
    }
}
