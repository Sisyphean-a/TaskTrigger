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
                val outcome = container.operations.handleAlarm(taskId)
                outcome.warnings.firstOrNull()?.let { throw IllegalStateException(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object { const val TASK_ID = "task_id" }
}
