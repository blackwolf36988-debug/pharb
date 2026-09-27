package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.core.SecurityManager
import com.example.data.model.Message
import com.example.data.model.PharbTypeConverters
import com.example.data.model.Post
import com.example.data.model.User
import com.example.data.model.toDomainModel

@Database(
    entities = [
        User::class,
        Post::class,
        Message::class,
        UserEntity::class,
        SessionEntity::class,
        PostEntity::class,
        CommentEntity::class,
        StoryEntity::class,
        ShortVideoEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        CommunityEntity::class,
        ChannelEntity::class,
        LiveStreamEntity::class,
        NotificationEntity::class,
        ReportEntity::class,
        BlockMuteEntity::class,
        VerificationRequestEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(PharbTypeConverters::class)
abstract class PharbDatabase : RoomDatabase() {
    abstract fun pharbDao(): PharbDao

    companion object {
        @Volatile
        private var INSTANCE: PharbDatabase? = null

        fun getDatabase(context: Context): PharbDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PharbDatabase::class.java,
                    "pharb_production.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun ensureSeeded(dao: PharbDao) {
            if (dao.getUserCount() > 0) return

            val now = System.currentTimeMillis()
            val defaultSalt = SecurityManager.generateSalt()
            val defaultHash = SecurityManager.hashPassword("Pharb@2026", defaultSalt)

            val users = listOf(
                UserEntity(
                    id = 1L,
                    fullName = " طارق المنصور",
                    username = "tarek_pharb",
                    email = "tarek@pharb.io",
                    phone = "+966501234567",
                    passwordHash = defaultHash,
                    passwordSalt = defaultSalt,
                    bio = "مهندس برمجيات ومؤسس مجتمعات تقنية | أؤمن بفلسفة Less Clutter, More Experience 🌐✨",
                    location = "الرياض • دبي",
                    website = "https://pharb.network/@tarek",
                    avatarColorHex = 0xFF2563EB,
                    accountType = "CREATOR",
                    isVerified = true,
                    followersCount = 28400,
                    followingCount = 342,
                    isFollowedByMe = false,
                    role = "ADMIN"
                ),
                UserEntity(
                    id = 2L,
                    fullName = "أكاديمية فارب التعليمية",
                    username = "pharb_edu",
                    email = "edu@pharb.io",
                    phone = "+971509876543",
                    passwordHash = defaultHash,
                    passwordSalt = defaultSalt,
                    bio = "المنصة التعليمية الرسمية على PHARB — دروس تفاعلية، ملخصات PDF، واختبارات قصيرة في البرمجة والعلوم والتصميم.",
                    location = "أبوظبي • القاهرة",
                    website = "https://edu.pharb.network",
                    avatarColorHex = 0xFF10B981,
                    accountType = "EDUCATION",
                    isVerified = true,
                    followersCount = 94200,
                    followingCount = 85,
                    isFollowedByMe = true,
                    role = "USER"
                ),
                UserEntity(
                    id = 3L,
                    fullName = "نورة العلي | استوديو أفق",
                    username = "noura_design",
                    email = "noura@pharb.io",
                    phone = "+96555112233",
                    passwordHash = defaultHash,
                    passwordSalt = defaultSalt,
                    bio = "مصممة نظم واجهات UX/UI وهوية بصرية | أشارك يوميات التصميم المعماري والرقمي 🎨",
                    location = "الكويت",
                    website = "https://ufuq.studio",
                    avatarColorHex = 0xFF7C3AED,
                    accountType = "BUSINESS",
                    isVerified = true,
                    followersCount = 41900,
                    followingCount = 210,
                    isFollowedByMe = true,
                    role = "USER"
                ),
                UserEntity(
                    id = 4L,
                    fullName = "د. سامي الحسن",
                    username = "dr_sami_ai",
                    email = "sami@pharb.io",
                    phone = "+962791122334",
                    passwordHash = defaultHash,
                    passwordSalt = defaultSalt,
                    bio = "باحث في الذكاء الاصطناعي الأخلاقي ومعالجة اللغات الطبيعية العربية (NLP) 🤖📚",
                    location = "عمّان",
                    website = "https://ai-arabia.org",
                    avatarColorHex = 0xFF0284C7,
                    accountType = "EDUCATION",
                    isVerified = true,
                    followersCount = 63100,
                    followingCount = 190,
                    isFollowedByMe = true,
                    isPrivateAccount = false,
                    role = "MODERATOR"
                ),
                UserEntity(
                    id = 5L,
                    fullName = "سارة الكيلاني",
                    username = "sara_ventures",
                    email = "sara@pharb.io",
                    phone = "+201012345678",
                    passwordHash = defaultHash,
                    passwordSalt = defaultSalt,
                    bio = "مستثمرة في الشركات الناشئة والتقنية المالية | نربط رواد الأعمال بالفرص الحقيقية 💼",
                    location = "القاهرة",
                    website = "https://mena-founders.vc",
                    avatarColorHex = 0xFFD97706,
                    accountType = "BUSINESS",
                    isVerified = false,
                    isPrivateAccount = true,
                    followersCount = 15800,
                    followingCount = 412,
                    isFollowedByMe = false,
                    role = "USER"
                )
            )
            dao.insertUsers(users)
            dao.upsertCachedUsers(users.map { it.toDomainModel() })

            val jwt = SecurityManager.generateJwtToken(1L, "tarek_pharb", "ADMIN")
            val rt = SecurityManager.generateRefreshToken(1L)
            dao.insertSession(
                SessionEntity(
                    id = 1L,
                    userId = 1L,
                    deviceName = "Android 16 • PHARB Official Client",
                    ipAddress = "192.168.1.104 (TLS 1.3 Encrypted)",
                    jwtToken = jwt,
                    refreshToken = rt,
                    isCurrentDevice = true,
                    lastActiveAt = now
                )
            )
            dao.insertSession(
                SessionEntity(
                    id = 2L,
                    userId = 1L,
                    deviceName = "PHARB Web Desktop • macOS Safari",
                    ipAddress = "10.24.88.19",
                    jwtToken = SecurityManager.generateJwtToken(1L, "tarek_pharb", "ADMIN"),
                    refreshToken = SecurityManager.generateRefreshToken(1L),
                    isCurrentDevice = false,
                    lastActiveAt = now - 7200_000L
                )
            )

            // Seed Stories (Active + 1 Archived)
            dao.insertStories(
                listOf(
                    StoryEntity(
                        id = 1L,
                        authorId = 1L,
                        authorName = "قصتي (طارق)",
                        authorUsername = "tarek_pharb",
                        authorAvatarColor = 0xFF2563EB,
                        authorVerified = true,
                        caption = "إطلاق النسخة الأولى من نظام التصميم الهندسي لـ PHARB 🚀 أقل ازدحامًا، أكثر وضوحًا!",
                        mediaType = "IMAGE",
                        backgroundHex = 0xFF0B192C,
                        drawableName = "img_story_featured",
                        musicTrack = "PHARB Ambient Horizon • 0:30",
                        viewsCount = 1420,
                        isViewed = false,
                        isArchived = false,
                        expiresAt = now + 22 * 3600_000L,
                        createdAt = now - 2 * 3600_000L
                    ),
                    StoryEntity(
                        id = 2L,
                        authorId = 2L,
                        authorName = "أكاديمية فارب",
                        authorUsername = "pharb_edu",
                        authorAvatarColor = 0xFF10B981,
                        authorVerified = true,
                        caption = "سؤال اليوم للمبرمجين: ما هي الميزة الأهم في معمارية تطبيقات الهواتف الحديثة؟",
                        mediaType = "POLL",
                        backgroundHex = 0xFF064E3B,
                        drawableName = "img_onboarding_hero",
                        interactiveQuestion = "اختر الأولوية القصوى لديك:",
                        pollOptionA = "الأداء وسرعة الاستجابة (68%)",
                        pollOptionB = "العمل دون اتصال Offline (32%)",
                        viewsCount = 3890,
                        isViewed = false,
                        isArchived = false,
                        expiresAt = now + 18 * 3600_000L,
                        createdAt = now - 5 * 3600_000L
                    ),
                    StoryEntity(
                        id = 3L,
                        authorId = 3L,
                        authorName = "نورة العلي",
                        authorUsername = "noura_design",
                        authorAvatarColor = 0xFF7C3AED,
                        authorVerified = true,
                        caption = "كواليس تصميم الوضع الداكن Graphite والـ AMOLED المريح للعين في استوديو أفق ✨",
                        mediaType = "IMAGE",
                        backgroundHex = 0xFF1E1B4B,
                        drawableName = "img_cover_default",
                        musicTrack = "Minimal Synth Wave",
                        viewsCount = 2150,
                        isViewed = false,
                        isArchived = false,
                        expiresAt = now + 14 * 3600_000L,
                        createdAt = now - 8 * 3600_000L
                    ),
                    StoryEntity(
                        id = 4L,
                        authorId = 4L,
                        authorName = "د. سامي الحسن",
                        authorUsername = "dr_sami_ai",
                        authorAvatarColor = 0xFF0284C7,
                        authorVerified = true,
                        caption = "بث مباشر الليلة الساعة 9 مساءً حول الذكاء الاصطناعي المسؤول وخصوصية البيانات 🤖",
                        mediaType = "QUESTION",
                        backgroundHex = 0xFF0C4A6E,
                        drawableName = "img_shorts_preview_1",
                        interactiveQuestion = "اطرح سؤالك للبث المباشر الليلة:",
                        viewsCount = 4120,
                        isViewed = true,
                        isArchived = false,
                        expiresAt = now + 10 * 3600_000L,
                        createdAt = now - 12 * 3600_000L
                    ),
                    StoryEntity(
                        id = 5L,
                        authorId = 1L,
                        authorName = "طارق المنصور",
                        authorUsername = "tarek_pharb",
                        authorAvatarColor = 0xFF2563EB,
                        authorVerified = true,
                        caption = "مسودة المعمارية السحابية الأولى لمنصة PHARB قبل أسبوع (محفوظة في الأرشيف الخاص)",
                        mediaType = "TEXT",
                        backgroundHex = 0xFF0B192C,
                        drawableName = "img_cover_default",
                        viewsCount = 980,
                        isViewed = true,
                        isArchived = true,
                        expiresAt = now - 48 * 3600_000L,
                        createdAt = now - 72 * 3600_000L
                    )
                )
            )

            // Seed Posts (Text, Image, Poll, Audio, Educational File, Business Link)
            val initialPosts = listOf(
                PostEntity(
                    id = 1L,
                    authorId = 1L,
                    authorName = "طارق المنصور",
                    authorUsername = "tarek_pharb",
                    authorAvatarColor = 0xFF2563EB,
                    authorVerified = true,
                    authorAccountType = "CREATOR",
                    content = "أهلاً بكم في منصة PHARB 🌐\nبنينا هذه المنصة على مبدأ أساسي:\n«Less Clutter — More Experience»\nأقل ازدحامًا، أكثر وضوحًا، وتجربة عربية وعالمية تحترم وقتك وخصوصيتك وتجمع المنشورات، الفيديو القصير، المجتمعات، والقنوات التعليمية في مكان واحد.",
                    postType = "IMAGE",
                    mediaDrawableName = "img_onboarding_hero",
                    hashtags = "#PHARB #تواصل_أبدع_اكتشف #تقنية #تصميم",
                    mentions = "@pharb_edu @noura_design",
                    locationTag = "الرياض، المملكة العربية السعودية",
                    audience = "PUBLIC",
                    category = "تقنية",
                    likeCount = 428,
                    loveCount = 195,
                    wowCount = 64,
                    myReaction = "LOVE",
                    commentsCount = 3,
                    repostCount = 87,
                    isSaved = true,
                    savedCollectionName = "المفضلة التقنية",
                    createdAt = now - 1800_000L
                ),
                PostEntity(
                    id = 2L,
                    authorId = 2L,
                    authorName = "أكاديمية فارب التعليمية",
                    authorUsername = "pharb_edu",
                    authorAvatarColor = 0xFF10B981,
                    authorVerified = true,
                    authorAccountType = "EDUCATION",
                    content = "📚 درس تفاعلي جديد ضمن مسار هندسة النظم الموزعة (System Architecture):\nكيف تصمم قاعدة بيانات PostgreSQL مع طبقة تخزين مؤقت Redis لخدمة أكثر من مليون مستخدم متزامن دون اختناق؟\nأرفقنا لكم الدليل الكامل بصيغة PDF مع اختبار قصير داخل القناة التعليمية.",
                    postType = "FILE",
                    fileAttachmentName = "PHARB_Scalable_Architecture_Guide_2026.pdf (4.2 MB)",
                    hashtags = "#PHARB_Education #تعليم #برمجة #قواعد_بيانات",
                    locationTag = "أبوظبي",
                    audience = "PUBLIC",
                    category = "تعليم",
                    likeCount = 312,
                    loveCount = 108,
                    wowCount = 49,
                    commentsCount = 2,
                    repostCount = 134,
                    isSaved = true,
                    savedCollectionName = "دروس ومراجع",
                    createdAt = now - 5400_000L
                ),
                PostEntity(
                    id = 3L,
                    authorId = 4L,
                    authorName = "د. سامي الحسن",
                    authorUsername = "dr_sami_ai",
                    authorAvatarColor = 0xFF0284C7,
                    authorVerified = true,
                    authorAccountType = "EDUCATION",
                    content = "استطلاع الأسبوع لمجتمع الذكاء الاصطناعي في PHARB 📊:\nما هي الميزة الذكية الأكثر فائدة لك أثناء كتابة المحتوى اليومي؟",
                    postType = "POLL",
                    pollOptionsSerialized = "تلخيص المقالات الطويلة فورًا:142|الترجمة الفورية ثنائية اللغة:98|اقتراح الوسوم وتحسين الصياغة:116|فلترة المحتوى المزعج Spam:84",
                    votedPollOptionIndex = -1,
                    hashtags = "#الذكاء_الاصطناعي #استطلاع_فارب #PHARB_AI",
                    locationTag = "عمّان",
                    audience = "PUBLIC",
                    category = "علوم",
                    likeCount = 189,
                    loveCount = 44,
                    laughCount = 6,
                    wowCount = 31,
                    commentsCount = 1,
                    repostCount = 29,
                    createdAt = now - 10800_000L
                ),
                PostEntity(
                    id = 4L,
                    authorId = 3L,
                    authorName = "نورة العلي | استوديو أفق",
                    authorUsername = "noura_design",
                    authorAvatarColor = 0xFF7C3AED,
                    authorVerified = true,
                    authorAccountType = "BUSINESS",
                    content = "🎙️ تدوينة صوتية سريعة (1:15 دقيقة):\nلماذا اخترنا درجات الأزرق الملكي (Royal Blue) والكحلي العميق (Deep Navy) مع رمادي Graphite لبناء هوية مريحة نفسيًا للعين خلال القراءة الليلية؟",
                    postType = "AUDIO",
                    audioDurationSec = 75,
                    mediaDrawableName = "img_cover_default",
                    linkUrl = "https://ufuq.studio/pharb-design-system",
                    hashtags = "#تصميم #هوية_بصرية #UX #PHARB_Business",
                    locationTag = "الكويت",
                    audience = "PUBLIC",
                    category = "تصميم",
                    likeCount = 265,
                    loveCount = 152,
                    wowCount = 28,
                    commentsCount = 1,
                    repostCount = 41,
                    createdAt = now - 18000_000L
                )
            )
            dao.insertPosts(initialPosts)
            dao.upsertCachedPosts(initialPosts.map { it.toDomainModel() })

            // Seed Comments
            dao.insertComments(
                listOf(
                    CommentEntity(
                        postId = 1L,
                        authorName = "د. سامي الحسن",
                        authorUsername = "dr_sami_ai",
                        authorAvatarColor = 0xFF0284C7,
                        authorVerified = true,
                        content = "تصميم هادئ ومنظم جدًا، خاصة سرعة الانتقال ودعم اللغة العربية RTL الأصلي!",
                        likesCount = 34,
                        createdAt = now - 1500_000L
                    ),
                    CommentEntity(
                        postId = 1L,
                        authorName = "نورة العلي",
                        authorUsername = "noura_design",
                        authorAvatarColor = 0xFF7C3AED,
                        authorVerified = true,
                        content = "فخورة جدًا بالهوية البصرية الجديدة، توازن مثالي بين البساطة والعمق الهندسي 💙",
                        likesCount = 28,
                        createdAt = now - 1200_000L
                    ),
                    CommentEntity(
                        postId = 1L,
                        authorName = "أكاديمية فارب التعليمية",
                        authorUsername = "pharb_edu",
                        authorAvatarColor = 0xFF10B981,
                        authorVerified = true,
                        content = "متحمسون لإطلاق القنوات التعليمية والمجموعات الدراسية لجميع الطلاب العرب!",
                        likesCount = 41,
                        createdAt = now - 900_000L
                    ),
                    CommentEntity(
                        postId = 2L,
                        authorName = "طارق المنصور",
                        authorUsername = "tarek_pharb",
                        authorAvatarColor = 0xFF2563EB,
                        authorVerified = true,
                        content = "مرجع ممتاز لكل مهندس يرغب في فهم التوسع الأفقي وقواعد البيانات.",
                        likesCount = 19,
                        createdAt = now - 4000_000L
                    )
                )
            )

            // Seed Short Videos (PHARB SHORTS)
            dao.insertShortVideos(
                listOf(
                    ShortVideoEntity(
                        id = 1L,
                        creatorId = 2L,
                        creatorName = "أكاديمية فارب التعليمية",
                        creatorUsername = "pharb_edu",
                        creatorAvatarColor = 0xFF10B981,
                        creatorVerified = true,
                        isFollowingCreator = true,
                        caption = "في 60 ثانية: كيف يعمل Adaptive Bitrate و CDN لتشغيل الفيديو الفوري بدون تقطيع حتى على الإنترنت الضعيف؟ ⚡🌐",
                        hashtags = "#PHARB_Shorts #تعليم #تقنية #شبكات",
                        audioTitle = "صوت أصلي — أكاديمية فارب التعليمية",
                        resolutionLabel = "1080p Adaptive • H.265 CDN",
                        bitrateKbps = 2450,
                        drawableName = "img_shorts_preview_1",
                        accentColorHex = 0xFF10B981,
                        likesCount = 14800,
                        isLiked = true,
                        commentsCount = 342,
                        sharesCount = 1290,
                        savesCount = 4120,
                        isSaved = true,
                        viewsCount = 92400,
                        createdAt = now - 3600_000L
                    ),
                    ShortVideoEntity(
                        id = 2L,
                        creatorId = 3L,
                        creatorName = "نورة العلي | استوديو أفق",
                        creatorUsername = "noura_design",
                        creatorAvatarColor = 0xFF7C3AED,
                        creatorVerified = true,
                        isFollowingCreator = true,
                        caption = "3 قواعد ذهبية لتصميم واجهات عربية RTL مريحة للعين باستخدام نظام الشبكة 8dp ✨🎨",
                        hashtags = "#تصميم #واجهات #RTL #PHARB_Design",
                        audioTitle = "Calm Studio Lo-Fi • مرخص لمنصة PHARB",
                        resolutionLabel = "1080p 60fps • Adaptive",
                        bitrateKbps = 2800,
                        drawableName = "img_story_featured",
                        accentColorHex = 0xFF7C3AED,
                        likesCount = 9640,
                        isLiked = false,
                        commentsCount = 218,
                        sharesCount = 840,
                        savesCount = 2950,
                        isSaved = false,
                        viewsCount = 54300,
                        createdAt = now - 7200_000L
                    ),
                    ShortVideoEntity(
                        id = 3L,
                        creatorId = 4L,
                        creatorName = "د. سامي الحسن",
                        creatorUsername = "dr_sami_ai",
                        creatorAvatarColor = 0xFF0284C7,
                        creatorVerified = true,
                        isFollowingCreator = true,
                        caption = "الفرق بين الخوارزميات المغلقة وخوارزمية التوصيات الشفافة في PHARB التي تمنحك السيطرة الكاملة 🤖🔍",
                        hashtags = "#الذكاء_الاصطناعي #خصوصية #PHARB_AI",
                        audioTitle = "صوت أصلي — د. سامي الحسن",
                        resolutionLabel = "720p Data-Saver • Adaptive",
                        bitrateKbps = 1500,
                        drawableName = "img_onboarding_hero",
                        accentColorHex = 0xFF0284C7,
                        likesCount = 19300,
                        isLiked = false,
                        commentsCount = 512,
                        sharesCount = 2410,
                        savesCount = 5600,
                        isSaved = true,
                        viewsCount = 128000,
                        createdAt = now - 14400_000L
                    )
                )
            )

            // Seed Conversations & Messages (PHARB MESSENGER)
            dao.insertConversations(
                listOf(
                    ConversationEntity(
                        id = 1L,
                        title = "نورة العلي | استوديو أفق",
                        participantUsername = "noura_design",
                        participantAvatarColor = 0xFF7C3AED,
                        isGroup = false,
                        memberCount = 2,
                        isOnline = true,
                        lastSeenStatus = "متصل الآن",
                        isTyping = false,
                        lastMessagePreview = "أرسلت لك ملف الرموز الهندسية الجديدة للشعار بصيغة SVG ✨",
                        unreadCount = 2,
                        updatedAt = now - 600_000L
                    ),
                    ConversationEntity(
                        id = 2L,
                        title = "مجموعة مهندسي معمارية PHARB",
                        participantUsername = "pharb_core_team",
                        participantAvatarColor = 0xFF2563EB,
                        isGroup = true,
                        memberCount = 14,
                        isOnline = true,
                        lastSeenStatus = "8 أعضاء متصلون الآن",
                        isTyping = true,
                        lastMessagePreview = "د. سامي: تم تفعيل نظام التشفير وفحص الروابط بنجاح ✅",
                        unreadCount = 5,
                        updatedAt = now - 1800_000L
                    ),
                    ConversationEntity(
                        id = 3L,
                        title = "أكاديمية فارب التعليمية",
                        participantUsername = "pharb_edu",
                        participantAvatarColor = 0xFF10B981,
                        isGroup = false,
                        memberCount = 2,
                        isOnline = false,
                        lastSeenStatus = "آخر ظهور منذ 25 دقيقة",
                        isTyping = false,
                        lastMessagePreview = "شكرًا لمشاركتك في إعداد منهج هندسة البرمجيات!",
                        unreadCount = 0,
                        updatedAt = now - 7200_000L
                    )
                )
            )

            dao.insertMessages(
                listOf(
                    MessageEntity(
                        id = 1L,
                        conversationId = 1L,
                        senderId = 3L,
                        senderName = "نورة العلي",
                        isFromMe = false,
                        content = "مرحباً طارق! انتهينا من مراجعة تباين الألوان للوضع الليلي Graphite ووضع AMOLED.",
                        messageType = "TEXT",
                        reactionEmoji = "💙",
                        deliveryStatus = "SEEN",
                        createdAt = now - 1200_000L
                    ),
                    MessageEntity(
                        id = 2L,
                        conversationId = 1L,
                        senderId = 1L,
                        senderName = "طارق المنصور",
                        isFromMe = true,
                        content = "عمل رائع يا نورة! التباين واضح جدًا ومريح للقراءة الطويلة بالعربية والإنجليزية.",
                        messageType = "TEXT",
                        replyToPreview = "مرحباً طارق! انتهينا من مراجعة تباين الألوان...",
                        deliveryStatus = "SEEN",
                        createdAt = now - 900_000L
                    ),
                    MessageEntity(
                        id = 3L,
                        conversationId = 1L,
                        senderId = 3L,
                        senderName = "نورة العلي",
                        isFromMe = false,
                        content = "أرسلت لك ملف الرموز الهندسية الجديدة للشعار بصيغة SVG ✨",
                        messageType = "FILE",
                        mediaMeta = "PHARB_Brand_Tokens_v1.svg • 840 KB",
                        deliveryStatus = "DELIVERED",
                        createdAt = now - 600_000L
                    ),
                    MessageEntity(
                        id = 4L,
                        conversationId = 2L,
                        senderId = 4L,
                        senderName = "د. سامي الحسن",
                        isFromMe = false,
                        content = "تم تفعيل نظام التشفير وفحص الروابط بنجاح ✅ جميع الجلسات تعمل عبر JWT + Refresh Tokens.",
                        messageType = "TEXT",
                        reactionEmoji = "🚀",
                        deliveryStatus = "SEEN",
                        createdAt = now - 1800_000L
                    )
                )
            )

            // Seed Communities (Section 15)
            dao.insertCommunities(
                listOf(
                    CommunityEntity(
                        id = 1L,
                        name = "مجتمع مطوري ومهندسي العرب",
                        category = "تقنية",
                        description = "مساحة حوارية متخصصة في هندسة البرمجيات، تطبيقات Android و iOS، الحوسبة السحابية، وأمن المعلومات.",
                        rules = "1. الاحترام المتبادل والنقاش العلمي البناء.\n2. يمنع نشر الروابط المضللة أو الإعلانات العشوائية.\n3. إرفاق المصدر أو الكود التوضيحي عند طرح المشكلات التقنية.",
                        coverColorHex = 0xFF1E56A0,
                        membersCount = 48500,
                        postsCount = 3420,
                        isJoined = true,
                        moderatorUsername = "tarek_pharb"
                    ),
                    CommunityEntity(
                        id = 2L,
                        name = "مختبر العلوم والذكاء الاصطناعي",
                        category = "علوم",
                        description = "أحدث الأبحاث العلمية، نماذج اللغات العربية، وتطبيقات الذكاء الاصطناعي المسؤول في الطب والتعليم.",
                        rules = "1. توثيق المصادر والأوراق البحثية.\n2. احترام الخصوصية وأخلاقيات البحث العلمي.",
                        coverColorHex = 0xFF0284C7,
                        membersCount = 36200,
                        postsCount = 1980,
                        isJoined = true,
                        moderatorUsername = "dr_sami_ai"
                    ),
                    CommunityEntity(
                        id = 3L,
                        name = "رواد الأعمال والشركات الناشئة",
                        category = "أعمال",
                        description = "تبادل الخبرات حول تأسيس الشركات، نماذج العمل، الاستثمار الجريء، والتجارة الرقمية الحديثة.",
                        rules = "1. مشاركة تجارب حقيقية ودراسات حالة واضحة.\n2. يمنع الترويج لمشاريع غير مرخصة.",
                        coverColorHex = 0xFFD97706,
                        membersCount = 29400,
                        postsCount = 1540,
                        isJoined = false,
                        moderatorUsername = "sara_ventures"
                    ),
                    CommunityEntity(
                        id = 4L,
                        name = "فنون التصميم والهوية البصرية",
                        category = "ثقافة",
                        description = "مجتمع المصممين المبدعين لمشاركة أعمال UX/UI، الخطوط العربية، والهويات التجارية.",
                        rules = "1. احترام حقوق الملكية الفكرية ونسب الأعمال لأصحابها.\n2. النقد الفني البناء.",
                        coverColorHex = 0xFF7C3AED,
                        membersCount = 22100,
                        postsCount = 2190,
                        isJoined = true,
                        moderatorUsername = "noura_design"
                    ),
                    CommunityEntity(
                        id = 5L,
                        name = "الرياضة واللياقة المستدامة",
                        category = "رياضة",
                        description = "برامج التدريب اليومي، التغذية الصحية، ومتابعة البطولات الرياضية المحلية والعالمية.",
                        rules = "1. عدم تقديم وصفات طبية غير معتمدة.\n2. التشجيع بروح رياضية.",
                        coverColorHex = 0xFF059669,
                        membersCount = 18900,
                        postsCount = 1120,
                        isJoined = false,
                        moderatorUsername = "tarek_pharb"
                    )
                )
            )

            // Seed Channels (Section 16, 40 Business, 41 Education)
            dao.insertChannels(
                listOf(
                    ChannelEntity(
                        id = 1L,
                        name = "قناة مساقات علوم الحاسب — PHARB Education",
                        handle = "@pharb_cs_channel",
                        ownerType = "EDUCATION",
                        description = "قناة تعليمية رسمية تقدم محاضرات فيديو، ملخصات PDF، واختبارات تفاعلية أسبوعية.",
                        category = "تعليم",
                        subscribersCount = 78400,
                        isSubscribed = true,
                        isVerified = true,
                        latestBroadcast = "درس رقم 14: تصميم REST API و GraphQL مع التوثيق عبر OpenAPI 3.1",
                        resourceAttachment = "Lecture14_API_Architecture_Notes.pdf",
                        quizQuestion = "ما هو البروتوكول الأنسب للمحادثات الفورية ثنائية الاتجاه؟ (WebSocket)"
                    ),
                    ChannelEntity(
                        id = 2L,
                        name = "استوديو أفق للحلول الرقمية — PHARB Business",
                        handle = "@ufuq_business",
                        ownerType = "BUSINESS",
                        description = "الصفحة التجارية الرسمية لاستوديو أفق: تصميم الأنظمة الرقمية، استشارات تجربة المستخدم، وتطوير الهويات.",
                        category = "أعمال",
                        subscribersCount = 34900,
                        isSubscribed = true,
                        isVerified = true,
                        latestBroadcast = "إطلاق باقة تصميم الهوية الرقمية للشركات الناشئة مع لوحة إحصائيات متكاملة.",
                        resourceAttachment = "Ufuq_Services_Catalog_2026.pdf"
                    ),
                    ChannelEntity(
                        id = 3L,
                        name = "نشرة أخبار التقنية والابتكار",
                        handle = "@pharb_tech_news",
                        ownerType = "CREATOR",
                        description = "ملخص يومي سريع وموثوق لأهم أخبار التقنية والعلوم حول العالم بدون ضجيج.",
                        category = "أخبار",
                        subscribersCount = 112000,
                        isSubscribed = false,
                        isVerified = true,
                        latestBroadcast = "تقرير اليوم: نمو متسارع في تبني معايير التشفير وحماية الخصوصية في المنصات الاجتماعية."
                    )
                )
            )

            // Seed Live Streams (Section 14)
            dao.insertLiveStreams(
                listOf(
                    LiveStreamEntity(
                        id = 1L,
                        hostName = "د. سامي الحسن",
                        hostUsername = "dr_sami_ai",
                        hostAvatarColor = 0xFF0284C7,
                        hostVerified = true,
                        title = "ورشة عمل مباشرة: بناء محركات البحث والتوصية الذكية باللغة العربية",
                        category = "تعليم وتقنية",
                        viewersCount = 1840,
                        likesCount = 6420,
                        isLiveNow = true,
                        recentChatMessages = "أحمد: شرح واضح جدًا دكتور سامي!|ليلى: كيف ندعم الجذور اللغوية في البحث العربي؟|خالد: الصوت ممتاز والواجهة سلسة جدًا",
                        startedAt = now - 2400_000L
                    ),
                    LiveStreamEntity(
                        id = 2L,
                        hostName = "نورة العلي | استوديو أفق",
                        hostUsername = "noura_design",
                        hostAvatarColor = 0xFF7C3AED,
                        hostVerified = true,
                        title = "مراجعة مباشرة لتصميمات أعضاء مجتمع PHARB ونصائح UX عملية",
                        category = "تصميم وإبداع",
                        viewersCount = 920,
                        likesCount = 3150,
                        isLiveNow = true,
                        recentChatMessages = "مريم: هل يمكن مراجعة تصميم تطبيقي القادم؟|عمر: ألوان الهوية الجديدة مريحة جدًا للعين",
                        startedAt = now - 1500_000L
                    )
                )
            )

            // Seed Notifications (Section 18)
            dao.insertNotifications(
                listOf(
                    NotificationEntity(
                        id = 1L,
                        type = "SECURITY",
                        title = "تنبيه أمني: جلسة موثقة نشطة",
                        body = "تم تأمين حسابك بنجاح باستخدام تشفير PBKDF2-SHA256 ورموز جلسة JWT المحمية.",
                        actorName = "نظام أمان PHARB",
                        actorAvatarColor = 0xFF0B192C,
                        isRead = false,
                        createdAt = now - 300_000L
                    ),
                    NotificationEntity(
                        id = 2L,
                        type = "LIKE",
                        title = "تفاعل جديد على منشورك",
                        body = "أعجبت نورة العلي و 427 آخرون بمنشورك حول فلسفة التصميم في PHARB.",
                        actorName = "نورة العلي",
                        actorAvatarColor = 0xFF7C3AED,
                        isRead = false,
                        createdAt = now - 900_000L
                    ),
                    NotificationEntity(
                        id = 3L,
                        type = "COMMENT",
                        title = "تعليق جديد من د. سامي الحسن",
                        body = "علّق على منشورك: «تصميم هادئ ومنظم جدًا، خاصة سرعة الانتقال ودعم العربية!»",
                        actorName = "د. سامي الحسن",
                        actorAvatarColor = 0xFF0284C7,
                        isRead = false,
                        createdAt = now - 1500_000L
                    ),
                    NotificationEntity(
                        id = 4L,
                        type = "LIVE",
                        title = "بث مباشر الآن 🔴",
                        body = "بدأ د. سامي الحسن بثًا مباشرًا: «بناء محركات البحث والتوصية الذكية باللغة العربية»",
                        actorName = "د. سامي الحسن",
                        actorAvatarColor = 0xFF0284C7,
                        isRead = true,
                        createdAt = now - 2400_000L
                    ),
                    NotificationEntity(
                        id = 5L,
                        type = "FOLLOW",
                        title = "متابع جديد",
                        body = "بدأت أكاديمية فارب التعليمية (@pharb_edu) بمتابعتك.",
                        actorName = "أكاديمية فارب",
                        actorAvatarColor = 0xFF10B981,
                        isRead = true,
                        createdAt = now - 3600_000L
                    )
                )
            )

            // Seed Moderation Reports & Verification Requests for PHARB Admin Panel (Section 23, 27, 28)
            dao.insertReports(
                listOf(
                    ReportEntity(
                        id = 1L,
                        targetType = "POST",
                        targetId = 99L,
                        targetSummary = "منشور ترويجي مكرر يزعم أرباحًا فورية عبر روابط خارجية مشبوهة",
                        reporterUsername = "dr_sami_ai",
                        reason = "Spam",
                        status = "PENDING",
                        moderatorAction = "بانتظار مراجعة المشرف",
                        createdAt = now - 3600_000L
                    ),
                    ReportEntity(
                        id = 2L,
                        targetType = "USER",
                        targetId = 105L,
                        targetSummary = "حساب ينتحل صفة علامة تجارية رسمية بدون توثيق",
                        reporterUsername = "noura_design",
                        reason = "Impersonation",
                        status = "RESOLVED",
                        moderatorAction = "تم تعليق الحساب المنتحل وتفعيل حماية الهوية",
                        createdAt = now - 14400_000L
                    )
                )
            )

            dao.insertVerificationRequests(
                listOf(
                    VerificationRequestEntity(
                        id = 1L,
                        userId = 5L,
                        username = "sara_ventures",
                        fullName = "سارة الكيلاني",
                        accountType = "BUSINESS",
                        documentReference = "سجل تجاري وترخيص صندوق استثماري رقم #VC-88412",
                        justification = "صندوق استثماري رسمي يدعم رواد الأعمال والشركات التقنية الناشئة في المنطقة.",
                        status = "PENDING",
                        submittedAt = now - 7200_000L
                    )
                )
            )
        }
    }
}
