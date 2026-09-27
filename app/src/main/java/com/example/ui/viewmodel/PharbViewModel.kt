package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.PharbAiEngine
import com.example.core.SecurityManager
import com.example.data.local.BlockMuteEntity
import com.example.data.local.ChannelEntity
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.LiveStreamEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PharbDatabase
import com.example.data.local.PostEntity
import com.example.data.local.ReportEntity
import com.example.data.local.SessionEntity
import com.example.data.local.ShortVideoEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserEntity
import com.example.data.local.VerificationRequestEntity
import com.example.data.model.Post
import com.example.data.model.User
import com.example.data.repository.PharbRepository
import com.example.ui.theme.PharbAccentPreset
import com.example.ui.theme.PharbThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainBottomTab(val route: String, val labelAr: String, val labelEn: String) {
    HOME("home", "الرئيسية", "Home"),
    DISCOVER("discover", "اكتشف", "Discover"),
    CREATE("create", "إنشاء", "Create"),
    MESSAGES("messages", "الرسائل", "Messages"),
    PROFILE("profile", "حسابي", "Profile")
}

enum class HomeFeedTab(val labelAr: String, val labelEn: String) {
    FOR_YOU("لك (For You)", "For You"),
    FOLLOWING("المتابَعون", "Following"),
    LATEST("الأحدث", "Latest"),
    COMMUNITIES("المجتمعات", "Communities"),
    EDUCATION("التعليم", "Education"),
    TRENDING("الرائج", "Trending")
}

enum class SubScreenDestination {
    NONE,
    SPLASH_SCREEN,
    ONBOARDING_TOUR,
    AUTH_PORTAL,
    SHORTS_PLAYER,
    STORY_VIEWER,
    STORY_ARCHIVE,
    SEARCH_ENGINE,
    NOTIFICATIONS_CENTER,
    COMMUNITIES_HUB,
    CHANNELS_HUB,
    LIVE_BROADCAST_HUB,
    SAVED_COLLECTIONS,
    PRIVACY_AND_SECURITY,
    BRAND_AND_DESIGN_SYSTEM,
    ADMIN_DASHBOARD,
    CALL_SCREEN
}

data class ActiveCallState(
    val participantName: String = "",
    val participantUsername: String = "",
    val isVideoCall: Boolean = false,
    val isGroupCall: Boolean = false,
    val isMuted: Boolean = false,
    val isCameraOn: Boolean = true,
    val isSpeakerOn: Boolean = true,
    val connectionQuality: String = "WebRTC Encrypted • 48kHz Opus HD"
)

data class PharbUiState(
    val isInitialized: Boolean = false,
    val showSplashOverlay: Boolean = false,
    val currentUserId: Long = 1L,
    val isAuthenticated: Boolean = true,
    val currentBottomTab: MainBottomTab = MainBottomTab.HOME,
    val currentFeedTab: HomeFeedTab = HomeFeedTab.FOR_YOU,
    val subScreen: SubScreenDestination = SubScreenDestination.NONE,
    val themeMode: PharbThemeMode = PharbThemeMode.DARK,
    val accentPreset: PharbAccentPreset = PharbAccentPreset.ROYAL_BLUE,
    val isArabic: Boolean = true,
    val fontScaleMultiplier: Float = 1.0f,
    val isOfflineMode: Boolean = false,
    val searchQuery: String = "",
    val searchFilter: String = "ALL", // ALL, USERS, POSTS, VIDEOS, HASHTAGS, COMMUNITIES, CHANNELS
    val searchHistory: List<String> = listOf("#PHARB", "الذكاء الاصطناعي", "أكاديمية فارب", "تصميم RTL"),
    val userInterests: List<String> = listOf("تقنية", "تعليم", "علوم", "تصميم", "أعمال"),
    val selectedStory: StoryEntity? = null,
    val selectedConversationId: Long? = null,
    val selectedPostForComments: PostEntity? = null,
    val selectedLiveStream: LiveStreamEntity? = null,
    val activeCall: ActiveCallState? = null,
    val generatedOtpCode: String = "",
    val statusBannerMessage: String? = null,
    val aiOutputPreview: String? = null
)

