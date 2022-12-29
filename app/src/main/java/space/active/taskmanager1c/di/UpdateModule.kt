package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.repository.UpdateJobInterfaceImpl
import space.active.taskmanager1c.domain.repository.UpdateJobInterface

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    fun provideUpdateJobInterface(
        inputTaskRepository: InputTaskRepository,
        outputTaskRepository: OutputTaskRepository,
        taskApi: TaskApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        logger: Logger
    ): UpdateJobInterface = UpdateJobInterfaceImpl(
        inputTaskRepository = inputTaskRepository,
    outputTaskRepository = outputTaskRepository,
    taskApi = taskApi,
    ioDispatcher = ioDispatcher,
    logger = logger
    )
}