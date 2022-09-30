package space.active.taskmanager1c.di

import android.app.Application
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import space.active.taskmanager1c.data.local.db.Converters
import space.active.taskmanager1c.data.local.db.TasksFromRemote.TaskWithUsersDao
import space.active.taskmanager1c.data.local.db.TasksFromRemote.TaskWithUsersDatabase
import space.active.taskmanager1c.data.local.db.TasksFromRemote.TasksFromRemoteDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.TaskDb
import space.active.taskmanager1c.data.local.db.TasksFromRemoteDbImpl
import space.active.taskmanager1c.data.remote.DemoData
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.utils.GsonParserImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

    @Provides
    @Singleton
    fun provideTasksFromRemote(dao: TaskWithUsersDao , api: TaskApi): TasksFromRemoteDb {
        return TasksFromRemoteDbImpl(dao, api)
    }

    @Provides
    @Singleton
    fun provideTaskWithUsersDao(db: TaskWithUsersDatabase) = db.dao

    @Provides
    @Singleton
    fun provideTaskWithUsersDatabase(app: Application): TaskWithUsersDatabase {
        return Room.databaseBuilder(
            app,
            TaskWithUsersDatabase::class.java,
            "input_db"
        ).addTypeConverter(Converters(GsonParserImpl(Gson())))
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskApi(): TaskApi {
        return DemoData()
    }
}