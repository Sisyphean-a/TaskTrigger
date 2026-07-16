package com.tasktrigger

import android.app.Application
import com.tasktrigger.data.AppContainer

class TaskTriggerApplication : Application() {
    val container by lazy { AppContainer(this) }
}
