package com.imcys.bilibilias.common.di

import io.ktor.client.plugins.logging.Logger
import timber.log.Timber

internal class LoggerManager : Logger {
    override fun log(message: String) {
        Timber.tag("misakamoe").d(message)
    }
}