package com.example.twofactorauth

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLogger {

    private const val LOG_DIR = "logs"
    private const val LOG_FILE = "crash_log.txt"

    // Crash logs stay in app-private internal storage so they are not exposed
    // via ADB/backup or readable by other apps.
    private fun logDir(context: Context): File = File(context.filesDir, LOG_DIR)

    fun init(context: Context) {
        val appContext = context.applicationContext
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val logDir = logDir(appContext)
                if (!logDir.exists()) logDir.mkdirs()

                val logFile = File(logDir, LOG_FILE)
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))

                val logEntry = buildString {
                    appendLine("=".repeat(60))
                    appendLine("Crash Time: $timestamp")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Exception: ${throwable.javaClass.name}")
                    appendLine("Message: ${throwable.message}")
                    appendLine()
                    appendLine("Stack Trace:")
                    appendLine(sw.toString())

                    // Log cause chain
                    var cause = throwable.cause
                    var depth = 0
                    while (cause != null && depth < 5) {
                        appendLine()
                        appendLine("Caused by (${depth + 1}):")
                        val causeSw = StringWriter()
                        cause.printStackTrace(PrintWriter(causeSw))
                        appendLine(causeSw.toString())
                        cause = cause.cause
                        depth++
                    }
                    appendLine()
                }

                logFile.appendText(logEntry)
            } catch (e: Exception) {
                // Silently fail if logging fails
            }

            // Let default handler kill the process
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    // Lightweight diagnostic logging. Only active in debug builds so release
    // builds never write app state (account counts, screen transitions) to disk.
    fun log(tag: String, message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(tag, message)
    }

    fun getLogFile(context: Context): File {
        return File(logDir(context), LOG_FILE)
    }
}
