package space.active.taskmanager1c.di

import android.app.Application
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.remote.TaskApiImpl
import space.active.taskmanager1c.data.remote.test.TaskApiMockk
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.data.utils.GsonParserImpl
import space.active.taskmanager1c.data.utils.JsonParser
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RemoteModule {

    @Provides
    @Singleton
    fun provideTaskApi(app: Application, logger: Logger, converters: Converters): TaskApi {
        return TaskApiImpl(
            taskApiMockk = TaskApiMockk(app),
            logger = logger,
            converters = converters
        )
    }

    @Provides
    @Singleton
    fun providesMock(app: Application) = TaskApiMockk(app)

}