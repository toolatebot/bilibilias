package com.imcys.bilibilias.core.download

import androidx.collection.mutableObjectListOf
import com.imcys.bilibilias.core.common.network.di.ApplicationScope
import com.imcys.bilibilias.core.data.toast.ToastMachine
import com.imcys.bilibilias.core.database.dao.DownloadTaskDao
import com.imcys.bilibilias.core.database.dao.DownloadTaskDao2
import com.imcys.bilibilias.core.database.model.Task
import com.imcys.bilibilias.core.download.chore.DefaultGroupTaskCall
import com.imcys.bilibilias.core.download.task.AsDownloadTask
import com.imcys.bilibilias.core.download.task.GroupTask
import com.imcys.bilibilias.core.model.download.FileType
import com.imcys.bilibilias.core.model.download.State
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.lang.Exception
import javax.inject.Inject

private const val TAG = "DownloadManager"

class AsDownloadListener @Inject constructor(
    private val downloadTaskDao: DownloadTaskDao,
    private val defaultGroupTaskCall: DefaultGroupTaskCall,
    private val toastMachine: ToastMachine,
    private val taskDao: DownloadTaskDao2,
) : DownloadListener1() {
    private val taskQueue = mutableObjectListOf<AsDownloadTask>()

    fun add(task: AsDownloadTask) {
        taskQueue.add(task)
        task.okTask.enqueue(this)
    }

    override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
        Napier.d(tag = TAG) { "任务开始 $task" }
        val asTask = taskQueue.first { it.okTask === task }
        val info = asTask.viewInfo
        val taskEntity = Task(
            uri = asTask.okTask.uri,
            created = Clock.System.now(),
            aid = info.aid,
            bvid = info.bvid,
            cid = info.cid,
            fileType = asTask.fileType,
            subTitle = asTask.subTitle,
            title = info.title,
            state = State.RUNNING,
        )
        taskDao.insertOrUpdateTask(taskEntity)
    }

    override fun taskEnd(
        task: DownloadTask,
        cause: EndCause,
        realCause: Exception?,
        model: Listener1Assist.Listener1Model
    ) {
        Napier.d(tag = TAG, throwable = realCause) { "任务结束 $cause-${task.file?.path}" }
        val asDownloadTask = taskQueue.first { it.okTask === task }
        toast(realCause, asDownloadTask)

        taskDao.updateStateByUri(
            if (realCause == null) State.COMPLETED else State.ERROR,
            task.uri,
        )

        val info = asDownloadTask.viewInfo
        val tasks = taskDao.findById(info.aid, info.bvid, info.cid)
        val v = tasks.find { it.fileType == FileType.VIDEO }
        val a = tasks.find { it.fileType == FileType.AUDIO }
        if (v != null && v.state == State.COMPLETED) {
            if (a != null && a.state == State.COMPLETED) {
                defaultGroupTaskCall.execute(GroupTask(v, a))
            }
        }
    }

    private fun toast(
        realCause: Exception?,
        task: AsDownloadTask
    ) {
        realCause?.message
//        val toastState = if (realCause == null) {
//            AsToastState("${task.subTitle}·${task.fileType}下载成功", AsToastType.Normal)
//        } else {
//            AsToastState("${task.subTitle}·${task.fileType}下载失败", AsToastType.Error)
//        }
//        toastMachine.show(toastState)
    }

    override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
        Napier.d(tag = TAG) { "任务中 ${task.filename} $currentOffset-$totalLength" }
        taskDao.updateProgressWithStateByUri(
            State.RUNNING,
            currentOffset,
            totalLength,
            task.uri,
        )
    }

    override fun retry(task: DownloadTask, cause: ResumeFailedCause) = Unit

    override fun connected(
        task: DownloadTask,
        blockCount: Int,
        currentOffset: Long,
        totalLength: Long
    ) = Unit
}
