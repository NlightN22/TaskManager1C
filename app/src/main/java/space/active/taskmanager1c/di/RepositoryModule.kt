package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.*
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.repository.*
import space.active.taskmanager1c.domain.repository.MessagesRepository
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun providesInputTaskRepository(
        inputDao: TaskInputDao,
        readingDao: TaskReadingDao,
        logger: Logger
    ): InputTaskRepository {
        return InputTaskRepositoryImpl(inputDao, readingDao, logger)
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

    @Provides
    @Singleton
    fun providesSettingRepository(
        settingsDao: SettingsDao,
        logger: Logger
    ): SettingsRepository = SettingsRepositoryImpl(settingsDao, logger)

}