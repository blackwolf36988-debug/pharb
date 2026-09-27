package com.example

import com.example.core.PharbAiEngine
import com.example.core.SecurityManager
import com.example.data.model.Message
import com.example.data.model.PharbTypeConverters
import com.example.data.model.Post
import com.example.data.model.User
import com.example.data.model.toDomainModel
import com.example.data.model.toEntity
import com.example.ui.theme.PharbAccentPreset
import com.example.ui.theme.PharbAdminDarkColorScheme
import com.example.ui.theme.PharbCharcoal
import com.example.ui.theme.PharbDeepNavy
import com.example.ui.theme.PharbGraphite
import com.example.ui.theme.PharbThemeMode
import com.example.ui.theme.PharbVibrantBlue
import com.example.ui.theme.createPharbColorScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PHARB Unit & Security Test Suite (Section 48)
 * Verifies:
 * - PBKDF2-HMAC-SHA256 Password Salting & Hashing
 * - JWT & Refresh Token Generation
 * - XSS & SQL Injection Sanitization
 * - Responsible AI Hashtags, Summarization, Translation & Anti-Spam Moderation
 * - Core Data Classes (User, Post, Message) bidirectional mapping
 */
class ExampleUnitTest {

    @Test
    fun `password hashing never stores plaintext and verifies accurately`() {
        val salt = SecurityManager.generateSalt()
        val rawPassword = "PharbSecure#2026"
        val hash = SecurityManager.hashPassword(rawPassword, salt)

        assertNotEquals(rawPassword, hash)
        assertTrue(SecurityManager.verifyPassword(rawPassword, salt, hash))
        assertFalse(SecurityManager.verifyPassword("WrongPassword123", salt, hash))
    }

    @Test
    fun `jwt and refresh tokens are generated with valid structure`() {
        val jwt = SecurityManager.generateJwtToken(userId = 1L, username = "tarek_pharb", role = "ADMIN")
        val refresh = SecurityManager.generateRefreshToken(userId = 1L)

        assertEquals(3, jwt.split(".").size)
        assertTrue(refresh.startsWith("pharb_rt_1_"))
    }

    @Test
    fun `input sanitizer removes xss scripts and sql injection patterns`() {
        val malicious = "<script>alert('xss')</script>Hello PHARB; DROP TABLE users--"
        val cleaned = SecurityManager.sanitizeInput(malicious)

        assertFalse(cleaned.contains("<script>"))
        assertFalse(cleaned.contains("DROP TABLE"))
        assertTrue(cleaned.contains("Hello PHARB"))
    }

    @Test
    fun `responsible ai engine suggests hashtags summarizes and detects spam`() {
        val techPost = "نطور اليوم خوارزميات الذكاء الاصطناعي وتعلم الآلة في مجتمع البرمجة العربي"
        val tags = PharbAiEngine.suggestHashtags(techPost)
        assertTrue(tags.any { it.contains("تقنية") || it.contains("الذكاء_الاصطناعي") })

        val summary = PharbAiEngine.summarizePost(techPost, isArabic = true)
        assertTrue(summary.isNotBlank())

        val safeCheck = PharbAiEngine.scanContentSafety(techPost)
        assertTrue(safeCheck.isSafe)

        val spamCheck = PharbAiEngine.scanContentSafety("اربح مليون دولار مجانا اضغط هنا مجانا")
        assertFalse(spamCheck.isSafe)
    }

    @Test
    fun `core data classes User Post and Message map cleanly to and from Room entities`() {
        val user = User(
            id = 10L,
            fullName = "طارق المنصور",
            username = "tarek_pharb",
            email = "tarek@pharb.io",
            accountType = "CREATOR",
            isVerified = true
        )
        val userRoundTrip = user.toEntity().toDomainModel()
        assertEquals(user.username, userRoundTrip.username)
        assertTrue(userRoundTrip.isVerified)

        val post = Post(
            id = 20L,
            authorId = user.id,
            authorName = user.fullName,
            authorUsername = user.username,
            content = "منشور تجريبي لبنية البيانات الأساسية",
            hashtags = listOf("#PHARB", "#تقنية"),
            likeCount = 12,
            loveCount = 8
        )
        assertEquals(20, post.totalReactions)
        val postRoundTrip = post.toEntity().toDomainModel()
        assertEquals(listOf("#PHARB", "#تقنية"), postRoundTrip.hashtags)

        val message = Message(
            id = 30L,
            conversationId = 1L,
            senderId = user.id,
            senderName = user.fullName,
            isFromMe = true,
            content = "مرحبًا بك في PHARB Messenger",
            deliveryStatus = "SEEN"
        )
        val msgRoundTrip = message.toEntity().toDomainModel()
        assertEquals("SEEN", msgRoundTrip.deliveryStatus)
        assertEquals(message.content, msgRoundTrip.content)
    }

    @Test
    fun `custom Material3 dark ColorScheme matches admin panel dark aesthetic`() {
        val scheme = createPharbColorScheme(
            themeMode = PharbThemeMode.DARK,
            accentPreset = PharbAccentPreset.ROYAL_BLUE,
            systemDark = true
        )
        assertEquals(PharbCharcoal, scheme.background)
        assertEquals(PharbGraphite, scheme.surface)
        assertEquals(PharbDeepNavy, scheme.primaryContainer)
        assertEquals(PharbVibrantBlue, scheme.primary)
        assertEquals(PharbAdminDarkColorScheme.background, scheme.background)
    }

    @Test
    fun `room type converters and offline post sync flag persist accurately`() {
        val converters = PharbTypeConverters()
        val tags = listOf("#PHARB", "#OfflineMode", "#RoomDB")
        val serialized = converters.fromStringList(tags)
        assertEquals(tags, converters.toStringList(serialized))

        val offlinePost = Post(
            id = 55L,
            authorId = 1L,
            authorName = "طارق المنصور",
            authorUsername = "tarek_pharb",
            content = "منشور محفوظ محليًا في وضع عدم الاتصال Offline Mode",
            hashtags = tags,
            isSynced = false
        )
        val entity = offlinePost.toEntity()
        assertFalse(entity.isSynced)
        val restored = entity.toDomainModel()
        assertFalse(restored.isSynced)
        assertEquals(tags, restored.hashtags)
    }
}
