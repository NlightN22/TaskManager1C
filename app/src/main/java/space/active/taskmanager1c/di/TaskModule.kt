package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.HandleEmptyTaskList
import space.active.taskmanager1c.domain.use_case.HandleJobForUpdateDb
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//object TaskModule {
//
//    @Provides
//    @Singleton
//    fun provideHandleEmptyTaskList(
//        repository: TasksRepository,
//        updateJob: HandleJobForUpdateDb,
//        dispatcher: IoDispatcher
//    ): HandleEmptyTaskList = HandleEmptyTaskList(repository, updateJob)
//
//}