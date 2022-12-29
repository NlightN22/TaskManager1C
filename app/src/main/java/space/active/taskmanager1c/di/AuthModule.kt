package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.repository.AuthorizationImpl
import space.active.taskmanager1c.domain.repository.Authorization

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    fun provideAuthorization(
        taskApi: TaskApi
    ): Authorization = AuthorizationImpl(taskApi)
}