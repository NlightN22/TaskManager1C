package space.active.taskmanager1c.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.active.taskmanager1c.domain.repository.DataStoreRepository
import javax.inject.Inject

class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DataStoreRepository {

    private object Keys {
        val UserName = stringPreferencesKey("UserName")
        val UserId = stringPreferencesKey("UserId")
        val UserPassword = stringPreferencesKey("UserPassword")
    }

    override suspend fun setUserName(userName: String) = dataStore.set(Keys.UserName, userName)

    override fun getUserName(): Flow<String?> = dataStore.data.map { it[Keys.UserName] }

    override suspend fun setUserId(userId: String) = dataStore.set(Keys.UserId, userId)

    override fun getUserId(): Flow<String?> = dataStore.data.map { it[Keys.UserId] }

    override suspend fun setUserPass(userPass: String) = dataStore.set(Keys.UserPassword, userPass)

    override fun getUserPass(): Flow<String?> = dataStore.data.map { it[Keys.UserPassword] }

    private suspend fun <T> DataStore<Preferences>.set(key: Preferences.Key<T>, value: T) {
        this.edit {
            it[key] = value
        }
    }


}