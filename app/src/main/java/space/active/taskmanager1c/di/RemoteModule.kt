package space.active.taskmanager1c.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.retrofit.RetrofitTasksSource
import space.active.taskmanager1c.data.remote.TaskApi
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RemoteModule {

    @Provides
    @Singleton
    fun provideTaskApi(
        logger: Logger,
        converters: Converters,
        retrofit: Retrofit
    ): TaskApi = RetrofitTasksSource(logger, converters, retrofit)

}