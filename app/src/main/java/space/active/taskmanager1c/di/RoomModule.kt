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
import space.active.taskmanager1c.data.utils.GsonParserImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {

    @Provides
    @Singleton
    fun provideTaskInputDao(db: TaskWithUsersDatabase) = db.inputDao

    @Provides
    @Singleton
    fun provideTaskOutputDao(db: TaskWithUsersDatabase) = db.outputDao

    @Provides
    @Singleton
    fun provideTaskWithUsersDatabase(app: Application): TaskWithUsersDatabase {
        return Room.databaseBuilder(
            app,
            TaskWithUsersDatabase::class.java,
            "tasks_db"
        )
            .fallbackToDestructiveMigration()
            .addTypeConverter(Converters(GsonParserImpl(Gson())))
            .build()
    }

}