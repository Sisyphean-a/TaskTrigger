package com.tasktrigger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tasktrigger.TaskTriggerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TASK_ID, -1)
        if (taskId < 0) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as TaskTriggerApplication).container
                val task = container.repository.findTask(taskId) ?: return@launch
                if (!task.enabled) return@launch
                container.repository.record(container.executor.execute(task))
                if (task.repeatDays.isBlank()) {
                    container.scheduler.cancel(task)
                } else {
                    container.scheduler.schedule(task)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object { const val TASK_ID = "task_id" }
}
