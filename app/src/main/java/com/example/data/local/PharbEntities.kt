package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String = "",
    val passwordHash: String,
    val passwordSalt: String,
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
    val hasPendingFollowRequest: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenText: String = "متصل الآن",
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val allowMessagesFrom: String = "EVERYONE", // EVERYONE, FOLLOWERS, NONE
    val allowTagging: Boolean = true,
    val searchableInDirectory: Boolean = true,
    val personalizedAds: Boolean = false,
    val role: String = "USER", // USER, MODERATOR, ADMIN
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val deviceName: String,
    val ipAddress: String,
    val jwtToken: String,
    val refreshToken: String,
    val isCurrentDevice: Boolean = true,
    val lastActiveAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val pollOptionsSerialized: String = "", // "Option A:45|Option B:30|Option C:15"
    val votedPollOptionIndex: Int = -1,
    val audioDurationSec: Int = 0,
    val fileAttachmentName: String = "",
    val hashtags: String = "",
    val mentions: String = "",
    val locationTag: String = "",
    val audience: String = "PUBLIC", // PUBLIC, FOLLOWERS, PRIVATE
    val category: String = "تقنية",
    val likeCount: Int = 0,
    val loveCount: Int = 0,
    val laughCount: Int = 0,
    val wowCount: Int = 0,
    val sadCount: Int = 0,
    val angryCount: Int = 0,
    val myReaction: String? = null, // LIKE, LOVE, LAUGH, WOW, SAD, ANGRY
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

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarColor: Long = 0xFF1E56A0,
    val authorVerified: Boolean = false,
    val content: String,
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarColor: Long = 0xFF2563EB,
    val authorVerified: Boolean = false,
    val caption: String,
    val mediaType: String = "IMAGE", // IMAGE, VIDEO, TEXT, POLL, QUESTION
    val backgroundHex: Long = 0xFF0B192C,
    val drawableName: String = "img_story_featured",
    val musicTrack: String = "",
    val interactiveQuestion: String = "",
    val pollOptionA: String = "",
    val pollOptionB: String = "",
    val viewsCount: Int = 0,
    val isViewed: Boolean = false,
    val isArchived: Boolean = false,
    val expiresAt: Long = System.currentTimeMillis() + 24 * 3600_000L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "short_videos")
data class ShortVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creatorId: Long,
    val creatorName: String,
    val creatorUsername: String,
    val creatorAvatarColor: Long = 0xFF2563EB,
    val creatorVerified: Boolean = true,
    val isFollowingCreator: Boolean = false,
    val caption: String,
    val hashtags: String,
    val audioTitle: String,
    val resolutionLabel: String = "1080p Adaptive • CDN",
    val bitrateKbps: Int = 2400,
    val drawableName: String = "img_shorts_preview_1",
    val accentColorHex: Long = 0xFF1E56A0,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val isSaved: Boolean = false,
    val viewsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val participantUsername: String,
    val participantAvatarColor: Long = 0xFF2563EB,
    val isGroup: Boolean = false,
    val memberCount: Int = 2,
    val isOnline: Boolean = true,
    val lastSeenStatus: String = "متصل الآن",
    val isTyping: Boolean = false,
    val lastMessagePreview: String,
    val unreadCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "communities")
data class CommunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // تعليم, علوم, رياضة, تقنية, ثقافة, أعمال, ألعاب, هوايات, أخبار, مجتمعات محلية
    val description: String,
    val rules: String,
    val coverColorHex: Long = 0xFF1E56A0,
    val membersCount: Int = 0,
    val postsCount: Int = 0,
    val isJoined: Boolean = false,
    val moderatorUsername: String = "pharb_admin",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val handle: String,
    val ownerType: String = "EDUCATION", // CREATOR, EDUCATION, BUSINESS
    val description: String,
    val category: String,
    val subscribersCount: Int = 0,
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = true,
    val latestBroadcast: String,
    val resourceAttachment: String = "",
    val quizQuestion: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_streams")
data class LiveStreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hostName: String,
    val hostUsername: String,
    val hostAvatarColor: Long = 0xFFE11D48,
    val hostVerified: Boolean = true,
    val title: String,
    val category: String,
    val viewersCount: Int = 0,
    val likesCount: Int = 0,
    val isLiveNow: Boolean = true,
    val recentChatMessages: String = "سارة: بث رائع ومفيد جدًا!|عمر: هل سيتم حفظ البث في الأرشيف؟|ماجد: الصوت والصورة واضحان جدًا",
    val startedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // LIKE, COMMENT, FOLLOW, MESSAGE, MENTION, REPOST, JOIN_REQUEST, LIVE, SECURITY
    val title: String,
    val body: String,
    val actorName: String,
    val actorAvatarColor: Long = 0xFF2563EB,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetType: String, // POST, USER, COMMENT, MESSAGE
    val targetId: Long,
    val targetSummary: String,
    val reporterUsername: String,
    val reason: String, // Spam, Harassment, Hate, Impersonation, Scam, Copyright, Illegal Content, Other
    val status: String = "PENDING", // PENDING, RESOLVED, DISMISSED
    val moderatorAction: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "block_mute_relations")
data class BlockMuteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUsername: String,
    val targetDisplayName: String,
    val relationType: String, // BLOCK, MUTE, RESTRICT
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "verification_requests")
data class VerificationRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val username: String,
    val fullName: String,
    val accountType: String,
    val documentReference: String,
    val justification: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val submittedAt: Long = System.currentTimeMillis()
)
