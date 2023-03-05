package space.active.taskmanager1c.di

import com.google.gson.Gson
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
        gson: Gson,
        retrofit: Retrofit
    ): TaskApi = RetrofitTasksSource(logger, gson, retrofit)

}