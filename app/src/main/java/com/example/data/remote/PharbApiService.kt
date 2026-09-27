package com.example.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class ApiAuthRequest(
    val identifier: String,
    val passwordHash: String,
    val deviceName: String
)

data class ApiAuthResponse(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class ApiPostDto(
    val id: Long,
    val authorUsername: String,
    val content: String,
    val postType: String,
    val category: String,
    val createdAt: Long
)

data class ApiStatusResponse(
    val success: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * PHARB REST API Interface (Section 31)
 * Documented via OpenAPI 3.1 in /api/openapi.yaml
 */
interface PharbApiService {

    @POST("v1/auth/login")
    suspend fun login(@Body request: ApiAuthRequest): ApiAuthResponse

    @POST("v1/auth/register")
    suspend fun register(@Body request: Map<String, String>): ApiAuthResponse

    @POST("v1/auth/verify-otp")
    suspend fun verifyOtp(@Body payload: Map<String, String>): ApiStatusResponse

    @GET("v1/users/{id}")
    suspend fun getUser(@Path("id") userId: Long): Map<String, Any>

    @GET("v1/profiles/{username}")
    suspend fun getProfile(@Path("username") username: String): Map<String, Any>

    @GET("v1/posts")
    suspend fun getFeedPosts(
        @Query("tab") tab: String = "for_you",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): List<ApiPostDto>

    @POST("v1/posts")
    suspend fun createPost(
        @Header("Authorization") bearerToken: String,
        @Body post: ApiPostDto
    ): ApiStatusResponse

    @POST("v1/comments")
    suspend fun addComment(
        @Header("Authorization") bearerToken: String,
        @Body payload: Map<String, String>
    ): ApiStatusResponse

    @POST("v1/reactions")
    suspend fun reactToPost(
        @Header("Authorization") bearerToken: String,
        @Body payload: Map<String, String>
    ): ApiStatusResponse

    @POST("v1/follow/{targetUserId}")
    suspend fun toggleFollow(
        @Header("Authorization") bearerToken: String,
        @Path("targetUserId") targetUserId: Long
    ): ApiStatusResponse

    @GET("v1/stories")
    suspend fun getStories(): List<Map<String, Any>>

    @GET("v1/videos/shorts")
    suspend fun getShortVideos(@Query("page") page: Int = 1): List<Map<String, Any>>

    @GET("v1/messages/conversations")
    suspend fun getConversations(@Header("Authorization") bearerToken: String): List<Map<String, Any>>

    @GET("v1/notifications")
    suspend fun getNotifications(@Header("Authorization") bearerToken: String): List<Map<String, Any>>

    @GET("v1/search")
    suspend fun searchAll(
        @Query("q") query: String,
        @Query("filter") filter: String = "ALL"
    ): Map<String, Any>

    @GET("v1/communities")
    suspend fun getCommunities(): List<Map<String, Any>>

    @GET("v1/channels")
    suspend fun getChannels(): List<Map<String, Any>>

    @GET("v1/live")
    suspend fun getLiveStreams(): List<Map<String, Any>>

    @POST("v1/reports")
    suspend fun submitReport(
        @Header("Authorization") bearerToken: String,
        @Body payload: Map<String, String>
    ): ApiStatusResponse

    @GET("v1/admin/metrics")
    suspend fun getAdminMetrics(@Header("Authorization") bearerToken: String): Map<String, Any>

    companion object {
        private const val DEFAULT_BASE_URL = "https://api.pharb.network/"

        fun create(baseUrl: String = DEFAULT_BASE_URL): PharbApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(PharbApiService::class.java)
        }
    }
}
