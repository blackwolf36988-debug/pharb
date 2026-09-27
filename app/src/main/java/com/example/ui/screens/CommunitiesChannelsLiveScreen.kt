package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.ChannelEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.LiveStreamEntity
import com.example.data.local.NotificationEntity
import com.example.ui.theme.PharbAvatar
import com.example.ui.theme.PharbBusinessGold
import com.example.ui.theme.PharbEducationEmerald
import com.example.ui.theme.PharbLiveRed
import com.example.ui.theme.PharbVerifiedBadge

/**
 * PHARB COMMUNITIES HUB (Section 15)
 * Supports 10 community categories, rules, moderators, joining, and creating communities.
 */
@Composable
fun PharbCommunitiesHubScreen(
    communities: List<CommunityEntity>,
    onToggleJoin: (CommunityEntity) -> Unit,
    onCreateCommunity: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val categories = listOf(
        "الكل", "تعليم", "علوم", "رياضة", "تقنية", "ثقافة", "أعمال", "ألعاب", "هوايات", "أخبار", "مجتمعات محلية"
    )
    var selectedCategory by remember { mutableStateOf("الكل") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filtered = remember(communities, selectedCategory) {
        if (selectedCategory == "الكل") communities
        else communities.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column {
                        Text("مجتمعات PHARB", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "مساحات نقاش متخصصة بإشراف وقواعد واضحة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إنشاء مجتمع")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { comm ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(comm.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "التصنيف: ${comm.category} • ${comm.membersCount} عضو • ${comm.postsCount} منشور",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(onClick = { onToggleJoin(comm) }) {
                            Text(if (comm.isJoined) "عضو منضم ✓" else "انضمام")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(comm.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("قواعد المجتمع (إشراف @${comm.moderatorUsername}):", style = MaterialTheme.typography.labelLarge)
                            Text(comm.rules, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("تقنية") }
        var desc by remember { mutableStateOf("") }
        var rules by remember { mutableStateOf("1. الاحترام المتبادل.\n2. منع المحتوى المضلل.") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("إنشاء مجتمع جديد في PHARB") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المجتمع") })
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("نوع المجتمع (تعليم، علوم، تقنية، رياضة...)") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("وصف المجتمع") })
                    OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("قواعد المجتمع") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onCreateCommunity(name, category, desc, rules)
                            showCreateDialog = false
                        }
                    }
                ) { Text("إنشاء") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

/**
 * PHARB CHANNELS, EDUCATION & BUSINESS HUB (Sections 16, 40, 41)
 */
@Composable
fun PharbChannelsHubScreen(
    channels: List<ChannelEntity>,
    onToggleSubscribe: (ChannelEntity) -> Unit,
    onCreateChannel: (String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var filterType by remember { mutableStateOf("ALL") } // ALL, EDUCATION, BUSINESS, CREATOR
    var showCreateChannel by remember { mutableStateOf(false) }
    var answeredQuizIds by remember { mutableStateOf(setOf<Long>()) }

    val filtered = remember(channels, filterType) {
        if (filterType == "ALL") channels else channels.filter { it.ownerType == filterType }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column {
                        Text("القنوات • التعليم والأعمال", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "PHARB Channels + Education + Business",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = { showCreateChannel = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إنشاء قناة")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filterType == "ALL", onClick = { filterType = "ALL" }, label = { Text("الكل") })
                FilterChip(
                    selected = filterType == "EDUCATION",
                    onClick = { filterType = "EDUCATION" },
                    leadingIcon = { Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("PHARB Education") }
                )
                FilterChip(
                    selected = filterType == "BUSINESS",
                    onClick = { filterType = "BUSINESS" },
                    leadingIcon = { Icon(Icons.Filled.BusinessCenter, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("PHARB Business") }
                )
                FilterChip(selected = filterType == "CREATOR", onClick = { filterType = "CREATOR" }, label = { Text("صناع المحتوى") })
            }
        }

        items(filtered, key = { it.id }) { channel ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(channel.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(6.dp))
                                PharbVerifiedBadge(isVerified = channel.isVerified, accountType = channel.ownerType)
                            }
                            Text(
                                text = "${channel.handle} • ${channel.subscribersCount} مشترك",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(onClick = { onToggleSubscribe(channel) }) {
                            Text(if (channel.isSubscribed) "مشترك 🔔" else "اشتراك")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(channel.description, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📢 آخر بث أو منشور في القناة:", style = MaterialTheme.typography.labelLarge)
                            Text(channel.latestBroadcast, style = MaterialTheme.typography.bodyMedium)

                            if (channel.resourceAttachment.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = null,
                                        tint = if (channel.ownerType == "EDUCATION") PharbEducationEmerald else PharbBusinessGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مرفق رسمي: ${channel.resourceAttachment}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            if (channel.quizQuestion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Quiz, contentDescription = null, tint = PharbEducationEmerald, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اختبار تعليمي قصير: ${channel.quizQuestion}", style = MaterialTheme.typography.labelMedium)
                                }
                                if (channel.id !in answeredQuizIds) {
                                    TextButton(onClick = { answeredQuizIds = answeredQuizIds + channel.id }) {
                                        Text("إظهار الإجابة النموذجية والتحقق")
                                    }
                                } else {
                                    Text("الإجابة الصحيحة موثقة ✓ (+10 نقاط معرفة)", color = PharbEducationEmerald, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateChannel) {
        var name by remember { mutableStateOf("") }
        var handle by remember { mutableStateOf("@pharb_") }
        var ownerType by remember { mutableStateOf("EDUCATION") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateChannel = false },
            title = { Text("إنشاء قناة تعليمية أو تجارية أو إخبارية") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم القناة") })
                    OutlinedTextField(value = handle, onValueChange = { handle = it }, label = { Text("معرف القناة (@handle)") })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("EDUCATION" to "تعليمية", "BUSINESS" to "تجارية", "CREATOR" to "صانع محتوى").forEach { (k, l) ->
                            FilterChip(selected = ownerType == k, onClick = { ownerType = k }, label = { Text(l) })
                        }
                    }
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("وصف القناة") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onCreateChannel(name, handle, ownerType, "عام", desc)
                            showCreateChannel = false
                        }
                    }
                ) { Text("إنشاء القناة") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateChannel = false }) { Text("إلغاء") }
            }
        )
    }
}

/**
 * PHARB LIVE BROADCAST HUB (Section 14)
 */
@Composable
fun PharbLiveBroadcastHubScreen(
    streams: List<LiveStreamEntity>,
    onStartStream: (String, String) -> Unit,
    onSendLiveComment: (LiveStreamEntity, String) -> Unit,
    onEndStream: (LiveStreamEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var liveInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Column {
                        Text("PHARB Live 🔴", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "البث المباشر التفاعلي والمحاضرات الحية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { onStartStream("بث مباشر تفاعلي مع مجتمع PHARB", "حوار وتقنية") }
                ) {
                    Icon(Icons.Filled.LiveTv, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بدء بث جديد")
                }
            }
        }

        items(streams, key = { it.id }) { stream ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PharbAvatar(name = stream.hostName, avatarColorHex = stream.hostAvatarColor, size = 44.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stream.hostName, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    PharbVerifiedBadge(isVerified = stream.hostVerified)
                                }
                                Text("@${stream.hostUsername} • ${stream.category}", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Surface(
                            color = if (stream.isLiveNow) PharbLiveRed else Color.Gray,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (stream.isLiveNow) "مباشر • ${stream.viewersCount}" else "انتهى البث",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(stream.title, style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("💬 التعليقات المباشرة:", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(6.dp))
                            stream.recentChatMessages.split("|").takeLast(4).forEach { chatLine ->
                                Text("• $chatLine", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    if (stream.isLiveNow) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = liveInput,
                                onValueChange = { liveInput = it },
                                placeholder = { Text("شارك بتعليق مباشر في البث...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (liveInput.isNotBlank()) {
                                        onSendLiveComment(stream, liveInput)
                                        liveInput = ""
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال")
                            }
                            TextButton(onClick = { onEndStream(stream) }) {
                                Text("إنهاء", color = PharbLiveRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * PHARB NOTIFICATIONS CENTER (Section 18)
 */
@Composable
fun PharbNotificationsScreen(
    notifications: List<NotificationEntity>,
    onMarkAllRead: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("notifications_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text("الإشعارات والتنبيهات الفورية", style = MaterialTheme.typography.titleLarge)
            }
            TextButton(
                onClick = onMarkAllRead,
                modifier = Modifier.testTag("mark_notifications_read_button")
            ) {
                Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تعليم الكل كمقروء")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(notifications, key = { it.id }) { notif ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        PharbAvatar(name = notif.actorName, avatarColorHex = notif.actorAvatarColor, size = 42.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, style = MaterialTheme.typography.titleSmall)
                            Text(notif.body, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
