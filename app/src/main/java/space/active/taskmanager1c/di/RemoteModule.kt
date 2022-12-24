package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.RetrofitConfig
import space.active.taskmanager1c.data.remote.RetrofitTasksSource
import space.active.taskmanager1c.data.repository.TaskApi
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RemoteModule {

    @Provides
    @Singleton
    fun provideTaskApi(
        logger: Logger,
        converters: Converters,
        config: RetrofitConfig
    ): TaskApi = RetrofitTasksSource(logger, converters, config)

}