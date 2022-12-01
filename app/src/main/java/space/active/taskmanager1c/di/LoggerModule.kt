package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.coreutils.logger.LoggerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LoggerModule {

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return LoggerImpl
    }
}