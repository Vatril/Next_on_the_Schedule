package com.vatril.nextontheschedule

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.vatril.nextontheschedule.entity.TaskDatabase
import kotlinx.coroutines.runBlocking

/**
 * Implementation of App Widget functionality.
 */
class TaskWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null && context != null && intent.hasExtra("com.vatril.TaskWidget")) {
            val manager = AppWidgetManager.getInstance(context)
            this.onUpdate(
                context, manager,
                manager.getAppWidgetIds(ComponentName(context, TaskWidget::class.java))
            )
        }
    }

    companion object {

        fun updateAll(context: Context) {
            val awm = AppWidgetManager.getInstance(context)
            val intent = Intent()
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra("com.vatril.TaskWidget", 5432)
            context.sendBroadcast(intent)
        }

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            runBlocking {
                val tasks = TaskDatabase.getDatabase(context).taskDao().getAll()

                if (!tasks.isNullOrEmpty()) {

                    val task = tasks.first()

                    val views = RemoteViews(context.packageName, R.layout.task_widget)
                    views.setTextViewText(R.id.widgetTitle, task.name)
                    views.setTextViewText(R.id.widgetDescription, task.description)
                    views.setInt(R.id.widgetColor, "setBackgroundColor", task.color)
                    val intent = Intent(context, RemoveTask::class.java)
                    intent.putExtra("task", task.uid)
                    views.setViewVisibility(R.id.dismiss, View.VISIBLE)
                    views.setOnClickPendingIntent(
                        R.id.dismiss, PendingIntent.getService(
                            context,
                            5421,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }else{
                    val views = RemoteViews(context.packageName, R.layout.task_widget)
                    views.setTextViewText(R.id.widgetTitle, "No Tasks")
                    views.setViewVisibility(R.id.dismiss, View.INVISIBLE)
                    views.setInt(R.id.widgetColor, "setBackgroundColor", 0)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }

        }
    }
}

