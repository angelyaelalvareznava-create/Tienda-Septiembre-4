package com.example.tiendita

import android.app.Application
import com.example.tiendita.di.AppContainer

class NexoApplication : Application() {
    private val dependencies = lazy { AppContainer(this) }
    val container get() = dependencies.value
    // Android kills the process without onTerminate; this releases resources in managed/test lifecycles.
    override fun onTerminate() { if (dependencies.isInitialized()) container.close(); super.onTerminate() }
}
