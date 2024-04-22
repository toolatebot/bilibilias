package com.imcys.bilibilias.core.download.task

import com.imcys.bilibilias.core.model.download.FileType
import com.imcys.bilibilias.core.model.download.State
import com.imcys.bilibilias.core.model.video.ViewInfo
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.kotlin.progressFlow
import java.io.File

class AudioTask(url: String, path: String, viewInfo: ViewInfo) :
    AsDownloadTask(viewInfo) {
    override val priority = 100
    override val fileType = FileType.AUDIO
    override val destFile = File(path, "audio.mp4")
    override val task = createTask(url, destFile, priority, viewInfo, fileType)
    override val state: State = getState(task)
    override val isCompleted: Boolean = StatusUtil.isCompleted(task)
    override val progress = task.progressFlow()
    override fun toString(): String {
        return "AudioTask(fileType=$fileType, task=$task, state=$state, isCompleted=$isCompleted)"
    }
}
