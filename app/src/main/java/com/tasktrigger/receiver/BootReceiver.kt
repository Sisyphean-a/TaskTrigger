package com.tasktrigger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tasktrigger.TaskTriggerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = (context.applicationContext as TaskTriggerApplication).container
                container.repository.enabledTasks().forEach(container.scheduler::schedule)
            } finally {
                result.finish()
            }
        }
    }
}
