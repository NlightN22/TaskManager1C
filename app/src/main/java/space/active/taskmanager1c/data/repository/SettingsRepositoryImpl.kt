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
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SettingsRepositoryImpl"

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val logger: Logger
) : SettingsRepository {

    override suspend fun getUser(): UserDomain {
        settingsDao.getSettings()?.let {
            return UserDomain(
                id = it.userId ?: throw EmptyObject("userId"),
                name = it.username?.getString() ?: throw EmptyObject("username")
            )
        } ?: throw EmptyObject("userDomain")
    }

    override fun getUserFlow(): Flow<UserDomain> = flow {
        emit(getUser()) ?: throw EmptyObject("getUserFlow")
    }

    override fun saveUser(userDomain: UserDomain): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.getSettings()?.let {
            settingsDao.insert(
                it.copy(
                    username = userDomain.name.toEncryptedData(),
                    userId = userDomain.id
                )
            )
        } ?: kotlin.run {
            settingsDao.insert(
                UserSettings(
                    userId = userDomain.id,
                    username = userDomain.name.toEncryptedData()
                )
            )
        }
        emit(SuccessRequest(Any()))
    }

    override fun savePassword(pass: String): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.getSettings()?.let {
            settingsDao.insert(it.copy(password = pass.toEncryptedData()))
        } ?: kotlin.run {
            settingsDao.insert(
                UserSettings(
                    password = pass.toEncryptedData()
                )
            )
        }
        emit(SuccessRequest(Any()))
    }

    override suspend fun getServerAddress(): String {
        settingsDao.getSettings()?.let {
            return it.serverAddress?.getString() ?: throw EmptyObject("serverAddress")
        } ?: throw EmptyObject("serverAddress")
    }

    override suspend fun saveServerAddress(serverAddress: String) {
        settingsDao.getSettings()?.let {
            settingsDao.insert(it.copy(serverAddress = serverAddress.toEncryptedData()))
        } ?: kotlin.run {
            settingsDao.insert(
                UserSettings(
                    serverAddress = serverAddress.toEncryptedData()
                )
            )
        }
    }


    override fun clearSettings(): Flow<Request<Any>> = flow {
        emit(PendingRequest())
        settingsDao.deleteAll()
        emit(SuccessRequest(Any()))
    }

    override fun getCredentials(): Flow<Credentials> = flow {
        settingsDao.getSettings()?.let {
            val cred = Credentials(
                it.username ?: throw EmptyObject("username"),
                it.password ?: throw EmptyObject("password")
            )
            emit(cred)
        } ?: throw EmptyObject("Credentials")
    }

    override suspend fun saveSkipStatusAlert(state: Boolean) {
        settingsDao.updateStatusAlert(state)
    }

    override suspend fun getSkipStatusAlert(): Boolean = settingsDao.getStatusAlert()
}