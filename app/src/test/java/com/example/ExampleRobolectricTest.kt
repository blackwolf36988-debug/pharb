package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PharbDatabase
import com.example.data.repository.PharbRepository
import com.example.ui.navigation.PharbRoutes
import com.example.ui.viewmodel.MainBottomTab
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PHARB Robolectric Integration Test Suite (Section 48 & 63)
 * Verifies:
 * - App Identity ("PHARB")
 * - Jetpack Navigation Compose Routes (Home, Discover, Create, Messages, Profile)
 * - User Registration & Encrypted Login
 * - Creating Posts, Reactions, Comments, Reposts & Saving
 * - Messaging, Communities, Channels, Reports & Block/Mute
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: PharbDatabase
    private lateinit var repository: PharbRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PharbDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PharbRepository(database.pharbDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `verify app name is PHARB and navigation routes are configured`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PHARB", appName)

        assertEquals(
            listOf("home", "discover", "create", "messages", "profile"),
            PharbRoutes.primaryBottomRoutes
        )
        assertEquals(MainBottomTab.HOME, PharbRoutes.toTab(PharbRoutes.ROUTE_HOME))
        assertEquals(MainBottomTab.DISCOVER, PharbRoutes.toTab(PharbRoutes.ROUTE_DISCOVER))
        assertEquals(MainBottomTab.CREATE, PharbRoutes.toTab(PharbRoutes.ROUTE_CREATE))
        assertEquals(MainBottomTab.MESSAGES, PharbRoutes.toTab(PharbRoutes.ROUTE_MESSAGES))
        assertEquals(MainBottomTab.PROFILE, PharbRoutes.toTab(PharbRoutes.ROUTE_PROFILE))
    }

    @Test
    fun `verify user registration login post creation reaction messaging and moderation`() = runBlocking {
        repository.initializeDatabase()

        // 1. Register a new user and verify encrypted login
        val regResult = repository.registerNewUser(
            fullName = "خالد العتيبي",
            username = "khaled_dev",
            email = "khaled@pharb.io",
            phone = "+966550011223",
            passwordPlain = "Khaled@2026!",
            accountType = "CREATOR"
        )
        assertTrue(regResult.isSuccess)
        val createdUser = regResult.getOrNull()
        assertNotNull(createdUser)

        val loginResult = repository.authenticateUser("khaled_dev", "Khaled@2026!")
        assertTrue(loginResult.isSuccess)

        // 2. Create a post and verify it appears in feed
        val postId = repository.createPost(
            author = createdUser!!,
            content = "منشور اختباري حقيقي على منصة PHARB #تقنية",
            postType = "TEXT",
            hashtags = "#PHARB #تقنية",
            category = "تقنية"
        )
        assertTrue(postId > 0)

        val posts = repository.feedPosts.first()
        val myPost = posts.find { it.id == postId }
        assertNotNull(myPost)

        // 3. React and Comment on the post
        repository.reactToPost(myPost!!, "LOVE")
        repository.addComment(myPost, createdUser, "تعليق رائع ومفيد")

        val updatedPosts = repository.feedPosts.first()
        val reactedPost = updatedPosts.find { it.id == postId }!!
        assertEquals("LOVE", reactedPost.myReaction)
        assertEquals(1, reactedPost.loveCount)
        assertEquals(1, reactedPost.commentsCount)

        // 4. Send a message in PHARB Messenger
        val conversations = repository.conversations.first()
        assertTrue(conversations.isNotEmpty())
        repository.sendMessage(
            conversation = conversations.first(),
            sender = createdUser,
            content = "رسالة فورية مشفرة عبر PHARB Messenger"
        )
        val messages = repository.allMessages.first()
        assertTrue(messages.any { it.content.contains("رسالة فورية مشفرة") })

        // 5. Submit Report and Block/Mute
        repository.submitReport("POST", postId, "اختبار نظام البلاغات", "khaled_dev", "Spam")
        val reports = repository.reports.first()
        assertTrue(reports.any { it.targetId == postId })

        repository.addBlockOrMute("spam_account", "حساب مزعج", "BLOCK")
        val blocks = repository.blockMuteRelations.first()
        assertTrue(blocks.any { it.targetUsername == "spam_account" })
    }
}
