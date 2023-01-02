package space.active.taskmanager1c.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import space.active.taskmanager1c.coreutils.CryptoManager
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.*
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.repository.MessagesRepositoryImpl
import space.active.taskmanager1c.data.repository.*
import space.active.taskmanager1c.domain.models.UserSettings
import space.active.taskmanager1c.domain.models.UserSettingsSerializer
import space.active.taskmanager1c.domain.repository.MessagesRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Singleton



@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun providesInputTaskRepository(
        inputDao: TaskInputDao,
        extraDao: TaskExtraInputDao,
        logger: Logger
    ): InputTaskRepository {
        return InputTaskRepositoryImpl(inputDao, extraDao, logger)
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