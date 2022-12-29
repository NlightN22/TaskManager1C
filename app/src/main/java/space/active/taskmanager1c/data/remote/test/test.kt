package space.active.taskmanager1c.data.remote

//private val API_USERNAME = "Михайлов Олег Федорович"
//private val API_PASSWORD = "test"
//private val API_URL = "http://172.16.17.242/torg_develop/hs/taskmgr/"
//
//interface Api {
//    @GET("tasks")
//    suspend fun listTasks(): TaskListDto
//}

//fun main() = runBlocking {
//
//    val loggingInterceptor = HttpLoggingInterceptor()
//        .setLevel(HttpLoggingInterceptor.Level.NONE)
//
//    val okHttpClient = OkHttpClient().newBuilder()
//        .addInterceptor(loggingInterceptor)
//        .addInterceptor(BasicAuthInterceptor(API_USERNAME, API_PASSWORD))
//        .build()
//
//    val retrofit = Retrofit.Builder()
//        .client(okHttpClient)
//        .addConverterFactory(GsonConverterFactory.create())
//        .baseUrl(API_URL)
//        .build()
//
//    val api = retrofit.create(Api::class.java)
//
//    try {
//        val response = api.listTasks()
//        println("Response: ${response.tasks}")
//    } catch (e: HttpException) {
//        println("ERROR ${e.response()}")
//    }
//}