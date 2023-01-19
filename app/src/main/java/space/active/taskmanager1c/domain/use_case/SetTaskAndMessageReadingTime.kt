package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTask
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.MessagesRepository
import java.time.LocalDateTime
import javax.inject.Inject

class SetTaskAndMessageReadingTime @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(
        credentials: Credentials,
        taskId: String,
        messageReadingTime: LocalDateTime,
        taskReadingTime: LocalDateTime
    ): Flow<Request<ReadingTimesTask>> = messagesRepository.sendReadingTime(
        credentials,
        taskId,
        messageReadingTime,
        taskReadingTime
    ).catch {
        exceptionHandler(it)
    }.flowOn(ioDispatcher)
}