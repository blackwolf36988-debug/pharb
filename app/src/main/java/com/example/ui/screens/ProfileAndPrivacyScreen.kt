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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.BlockMuteEntity
import com.example.data.local.ChannelEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.PostEntity
import com.example.data.local.SessionEntity
import com.example.data.local.ShortVideoEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.PharbAccentPreset
import com.example.ui.theme.PharbAvatar
import com.example.ui.theme.PharbLiveRed
import com.example.ui.theme.PharbThemeMode
import com.example.ui.theme.PharbVerifiedBadge
import com.example.ui.viewmodel.SubScreenDestination

@Composable
fun PharbProfileScreen(
    user: UserEntity,
    myPosts: List<PostEntity>,
    savedPosts: List<PostEntity>,
    shortVideos: List<ShortVideoEntity>,
    communities: List<CommunityEntity>,
    channels: List<ChannelEntity>,
    onUpdateProfile: (String, String, String, String, String, Boolean) -> Unit,
    onRequestVerification: (String, String) -> Unit,
    onOpenSubScreen: (SubScreenDestination) -> Unit,
    onLogout: () -> Unit
) {
    var profileTab by remember { mutableIntStateOf(0) } // 0: Posts, 1: Saved, 2: Reposts, 3: Shorts, 4: Communities & Channels, 5: Media
    var showEditDialog by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    var isFollowingSelfPreview by remember { mutableStateOf(true) }
    var shareBannerShown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Cover Photo & Avatar Header (Section 7)
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_cover_default),
                    contentDescription = "صورة الغلاف",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .clickable { onOpenSubScreen(SubScreenDestination.PRIVACY_AND_SECURITY) }
                            .testTag("open_privacy_center_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.PrivacyTip, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الخصوصية والأمان", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .clickable { onOpenSubScreen(SubScreenDestination.ADMIN_DASHBOARD) }
                            .testTag("profile_admin_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PharbAvatar(
                        name = user.fullName,
                        avatarColorHex = user.avatarColorHex,
                        size = 76.dp,
                        isOnline = user.isOnline
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تعديل الملف")
                        }

                        if (!user.isVerified) {
                            Button(onClick = { showVerifyDialog = true }) {
                                Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("توثيق")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.fullName, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    PharbVerifiedBadge(isVerified = user.isVerified, accountType = user.accountType)
                }

                Text(
                    text = "@${user.username} • ${if (user.isPrivateAccount) "حساب خاص 🔒" else "حساب عام 🌐"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(user.bio, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(user.location, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (user.website.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(user.website, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Counters (Followers, Following, Posts, Saved)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(count = "${user.followersCount}", label = "متابعون")
                    ProfileStatItem(count = "${user.followingCount}", label = "يتابع")
                    ProfileStatItem(count = "${myPosts.size}", label = "منشورات")
                    ProfileStatItem(count = "${savedPosts.size}", label = "محفوظات")
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Follow, Message, Share Action Buttons Row (Section 8)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isFollowingSelfPreview = !isFollowingSelfPreview },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_follow_button")
                    ) {
                        Text(if (isFollowingSelfPreview) "Following • متابَع" else "Follow • متابعة")
                    }
                    OutlinedButton(
                        onClick = { onOpenSubScreen(SubScreenDestination.NONE) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_message_button")
                    ) {
                        Text("Message • رسالة")
                    }
                    OutlinedButton(
                        onClick = { shareBannerShown = !shareBannerShown },
                        modifier = Modifier.testTag("profile_share_button")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Profile", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
                if (shareBannerShown) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔗 رابط الملف الشخصي جاهز للمشاركة: https://pharb.network/@${user.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Actions Row (Brand System, Auth Switch, Logout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { onOpenSubScreen(SubScreenDestination.BRAND_AND_DESIGN_SYSTEM) },
                        leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("هوية وشعارات PHARB") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onOpenSubScreen(SubScreenDestination.AUTH_PORTAL) },
                        label = { Text("تبديل الحساب / OTP") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = onLogout,
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("تسجيل الخروج") }
                    )
                }
            }
        }

        // Profile Content Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "Posts • منشوراتي (${myPosts.size})",
                    3 to "Videos • Shorts (${shortVideos.size})",
                    5 to "Media • الوسائط",
                    1 to "Saved • المحفوظات (${savedPosts.size})",
                    2 to "Reposts • إعادات النشر",
                    4 to "قنواتي ومجتمعاتي"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = profileTab == idx,
                        onClick = { profileTab = idx },
                        label = { Text(label) }
                    )
                }
            }
            HorizontalDivider()
        }

        when (profileTab) {
            0 -> {
                items(myPosts, key = { "my_${it.id}" }) { post ->
                    SimpleProfilePostItem(post = post)
                }
            }
            1 -> {
                items(savedPosts, key = { "saved_${it.id}" }) { post ->
                    SimpleProfilePostItem(post = post, showCollectionTag = true)
                }
            }
            2 -> {
                val reposts = myPosts.filter { it.isRepost }
                items(reposts, key = { "rep_${it.id}" }) { post ->
                    SimpleProfilePostItem(post = post)
                }
            }
            3 -> {
                items(shortVideos, key = { "short_${it.id}" }) { video ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(video.caption, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${video.viewsCount} مشاهدة • ${video.likesCount} إعجاب • ${video.resolutionLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            4 -> {
                items(communities.filter { it.isJoined }, key = { "pcomm_${it.id}" }) { comm ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("مجتمع: ${comm.name}", style = MaterialTheme.typography.titleSmall)
                            Text(comm.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                items(channels.filter { it.isSubscribed }, key = { "pchan_${it.id}" }) { chan ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("قناة: ${chan.name} (${chan.handle})", style = MaterialTheme.typography.titleSmall)
                            Text(chan.latestBroadcast, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            5 -> {
                val mediaPosts = myPosts.filter { it.postType in listOf("IMAGE", "MULTI_IMAGE", "VIDEO", "GIF") }.ifEmpty { myPosts }
                items(mediaPosts, key = { "media_${it.id}" }) { post ->
                    SimpleProfilePostItem(post = post)
                }
            }
        }
    }

    if (showEditDialog) {
        var name by remember { mutableStateOf(user.fullName) }
        var bio by remember { mutableStateOf(user.bio) }
        var loc by remember { mutableStateOf(user.location) }
        var web by remember { mutableStateOf(user.website) }
        var accType by remember { mutableStateOf(user.accountType) }
        var isPriv by remember { mutableStateOf(user.isPrivateAccount) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل الملف الشخصي ونوع الحساب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
                    OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("النبذة التعريفية Bio") })
                    OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("الموقع") })
                    OutlinedTextField(value = web, onValueChange = { web = it }, label = { Text("الرابط الشخصي") })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("CREATOR" to "صانع محتوى", "EDUCATION" to "تعليمي", "BUSINESS" to "أعمال").forEach { (k, l) ->
                            FilterChip(selected = accType == k, onClick = { accType = k }, label = { Text(l) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isPriv, onCheckedChange = { isPriv = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حساب خاص (Private Account)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(name, bio, loc, web, accType, isPriv)
                        showEditDialog = false
                    }
                ) { Text("حفظ التغييرات") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showVerifyDialog) {
        var docRef by remember { mutableStateOf("هوية رسمية / سجل مؤسسة رقم #") }
        var justification by remember { mutableStateOf("حساب رسمي فاعل يقدم محتوى أصيلًا لمجتمع PHARB") }

        AlertDialog(
            onDismissRequest = { showVerifyDialog = false },
            title = { Text("طلب توثيق الحساب (PHARB VERIFIED)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "لا تُمنح علامة التحقق لمجرد عدد المتابعين، بل عبر التحقق من الهوية والأصالة.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(value = docRef, onValueChange = { docRef = it }, label = { Text("مرجع الوثيقة الرسمية") })
                    OutlinedTextField(value = justification, onValueChange = { justification = it }, label = { Text("سبب طلب التحقق") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestVerification(docRef, justification)
                        showVerifyDialog = false
                    }
                ) { Text("إرسال الطلب للإدارة") }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SimpleProfilePostItem(post: PostEntity, showCollectionTag: Boolean = false) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (showCollectionTag && post.savedCollectionName.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مجموعة المحفوظات: ${post.savedCollectionName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(post.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "التفاعلات: ${post.totalReactions} • التعليقات: ${post.commentsCount} • إعادة النشر: ${post.repostCount}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * PHARB PRIVACY & SECURITY CENTER (Sections 3, 25, 26, 27, 42, 43)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PharbPrivacyAndSecurityScreen(
    user: UserEntity,
    sessions: List<SessionEntity>,
    blockMuteList: List<BlockMuteEntity>,
    themeMode: PharbThemeMode,
    accentPreset: PharbAccentPreset,
    isArabic: Boolean,
    fontScale: Float,
    onSelectThemeMode: (PharbThemeMode) -> Unit,
    onSelectAccentPreset: (PharbAccentPreset) -> Unit,
    onToggleLanguage: () -> Unit,
    onChangeFontScale: (Float) -> Unit,
    onSavePrivacySettings: (Boolean, Boolean, Boolean, String, Boolean, Boolean) -> Unit,
    onChangePassword: (String) -> Unit,
    onRevokeOtherSessions: () -> Unit,
    onRemoveBlockMute: (Long) -> Unit,
    onExportMyData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var isPrivate by remember(user) { mutableStateOf(user.isPrivateAccount) }
    var showOnline by remember(user) { mutableStateOf(user.showOnlineStatus) }
    var showLastSeen by remember(user) { mutableStateOf(user.showLastSeen) }
    var allowMsgFrom by remember(user) { mutableStateOf(user.allowMessagesFrom) }
    var allowTagging by remember(user) { mutableStateOf(user.allowTagging) }
    var searchable by remember(user) { mutableStateOf(user.searchableInDirectory) }
    var newPassword by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("privacy_center_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Column {
                    Text("مركز الخصوصية والأمان والمظهر", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = "تحكم كامل في بياناتك، الجلسات النشطة، المظهر، وإمكانية الوصول",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. Visual Theme, AMOLED, Accent Colors & Accessibility Font Scaling
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("المظهر والألوان وإمكانية الوصول (Visual & Accessibility)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("وضع الإضاءة (Light / Dark Graphite / AMOLED):", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PharbThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onSelectThemeMode(mode) },
                                label = { Text(if (isArabic) mode.labelAr else mode.labelEn) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لون الهوية المخصص (Accent Color):", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PharbAccentPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = accentPreset == preset,
                                onClick = { onSelectAccentPreset(preset) },
                                label = { Text(if (isArabic) preset.labelAr else preset.labelEn) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("تكبير الخط (Accessibility Font Scale: ${(fontScale * 100).toInt()}%):", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = fontScale,
                        onValueChange = onChangeFontScale,
                        valueRange = 0.85f..1.35f
                    )

                    OutlinedButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isArabic) "التبديل إلى الواجهة الإنجليزية (English LTR)" else "Switch to Arabic (العربية RTL)")
                    }
                }
            }
        }

        // 2. Privacy Controls (Section 26)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إعدادات الخصوصية الدقيقة", style = MaterialTheme.typography.titleMedium)

                    PrivacyToggleRow(
                        label = "حساب خاص (يتطلب موافقتك على طلبات المتابعة)",
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                    PrivacyToggleRow(
                        label = "إظهار حالة الاتصال الآن (Online)",
                        checked = showOnline,
                        onCheckedChange = { showOnline = it }
                    )
                    PrivacyToggleRow(
                        label = "إظهار وقت آخر ظهور (Last Seen)",
                        checked = showLastSeen,
                        onCheckedChange = { showLastSeen = it }
                    )
                    PrivacyToggleRow(
                        label = "السماح للآخرين بالإشارة إليّ (@Mentions)",
                        checked = allowTagging,
                        onCheckedChange = { allowTagging = it }
                    )
                    PrivacyToggleRow(
                        label = "ظهور الحساب في نتائج محرك البحث",
                        checked = searchable,
                        onCheckedChange = { searchable = it }
                    )

                    Text("من يستطيع مراسلتي على PHARB Messenger:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("EVERYONE" to "الجميع", "FOLLOWERS" to "المتابعون فقط", "NONE" to "لا أحد").forEach { (k, l) ->
                            FilterChip(selected = allowMsgFrom == k, onClick = { allowMsgFrom = k }, label = { Text(l) })
                        }
                    }

                    Button(
                        onClick = {
                            onSavePrivacySettings(isPrivate, showOnline, showLastSeen, allowMsgFrom, allowTagging, searchable)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_privacy_button")
                    ) {
                        Text("حفظ إعدادات الخصوصية")
                    }
                }
            }
        }

        // 3. Password & Active Sessions (Section 6 & 25)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الأمان وإدارة الجلسات والأجهزة", style = MaterialTheme.typography.titleMedium)
                    }

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("تغيير كلمة المرور (تشفير PBKDF2-SHA256)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = {
                            if (newPassword.isNotBlank()) {
                                onChangePassword(newPassword)
                                newPassword = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تحديث كلمة المرور المشفرة")
                    }

                    HorizontalDivider()
                    Text("الأجهزة والجلسات النشطة (${sessions.size}):", style = MaterialTheme.typography.labelLarge)
                    sessions.forEach { sess ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(sess.deviceName, style = MaterialTheme.typography.titleSmall)
                                }
                                Text("العنوان: ${sess.ipAddress}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onRevokeOtherSessions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تسجيل الخروج من جميع الأجهزة الأخرى")
                    }
                }
            }
        }

        // 4. Blocked / Muted Accounts & Data Export + Delete Account
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("الحسابات المحظورة والمكتومة (${blockMuteList.size})", style = MaterialTheme.typography.titleMedium)
                    if (blockMuteList.isEmpty()) {
                        Text("لا توجد حسابات محظورة أو مكتومة حاليًا.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        blockMuteList.forEach { rel ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("@${rel.targetUsername} (${rel.relationType})", style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { onRemoveBlockMute(rel.id) }) {
                                    Text("إلغاء")
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    OutlinedButton(
                        onClick = onExportMyData,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تنزيل نسخة من بياناتي (GDPR / Data Portability)")
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PharbLiveRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_button")
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف الحساب نهائيًا (Delete Account)", color = Color.White)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف الحساب الآمن") },
            text = {
                Text("هل أنت متأكد من رغبتك في حذف حسابك وجميع الجلسات المرتبطة به بشكل آمن ومتوافق مع سياسات الخصوصية في PHARB؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharbLiveRed)
                ) {
                    Text("تأكيد الحذف النهائي", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("تراجع")
                }
            }
        )
    }
}

@Composable
private fun PrivacyToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
