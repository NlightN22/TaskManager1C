package space.active.taskmanager1c.domain.use_case


// todo delete
//class SaveUserSettingsToDataStoreTMP @Inject constructor(
//    private val dataStore: DataStore<UserSettings>,
//    private val exceptionHandler: ExceptionHandler,
//    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
//) {
//    operator fun invoke(settings: UserSettings) = flow<Request<Any>> {
//        emit(PendingRequest())
//        try {
//            dataStore.updateData { settings }
//            emit(SuccessRequest(Any()))
//        } catch (e: Throwable) {
//            emit(ErrorRequest(e))
//        }
//    }
//        .catch { exceptionHandler(it) }
//        .flowOn(ioDispatcher)
//}