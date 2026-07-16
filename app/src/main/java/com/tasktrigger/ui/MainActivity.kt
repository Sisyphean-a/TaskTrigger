package com.tasktrigger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasktrigger.TaskTriggerApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as TaskTriggerApplication).container
        setContent {
            TaskTriggerTheme {
                TaskTriggerApp(TaskViewModel(container))
            }
        }
    }
}
