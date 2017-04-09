package main

fun main(args: Array<String>) {

    if (!DB.connected) {
        return
    }

    Settings.load(AppSettings)
    Settings.saveOnShutdown(AppSettings)

    // The app won't exit while the scheduler is running.
    appScheduler.start()

    setupEndOfDayFetcher()
}