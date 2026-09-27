package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.PharbAiEngine
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.PharbAvatar
import com.example.ui.theme.PharbGeometricLogo
import com.example.ui.theme.PharbLiveRed
import com.example.ui.theme.PharbLogoVariant
import com.example.ui.theme.PharbVerifiedBadge
import com.example.ui.viewmodel.HomeFeedTab
import com.example.ui.viewmodel.SubScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharbHomeFeedScreen(
    currentUser: UserEntity,
    posts: List<PostEntity>,
    stories: List<StoryEntity>,
    comments: List<CommentEntity>,
    unreadNotificationsCount: Int,
    unreadMessagesCount: Int = 0,
    currentFeedTab: HomeFeedTab,
    userInterests: List<String>,
    isArabic: Boolean,
    isOfflineMode: Boolean,
    selectedPostForComments: PostEntity?,
    onSelectFeedTab: (HomeFeedTab) -> Unit,
    onOpenSubScreen: (SubScreenDestination) -> Unit,
    onOpenMessages: () -> Unit = {},
    onOpenStory: (StoryEntity) -> Unit,
    onCreateStoryQuick: () -> Unit,
    onReactToPost: (PostEntity, String) -> Unit,
    onVotePoll: (PostEntity, Int) -> Unit,
    onRepost: (PostEntity, String) -> Unit,
    onToggleSave: (PostEntity) -> Unit,
    onOpenComments: (PostEntity?) -> Unit,
    onSubmitComment: (PostEntity, String) -> Unit,
    onReportPost: (PostEntity, String) -> Unit,
    onBlockOrMuteAuthor: (String, String, String) -> Unit,
    onToggleOffline: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    var quoteDialogPost by remember { mutableStateOf<PostEntity?>(null) }
    var quoteInput by remember { mutableStateOf("") }
    var reportDialogPost by remember { mutableStateOf<PostEntity?>(null) }
    var showDownloadApkDialog by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, currentFeedTab, userInterests) {
        when (currentFeedTab) {
            HomeFeedTab.FOR_YOU -> posts.sortedByDescending {
                val matchesInterest = userInterests.any { topic -> it.category.contains(topic) }
                (if (matchesInterest) 1000 else 0) + it.totalReactions
            }
            HomeFeedTab.FOLLOWING -> posts.filter { it.authorVerified || it.authorId == currentUser.id }
            HomeFeedTab.LATEST -> posts.sortedByDescending { it.createdAt }
            HomeFeedTab.COMMUNITIES -> posts.filter { it.category in listOf("تقنية", "علوم", "تصميم") }
            HomeFeedTab.EDUCATION -> posts.filter { it.authorAccountType == "EDUCATION" || it.category == "تعليم" || it.postType == "FILE" }
            HomeFeedTab.TRENDING -> posts.sortedByDescending { it.totalReactions + it.repostCount * 2 }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_feed_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Clean Top Header (Section 58: PHARB Logo, Search, Notifications)
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PharbGeometricLogo(
                            variant = PharbLogoVariant.MAIN_LOGO,
                            markSize = 36.dp,
                            showTagline = true,
                            isArabic = isArabic,
                            modifier = Modifier.clickable {
                                onOpenSubScreen(SubScreenDestination.BRAND_AND_DESIGN_SYSTEM)
                            }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleLanguage,
                                modifier = Modifier.testTag("header_language_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = "تبديل اللغة العربية / الإنجليزية"
                                )
                            }
                            IconButton(
                                onClick = { onOpenSubScreen(SubScreenDestination.SEARCH_ENGINE) },
                                modifier = Modifier.testTag("header_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "البحث المتقدم"
                                )
                            }
                            IconButton(
                                onClick = { onOpenSubScreen(SubScreenDestination.NOTIFICATIONS_CENTER) },
                                modifier = Modifier.testTag("header_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationsCount > 0) {
                                            Badge { Text(unreadNotificationsCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "الإشعارات"
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenMessages,
                                modifier = Modifier.testTag("header_messages_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadMessagesCount > 0) {
                                            Badge { Text(unreadMessagesCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "الرسائل"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Modular Feature Bar (Shorts, Live, Communities, Channels, Admin, Onboarding, Offline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.SHORTS_PLAYER) },
                            leadingIcon = {
                                Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("PHARB Shorts") },
                            modifier = Modifier.testTag("quick_shorts_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.LIVE_BROADCAST_HUB) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.LiveTv,
                                    contentDescription = null,
                                    tint = PharbLiveRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("البث المباشر Live") },
                            modifier = Modifier.testTag("quick_live_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.COMMUNITIES_HUB) },
                            label = { Text("المجتمعات") },
                            modifier = Modifier.testTag("quick_communities_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.CHANNELS_HUB) },
                            label = { Text("القنوات التعليمية والتجارية") },
                            modifier = Modifier.testTag("quick_channels_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.ADMIN_DASHBOARD) },
                            leadingIcon = {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text("لوحة الإدارة Admin") },
                            modifier = Modifier.testTag("quick_admin_chip")
                        )
                        FilterChip(
                            selected = isOfflineMode,
                            onClick = onToggleOffline,
                            leadingIcon = {
                                Icon(Icons.Filled.CloudOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(if (isOfflineMode) "وضع Offline نشط" else "محاكاة Offline") },
                            modifier = Modifier.testTag("quick_offline_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.ONBOARDING_TOUR) },
                            label = { Text("الجولة التعريفية") },
                            modifier = Modifier.testTag("quick_onboarding_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.SPLASH_SCREEN) },
                            label = { Text("01 شاشة البداية Splash") },
                            modifier = Modifier.testTag("quick_splash_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { onOpenSubScreen(SubScreenDestination.BRAND_AND_DESIGN_SYSTEM) },
                            label = { Text("دليل الـ 24 شاشة • About PHARB") },
                            modifier = Modifier.testTag("quick_about_chip")
                        )
                        FilterChip(
                            selected = false,
                            onClick = { showDownloadApkDialog = true },
                            label = { Text("📲 تحميل APK / التصدير") },
                            modifier = Modifier.testTag("quick_download_apk_chip")
                        )
                    }

                    val pendingOfflineCount = posts.count { !it.isSynced }
                    AnimatedVisibility(visible = isOfflineMode || pendingOfflineCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("offline_persistence_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isArabic) {
                                                "وضع عدم الاتصال نشط (Room Local Database)"
                                            } else {
                                                "Offline Mode Active (Room Local Database)"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = if (isArabic) {
                                                "محفوظ محليًا: ${posts.size} منشور • بانتظار المزامنة: $pendingOfflineCount"
                                            } else {
                                                "Cached locally: ${posts.size} posts • Pending sync: $pendingOfflineCount"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                TextButton(
                                    onClick = onToggleOffline,
                                    modifier = Modifier.testTag("sync_offline_posts_button")
                                ) {
                                    Text(
                                        text = if (isArabic) "مزامنة الآن" else "Sync Now",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. PHARB Stories Row (24h Stories + Archive)
        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "القصص اليومية • PHARB Stories (24h)" else "PHARB Stories • 24h",
                            style = MaterialTheme.typography.titleSmall
                        )
                        TextButton(
                            onClick = { onOpenSubScreen(SubScreenDestination.STORY_ARCHIVE) },
                            modifier = Modifier.testTag("open_story_archive_button")
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "الأرشيف" else "Archive", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clickable { onCreateStoryQuick() }
                                    .testTag("add_story_button")
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    PharbAvatar(
                                        name = currentUser.fullName,
                                        avatarColorHex = currentUser.avatarColorHex,
                                        size = 62.dp,
                                        hasStoryRing = false
                                    )
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "إضافة قصة",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isArabic) "أضف قصة" else "Add Story",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        items(stories, key = { it.id }) { story ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(74.dp)
                                    .clickable { onOpenStory(story) }
                            ) {
                                PharbAvatar(
                                    name = story.authorName,
                                    avatarColorHex = story.authorAvatarColor,
                                    size = 62.dp,
                                    hasStoryRing = !story.isViewed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = story.authorName,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Smart Feed Tabs (For You, Following, Latest, Communities, Education, Trending)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HomeFeedTab.entries.forEach { tab ->
                    FilterChip(
                        selected = currentFeedTab == tab,
                        onClick = { onSelectFeedTab(tab) },
                        label = {
                            Text(if (isArabic) tab.labelAr else tab.labelEn)
                        },
                        modifier = Modifier.testTag("feed_tab_${tab.name.lowercase()}")
                    )
                }
            }
            HorizontalDivider()
        }

        // 4. Feed Posts List
        items(filteredPosts, key = { it.id }) { post ->
            PharbPostCard(
                post = post,
                userInterests = userInterests,
                isArabic = isArabic,
                onReact = { reaction -> onReactToPost(post, reaction) },
                onVotePoll = { idx -> onVotePoll(post, idx) },
                onOpenComments = { onOpenComments(post) },
                onRepostClick = { quoteDialogPost = post },
                onToggleSave = { onToggleSave(post) },
                onReportClick = { reportDialogPost = post },
                onBlockMute = { relType ->
                    onBlockOrMuteAuthor(post.authorUsername, post.authorName, relType)
                }
            )
        }
    }

    // Repost / Quote Post Dialog (Section 20)
    quoteDialogPost?.let { targetPost ->
        AlertDialog(
            onDismissRequest = { quoteDialogPost = null },
            title = { Text("إعادة النشر (Repost / Quote Post)") },
            text = {
                Column {
                    Text(
                        text = "سيتم الحفاظ على حقوق الناشر الأصلي (@${targetPost.authorUsername}) وظهور اسمه بوضوح.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = quoteInput,
                        onValueChange = { quoteInput = it },
                        label = { Text("أضف تعليقك لإعادة النشر باقتباس (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRepost(targetPost, quoteInput)
                        quoteInput = ""
                        quoteDialogPost = null
                    }
                ) {
                    Text(if (quoteInput.isBlank()) "إعادة نشر فورية" else "نشر مع اقتباس")
                }
            },
            dismissButton = {
                TextButton(onClick = { quoteDialogPost = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Report Post Dialog (Section 27)
    reportDialogPost?.let { targetPost ->
        val reasons = listOf(
            "Spam" to "محتوى مزعج أو مكرر (Spam)",
            "Harassment" to "مضايقة أو تنمر (Harassment)",
            "Hate" to "خطاب كراهية (Hate)",
            "Impersonation" to "انتحال شخصية (Impersonation)",
            "Scam" to "احتيال مالي (Scam)",
            "Copyright" to "انتهاك حقوق ملكية (Copyright)",
            "Illegal Content" to "محتوى غير قانوني (Illegal)",
            "Other" to "سبب آخر (Other)"
        )
        AlertDialog(
            onDismissRequest = { reportDialogPost = null },
            title = { Text("الإبلاغ عن محتوى لحماية مجتمع PHARB") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reasons.forEach { (code, label) ->
                        OutlinedButton(
                            onClick = {
                                onReportPost(targetPost, code)
                                reportDialogPost = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { reportDialogPost = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showDownloadApkDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadApkDialog = false },
            title = { Text("📲 تحميل وتصدير تطبيق PHARB (APK / ZIP)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "يمكنك تحميل نسخة التثبيت المباشرة لهاتفك الأندرويد (APK) أو حزمة المتجر (AAB) أو الكود المصدري الكامل (ZIP) مباشرة من منصة Google AI Studio عبر الخطوات التالية:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1️⃣ لتنزيل ملف APK مباشرة:\nاضغط على أيقونة الإعدادات / التصدير (Settings / Export) في الشريط العلوي لـ AI Studio ثم اختر «Generate APK» أو «Download APK».",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2️⃣ لتنزيل المشروع كاملاً (ZIP) أو رفعه إلى GitHub:\nاختر «Export as ZIP» أو «Push to GitHub» من نفس القائمة العلوية.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3️⃣ رابط المعاينة المباشرة (Shared Preview URL):\nhttps://ais-pre-wuyc3xgwwtrrpkwdwhuy2i-206898236224.europe-west2.run.app",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDownloadApkDialog = false }) {
                    Text("تم، شكرًا")
                }
            }
        )
    }

    // Comments Bottom Sheet
    if (selectedPostForComments != null) {
        val postComments = comments.filter { it.postId == selectedPostForComments.id }
        var newCommentText by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { onOpenComments(null) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "التعليقات (${postComments.size})",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))

                postComments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        PharbAvatar(
                            name = comment.authorName,
                            avatarColorHex = comment.authorAvatarColor,
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(comment.authorName, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.width(4.dp))
                                PharbVerifiedBadge(isVerified = comment.authorVerified)
                            }
                            Text(comment.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    HorizontalDivider()
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("اكتب تعليقًا بناءً...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onSubmitComment(selectedPostForComments, newCommentText)
                                newCommentText = ""
                            }
                        },
                        modifier = Modifier.testTag("send_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال التعليق",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PharbPostCard(
    post: PostEntity,
    userInterests: List<String>,
    isArabic: Boolean,
    onReact: (String) -> Unit,
    onVotePoll: (Int) -> Unit,
    onOpenComments: () -> Unit,
    onRepostClick: () -> Unit,
    onToggleSave: () -> Unit,
    onReportClick: () -> Unit,
    onBlockMute: (String) -> Unit
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var aiHelperOutput by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Repost Attribution Header (Section 20)
            if (post.isRepost) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "أعاد ${post.authorName} النشر من الناشر الأصلي @${post.originalAuthorUsername}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (post.quoteText.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "«${post.quoteText}»",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Author Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PharbAvatar(
                    name = post.authorName,
                    avatarColorHex = post.authorAvatarColor,
                    size = 44.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        PharbVerifiedBadge(
                            isVerified = post.authorVerified,
                            accountType = post.authorAccountType
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${post.authorUsername} • ${post.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (post.locationTag.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = post.locationTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (!post.isSynced) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "بانتظار المزامنة",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "خيارات المنشور")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("لماذا يظهر لي هذا المنشور؟ (شفافية التوصيات)") },
                            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                aiHelperOutput = PharbAiEngine.explainRecommendation(
                                    postCategory = post.category,
                                    authorVerified = post.authorVerified,
                                    isFollowingAuthor = true,
                                    userInterests = userInterests,
                                    isArabic = isArabic
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تلخيص ذكي بالذكاء الاصطناعي (PHARB AI)") },
                            leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                aiHelperOutput = PharbAiEngine.summarizePost(post.content, isArabic)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ترجمة فورية للمنشور") },
                            leadingIcon = { Icon(Icons.Filled.Translate, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                aiHelperOutput = PharbAiEngine.translatePost(post.content)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("كتم الحساب @${post.authorUsername}") },
                            onClick = {
                                showMoreMenu = false
                                onBlockMute("MUTE")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حظر الحساب @${post.authorUsername}") },
                            onClick = {
                                showMoreMenu = false
                                onBlockMute("BLOCK")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("إبلاغ عن المنشور (Report)") },
                            leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                onReportClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Post Text Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Hashtags & Mentions
            if (post.hashtags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.hashtags,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Specialized Media / Poll / Audio / File Attachments (Section 9)
            if (post.postType == "IMAGE" || post.postType == "MULTI_IMAGE" || post.postType == "VIDEO") {
                Spacer(modifier = Modifier.height(12.dp))
                val resId = when (post.mediaDrawableName) {
                    "img_onboarding_hero" -> R.drawable.img_onboarding_hero
                    "img_shorts_preview_1" -> R.drawable.img_shorts_preview_1
                    "img_story_featured" -> R.drawable.img_story_featured
                    else -> R.drawable.img_cover_default
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "وسائط المنشور",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (post.postType == "POLL" && post.pollOptionsSerialized.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                val parsedOptions = post.pollOptionsSerialized.split("|").mapNotNull { item ->
                    val idx = item.lastIndexOf(':')
                    if (idx == -1) null
                    else item.substring(0, idx) to (item.substring(idx + 1).toIntOrNull() ?: 0)
                }
                val totalVotes = parsedOptions.sumOf { it.second }.coerceAtLeast(1)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📊 استطلاع تفاعلي (${parsedOptions.sumOf { it.second }} صوت)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    parsedOptions.forEachIndexed { idx, (optionLabel, count) ->
                        val ratio = count.toFloat() / totalVotes.toFloat()
                        val percent = (ratio * 100).toInt()
                        val isVoted = post.votedPollOptionIndex == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isVoted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVotePoll(idx) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(optionLabel, style = MaterialTheme.typography.bodyMedium)
                                    Text("$percent% ($count)", style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            if (post.postType == "AUDIO") {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        IconButton(onClick = { isAudioPlaying = !isAudioPlaying }) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Filled.GraphicEq else Icons.Filled.PlayArrow,
                                contentDescription = "تشغيل المنشور الصوتي",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAudioPlaying) "جارٍ تشغيل التدوينة الصوتية... 🔊" else "تدوينة صوتية (${post.audioDurationSec} ثانية)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            LinearProgressIndicator(
                                progress = { if (isAudioPlaying) 0.65f else 0.25f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (post.postType == "FILE" && post.fileAttachmentName.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = post.fileAttachmentName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "مرفق تعليمي مفحوص وآمن • PHARB Cloud Storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Responsible AI Inline Helper Output (Summary, Translation, or Transparency Explanation)
            AnimatedVisibility(visible = aiHelperOutput != null) {
                aiHelperOutput?.let { text ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مساعد PHARB الذكي",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(onClick = { aiHelperOutput = null }) {
                                    Text("إخفاء")
                                }
                            }
                            Text(text = text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick AI Bar (Summarize + Translate + Suggest Titles via Gemini 3.5 Flash / Hybrid AI)
            val postAiScope = rememberCoroutineScope()
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        aiHelperOutput = PharbAiEngine.summarizePost(post.content, isArabic)
                        postAiScope.launch {
                            aiHelperOutput = PharbAiEngine.summarizePostAsync(post.content, isArabic)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("ai_summarize_post_${post.id}")
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تلخيص ذكي", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(
                    onClick = {
                        aiHelperOutput = PharbAiEngine.translatePost(post.content)
                        postAiScope.launch {
                            aiHelperOutput = PharbAiEngine.translatePostAsync(post.content)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("ai_translate_post_${post.id}")
                ) {
                    Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ترجمة المنشور", style = MaterialTheme.typography.labelSmall)
                }
            }

            // 6-Reaction Picker Popup (Section 19: Like, Love, Laugh, Wow, Sad, Angry)
            AnimatedVisibility(visible = showReactionPicker) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "LIKE" to "👍 إعجاب",
                            "LOVE" to "❤️ حب",
                            "LAUGH" to "😄 ضحك",
                            "WOW" to "😮 دهشة",
                            "SAD" to "😢 حزن",
                            "ANGRY" to "😡 غضب"
                        ).forEach { (code, label) ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onReact(code)
                                        showReactionPicker = false
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Action Footer (Reaction, Comment, Repost, Save, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val reactionEmoji = when (post.myReaction) {
                    "LOVE" -> "❤️"
                    "LAUGH" -> "😄"
                    "WOW" -> "😮"
                    "SAD" -> "😢"
                    "ANGRY" -> "😡"
                    "LIKE" -> "👍"
                    else -> null
                }

                TextButton(
                    onClick = { showReactionPicker = !showReactionPicker },
                    modifier = Modifier.testTag("react_button_${post.id}")
                ) {
                    if (reactionEmoji != null) {
                        Text(reactionEmoji, fontSize = 15.sp)
                    } else {
                        Icon(Icons.Filled.ThumbUp, contentDescription = "تفاعل", modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${post.totalReactions}")
                }

                TextButton(
                    onClick = onOpenComments,
                    modifier = Modifier.testTag("comment_button_${post.id}")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "تعليقات", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${post.commentsCount}")
                }

                TextButton(
                    onClick = onRepostClick,
                    modifier = Modifier.testTag("repost_button_${post.id}")
                ) {
                    Icon(Icons.Filled.Repeat, contentDescription = "إعادة نشر", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${post.repostCount}")
                }

                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.testTag("save_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "حفظ المنشور",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
