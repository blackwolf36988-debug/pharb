package com.example.data.repository

import com.example.core.SecurityManager
import com.example.data.local.BlockMuteEntity
import com.example.data.local.ChannelEntity
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.LiveStreamEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PharbDao
import com.example.data.local.PostEntity
import com.example.data.local.ReportEntity
import com.example.data.local.SessionEntity
import com.example.data.local.ShortVideoEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserEntity
import com.example.data.local.VerificationRequestEntity
import com.example.data.model.Post
import com.example.data.model.User
import com.example.data.model.toDomainModel
import com.example.data.model.toEntity
import kotlinx.coroutines.flow.Flow

/**
 * PHARB Offline-First Repository (Sections 32, 33)
 * Coordinates Room Local Persistence, Security Cryptography, and REST API Sync.
 */
class PharbRepository(
    private val dao: PharbDao
) {
    val allUsers: Flow<List<UserEntity>> = dao.observeAllUsers()
    val feedPosts: Flow<List<PostEntity>> = dao.observeFeedPosts()
    val draftPosts: Flow<List<PostEntity>> = dao.observeDraftPosts()
    val savedPosts: Flow<List<PostEntity>> = dao.observeSavedPosts()
    val unsyncedPosts: Flow<List<PostEntity>> = dao.observeUnsyncedPosts()
    val unsyncedPostCount: Flow<Int> = dao.observeUnsyncedPostCount()

    // Direct Room Flows for Domain Data Classes (User & Post)
    val cachedUsers: Flow<List<User>> = dao.observeCachedUsers()
    val cachedPosts: Flow<List<Post>> = dao.observeCachedPosts()
    val unsyncedCachedPosts: Flow<List<Post>> = dao.observeUnsyncedCachedPosts()
    val allComments: Flow<List<CommentEntity>> = dao.observeAllComments()
    val activeStories: Flow<List<StoryEntity>> = dao.observeActiveStories()
    val archivedStories: Flow<List<StoryEntity>> = dao.observeArchivedStories()
    val shortVideos: Flow<List<ShortVideoEntity>> = dao.observeShortVideos()
    val conversations: Flow<List<ConversationEntity>> = dao.observeConversations()
    val allMessages: Flow<List<MessageEntity>> = dao.observeAllMessages()
    val communities: Flow<List<CommunityEntity>> = dao.observeCommunities()
    val channels: Flow<List<ChannelEntity>> = dao.observeChannels()
    val liveStreams: Flow<List<LiveStreamEntity>> = dao.observeLiveStreams()
    val notifications: Flow<List<NotificationEntity>> = dao.observeNotifications()
    val reports: Flow<List<ReportEntity>> = dao.observeReports()
    val blockMuteRelations: Flow<List<BlockMuteEntity>> = dao.observeBlockMuteRelations()
    val verificationRequests: Flow<List<VerificationRequestEntity>> = dao.observeVerificationRequests()

    fun observeSessions(userId: Long): Flow<List<SessionEntity>> = dao.observeSessionsForUser(userId)

    suspend fun initializeDatabase() {
        PharbDatabaseSeeder.seedIfNeeded(dao)
    }

    // --- Offline-First Persistence for User & Post Data Classes ---
    suspend fun persistUserOffline(user: User): Long {
        val cachedId = dao.upsertCachedUser(user)
        val existingEntity = dao.getUserById(user.id)
        val entity = if (existingEntity != null) {
            user.toEntity(
                passwordHash = existingEntity.passwordHash,
                passwordSalt = existingEntity.passwordSalt
            )
        } else {
            val salt = SecurityManager.generateSalt()
            val hash = SecurityManager.hashPassword("PharbOffline#2026", salt)
            user.toEntity(passwordHash = hash, passwordSalt = salt)
        }
        dao.insertUser(entity.copy(id = if (user.id > 0L) user.id else cachedId))
        return cachedId
    }

    suspend fun persistUsersOffline(users: List<User>) {
        dao.upsertCachedUsers(users)
    }

    suspend fun persistPostOffline(post: Post, isOfflineMode: Boolean = false): Long {
        val syncedFlag = !isOfflineMode && post.isSynced
        val normalizedPost = post.copy(isSynced = syncedFlag)
        val newId = dao.insertPost(normalizedPost.toEntity())
        dao.upsertCachedPost(normalizedPost.copy(id = newId))
        return newId
    }

    suspend fun persistPostsOffline(posts: List<Post>) {
        dao.upsertCachedPosts(posts)
    }

    // --- Authentication & Sessions ---
    suspend fun authenticateUser(identifier: String, passwordPlain: String): Result<UserEntity> {
        val cleanId = SecurityManager.sanitizeInput(identifier)
        if (!SecurityManager.checkRateLimit("login:$cleanId", maxRequests = 10)) {
            return Result.failure(IllegalStateException("تم تجاوز الحد المسموح لمحاولات الدخول. يرجى الانتظار دقيقة."))
        }
        val user = dao.findUserByIdentifier(cleanId)
            ?: return Result.failure(IllegalArgumentException("لم يتم العثور على حساب بهذا البريد أو اسم المستخدم أو الهاتف"))

        if (user.isSuspended) {
            return Result.failure(IllegalStateException("هذا الحساب معلّق حاليًا من قِبل إدارة PHARB لمخالفة معايير المجتمع."))
        }

        val isPasswordValid = SecurityManager.verifyPassword(passwordPlain, user.passwordSalt, user.passwordHash)
        if (!isPasswordValid) {
            return Result.failure(IllegalArgumentException("كلمة المرور غير صحيحة. يرجى التأكد والمحاولة مرة أخرى."))
        }

        val jwt = SecurityManager.generateJwtToken(user.id, user.username, user.role)
        val refresh = SecurityManager.generateRefreshToken(user.id)
        dao.insertSession(
            SessionEntity(
                userId = user.id,
                deviceName = "Android • PHARB Secure Session",
                ipAddress = "192.168.1.104 (TLS 1.3)",
                jwtToken = jwt,
                refreshToken = refresh,
                isCurrentDevice = true
            )
        )
        return Result.success(user)
    }

    suspend fun registerNewUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        passwordPlain: String,
        accountType: String
    ): Result<UserEntity> {
        val cleanUsername = SecurityManager.sanitizeInput(username).lowercase().replace(" ", "_")
        val cleanEmail = SecurityManager.sanitizeInput(email).lowercase()
        val existing = dao.findUserByIdentifier(cleanUsername) ?: dao.findUserByIdentifier(cleanEmail)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("اسم المستخدم أو البريد الإلكتروني مسجل مسبقًا"))
        }

        val salt = SecurityManager.generateSalt()
        val hash = SecurityManager.hashPassword(passwordPlain, salt)
        val avatarPalette = listOf(0xFF2563EB, 0xFF059669, 0xFF7C3AED, 0xFF0284C7, 0xFFD97706)
        val newUser = UserEntity(
            fullName = SecurityManager.sanitizeInput(fullName),
            username = cleanUsername,
            email = cleanEmail,
            phone = SecurityManager.sanitizeInput(phone),
            passwordHash = hash,
            passwordSalt = salt,
            bio = "عضو جديد في مجتمع PHARB 🌐 | تواصل. أبدع. اكتشف.",
            location = "العالم العربي",
            website = "https://pharb.network/@$cleanUsername",
            avatarColorHex = avatarPalette.random(),
            accountType = accountType,
            isVerified = false,
            followersCount = 1,
            followingCount = 3,
            role = "USER"
        )
        val newId = dao.insertUser(newUser)
        val savedUser = newUser.copy(id = newId)
        dao.upsertCachedUser(savedUser.toDomainModel())

        val jwt = SecurityManager.generateJwtToken(newId, cleanUsername, "USER")
        val refresh = SecurityManager.generateRefreshToken(newId)
        dao.insertSession(
            SessionEntity(
                userId = newId,
                deviceName = "Android • PHARB Primary Device",
                ipAddress = "192.168.1.104 (TLS 1.3)",
                jwtToken = jwt,
                refreshToken = refresh,
                isCurrentDevice = true
            )
        )
        dao.insertNotification(
            NotificationEntity(
                type = "SECURITY",
                title = "مرحبًا بك في PHARB 🌐",
                body = "تم إنشاء حسابك وتأمينه بتشفير PBKDF2-SHA256. استكشف المجتمعات والقنوات الآن!",
                actorName = "فريق PHARB",
                actorAvatarColor = 0xFF2563EB
            )
        )
        return Result.success(savedUser)
    }

    suspend fun changePassword(userId: Long, newPasswordPlain: String): Boolean {
        val user = dao.getUserById(userId) ?: return false
        val newSalt = SecurityManager.generateSalt()
        val newHash = SecurityManager.hashPassword(newPasswordPlain, newSalt)
        dao.updateUser(user.copy(passwordSalt = newSalt, passwordHash = newHash))
        dao.insertNotification(
            NotificationEntity(
                type = "SECURITY",
                title = "تحديث أمني لكلمة المرور",
                body = "تم تغيير كلمة مرور حسابك وإعادة تشفير المفتاح بنجاح.",
                actorName = "أمان PHARB",
                actorAvatarColor = 0xFF0B192C
            )
        )
        return true
    }

    suspend fun updateUserProfile(user: UserEntity) {
        dao.updateUser(user)
        dao.upsertCachedUser(user.toDomainModel())
    }

    suspend fun deleteAccount(userId: Long) {
        dao.deleteUserById(userId)
        dao.deleteCachedUserById(userId)
    }

    suspend fun revokeSession(sessionId: Long) {
        dao.deleteSession(sessionId)
    }

    suspend fun revokeAllOtherSessions(userId: Long) {
        dao.revokeOtherSessions(userId)
    }

    // --- Posts, Reactions, Polls, Reposts, Saved ---
    suspend fun createPost(
        author: UserEntity,
        content: String,
        postType: String,
        mediaDrawableName: String = "",
        linkUrl: String = "",
        pollOptions: List<String> = emptyList(),
        audioDurationSec: Int = 0,
        fileAttachmentName: String = "",
        hashtags: String = "",
        mentions: String = "",
        locationTag: String = "",
        audience: String = "PUBLIC",
        category: String = "تقنية",
        isDraft: Boolean = false,
        isOfflineMode: Boolean = false
    ): Long {
        val pollSerialized = if (pollOptions.isNotEmpty()) {
            pollOptions.joinToString("|") { "${SecurityManager.sanitizeInput(it)}:0" }
        } else ""

        val post = PostEntity(
            authorId = author.id,
            authorName = author.fullName,
            authorUsername = author.username,
            authorAvatarColor = author.avatarColorHex,
            authorVerified = author.isVerified,
            authorAccountType = author.accountType,
            content = SecurityManager.sanitizeInput(content),
            postType = postType,
            mediaDrawableName = mediaDrawableName,
            linkUrl = linkUrl,
            pollOptionsSerialized = pollSerialized,
            audioDurationSec = audioDurationSec,
            fileAttachmentName = fileAttachmentName,
            hashtags = hashtags,
            mentions = mentions,
            locationTag = locationTag,
            audience = audience,
            category = category,
            isDraft = isDraft,
            isSynced = !isOfflineMode
        )
        val insertedId = dao.insertPost(post)
        dao.upsertCachedPost(post.copy(id = insertedId).toDomainModel())
        return insertedId
    }

    suspend fun publishDraft(draft: PostEntity, isOfflineMode: Boolean) {
        val updated = draft.copy(isDraft = false, isSynced = !isOfflineMode, createdAt = System.currentTimeMillis())
        dao.updatePost(updated)
        dao.upsertCachedPost(updated.toDomainModel())
    }

    suspend fun syncOfflinePosts() {
        dao.markAllPostsSynced()
        dao.markAllCachedPostsSynced()
    }

    suspend fun reactToPost(post: PostEntity, reactionType: String) {
        val current = post.myReaction
        val isRemoving = current == reactionType

        var like = post.likeCount
        var love = post.loveCount
        var laugh = post.laughCount
        var wow = post.wowCount
        var sad = post.sadCount
        var angry = post.angryCount

        // Decrement previous reaction if any
        when (current) {
            "LIKE" -> like = (like - 1).coerceAtLeast(0)
            "LOVE" -> love = (love - 1).coerceAtLeast(0)
            "LAUGH" -> laugh = (laugh - 1).coerceAtLeast(0)
            "WOW" -> wow = (wow - 1).coerceAtLeast(0)
            "SAD" -> sad = (sad - 1).coerceAtLeast(0)
            "ANGRY" -> angry = (angry - 1).coerceAtLeast(0)
        }

        val nextReaction = if (isRemoving) null else reactionType
        if (!isRemoving) {
            when (reactionType) {
                "LIKE" -> like++
                "LOVE" -> love++
                "LAUGH" -> laugh++
                "WOW" -> wow++
                "SAD" -> sad++
                "ANGRY" -> angry++
            }
        }

        dao.updatePost(
            post.copy(
                likeCount = like,
                loveCount = love,
                laughCount = laugh,
                wowCount = wow,
                sadCount = sad,
                angryCount = angry,
                myReaction = nextReaction
            )
        )
    }

    suspend fun voteInPoll(post: PostEntity, optionIndex: Int) {
        if (post.votedPollOptionIndex != -1) return
        val parts = post.pollOptionsSerialized.split("|").filter { it.isNotBlank() }
        if (optionIndex !in parts.indices) return
        val updated = parts.mapIndexed { idx, item ->
            val splitIdx = item.lastIndexOf(':')
            if (splitIdx == -1) item
            else {
                val label = item.substring(0, splitIdx)
                val count = item.substring(splitIdx + 1).toIntOrNull() ?: 0
                val newCount = if (idx == optionIndex) count + 1 else count
                "$label:$newCount"
            }
        }.joinToString("|")

        dao.updatePost(
            post.copy(
                pollOptionsSerialized = updated,
                votedPollOptionIndex = optionIndex
            )
        )
    }

    suspend fun repostOrQuote(
        original: PostEntity,
        currentUser: UserEntity,
        quoteComment: String = ""
    ) {
        dao.updatePost(original.copy(repostCount = original.repostCount + 1))
        val repost = PostEntity(
            authorId = currentUser.id,
            authorName = currentUser.fullName,
            authorUsername = currentUser.username,
            authorAvatarColor = currentUser.avatarColorHex,
            authorVerified = currentUser.isVerified,
            authorAccountType = currentUser.accountType,
            content = original.content,
            postType = original.postType,
            mediaDrawableName = original.mediaDrawableName,
            hashtags = original.hashtags,
            category = original.category,
            isRepost = true,
            originalAuthorUsername = original.authorUsername.ifEmpty { original.authorName },
            quoteText = SecurityManager.sanitizeInput(quoteComment),
            createdAt = System.currentTimeMillis()
        )
        dao.insertPost(repost)
    }

    suspend fun toggleSavePost(post: PostEntity, collectionName: String = "الكل") {
        val nextSaved = !post.isSaved
        dao.updatePost(
            post.copy(
                isSaved = nextSaved,
                savedCollectionName = if (nextSaved) collectionName else ""
            )
        )
    }

    suspend fun deletePost(postId: Long) {
        dao.deletePost(postId)
        dao.deleteCachedPostById(postId)
    }

    suspend fun addComment(post: PostEntity, author: UserEntity, text: String) {
        val clean = SecurityManager.sanitizeInput(text)
        if (clean.isBlank()) return
        dao.insertComment(
            CommentEntity(
                postId = post.id,
                authorName = author.fullName,
                authorUsername = author.username,
                authorAvatarColor = author.avatarColorHex,
                authorVerified = author.isVerified,
                content = clean
            )
        )
        val latestPost = dao.getPostById(post.id) ?: post
        dao.updatePost(latestPost.copy(commentsCount = latestPost.commentsCount + 1))
    }

    // --- Follow System (Public & Private Follow Requests) ---
    suspend fun toggleFollowUser(target: UserEntity) {
        if (target.isFollowedByMe) {
            dao.updateUser(
                target.copy(
                    isFollowedByMe = false,
                    hasPendingFollowRequest = false,
                    followersCount = (target.followersCount - 1).coerceAtLeast(0)
                )
            )
        } else if (target.isPrivateAccount && !target.hasPendingFollowRequest) {
            dao.updateUser(target.copy(hasPendingFollowRequest = true))
            dao.insertNotification(
                NotificationEntity(
                    type = "JOIN_REQUEST",
                    title = "طلب متابعة لحساب خاص 🔒",
                    body = "تم إرسال طلب متابعة إلى ${target.fullName} (@${target.username}) بانتظار الموافقة.",
                    actorName = target.fullName,
                    actorAvatarColor = target.avatarColorHex
                )
            )
        } else {
            dao.updateUser(
                target.copy(
                    isFollowedByMe = true,
                    hasPendingFollowRequest = false,
                    followersCount = target.followersCount + 1
                )
            )
        }
    }

    // --- Stories ---
    suspend fun createStory(
        author: UserEntity,
        caption: String,
        mediaType: String,
        musicTrack: String = "",
        interactiveQuestion: String = ""
    ) {
        dao.insertStory(
            StoryEntity(
                authorId = author.id,
                authorName = author.fullName,
                authorUsername = author.username,
                authorAvatarColor = author.avatarColorHex,
                authorVerified = author.isVerified,
                caption = SecurityManager.sanitizeInput(caption),
                mediaType = mediaType,
                musicTrack = musicTrack,
                interactiveQuestion = interactiveQuestion,
                viewsCount = 1
            )
        )
    }

    suspend fun markStoryViewed(story: StoryEntity) {
        dao.updateStory(story.copy(isViewed = true, viewsCount = story.viewsCount + 1))
    }

    suspend fun archiveStory(story: StoryEntity) {
        dao.updateStory(story.copy(isArchived = true))
    }

    // --- Short Videos (PHARB SHORTS) ---
    suspend fun toggleLikeShort(video: ShortVideoEntity) {
        val next = !video.isLiked
        val count = if (next) video.likesCount + 1 else (video.likesCount - 1).coerceAtLeast(0)
        dao.updateShortVideo(video.copy(isLiked = next, likesCount = count))
    }

    suspend fun toggleSaveShort(video: ShortVideoEntity) {
        val next = !video.isSaved
        val count = if (next) video.savesCount + 1 else (video.savesCount - 1).coerceAtLeast(0)
        dao.updateShortVideo(video.copy(isSaved = next, savesCount = count))
    }

    suspend fun toggleFollowShortCreator(video: ShortVideoEntity) {
        dao.updateShortVideo(video.copy(isFollowingCreator = !video.isFollowingCreator))
    }

    // --- Messaging (PHARB MESSENGER) ---
    suspend fun sendMessage(
        conversation: ConversationEntity,
        sender: UserEntity,
        content: String,
        messageType: String = "TEXT",
        mediaMeta: String = "",
        replyToPreview: String = ""
    ) {
        val clean = SecurityManager.sanitizeInput(content)
        if (clean.isBlank() && mediaMeta.isBlank()) return
        val now = System.currentTimeMillis()
        dao.insertMessage(
            MessageEntity(
                conversationId = conversation.id,
                senderId = sender.id,
                senderName = sender.fullName,
                isFromMe = true,
                content = clean,
                messageType = messageType,
                mediaMeta = mediaMeta,
                replyToPreview = replyToPreview,
                deliveryStatus = "SEEN",
                createdAt = now
            )
        )
        dao.updateConversation(
            conversation.copy(
                lastMessagePreview = clean.ifEmpty { "مرفق: $mediaMeta" },
                unreadCount = 0,
                updatedAt = now
            )
        )
    }

    suspend fun reactToMessage(message: MessageEntity, emoji: String) {
        val next = if (message.reactionEmoji == emoji) "" else emoji
        dao.updateMessage(message.copy(reactionEmoji = next))
    }

    suspend fun deleteMessage(messageId: Long) {
        dao.deleteMessage(messageId)
    }

    suspend fun createDirectOrGroupConversation(title: String, username: String, isGroup: Boolean): Long {
        return dao.insertConversation(
            ConversationEntity(
                title = SecurityManager.sanitizeInput(title),
                participantUsername = username,
                participantAvatarColor = if (isGroup) 0xFF059669 else 0xFF2563EB,
                isGroup = isGroup,
                memberCount = if (isGroup) 6 else 2,
                isOnline = true,
                lastSeenStatus = if (isGroup) "مجموعة نشطة الآن" else "متصل الآن",
                lastMessagePreview = "بدأت المحادثة المشفرة على PHARB Messenger",
                unreadCount = 0
            )
        )
    }

    // --- Communities, Channels & Live ---
    suspend fun toggleJoinCommunity(community: CommunityEntity) {
        val next = !community.isJoined
        val members = if (next) community.membersCount + 1 else (community.membersCount - 1).coerceAtLeast(0)
        dao.updateCommunity(community.copy(isJoined = next, membersCount = members))
    }

    suspend fun createCommunity(name: String, category: String, description: String, rules: String, moderatorUsername: String) {
        dao.insertCommunity(
            CommunityEntity(
                name = SecurityManager.sanitizeInput(name),
                category = category,
                description = SecurityManager.sanitizeInput(description),
                rules = SecurityManager.sanitizeInput(rules),
                membersCount = 1,
                postsCount = 0,
                isJoined = true,
                moderatorUsername = moderatorUsername
            )
        )
    }

    suspend fun toggleSubscribeChannel(channel: ChannelEntity) {
        val next = !channel.isSubscribed
        val subs = if (next) channel.subscribersCount + 1 else (channel.subscribersCount - 1).coerceAtLeast(0)
        dao.updateChannel(channel.copy(isSubscribed = next, subscribersCount = subs))
    }

    suspend fun createChannel(name: String, handle: String, ownerType: String, category: String, description: String) {
        dao.insertChannel(
            ChannelEntity(
                name = SecurityManager.sanitizeInput(name),
                handle = if (handle.startsWith("@")) handle else "@$handle",
                ownerType = ownerType,
                description = SecurityManager.sanitizeInput(description),
                category = category,
                subscribersCount = 1,
                isSubscribed = true,
                isVerified = false,
                latestBroadcast = "مرحبًا بكم في القناة الرسمية الجديدة على PHARB!"
            )
        )
    }

    suspend fun startLiveStream(host: UserEntity, title: String, category: String): Long {
        return dao.insertLiveStream(
            LiveStreamEntity(
                hostName = host.fullName,
                hostUsername = host.username,
                hostAvatarColor = host.avatarColorHex,
                hostVerified = host.isVerified,
                title = SecurityManager.sanitizeInput(title),
                category = category,
                viewersCount = 14,
                likesCount = 1,
                isLiveNow = true,
                recentChatMessages = "نظام PHARB Live: بدأ البث المباشر بدقة متكيفة Adaptive HD"
            )
        )
    }

    suspend fun sendLiveStreamComment(stream: LiveStreamEntity, senderName: String, comment: String) {
        val clean = SecurityManager.sanitizeInput(comment)
        if (clean.isBlank()) return
        val updatedChat = "${stream.recentChatMessages}|$senderName: $clean"
        dao.updateLiveStream(
            stream.copy(
                recentChatMessages = updatedChat,
                likesCount = stream.likesCount + 1,
                viewersCount = stream.viewersCount + 1
            )
        )
    }

    suspend fun endLiveStream(stream: LiveStreamEntity) {
        dao.updateLiveStream(stream.copy(isLiveNow = false))
    }

    // --- Notifications, Reports, Block/Mute, Verification & Admin ---
    suspend fun markAllNotificationsRead() {
        dao.markAllNotificationsRead()
    }

    suspend fun submitReport(
        targetType: String,
        targetId: Long,
        targetSummary: String,
        reporterUsername: String,
        reason: String
    ) {
        dao.insertReport(
            ReportEntity(
                targetType = targetType,
                targetId = targetId,
                targetSummary = SecurityManager.sanitizeInput(targetSummary),
                reporterUsername = reporterUsername,
                reason = reason,
                status = "PENDING",
                moderatorAction = "تم الاستلام في مركز حماية المجتمع"
            )
        )
    }

    suspend fun resolveReportAsAdmin(report: ReportEntity, actionTaken: String, status: String = "RESOLVED") {
        dao.updateReport(report.copy(status = status, moderatorAction = actionTaken))
    }

    suspend fun addBlockOrMute(targetUsername: String, targetDisplayName: String, relationType: String) {
        dao.insertBlockMute(
            BlockMuteEntity(
                targetUsername = targetUsername,
                targetDisplayName = targetDisplayName,
                relationType = relationType
            )
        )
    }

    suspend fun removeBlockOrMute(id: Long) {
        dao.removeBlockMute(id)
    }

    suspend fun submitVerificationRequest(
        user: UserEntity,
        documentReference: String,
        justification: String
    ) {
        dao.insertVerificationRequest(
            VerificationRequestEntity(
                userId = user.id,
                username = user.username,
                fullName = user.fullName,
                accountType = user.accountType,
                documentReference = SecurityManager.sanitizeInput(documentReference),
                justification = SecurityManager.sanitizeInput(justification),
                status = "PENDING"
            )
        )
    }

    suspend fun reviewVerificationRequest(request: VerificationRequestEntity, approve: Boolean) {
        val newStatus = if (approve) "APPROVED" else "REJECTED"
        dao.updateVerificationRequest(request.copy(status = newStatus))
        if (approve) {
            val targetUser = dao.getUserById(request.userId) ?: dao.findUserByIdentifier(request.username)
            if (targetUser != null) {
                dao.updateUser(targetUser.copy(isVerified = true))
            }
        }
    }

    suspend fun toggleSuspendUserAsAdmin(user: UserEntity) {
        dao.updateUser(user.copy(isSuspended = !user.isSuspended))
    }
}

private object PharbDatabaseSeeder {
    suspend fun seedIfNeeded(dao: PharbDao) {
        com.example.data.local.PharbDatabase.ensureSeeded(dao)
    }
}
