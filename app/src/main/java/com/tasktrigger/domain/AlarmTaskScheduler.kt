package com.tasktrigger.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.tasktrigger.data.TaskEntity
import com.tasktrigger.receiver.TaskAlarmReceiver

class AlarmTaskScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(task: TaskEntity) {
        val triggerAt = TaskSchedule.nextTrigger(task) ?: return cancel(task)
        val alarm = alarmManager ?: error("AlarmManager 不可用")
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(task.id))
    }

    fun cancel(task: TaskEntity) {
        alarmManager?.cancel(pendingIntent(task.id))
    }

    private fun pendingIntent(taskId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        Intent(context, TaskAlarmReceiver::class.java).putExtra(TaskAlarmReceiver.TASK_ID, taskId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
