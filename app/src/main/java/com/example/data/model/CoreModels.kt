package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.data.local.MessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.UserEntity

/**
 * Core Domain & Room-Persisted Data Classes for PHARB Social Network:
 * - User (persisted locally in `cached_users` table for offline mode)
 * - Post (persisted locally in `cached_posts` table with offline sync tracking)
 * - Message (persisted locally in `cached_messages` table)
 * Includes Room TypeConverters and bidirectional mapping with full database entities.
 */

class PharbTypeConverters {
    @TypeConverter
    fun fromStringList(items: List<String>?): String {
        return items?.filter { it.isNotBlank() }?.joinToString(" ") ?: ""
    }

    @TypeConverter
    fun toStringList(serialized: String?): List<String> {
        if (serialized.isNullOrBlank()) return emptyList()
        return serialized.split(" ").filter { it.isNotBlank() }
    }
}

@Entity(tableName = "cached_users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String = "",
    val bio: String = "",
    val location: String = "",
    val website: String = "",
    val avatarColorHex: Long = 0xFF2563EB,
    val coverDrawableName: String = "img_cover_default",
    val accountType: String = "PERSONAL", // PERSONAL, CREATOR, BUSINESS, EDUCATION
    val isVerified: Boolean = false,
    val isPrivateAccount: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowedByMe: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenText: String = "متصل الآن",
    val role: String = "USER",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val authorId: Long,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarColor: Long = 0xFF2563EB,
    val authorVerified: Boolean = false,
    val authorAccountType: String = "PERSONAL",
    val content: String,
    val postType: String = "TEXT", // TEXT, IMAGE, MULTI_IMAGE, VIDEO, GIF, LINK, POLL, AUDIO, QUESTION, FILE
    val mediaDrawableName: String = "",
    val linkUrl: String = "",
    val hashtags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val locationTag: String = "",
    val audience: String = "PUBLIC",
    val category: String = "تقنية",
    val likeCount: Int = 0,
    val loveCount: Int = 0,
    val laughCount: Int = 0,
    val wowCount: Int = 0,
    val sadCount: Int = 0,
    val angryCount: Int = 0,
    val myReaction: String? = null,
    val commentsCount: Int = 0,
    val repostCount: Int = 0,
    val isRepost: Boolean = false,
    val originalAuthorUsername: String = "",
    val quoteText: String = "",
    val isSaved: Boolean = false,
    val savedCollectionName: String = "الكل",
    val isDraft: Boolean = false,
    val isSynced: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalReactions: Int
        get() = likeCount + loveCount + laughCount + wowCount + sadCount + angryCount
}

@Entity(tableName = "cached_messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conversationId: Long,
    val senderId: Long,
    val senderName: String,
    val isFromMe: Boolean,
    val content: String,
    val messageType: String = "TEXT", // TEXT, IMAGE, VIDEO, VOICE, FILE, STICKER, GIF
    val mediaMeta: String = "",
    val replyToPreview: String = "",
    val reactionEmoji: String = "",
    val deliveryStatus: String = "SEEN", // SENT, DELIVERED, SEEN
    val isForwarded: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// --- Bidirectional Domain <-> Room Entity Mappers ---

fun UserEntity.toDomainModel(): User = User(
    id = id,
    fullName = fullName,
    username = username,
    email = email,
    phone = phone,
    bio = bio,
    location = location,
    website = website,
    avatarColorHex = avatarColorHex,
    coverDrawableName = coverDrawableName,
    accountType = accountType,
    isVerified = isVerified,
    isPrivateAccount = isPrivateAccount,
    followersCount = followersCount,
    followingCount = followingCount,
    isFollowedByMe = isFollowedByMe,
    isOnline = isOnline,
    lastSeenText = lastSeenText,
    role = role,
    createdAt = createdAt
)

fun User.toEntity(passwordHash: String = "", passwordSalt: String = ""): UserEntity = UserEntity(
    id = id,
    fullName = fullName,
    username = username,
    email = email,
    phone = phone,
    passwordHash = passwordHash,
    passwordSalt = passwordSalt,
    bio = bio,
    location = location,
    website = website,
    avatarColorHex = avatarColorHex,
    coverDrawableName = coverDrawableName,
    accountType = accountType,
    isVerified = isVerified,
    isPrivateAccount = isPrivateAccount,
    followersCount = followersCount,
    followingCount = followingCount,
    isFollowedByMe = isFollowedByMe,
    isOnline = isOnline,
    lastSeenText = lastSeenText,
    role = role,
    createdAt = createdAt
)

fun PostEntity.toDomainModel(): Post = Post(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorUsername = authorUsername,
    authorAvatarColor = authorAvatarColor,
    authorVerified = authorVerified,
    authorAccountType = authorAccountType,
    content = content,
    postType = postType,
    mediaDrawableName = mediaDrawableName,
    linkUrl = linkUrl,
    hashtags = hashtags.split(" ").filter { it.isNotBlank() },
    mentions = mentions.split(" ").filter { it.isNotBlank() },
    locationTag = locationTag,
    audience = audience,
    category = category,
    likeCount = likeCount,
    loveCount = loveCount,
    laughCount = laughCount,
    wowCount = wowCount,
    sadCount = sadCount,
    angryCount = angryCount,
    myReaction = myReaction,
    commentsCount = commentsCount,
    repostCount = repostCount,
    isRepost = isRepost,
    originalAuthorUsername = originalAuthorUsername,
    quoteText = quoteText,
    isSaved = isSaved,
    savedCollectionName = savedCollectionName,
    isDraft = isDraft,
    isSynced = isSynced,
    createdAt = createdAt
)

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorUsername = authorUsername,
    authorAvatarColor = authorAvatarColor,
    authorVerified = authorVerified,
    authorAccountType = authorAccountType,
    content = content,
    postType = postType,
    mediaDrawableName = mediaDrawableName,
    linkUrl = linkUrl,
    hashtags = hashtags.joinToString(" "),
    mentions = mentions.joinToString(" "),
    locationTag = locationTag,
    audience = audience,
    category = category,
    likeCount = likeCount,
    loveCount = loveCount,
    laughCount = laughCount,
    wowCount = wowCount,
    sadCount = sadCount,
    angryCount = angryCount,
    myReaction = myReaction,
    commentsCount = commentsCount,
    repostCount = repostCount,
    isRepost = isRepost,
    originalAuthorUsername = originalAuthorUsername,
    quoteText = quoteText,
    isSaved = isSaved,
    savedCollectionName = savedCollectionName,
    isDraft = isDraft,
    isSynced = isSynced,
    createdAt = createdAt
)

fun MessageEntity.toDomainModel(): Message = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    isFromMe = isFromMe,
    content = content,
    messageType = messageType,
    mediaMeta = mediaMeta,
    replyToPreview = replyToPreview,
    reactionEmoji = reactionEmoji,
    deliveryStatus = deliveryStatus,
    isForwarded = isForwarded,
    isDeleted = isDeleted,
    createdAt = createdAt
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    senderName = senderName,
    isFromMe = isFromMe,
    content = content,
    messageType = messageType,
    mediaMeta = mediaMeta,
    replyToPreview = replyToPreview,
    reactionEmoji = reactionEmoji,
    deliveryStatus = deliveryStatus,
    isForwarded = isForwarded,
    isDeleted = isDeleted,
    createdAt = createdAt
)
