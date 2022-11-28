package space.active.taskmanager1c.di

import android.app.Application
import androidx.room.Room
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskWithUsersDatabase
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.data.remote.TaskApiImpl
import space.active.taskmanager1c.data.remote.test.GetFromFile
import space.active.taskmanager1c.data.utils.GsonParserImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

//    @Provides
//    @Singleton
//    fun provideTasksFromRemote(dao: TaskInputDao , api: TaskApi): TasksFromRemoteDb {
//        return TasksFromRemoteDbImpl(dao, api)
//    }

    @Provides
    @Singleton
    fun provideTaskWithUsersDao(db: TaskWithUsersDatabase) = db.inputDao

    @Provides
    @Singleton
    fun provideTaskWithUsersDatabase(app: Application): TaskWithUsersDatabase {
        return Room.databaseBuilder(
            app,
            TaskWithUsersDatabase::class.java,
            "input_db"
        )
            .fallbackToDestructiveMigration()
            .addTypeConverter(Converters(GsonParserImpl(Gson())))
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskApi(app: Application): TaskApi {
        return TaskApiImpl(
            jsonParser = GsonParserImpl(Gson()),
            getFromFile = GetFromFile(app)
            )
    }
}