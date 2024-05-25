package com.imcys.bilibilias.feature.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.imcys.bilibilias.core.datastore.login.LoginInfoDataSource
import com.imcys.bilibilias.core.network.repository.LoginRepository
import com.imcys.bilibilias.feature.common.BaseViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LoginComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    private val loginRepository: LoginRepository,
    private val loginInfoDataSource: LoginInfoDataSource,
) : BaseViewModel<LoginEvent, LoginModel>(componentContext) {

    @Composable
    override fun models(events: Flow<LoginEvent>): LoginModel {
        return LoginPresenter(events, loginRepository, loginInfoDataSource)
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
        ): LoginComponent
    }
}

@Composable
private fun LoginPresenter(
    events: Flow<LoginEvent>,
    loginRepository: LoginRepository,
    loginInfoDataSource: LoginInfoDataSource
): LoginModel {
    var qrUrl by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val (qrcodeKey, url) = loginRepository.获取二维码()
        key = qrcodeKey
        qrUrl = url
    }

    LaunchedEffect(key) {
        var ok = false
        withTimeout(3.minutes) {
            while (!ok && isActive) {
                delay(1.seconds)
                val qrcodePoll = loginRepository.轮询登录(key)
                ok = qrcodePoll.success
                isSuccess = qrcodePoll.success
                message = qrcodePoll.message
                loginInfoDataSource.setRefreshToken(qrcodePoll.refreshToken)
            }
        }
    }
    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                LoginEvent.RefreshQrCode -> {
                    val (qrcodeKey, url) = loginRepository.获取二维码()
                    key = qrcodeKey
                    qrUrl = url
                }
            }
        }
    }
    val painter = rememberQrCodePainter(data = qrUrl)
    return LoginModel(isSuccess, message, painter)
}
