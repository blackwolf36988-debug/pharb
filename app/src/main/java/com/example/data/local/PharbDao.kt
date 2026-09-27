package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Post
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface PharbDao {

    // --- Offline-Persisted Domain Data Classes (User & Post) ---
    @Query("SELECT * FROM cached_users ORDER BY followersCount DESC")
    fun observeCachedUsers(): Flow<List<User>>

    @Query("SELECT * FROM cached_users WHERE id = :userId LIMIT 1")
    fun observeCachedUserById(userId: Long): Flow<User?>

    @Query("SELECT * FROM cached_users WHERE id = :userId LIMIT 1")
    suspend fun getCachedUserById(userId: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedUsers(users: List<User>)

    @Update
    suspend fun updateCachedUser(user: User)

    @Query("DELETE FROM cached_users WHERE id = :userId")
    suspend fun deleteCachedUserById(userId: Long)

    @Query("SELECT * FROM cached_posts WHERE isDraft = 0 ORDER BY createdAt DESC")
    fun observeCachedPosts(): Flow<List<Post>>

    @Query("SELECT * FROM cached_posts WHERE isSynced = 0 ORDER BY createdAt DESC")
    fun observeUnsyncedCachedPosts(): Flow<List<Post>>

    @Query("SELECT * FROM cached_posts WHERE id = :postId LIMIT 1")
    suspend fun getCachedPostById(postId: Long): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedPost(post: Post): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCachedPosts(posts: List<Post>)

    @Update
    suspend fun updateCachedPost(post: Post)

    @Query("DELETE FROM cached_posts WHERE id = :postId")
    suspend fun deleteCachedPostById(postId: Long)

    @Query("UPDATE cached_posts SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllCachedPostsSynced()

    // --- Users & Auth ---
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users ORDER BY followersCount DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:identifier) OR LOWER(email) = LOWER(:identifier) OR phone = :identifier LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)

    // --- Sessions ---
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY lastActiveAt DESC")
    fun observeSessionsForUser(userId: Long): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("DELETE FROM sessions WHERE userId = :userId AND isCurrentDevice = 0")
    suspend fun revokeOtherSessions(userId: Long)

    // --- Posts & Feed ---
    @Query("SELECT * FROM posts WHERE isDraft = 0 ORDER BY createdAt DESC")
    fun observeFeedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isDraft = 1 ORDER BY createdAt DESC")
    fun observeDraftPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun observeSavedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Long): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("UPDATE posts SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllPostsSynced()

    @Query("UPDATE posts SET isSynced = 1 WHERE id = :postId")
    suspend fun markPostSynced(postId: Long)

    @Query("SELECT * FROM posts WHERE isSynced = 0 ORDER BY createdAt DESC")
    fun observeUnsyncedPosts(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM posts WHERE isSynced = 0")
    fun observeUnsyncedPostCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isSynced = 0")
    suspend fun getUnsyncedPostCount(): Int

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments ORDER BY createdAt DESC")
    fun observeAllComments(): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    // --- Stories ---
    @Query("SELECT * FROM stories WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM stories WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun observeArchivedStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Update
    suspend fun updateStory(story: StoryEntity)

    // --- Short Videos (PHARB SHORTS) ---
    @Query("SELECT * FROM short_videos ORDER BY createdAt DESC")
    fun observeShortVideos(): Flow<List<ShortVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortVideo(video: ShortVideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortVideos(videos: List<ShortVideoEntity>)

    @Update
    suspend fun updateShortVideo(video: ShortVideoEntity)

    // --- Messaging (PHARB MESSENGER) ---
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY createdAt ASC")
    fun observeAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    // --- Communities ---
    @Query("SELECT * FROM communities ORDER BY membersCount DESC")
    fun observeCommunities(): Flow<List<CommunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(community: CommunityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunities(communities: List<CommunityEntity>)

    @Update
    suspend fun updateCommunity(community: CommunityEntity)

    // --- Channels ---
    @Query("SELECT * FROM channels ORDER BY subscribersCount DESC")
    fun observeChannels(): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    // --- Live Streams ---
    @Query("SELECT * FROM live_streams ORDER BY isLiveNow DESC, viewersCount DESC")
    fun observeLiveStreams(): Flow<List<LiveStreamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveStream(stream: LiveStreamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveStreams(streams: List<LiveStreamEntity>)

    @Update
    suspend fun updateLiveStream(stream: LiveStreamEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    // --- Reports & Moderation ---
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun observeReports(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>)

    @Update
    suspend fun updateReport(report: ReportEntity)

    // --- Block / Mute / Restrict ---
    @Query("SELECT * FROM block_mute_relations ORDER BY createdAt DESC")
    fun observeBlockMuteRelations(): Flow<List<BlockMuteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockMute(relation: BlockMuteEntity): Long

    @Query("DELETE FROM block_mute_relations WHERE id = :id")
    suspend fun removeBlockMute(id: Long)

    // --- Verification Requests ---
    @Query("SELECT * FROM verification_requests ORDER BY submittedAt DESC")
    fun observeVerificationRequests(): Flow<List<VerificationRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequest(request: VerificationRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequests(requests: List<VerificationRequestEntity>)

    @Update
    suspend fun updateVerificationRequest(request: VerificationRequestEntity)
}
