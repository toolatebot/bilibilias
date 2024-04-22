package com.imcys.bilibilias.core.download.task

import com.imcys.bilibilias.core.model.download.FileType
import com.imcys.bilibilias.core.download.TaskEnd
import com.imcys.bilibilias.core.model.download.State
import com.imcys.bilibilias.core.model.video.ViewInfo
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.kotlin.DownloadProgress
import com.liulishuo.okdownload.kotlin.enqueue1
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import java.io.File

sealed class AsDownloadTask(val viewInfo: ViewInfo) {
    internal abstract val priority: Int
    internal abstract val destFile: File
    abstract val fileType: FileType
    internal abstract val task: DownloadTask
    abstract val state: State
    abstract val isCompleted: Boolean

    abstract val progress: Flow<DownloadProgress>
    protected fun getState(task: DownloadTask): State {
        return when (StatusUtil.getStatus(task)) {
            StatusUtil.Status.PENDING -> State.PENDING
            StatusUtil.Status.RUNNING -> State.RUNNING
            StatusUtil.Status.COMPLETED -> State.COMPLETED
            StatusUtil.Status.IDLE -> State.IDLE
            StatusUtil.Status.UNKNOWN -> State.UNKNOWN
            null -> error("获取的状态: null")
        }
    }

    protected fun createTask(
        url: String,
        file: File,
        priority: Int,
        info: ViewInfo,
        fileType: FileType
    ): DownloadTask {
        return DownloadTask.Builder(url, file)
            .setPassIfAlreadyCompleted(true)
            .setAutoCallbackToUIThread(false)
            .setHeaderMapFields(
                mapOf(
                    "User-Agent" to
                            listOf("Mozilla/4.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/70.0.3538.77 Chrome/70.0.3538.77 Safari/537.36"),
                    "Referer" to listOf("https://www.bilibili.com/")
                )
            )
            .setPriority(priority)
            .build()
            .apply {
                addTag(INDEX_INFO, info)
                addTag(INDEX_TYPE, fileType)
            }
    }

    internal fun executeTask(onEnd: TaskEnd) {
        task.enqueue1 { task, cause, realCause, model ->
            val info = getDownloadInfo(task)
            Napier.d { "完成任务 ${toString()}" }
            onEnd(info.first, info.second)
        }
    }

    abstract override fun toString(): String

    companion object {
        internal fun getDownloadInfo(task: DownloadTask): Pair<ViewInfo, FileType> {
            return task.getTag(INDEX_INFO) as ViewInfo to task.getTag(INDEX_TYPE) as FileType
        }
    }
}

private const val INDEX_INFO = 0
private const val INDEX_TYPE = 1
