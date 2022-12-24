package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.InputTaskRepositoryImpl
import space.active.taskmanager1c.data.local.db.tasks_room_db.OutputTaskRepositoryImpl
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskInputDao
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskOutputDao
import space.active.taskmanager1c.data.repository.*
import space.active.taskmanager1c.domain.repository.MessagesRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun providesInputTaskRepository(inputDao: TaskInputDao): InputTaskRepository {
        return InputTaskRepositoryImpl(inputDao)
    }

    @Provides
    @Singleton
    fun providesOutputTaskRepository(
        outputDao: TaskOutputDao,
        logger: Logger
    ): OutputTaskRepository {
        return OutputTaskRepositoryImpl(outputDao, logger)
    }

    @Provides
    @Singleton
    fun providesTasksRepository(
        inputRepo: InputTaskRepository,
        outputRepo: OutputTaskRepository,
        logger: Logger,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): TasksRepository {
        return MergedTaskRepositoryImpl(inputRepo, outputRepo, ioDispatcher, logger)
    }

    @Provides
    @Singleton
    fun providesMessagesRepository(
        taskApi: TaskApi
    ): MessagesRepository = MessagesRepositoryImpl(taskApi)
}