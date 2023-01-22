package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.MessagesRepository
import javax.inject.Inject

class GetUnreadListIds @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(credentials: Credentials, listIDs: List<String>): Flow<Request<List<String>>> =
        messagesRepository.getUnreadTaskIds(credentials, listIDs)
            .catch {
                exceptionHandler(it)
            }
            .flowOn(ioDispatcher)
}