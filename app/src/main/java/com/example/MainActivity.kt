package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AppNavigation
import com.example.ui.navigation.BottomNavigationBar
import com.example.ui.navigation.PharbRoutes
import com.example.ui.navigation.navigateToBottomBarRoute
import com.example.ui.screens.PharbAdminDashboardScreen
import com.example.ui.screens.PharbAuthPortalScreen
import com.example.ui.screens.PharbBrandDesignSystemScreen
import com.example.ui.screens.PharbChannelsHubScreen
import com.example.ui.screens.PharbCommunitiesHubScreen
import com.example.ui.screens.PharbDiscoverScreen
import com.example.ui.screens.PharbLiveBroadcastHubScreen
import com.example.ui.screens.PharbNotificationsScreen
import com.example.ui.screens.PharbOnboardingScreen
import com.example.ui.screens.PharbPrivacyAndSecurityScreen
import com.example.ui.screens.PharbShortsPlayerScreen
import com.example.ui.screens.PharbSplashScreen
import com.example.ui.screens.PharbStoryArchiveScreen
import com.example.ui.screens.PharbStoryViewerScreen
import com.example.ui.screens.PharbWebRtcCallScreen
import com.example.ui.theme.PharbTheme
import com.example.ui.viewmodel.MainBottomTab
import com.example.ui.viewmodel.PharbViewModel
import com.example.ui.viewmodel.SubScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val viewModel: PharbViewModel = viewModel(
                factory = PharbViewModel.provideFactory(context)
            )
            val navController = rememberNavController()
            PharbAppRoot(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@Composable
fun PharbAppRoot(
    viewModel: PharbViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val drafts by viewModel.draftPosts.collectAsStateWithLifecycle()
    val savedPosts by viewModel.savedPosts.collectAsStateWithLifecycle()
    val comments by viewModel.allComments.collectAsStateWithLifecycle()
    val activeStories by viewModel.activeStories.collectAsStateWithLifecycle()
    val archivedStories by viewModel.archivedStories.collectAsStateWithLifecycle()
    val shortVideos by viewModel.shortVideos.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.allMessages.collectAsStateWithLifecycle()
    val communities by viewModel.communities.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val liveStreams by viewModel.liveStreams.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val blockMuteRelations by viewModel.blockMuteRelations.collectAsStateWithLifecycle()
    val verificationRequests by viewModel.verificationRequests.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: PharbRoutes.ROUTE_HOME

    LaunchedEffect(uiState.currentBottomTab) {
        val targetRoute = PharbRoutes.fromTab(uiState.currentBottomTab)
        navController.navigateToBottomBarRoute(targetRoute)
    }

    val currentUser = viewModel.currentUser()
    val unreadNotificationsCount = notifications.count { !it.isRead }
    val unreadMessagesCount = conversations.sumOf { it.unreadCount }

    val blockedOrMutedUsernames = blockMuteRelations.map { it.targetUsername }.toSet()
    val visiblePosts = posts.filter { it.authorUsername !in blockedOrMutedUsernames }

    PharbTheme(
        themeMode = uiState.themeMode,
        accentPreset = uiState.accentPreset,
        isArabic = uiState.isArabic,
        fontScaleMultiplier = uiState.fontScaleMultiplier
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (uiState.subScreen == SubScreenDestination.NONE) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        isArabic = uiState.isArabic,
                        unreadMessagesCount = unreadMessagesCount,
                        navController = navController,
                        onSelectTab = viewModel::selectBottomTab
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.subScreen) {
                    SubScreenDestination.SPLASH_SCREEN -> {
                        PharbSplashScreen(
                            isArabic = uiState.isArabic,
                            onFinishSplash = { viewModel.navigateToSubScreen(SubScreenDestination.NONE) },
                            onOpenOnboarding = { viewModel.navigateToSubScreen(SubScreenDestination.ONBOARDING_TOUR) }
                        )
                    }

                    SubScreenDestination.ONBOARDING_TOUR -> {
                        PharbOnboardingScreen(
                            isArabic = uiState.isArabic,
                            onFinishOnboarding = { viewModel.navigateToSubScreen(SubScreenDestination.NONE) },
                            onOpenAuthPortal = { viewModel.navigateToSubScreen(SubScreenDestination.AUTH_PORTAL) }
                        )
                    }

                    SubScreenDestination.AUTH_PORTAL -> {
                        PharbAuthPortalScreen(
                            users = users,
                            generatedOtp = uiState.generatedOtpCode,
                            isArabic = uiState.isArabic,
                            onLogin = viewModel::login,
                            onRegister = viewModel::register,
                            onRequestOtp = viewModel::requestOtpCode,
                            onVerifyOtp = viewModel::verifyOtpAndLogin,
                            onQuickSwitchUser = viewModel::switchActiveDemoAccount,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.SHORTS_PLAYER -> {
                        PharbShortsPlayerScreen(
                            videos = shortVideos,
                            onToggleLike = viewModel::toggleLikeShort,
                            onToggleSave = viewModel::toggleSaveShort,
                            onToggleFollowCreator = viewModel::toggleFollowShortCreator,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.STORY_VIEWER -> {
                        val story = uiState.selectedStory ?: activeStories.firstOrNull()
                        if (story != null) {
                            PharbStoryViewerScreen(
                                story = story,
                                onArchiveStory = viewModel::moveStoryToArchive,
                                onBack = viewModel::navigateBackToMain
                            )
                        } else {
                            viewModel.navigateBackToMain()
                        }
                    }

                    SubScreenDestination.STORY_ARCHIVE -> {
                        PharbStoryArchiveScreen(
                            archivedStories = archivedStories,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.SEARCH_ENGINE -> {
                        PharbDiscoverScreen(
                            searchQuery = uiState.searchQuery,
                            searchFilter = uiState.searchFilter,
                            searchHistory = uiState.searchHistory,
                            userInterests = uiState.userInterests,
                            users = users,
                            posts = visiblePosts,
                            shortVideos = shortVideos,
                            communities = communities,
                            channels = channels,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSearchFilterChange = viewModel::setSearchFilter,
                            onToggleInterest = viewModel::toggleInterestTopic,
                            onToggleFollowUser = viewModel::toggleFollowUser,
                            onToggleJoinCommunity = viewModel::toggleJoinCommunity,
                            onToggleSubscribeChannel = viewModel::toggleSubscribeChannel,
                            onOpenSubScreen = viewModel::navigateToSubScreen
                        )
                    }

                    SubScreenDestination.NOTIFICATIONS_CENTER -> {
                        PharbNotificationsScreen(
                            notifications = notifications,
                            onMarkAllRead = viewModel::markAllNotificationsRead,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.COMMUNITIES_HUB -> {
                        PharbCommunitiesHubScreen(
                            communities = communities,
                            onToggleJoin = viewModel::toggleJoinCommunity,
                            onCreateCommunity = viewModel::createCommunity,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.CHANNELS_HUB -> {
                        PharbChannelsHubScreen(
                            channels = channels,
                            onToggleSubscribe = viewModel::toggleSubscribeChannel,
                            onCreateChannel = viewModel::createChannel,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.LIVE_BROADCAST_HUB -> {
                        PharbLiveBroadcastHubScreen(
                            streams = liveStreams,
                            onStartStream = viewModel::startLiveStream,
                            onSendLiveComment = viewModel::sendLiveStreamComment,
                            onEndStream = viewModel::endLiveStream,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.PRIVACY_AND_SECURITY -> {
                        PharbPrivacyAndSecurityScreen(
                            user = currentUser,
                            sessions = sessions,
                            blockMuteList = blockMuteRelations,
                            themeMode = uiState.themeMode,
                            accentPreset = uiState.accentPreset,
                            isArabic = uiState.isArabic,
                            fontScale = uiState.fontScaleMultiplier,
                            onSelectThemeMode = viewModel::setThemeMode,
                            onSelectAccentPreset = viewModel::setAccentPreset,
                            onToggleLanguage = viewModel::toggleLanguage,
                            onChangeFontScale = viewModel::setFontScale,
                            onSavePrivacySettings = { priv, online, lastSeen, msgFrom, tag, search ->
                                viewModel.updateProfileAndPrivacy(
                                    fullName = currentUser.fullName,
                                    bio = currentUser.bio,
                                    location = currentUser.location,
                                    website = currentUser.website,
                                    accountType = currentUser.accountType,
                                    isPrivateAccount = priv,
                                    showOnlineStatus = online,
                                    showLastSeen = lastSeen,
                                    allowMessagesFrom = msgFrom,
                                    allowTagging = tag,
                                    searchableInDirectory = search
                                )
                            },
                            onChangePassword = viewModel::changePassword,
                            onRevokeOtherSessions = viewModel::revokeOtherSessions,
                            onRemoveBlockMute = viewModel::removeBlockOrMute,
                            onExportMyData = {
                                viewModel.showBanner("تم تجهيز أرشيف بياناتك المشفر بصيغة JSON للتنزيل الفوري 📦")
                            },
                            onDeleteAccount = viewModel::deleteMyAccountPermanently,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.BRAND_AND_DESIGN_SYSTEM -> {
                        PharbBrandDesignSystemScreen(
                            isArabic = uiState.isArabic,
                            onOpenSubScreen = viewModel::navigateToSubScreen,
                            onSelectBottomTab = viewModel::selectBottomTab,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.ADMIN_DASHBOARD -> {
                        PharbAdminDashboardScreen(
                            users = users,
                            posts = posts,
                            shortVideos = shortVideos,
                            messages = messages,
                            reports = reports,
                            communities = communities,
                            channels = channels,
                            liveStreams = liveStreams,
                            verificationRequests = verificationRequests,
                            onToggleSuspendUser = viewModel::adminToggleSuspendUser,
                            onReviewVerification = viewModel::adminReviewVerification,
                            onResolveReport = viewModel::adminResolveReport,
                            onBack = viewModel::navigateBackToMain
                        )
                    }

                    SubScreenDestination.CALL_SCREEN -> {
                        val call = uiState.activeCall
                        if (call != null) {
                            PharbWebRtcCallScreen(
                                callState = call,
                                onToggleMute = viewModel::toggleCallMute,
                                onToggleCamera = viewModel::toggleCallCamera,
                                onEndCall = viewModel::endActiveCall
                            )
                        } else {
                            viewModel.navigateBackToMain()
                        }
                    }

                    else -> {
                        AppNavigation(
                            viewModel = viewModel,
                            uiState = uiState,
                            currentUser = currentUser,
                            users = users,
                            visiblePosts = visiblePosts,
                            allPosts = posts,
                            drafts = drafts,
                            savedPosts = savedPosts,
                            comments = comments,
                            activeStories = activeStories,
                            shortVideos = shortVideos,
                            conversations = conversations,
                            messages = messages,
                            communities = communities,
                            channels = channels,
                            unreadNotificationsCount = unreadNotificationsCount,
                            unreadMessagesCount = unreadMessagesCount,
                            navController = navController
                        )
                    }
                }

                // Floating Status Banner
                AnimatedVisibility(
                    visible = uiState.statusBannerMessage != null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    uiState.statusBannerMessage?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.inverseSurface,
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = viewModel::clearBanner) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "إغلاق",
                                        tint = MaterialTheme.colorScheme.inverseOnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
