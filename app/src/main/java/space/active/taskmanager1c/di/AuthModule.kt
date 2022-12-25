package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.data.remote.test.AuthMockImpl
import space.active.taskmanager1c.domain.repository.Authorization

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    fun provideAuthorization(): Authorization = AuthMockImpl()
}