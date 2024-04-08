package com.imcys.bilibilias.core.network.download

import com.imcys.bilibilias.core.common.network.di.ApplicationScope
import com.imcys.bilibilias.core.network.repository.VideoRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MAX_RUNNING = 4

class Downloader @Inject constructor(
    private val videoRepository: VideoRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val ready = ArrayDeque<Task>(MAX_RUNNING)
    private val running = ArrayDeque<Task>(MAX_RUNNING)
    private val completed = ArrayDeque<Task>()
    private val _taskFlow = Channel<List<Task>>(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val taskFlow = _taskFlow.consumeAsFlow()
    fun dispatch(task: Task) {
        ready.add(task)
        _taskFlow.trySend(allTask())
        promoteAndExecute()
    }

    fun allTask() = readyTask() + runningTask()
    fun readyTask(): List<Task> {
        return ready.toList()
    }

    fun runningTask(): List<Task> {
        return running.toList()
    }

    fun cancel(task: Task) {
        Napier.d { task.toString() }
        ready.remove(task)
        running.remove(task)
        _taskFlow.trySend(allTask())
        task.cancelTask()
    }

    private fun promoteAndExecute() {
        val i = ready.iterator()
        while (i.hasNext() && running.size < MAX_RUNNING) {
            val task = i.next()
            running.add(task)
            execute(task)
            i.remove()
        }
    }

    private fun execute(task: Task) {
        task.job = scope.launch {
            changeRunningState(task, true)
            videoRepository.downloader(task.url, task.path)
                .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST).collect {
                    when (it) {
                        is DownloadResult.Error -> {
                            Napier.d { "下载错误 ${it.message} " }
                            finished(task)
                            addErrorMessage(task, it)
                        }

                        is DownloadResult.Progress -> {
                            task.progress = it.percent
                        }

                        DownloadResult.Success -> {
                            finished(task)
                            Napier.d { "下载进度成功" }
                        }
                    }
                }
        }
    }

    private fun finished(task: Task) {
        finished(running, task)
    }

    private fun finished(deque: ArrayDeque<Task>, task: Task) {
        completed.add(task)
        promoteAndExecute()
        changeRunningState(task, false)
    }

    private fun changeRunningState(task: Task, isRunning: Boolean) {
        task.isRunning = isRunning
    }

    private fun addErrorMessage(task: Task, message: DownloadResult.Error) {
        task.errorMessage = message.message
        changeRunningState(task, false)
    }
}
