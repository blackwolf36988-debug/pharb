package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ChannelEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.data.local.ShortVideoEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.PharbAvatar
import com.example.ui.theme.PharbDeepNavy
import com.example.ui.theme.PharbLiveRed
import com.example.ui.theme.PharbSoftBlue
import com.example.ui.theme.PharbVerifiedBadge
import com.example.ui.viewmodel.SubScreenDestination

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PharbDiscoverScreen(
    searchQuery: String,
    searchFilter: String,
    searchHistory: List<String>,
    userInterests: List<String>,
    users: List<UserEntity>,
    posts: List<PostEntity>,
    shortVideos: List<ShortVideoEntity>,
    communities: List<CommunityEntity>,
    channels: List<ChannelEntity>,
    onSearchQueryChange: (String) -> Unit,
    onSearchFilterChange: (String) -> Unit,
    onToggleInterest: (String) -> Unit,
    onToggleFollowUser: (UserEntity) -> Unit,
    onToggleJoinCommunity: (CommunityEntity) -> Unit,
    onToggleSubscribeChannel: (ChannelEntity) -> Unit,
    onOpenSubScreen: (SubScreenDestination) -> Unit
) {
    val q = searchQuery.trim().lowercase()
    val matchedUsers = remember(users, q) {
        if (q.isEmpty()) users else users.filter {
            it.fullName.lowercase().contains(q) || it.username.lowercase().contains(q) || it.bio.lowercase().contains(q)
        }
    }
    val matchedPosts = remember(posts, q) {
        if (q.isEmpty()) posts else posts.filter {
            it.content.lowercase().contains(q) || it.hashtags.lowercase().contains(q) || it.category.lowercase().contains(q)
        }
    }
    val matchedCommunities = remember(communities, q) {
        if (q.isEmpty()) communities else communities.filter {
            it.name.lowercase().contains(q) || it.category.lowercase().contains(q) || it.description.lowercase().contains(q)
        }
    }
    val matchedChannels = remember(channels, q) {
        if (q.isEmpty()) channels else channels.filter {
            it.name.lowercase().contains(q) || it.category.lowercase().contains(q) || it.description.lowercase().contains(q)
        }
    }

    val trendingHashtags = listOf(
        "#PHARB" to "142K منشور",
        "#الذكاء_الاصطناعي" to "89K منشور",
        "#PHARB_Education" to "64K درس",
        "#تصميم_RTL" to "41K منشور",
        "#ريادة_الأعمال" to "53K نقاش",
        "#PHARB_Shorts" to "210K فيديو"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("discover_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Engine Input (Section 17)
        item {
            Text(
                text = "اكتشف • PHARB Explore & Search Engine",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("ابحث عن مستخدمين، منشورات، فيديوهات، #وسوم، مجتمعات، أو قنوات...") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("discover_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Category Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "الكل",
                    "USERS" to "المستخدمون",
                    "POSTS" to "المنشورات",
                    "VIDEOS" to "الفيديوهات القصيرة",
                    "HASHTAGS" to "الوسوم الرائجة",
                    "COMMUNITIES" to "المجتمعات",
                    "CHANNELS" to "القنوات"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = searchFilter == key,
                        onClick = { onSearchFilterChange(key) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Search History & Autocomplete Chips
        if (searchHistory.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("عمليات البحث الأخيرة والإكمال التلقائي:", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    searchHistory.forEach { term ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 3.dp)
                                .clickable { onSearchQueryChange(term) }
                        ) {
                            Text(
                                text = term,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hub Shortcuts (Shorts, Communities, Channels, Live)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSubScreen(SubScreenDestination.SHORTS_PLAYER) }
                        .testTag("discover_shorts_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.VideoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("PHARB Shorts", style = MaterialTheme.typography.titleMedium)
                        Text("${shortVideos.size} فيديو عمودي متكيف", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSubScreen(SubScreenDestination.LIVE_BROADCAST_HUB) }
                        .testTag("discover_live_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.LiveTv, contentDescription = null, tint = PharbLiveRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("PHARB Live", style = MaterialTheme.typography.titleMedium)
                        Text("البث المباشر والتفاعلي", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSubScreen(SubScreenDestination.COMMUNITIES_HUB) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("المجتمعات", style = MaterialTheme.typography.titleMedium)
                        Text("${communities.size} مجتمع متخصص", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenSubScreen(SubScreenDestination.CHANNELS_HUB) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.Podcasts, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("القنوات", style = MaterialTheme.typography.titleMedium)
                        Text("تعليمية • تجارية • إخبارية", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Transparent Recommendation Engine Controls (Section 38)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "محرك التوصيات الشفاف (تحكم في اهتماماتك)",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "خوارزمية PHARB ليست صندوقًا أسود؛ اختر الموضوعات التي تفضل رؤيتها في تبويب For You:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("تقنية", "تعليم", "علوم", "تصميم", "أعمال", "رياضة", "ثقافة", "أخبار").forEach { topic ->
                            FilterChip(
                                selected = topic in userInterests,
                                onClick = { onToggleInterest(topic) },
                                label = { Text(topic) }
                            )
                        }
                    }
                }
            }
        }

        // Trending Hashtags & Topics
        if (searchFilter == "ALL" || searchFilter == "HASHTAGS") {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الوسوم والموضوعات الرائجة الآن", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        trendingHashtags.forEach { (tag, volume) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchQueryChange(tag) }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tag, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                Text(volume, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Creators & Users Results
        if (searchFilter == "ALL" || searchFilter == "USERS") {
            item {
                Text("صناع المحتوى والحسابات (${matchedUsers.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(matchedUsers, key = { "usr_${it.id}" }) { user ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        PharbAvatar(name = user.fullName, avatarColorHex = user.avatarColorHex, size = 46.dp, isOnline = user.isOnline)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.width(6.dp))
                                PharbVerifiedBadge(isVerified = user.isVerified, accountType = user.accountType)
                            }
                            Text(
                                text = "@${user.username} • ${user.followersCount} متابع",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = user.bio,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                        val buttonLabel = when {
                            user.isFollowedByMe -> "متابَع ✓"
                            user.hasPendingFollowRequest -> "قيد الانتظار 🔒"
                            user.isPrivateAccount -> "طلب متابعة 🔒"
                            else -> "متابعة"
                        }
                        OutlinedButton(onClick = { onToggleFollowUser(user) }) {
                            Text(buttonLabel)
                        }
                    }
                }
            }
        }

        // Communities Results
        if (searchFilter == "ALL" || searchFilter == "COMMUNITIES") {
            item {
                Text("المجتمعات النشطة (${matchedCommunities.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(matchedCommunities.take(3), key = { "comm_${it.id}" }) { comm ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${comm.name} • ${comm.category}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${comm.membersCount} عضو • ${comm.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onToggleJoinCommunity(comm) }) {
                            Text(if (comm.isJoined) "منضم ✓" else "انضمام")
                        }
                    }
                }
            }
        }

        // Channels Results
        if (searchFilter == "ALL" || searchFilter == "CHANNELS") {
            item {
                Text("القنوات التعليمية والتجارية (${matchedChannels.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(matchedChannels, key = { "chan_${it.id}" }) { channel ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(channel.name, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.width(6.dp))
                                PharbVerifiedBadge(isVerified = channel.isVerified, accountType = channel.ownerType)
                            }
                            Text(
                                text = "${channel.handle} • ${channel.subscribersCount} مشترك",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = channel.latestBroadcast,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { onToggleSubscribeChannel(channel) }) {
                            Text(if (channel.isSubscribed) "مشترك 🔔" else "اشتراك")
                        }
                    }
                }
            }
        }
    }
}

/**
 * PHARB SHORTS Vertical Video Player (Section 10)
 * Supports vertical swipe, Adaptive Bitrate indicator, play/pause, like, comment, share, save, follow creator.
 */
@Composable
fun PharbShortsPlayerScreen(
    videos: List<ShortVideoEntity>,
    onToggleLike: (ShortVideoEntity) -> Unit,
    onToggleSave: (ShortVideoEntity) -> Unit,
    onToggleFollowCreator: (ShortVideoEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val pagerState = rememberPagerState(pageCount = { videos.size.coerceAtLeast(1) })
    var isPlaying by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PharbDeepNavy)
            .testTag("shorts_player_screen")
    ) {
        if (videos.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val video = videos[page]
                val resId = when (video.drawableName) {
                    "img_story_featured" -> R.drawable.img_story_featured
                    "img_onboarding_hero" -> R.drawable.img_onboarding_hero
                    else -> R.drawable.img_shorts_preview_1
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { isPlaying = !isPlaying }
                ) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = video.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay for high legibility
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        PharbDeepNavy.copy(alpha = 0.65f),
                                        Color.Transparent,
                                        PharbDeepNavy.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    // Pause indicator in center
                    if (!isPlaying) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "تشغيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    // Right / End Vertical Action Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 36.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { onToggleLike(video) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = if (video.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "إعجاب بالفيديو",
                                    tint = if (video.isLiked) Color(0xFFE11D48) else Color.White
                                )
                            }
                            Text("${video.likesCount}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Comment,
                                    contentDescription = "التعليقات",
                                    tint = Color.White
                                )
                            }
                            Text("${video.commentsCount}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { onToggleSave(video) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = if (video.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "حفظ الفيديو",
                                    tint = if (video.isSaved) PharbSoftBlue else Color.White
                                )
                            }
                            Text("${video.savesCount}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "مشاركة",
                                    tint = Color.White
                                )
                            }
                            Text("${video.sharesCount}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Bottom Creator & Video Info Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.80f)
                            .padding(start = 16.dp, bottom = 32.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = PharbSoftBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${video.resolutionLabel} • ${video.bitrateKbps} kbps",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PharbSoftBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PharbAvatar(
                                name = video.creatorName,
                                avatarColorHex = video.creatorAvatarColor,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = video.creatorName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    PharbVerifiedBadge(isVerified = video.creatorVerified)
                                }
                                Text(
                                    text = "@${video.creatorUsername}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { onToggleFollowCreator(video) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(if (video.isFollowingCreator) "متابَع ✓" else "متابعة +")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = video.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = video.hashtags,
                            style = MaterialTheme.typography.labelLarge,
                            color = PharbSoftBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = video.audioTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Top Bar overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
            }
            Text(
                text = "PHARB SHORTS • تمرير عمودي",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "إيقاف / تشغيل",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * PHARB STORIES 24h Viewer & Story Archive (Section 11)
 */
@Composable
fun PharbStoryViewerScreen(
    story: StoryEntity,
    onArchiveStory: (StoryEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedOption by remember { mutableStateOf<String?>(null) }
    val resId = when (story.drawableName) {
        "img_onboarding_hero" -> R.drawable.img_onboarding_hero
        "img_cover_default" -> R.drawable.img_cover_default
        "img_shorts_preview_1" -> R.drawable.img_shorts_preview_1
        else -> R.drawable.img_story_featured
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(story.backgroundHex))
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = story.caption,
            contentScale = ContentScale.Crop,
            alpha = 0.55f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إغلاق القصة", tint = Color.White)
                    }
                    PharbAvatar(name = story.authorName, avatarColorHex = story.authorAvatarColor, size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(story.authorName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(
                            text = "متاحة لمدة 24 ساعة • ${story.viewsCount} مشاهدة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onArchiveStory(story) }
                ) {
                    Icon(Icons.Filled.Archive, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ بالأرشيف", color = Color.White)
                }
            }

            // Story Content & Interactive Stickers
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PharbDeepNavy.copy(alpha = 0.88f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (story.musicTrack.isNotBlank()) {
                        Surface(
                            color = PharbSoftBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = PharbSoftBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(story.musicTrack, style = MaterialTheme.typography.labelSmall, color = PharbSoftBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = story.caption,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    if (story.interactiveQuestion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = story.interactiveQuestion,
                            style = MaterialTheme.typography.titleSmall,
                            color = PharbSoftBlue
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (story.pollOptionA.isNotBlank()) {
                            Button(
                                onClick = { selectedOption = story.pollOptionA },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(story.pollOptionA)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { selectedOption = story.pollOptionB },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(story.pollOptionB, color = Color.White)
                            }
                        }
                        selectedOption?.let { voted ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("تم تسجيل تفاعلك: $voted ✅", color = PharbSoftBlue, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Footer views counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${story.viewsCount} مشاهد لهذه القصة", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun PharbStoryArchiveScreen(
    archivedStories: List<StoryEntity>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("أرشيف القصص الخاص (Story Archive)", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "القصص التي انتهت مدتها (24 ساعة) تبقى محفوظة لك وحدك هنا",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(archivedStories, key = { it.id }) { story ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(story.authorName, style = MaterialTheme.typography.titleSmall)
                            Text("${story.viewsCount} مشاهدة سابقة", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(story.caption, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
