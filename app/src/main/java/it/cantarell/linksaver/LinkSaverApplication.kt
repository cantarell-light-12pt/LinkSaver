package it.cantarell.linksaver

import android.app.Application

class LinkSaverApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
