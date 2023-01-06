package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.EncryptedData.Companion.toEncryptedData
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.SettingsDao
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.UserSettings
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
    private val logger: Logger
) : SettingsRepository {

    override suspend fun getUser(): User {
        settingsDao.getSettings()?.let {
            return User(
                id = it.userId ?: throw EmptyObject("userId"),
                name = it.username?.getString() ?: throw EmptyObject("username")
            )
        } ?: throw EmptyObject("user")
    }

    override fun getUserFlow(): Flow<User> = flow {
        getUser()?: throw EmptyObject("getUserFlow")
    }

    override fun saveUser(user: User): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.getSettings()?.let {
            settingsDao.insert(it.copy(username = user.name.toEncryptedData(), userId = user.id))
        } ?: kotlin.run {
            settingsDao.insert(
                UserSettings(
                    userId = user.id,
                    username = user.name.toEncryptedData()
                )
            )
        }
        emit(SuccessRequest(Any()))
    }

    //todo delete
//    override suspend fun getPassword(): String? {
//        settingsDao.getSettings()?.let {
//            return it.password?.getString()
//        } ?: return null
//    }

    override fun savePassword(pass: String): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.getSettings()?.let {
            settingsDao.insert(it.copy(password = pass.toEncryptedData()))
        } ?: kotlin.run {
            UserSettings(
                password = pass.toEncryptedData()
            )
        }
        emit(SuccessRequest(Any()))
    }

    override suspend fun getServerAddress(): String? {
        settingsDao.getSettings()?.let {
            return it.serverAddress?.getString()
        } ?: return null
    }

    override fun saveServerAddress(serverAddress: String): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.getSettings()?.let {
            settingsDao.insert(it.copy(serverAddress = serverAddress.toEncryptedData()))
        } ?: kotlin.run {
            UserSettings(
                serverAddress = serverAddress.toEncryptedData()
            )
        }
        emit(SuccessRequest(Any()))
    }

    override fun clearSettings(): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.deleteAll()
        emit(SuccessRequest(Any()))
    }

    override fun getCredentials(): Flow<Credentials> = flow {
        settingsDao.getSettings()?.let {
            val cred = Credentials(it.username?: throw EmptyObject("username")
                , it.password?: throw EmptyObject("password")
            )
            emit(cred)
        }?: throw EmptyObject("Credentials")
    }
}