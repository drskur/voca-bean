package com.drskur.vocabean

import android.app.Application
import com.drskur.vocabean.di.AppContainer

class VocaBeanApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
