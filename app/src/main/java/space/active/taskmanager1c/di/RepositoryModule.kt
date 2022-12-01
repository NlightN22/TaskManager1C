package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.InputTaskRepositoryImpl
import space.active.taskmanager1c.data.local.db.tasks_room_db.OutputTaskRepositoryImpl
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskInputDao
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskOutputDao
import space.active.taskmanager1c.data.repository.*
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.repository.UpdateJobHandler
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
    fun providesOutputTaskRepository(outputDao: TaskOutputDao): OutputTaskRepository {
        return OutputTaskRepositoryImpl(outputDao)
    }

    @Provides
    @Singleton
    fun providesTasksRepository(
        inputRepo: InputTaskRepository,
        outputRepo: OutputTaskRepository,
        logger: Logger
    ): TasksRepository {
        return MergedTaskRepositoryImpl(inputRepo, outputRepo, logger)
    }

    @Provides
    @Singleton
    fun providesUpdateJobHandler(
        inputRepo: InputTaskRepository,
        outputRepo: OutputTaskRepository,
        taskApi: TaskApi,
        logger: Logger
    ): UpdateJobHandler {
        return UpdateJobHandlerImpl(inputRepo, outputRepo, taskApi, logger)
    }



}