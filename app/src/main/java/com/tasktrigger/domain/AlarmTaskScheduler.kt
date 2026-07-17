package com.tasktrigger.domain

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tasktrigger.data.TaskEntity
import com.tasktrigger.receiver.TaskAlarmReceiver

class AlarmTaskScheduler(private val context: Context) : TaskScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(task: TaskEntity): ScheduleResult {
        val triggerAt = TaskSchedule.nextTrigger(task) ?: return cancel(task)
        val alarm = alarmManager ?: return ScheduleResult.Failure(
            LogReason.ALARM_MANAGER_UNAVAILABLE,
            "AlarmManager 不可用",
        )
        if (Build.VERSION.SDK_INT >= 31 && !alarm.canScheduleExactAlarms()) {
            return ScheduleResult.Failure(
                LogReason.EXACT_ALARM_DENIED,
                "未获得精确定时权限，请先授权。",
            )
        }
        return runCatching {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(task.id))
            ScheduleResult.Scheduled
        }.getOrElse {
            ScheduleResult.Failure(LogReason.SCHEDULE_EXCEPTION, it.message ?: "定时注册失败")
        }
    }

    override fun cancel(task: TaskEntity): ScheduleResult {
        val alarm = alarmManager ?: return ScheduleResult.Failure(
            LogReason.ALARM_MANAGER_UNAVAILABLE,
            "AlarmManager 不可用",
        )
        return runCatching {
            alarm.cancel(pendingIntent(task.id))
            ScheduleResult.Scheduled
        }.getOrElse {
            ScheduleResult.Failure(LogReason.CANCEL_FAILED, it.message ?: "取消定时失败")
        }
    }

    private fun pendingIntent(taskId: Long): PendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.hashCode(),
        Intent(context, TaskAlarmReceiver::class.java).putExtra(TaskAlarmReceiver.TASK_ID, taskId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

sealed interface ScheduleResult {
    data object Scheduled : ScheduleResult
    data class Failure(val reason: LogReason, val message: String) : ScheduleResult
}
