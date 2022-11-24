package com.ikhsan.dagger2test

import android.app.Application

class RootApp: Application() {
    companion object {
        val mainComponent: MainComponent by lazy {
            DaggerMainComponent.create()
        }
    }
}