class PharbViewModel(
    private val repository: PharbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PharbUiState())
    val uiState: StateFlow<PharbUiState> = _uiState.asStateFlow()

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<PostEntity>> = repository.feedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedUsers: StateFlow<List<User>> = repository.cachedUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedPosts: StateFlow<List<Post>> = repository.cachedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedPosts: StateFlow<List<PostEntity>> = repository.unsyncedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsyncedPostCount: StateFlow<Int> = repository.unsyncedPostCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val draftPosts: StateFlow<List<PostEntity>> = repository.draftPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPosts: StateFlow<List<PostEntity>> = repository.savedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComments: StateFlow<List<CommentEntity>> = repository.allComments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStories: StateFlow<List<StoryEntity>> = repository.activeStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedStories: StateFlow<List<StoryEntity>> = repository.archivedStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortVideos: StateFlow<List<ShortVideoEntity>> = repository.shortVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communities: StateFlow<List<CommunityEntity>> = repository.communities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChannelEntity>> = repository.channels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveStreams: StateFlow<List<LiveStreamEntity>> = repository.liveStreams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportEntity>> = repository.reports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockMuteRelations: StateFlow<List<BlockMuteEntity>> = repository.blockMuteRelations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val verificationRequests: StateFlow<List<VerificationRequestEntity>> = repository.verificationRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<SessionEntity>> = repository.observeSessions(1L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            _uiState.update { it.copy(isInitialized = true) }
        }
    }

    fun currentUser(): UserEntity {
        val currentId = _uiState.value.currentUserId
        return users.value.find { it.id == currentId }
            ?: users.value.firstOrNull()
            ?: UserEntity(
                id = 1L,
                fullName = "طارق المنصور",
                username = "tarek_pharb",
                email = "tarek@pharb.io",
                passwordHash = "",
                passwordSalt = "",
                bio = "مهندس برمجيات ومؤسس مجتمعات تقنية | Less Clutter, More Experience",
                location = "الرياض • دبي",
                website = "https://pharb.network/@tarek",
                accountType = "CREATOR",
                isVerified = true,
                followersCount = 28400,
                followingCount = 342,
                role = "ADMIN"
            )
    }

    // --- Navigation & UI Controls ---
    fun selectBottomTab(tab: MainBottomTab) {
        _uiState.update {
            it.copy(
                currentBottomTab = tab,
                subScreen = SubScreenDestination.NONE,
                selectedConversationId = null
            )
        }
    }

    fun selectFeedTab(tab: HomeFeedTab) {
        _uiState.update { it.copy(currentFeedTab = tab) }
    }

    fun navigateToSubScreen(destination: SubScreenDestination) {
        _uiState.update { it.copy(subScreen = destination) }
    }

    fun navigateBackToMain() {
        _uiState.update {
            if (it.selectedConversationId != null) {
                it.copy(selectedConversationId = null)
            } else if (it.subScreen != SubScreenDestination.NONE) {
                it.copy(subScreen = SubScreenDestination.NONE, selectedStory = null, selectedLiveStream = null)
            } else if (it.currentBottomTab != MainBottomTab.HOME) {
                it.copy(currentBottomTab = MainBottomTab.HOME)
            } else {
                it
            }
        }
    }

    fun setThemeMode(mode: PharbThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        showBanner(if (_uiState.value.isArabic) "تم تطبيق ${mode.labelAr}" else "Applied ${mode.labelEn}")
    }

    fun setAccentPreset(preset: PharbAccentPreset) {
        _uiState.update { it.copy(accentPreset = preset) }
        showBanner(if (_uiState.value.isArabic) "تم تحديث لون الهوية إلى ${preset.labelAr}" else "Accent updated to ${preset.labelEn}")
    }

    fun toggleLanguage() {
        _uiState.update {
            val nextAr = !it.isArabic
            it.copy(
                isArabic = nextAr,
                statusBannerMessage = if (nextAr) "تم التبديل إلى اللغة العربية (RTL)" else "Switched to English (LTR)"
            )
        }
    }

    fun setFontScale(scale: Float) {
        _uiState.update { it.copy(fontScaleMultiplier = scale.coerceIn(0.85f, 1.35f)) }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val nextOffline = !_uiState.value.isOfflineMode
            if (!nextOffline) {
                repository.syncOfflinePosts()
            }
            _uiState.update {
                it.copy(
                    isOfflineMode = nextOffline,
                    statusBannerMessage = if (nextOffline) {
                        "تم تفعيل وضع عدم الاتصال (Offline Mode) — يتم قراءة وحفظ بيانات User و Post محليًا عبر Room Database"
                    } else {
                        "عاد الاتصال بالإنترنت — تمت مزامنة جميع المنشورات والمسودات المعلقة بنجاح ✅"
                    }
                )
            }
        }
    }

    fun syncOfflinePostsNow() {
        viewModelScope.launch {
            repository.syncOfflinePosts()
            _uiState.update {
                it.copy(
                    isOfflineMode = false,
                    statusBannerMessage = "تمت مزامنة جميع المنشورات المخزنة محليًا مع الخادم بنجاح ✅"
                )
            }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun showBanner(message: String) {
        _uiState.update { it.copy(statusBannerMessage = message) }
    }

    // --- Authentication, OTP & Sessions ---
    fun login(identifier: String, passwordPlain: String) {
        viewModelScope.launch {
            val result = repository.authenticateUser(identifier, passwordPlain)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUserId = user.id,
                        isAuthenticated = true,
                        subScreen = SubScreenDestination.NONE,
                        statusBannerMessage = "مرحبًا بعودتك، ${user.fullName} ✅"
                    )
                }
            }.onFailure { err ->
                showBanner(err.message ?: "خطأ في تسجيل الدخول")
            }
        }
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        passwordPlain: String,
        accountType: String
    ) {
        viewModelScope.launch {
            val strength = SecurityManager.evaluatePasswordStrength(passwordPlain)
            if (!strength.isAcceptable) {
                showBanner("كلمة المرور ضعيفة: يرجى استخدام 6 أحرف على الأقل مع أرقام أو رموز")
                return@launch
            }
            val result = repository.registerNewUser(fullName, username, email, phone, passwordPlain, accountType)
            result.onSuccess { newUser ->
                _uiState.update {
                    it.copy(
                        currentUserId = newUser.id,
                        isAuthenticated = true,
                        subScreen = SubScreenDestination.NONE,
                        statusBannerMessage = "تم إنشاء حسابك في PHARB وتشفير بياناتك بنجاح 🎉"
                    )
                }
            }.onFailure { err ->
                showBanner(err.message ?: "تعذر إنشاء الحساب")
            }
        }
    }

    fun requestOtpCode(destination: String) {
        val code = SecurityManager.generateOtpCode()
        _uiState.update {
            it.copy(
                generatedOtpCode = code,
                statusBannerMessage = "رمز التحقق OTP المرسل إلى $destination هو: $code"
            )
        }
    }

    fun verifyOtpAndLogin(enteredCode: String) {
        val expected = _uiState.value.generatedOtpCode
        if (enteredCode.trim() == expected && expected.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    generatedOtpCode = "",
                    subScreen = SubScreenDestination.NONE,
                    statusBannerMessage = "تم التحقق عبر رمز OTP بنجاح ✅"
                )
            }
        } else {
            showBanner("رمز OTP غير مطابق، يرجى التأكد من الرمز المكون من 6 أرقام")
        }
    }

    fun switchActiveDemoAccount(userId: Long) {
        val target = users.value.find { it.id == userId } ?: return
        _uiState.update {
            it.copy(
                currentUserId = target.id,
                isAuthenticated = true,
                subScreen = SubScreenDestination.NONE,
                statusBannerMessage = "تم التبديل إلى حساب: ${target.fullName} (@${target.username})"
            )
        }
    }

    fun logoutCurrentSession() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                subScreen = SubScreenDestination.AUTH_PORTAL,
                statusBannerMessage = "تم تسجيل الخروج بأمان من الجلسة الحالية"
            )
        }
    }

    fun revokeOtherSessions() {
        viewModelScope.launch {
            repository.revokeAllOtherSessions(currentUser().id)
            showBanner("تم إنهاء جميع الجلسات الأخرى بنجاح 🔒")
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            val strength = SecurityManager.evaluatePasswordStrength(newPassword)
            if (!strength.isAcceptable) {
                showBanner("يرجى اختيار كلمة مرور أقوى (${strength.labelAr})")
                return@launch
            }
            repository.changePassword(currentUser().id, newPassword)
            showBanner("تم تحديث كلمة المرور وتشفيرها عبر PBKDF2-SHA256 بنجاح ✅")
        }
    }

    // --- Posts, Polls, Reactions, Repost, Saved & Comments ---
    fun createNewPost(
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
        asDraft: Boolean = false
    ) {
        viewModelScope.launch {
            val safety = PharbAiEngine.scanContentSafety(content)
            if (!safety.isSafe) {
                showBanner(safety.messageAr)
                return@launch
            }
            val finalDrawable = when {
                mediaDrawableName.isNotBlank() -> mediaDrawableName
                postType == "IMAGE" || postType == "MULTI_IMAGE" -> "img_cover_default"
                postType == "VIDEO" -> "img_shorts_preview_1"
                else -> ""
            }
            val autoTags = hashtags.ifBlank {
                PharbAiEngine.suggestHashtags(content).joinToString(" ")
            }
            repository.createPost(
                author = currentUser(),
                content = content,
                postType = postType,
                mediaDrawableName = finalDrawable,
                linkUrl = linkUrl,
                pollOptions = pollOptions,
                audioDurationSec = audioDurationSec,
                fileAttachmentName = fileAttachmentName,
                hashtags = autoTags,
                mentions = mentions,
                locationTag = locationTag,
                audience = audience,
                category = category,
                isDraft = asDraft,
                isOfflineMode = _uiState.value.isOfflineMode
            )
            _uiState.update {
                it.copy(
                    currentBottomTab = MainBottomTab.HOME,
                    statusBannerMessage = if (asDraft) {
                        "تم حفظ المسودة في التخزين المحلي بنجاح 📝"
                    } else if (it.isOfflineMode) {
                        "تم حفظ المنشور محليًا وسيتم رفعه تلقائيًا عند عودة الاتصال 📡"
                    } else {
                        "تم نشر منشورك على PHARB بنجاح 🚀"
                    }
                )
            }
        }
    }

    fun publishDraft(draft: PostEntity) {
        viewModelScope.launch {
            repository.publishDraft(draft, _uiState.value.isOfflineMode)
            showBanner("تم نشر المسودة بنجاح ✅")
        }
    }

    fun reactToPost(post: PostEntity, reactionType: String) {
        viewModelScope.launch {
            repository.reactToPost(post, reactionType)
        }
    }

    fun voteInPoll(post: PostEntity, optionIndex: Int) {
        viewModelScope.launch {
            repository.voteInPoll(post, optionIndex)
            showBanner("تم تسجيل صوتك في الاستطلاع بنجاح 📊")
        }
    }

    fun repost(post: PostEntity, quoteText: String = "") {
        viewModelScope.launch {
            repository.repostOrQuote(post, currentUser(), quoteText)
            showBanner(
                if (quoteText.isBlank()) "تمت إعادة نشر المحتوى مع حفظ حقوق الناشر الأصلي @${post.authorUsername} 🔁"
                else "تم نشر اقتباسك (Quote Post) بنجاح ✨"
            )
        }
    }

    fun toggleSavePost(post: PostEntity, collectionName: String = "المفضلة") {
        viewModelScope.launch {
            repository.toggleSavePost(post, collectionName)
            showBanner(
                if (!post.isSaved) "تم حفظ المحتوى في مجموعة «$collectionName» 🔖"
                else "تمت إزالة المحتوى من المحفوظات"
            )
        }
    }

    fun openCommentsSheet(post: PostEntity?) {
        _uiState.update { it.copy(selectedPostForComments = post) }
    }

    fun submitComment(post: PostEntity, commentText: String) {
        viewModelScope.launch {
            repository.addComment(post, currentUser(), commentText)
        }
    }

    // --- Follow System ---
    fun toggleFollowUser(target: UserEntity) {
        viewModelScope.launch {
            repository.toggleFollowUser(target)
        }
    }

    // --- Stories & Archive ---
    fun openStoryViewer(story: StoryEntity) {
        viewModelScope.launch {
            repository.markStoryViewed(story)
            _uiState.update {
                it.copy(
                    selectedStory = story.copy(isViewed = true, viewsCount = story.viewsCount + 1),
                    subScreen = SubScreenDestination.STORY_VIEWER
                )
            }
        }
    }

    fun createNewStory(caption: String, mediaType: String, musicTrack: String, question: String) {
        viewModelScope.launch {
            repository.createStory(
                author = currentUser(),
                caption = caption,
                mediaType = mediaType,
                musicTrack = musicTrack,
                interactiveQuestion = question
            )
            showBanner("تم نشر قصتك لمدة 24 ساعة في PHARB Stories ✨")
        }
    }

    fun moveStoryToArchive(story: StoryEntity) {
        viewModelScope.launch {
            repository.archiveStory(story)
            _uiState.update {
                it.copy(
                    subScreen = SubScreenDestination.STORY_ARCHIVE,
                    selectedStory = null,
                    statusBannerMessage = "تم نقل القصة إلى أرشيفك الخاص بنجاح 📦"
                )
            }
        }
    }

    // --- PHARB Shorts ---
    fun toggleLikeShort(video: ShortVideoEntity) {
        viewModelScope.launch { repository.toggleLikeShort(video) }
    }

    fun toggleSaveShort(video: ShortVideoEntity) {
        viewModelScope.launch { repository.toggleSaveShort(video) }
    }

    fun toggleFollowShortCreator(video: ShortVideoEntity) {
        viewModelScope.launch {
            repository.toggleFollowShortCreator(video)
            showBanner(
                if (!video.isFollowingCreator) "أصبحت تتابع @${video.creatorUsername} ✅"
                else "تم إلغاء متابعة @${video.creatorUsername}"
            )
        }
    }

    // --- PHARB Messenger & Calls ---
    fun openConversation(conversationId: Long?) {
        _uiState.update { it.copy(selectedConversationId = conversationId) }
    }

    fun sendMessage(
        conversation: ConversationEntity,
        content: String,
        messageType: String = "TEXT",
        mediaMeta: String = "",
        replyToPreview: String = ""
    ) {
        viewModelScope.launch {
            repository.sendMessage(
                conversation = conversation,
                sender = currentUser(),
                content = content,
                messageType = messageType,
                mediaMeta = mediaMeta,
                replyToPreview = replyToPreview
            )
        }
    }

    fun reactToMessage(message: MessageEntity, emoji: String) {
        viewModelScope.launch { repository.reactToMessage(message, emoji) }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch { repository.deleteMessage(messageId) }
    }

    fun createConversation(title: String, username: String, isGroup: Boolean) {
        viewModelScope.launch {
            val id = repository.createDirectOrGroupConversation(title, username, isGroup)
            _uiState.update {
                it.copy(
                    selectedConversationId = id,
                    statusBannerMessage = if (isGroup) "تم إنشاء المحادثة الجماعية «$title»" else "تم بدء المحادثة مع $title"
                )
            }
        }
    }

    fun startCall(participantName: String, participantUsername: String, isVideo: Boolean, isGroup: Boolean = false) {
        _uiState.update {
            it.copy(
                activeCall = ActiveCallState(
                    participantName = participantName,
                    participantUsername = participantUsername,
                    isVideoCall = isVideo,
                    isGroupCall = isGroup
                ),
                subScreen = SubScreenDestination.CALL_SCREEN
            )
        }
    }

    fun toggleCallMute() {
        _uiState.update { state ->
            state.copy(activeCall = state.activeCall?.copy(isMuted = !state.activeCall.isMuted))
        }
    }

    fun toggleCallCamera() {
        _uiState.update { state ->
            state.copy(activeCall = state.activeCall?.copy(isCameraOn = !state.activeCall.isCameraOn))
        }
    }

    fun endActiveCall() {
        _uiState.update {
            it.copy(
                activeCall = null,
                subScreen = SubScreenDestination.NONE,
                statusBannerMessage = "انتهت المكالمة المشفرة عبر WebRTC"
            )
        }
    }

    // --- Communities, Channels & Live ---
    fun toggleJoinCommunity(community: CommunityEntity) {
        viewModelScope.launch {
            repository.toggleJoinCommunity(community)
            showBanner(
                if (!community.isJoined) "انضممت إلى «${community.name}» 🎉"
                else "غادرت مجتمع «${community.name}»"
            )
        }
    }

    fun createCommunity(name: String, category: String, description: String, rules: String) {
        viewModelScope.launch {
            repository.createCommunity(name, category, description, rules, currentUser().username)
            showBanner("تم إنشاء مجتمع «$name» بنجاح 🌐")
        }
    }

    fun toggleSubscribeChannel(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleSubscribeChannel(channel)
            showBanner(
                if (!channel.isSubscribed) "اشتركت في قناة «${channel.name}» 🔔"
                else "تم إلغاء الاشتراك من القناة"
            )
        }
    }

    fun createChannel(name: String, handle: String, ownerType: String, category: String, description: String) {
        viewModelScope.launch {
            repository.createChannel(name, handle, ownerType, category, description)
            showBanner("تم إطلاق قناة «$name» بنجاح 📡")
        }
    }

    fun startLiveStream(title: String, category: String) {
        viewModelScope.launch {
            repository.startLiveStream(currentUser(), title, category)
            _uiState.update {
                it.copy(
                    subScreen = SubScreenDestination.LIVE_BROADCAST_HUB,
                    statusBannerMessage = "بدأ البث المباشر على PHARB Live 🔴"
                )
            }
        }
    }

    fun sendLiveStreamComment(stream: LiveStreamEntity, text: String) {
        viewModelScope.launch {
            repository.sendLiveStreamComment(stream, currentUser().fullName, text)
        }
    }

    fun endLiveStream(stream: LiveStreamEntity) {
        viewModelScope.launch {
            repository.endLiveStream(stream)
            showBanner("تم إنهاء البث المباشر وحفظ ملخص الإحصائيات")
        }
    }

    // --- Search & Recommendations ---
    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val updatedHistory = if (query.length >= 3 && query !in state.searchHistory) {
                (listOf(query) + state.searchHistory).take(8)
            } else state.searchHistory
            state.copy(searchQuery = query, searchHistory = updatedHistory)
        }
    }

    fun setSearchFilter(filter: String) {
        _uiState.update { it.copy(searchFilter = filter) }
    }

    fun toggleInterestTopic(topic: String) {
        _uiState.update { state ->
            val next = if (topic in state.userInterests) {
                state.userInterests - topic
            } else {
                state.userInterests + topic
            }
            state.copy(
                userInterests = next,
                statusBannerMessage = "تم تحديث إشارات محرك التوصيات الشفاف في PHARB ✅"
            )
        }
    }

    // --- Notifications, Privacy, Reports, Verification & Admin ---
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            showBanner("تم تعليم جميع الإشعارات كمقروءة ✓")
        }
    }

    fun updateProfileAndPrivacy(
        fullName: String,
        bio: String,
        location: String,
        website: String,
        accountType: String,
        isPrivateAccount: Boolean,
        showOnlineStatus: Boolean,
        showLastSeen: Boolean,
        allowMessagesFrom: String,
        allowTagging: Boolean,
        searchableInDirectory: Boolean
    ) {
        viewModelScope.launch {
            val user = currentUser()
            repository.updateUserProfile(
                user.copy(
                    fullName = SecurityManager.sanitizeInput(fullName),
                    bio = SecurityManager.sanitizeInput(bio),
                    location = SecurityManager.sanitizeInput(location),
                    website = SecurityManager.sanitizeInput(website),
                    accountType = accountType,
                    isPrivateAccount = isPrivateAccount,
                    showOnlineStatus = showOnlineStatus,
                    showLastSeen = showLastSeen,
                    allowMessagesFrom = allowMessagesFrom,
                    allowTagging = allowTagging,
                    searchableInDirectory = searchableInDirectory
                )
            )
            showBanner("تم حفظ إعدادات الملف الشخصي ومركز الخصوصية بنجاح 🛡️")
        }
    }

    fun submitReport(targetType: String, targetId: Long, summary: String, reason: String) {
        viewModelScope.launch {
            repository.submitReport(
                targetType = targetType,
                targetId = targetId,
                targetSummary = summary,
                reporterUsername = currentUser().username,
                reason = reason
            )
            showBanner("تم إرسال البلاغ ($reason) إلى فريق مراجعة المحتوى في PHARB 🛡️")
        }
    }

    fun blockOrMuteUser(username: String, displayName: String, relationType: String) {
        viewModelScope.launch {
            repository.addBlockOrMute(username, displayName, relationType)
            val actionAr = when (relationType) {
                "BLOCK" -> "حظر"
                "MUTE" -> "كتم"
                else -> "تقييد"
            }
            showBanner("تم $actionAr الحساب @$username بنجاح")
        }
    }

    fun removeBlockOrMute(id: Long) {
        viewModelScope.launch {
            repository.removeBlockOrMute(id)
            showBanner("تمت إزالة القيد عن الحساب")
        }
    }

    fun requestVerification(documentRef: String, justification: String) {
        viewModelScope.launch {
            repository.submitVerificationRequest(currentUser(), documentRef, justification)
            showBanner("تم تقديم طلب التوثيق (PHARB Verified) للمراجعة الرسمية ✅")
        }
    }

    fun adminReviewVerification(request: VerificationRequestEntity, approve: Boolean) {
        viewModelScope.launch {
            repository.reviewVerificationRequest(request, approve)
            showBanner(
                if (approve) "تم منح شارة التوثيق الرسمية PHARB Verified لـ @${request.username} ✅"
                else "تم رفض طلب التوثيق لـ @${request.username}"
            )
        }
    }

    fun adminResolveReport(report: ReportEntity, action: String) {
        viewModelScope.launch {
            repository.resolveReportAsAdmin(report, action)
            showBanner("تم تنفيذ الإجراء الإداري على البلاغ #${report.id}")
        }
    }

    fun adminToggleSuspendUser(user: UserEntity) {
        viewModelScope.launch {
            repository.toggleSuspendUserAsAdmin(user)
            showBanner(
                if (!user.isSuspended) "تم تعليق الحساب @${user.username}"
                else "تمت إعادة تفعيل الحساب @${user.username}"
            )
        }
    }

    fun deleteMyAccountPermanently() {
        viewModelScope.launch {
            val current = currentUser()
            if (current.id != 1L) {
                repository.deleteAccount(current.id)
            }
            _uiState.update {
                it.copy(
                    currentUserId = 1L,
                    isAuthenticated = false,
                    subScreen = SubScreenDestination.AUTH_PORTAL,
                    statusBannerMessage = "تم حذف بيانات الحساب نهائيًا وفق سياسة الخصوصية في PHARB"
                )
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = PharbDatabase.getDatabase(context)
                    val repo = PharbRepository(db.pharbDao())
                    return PharbViewModel(repo) as T
                }
            }
        }
    }
}
