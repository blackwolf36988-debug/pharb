package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.local.ChannelEntity
import com.example.data.local.CommentEntity
import com.example.data.local.CommunityEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PostEntity
import com.example.data.local.ShortVideoEntity
import com.example.data.local.StoryEntity
import com.example.data.local.UserEntity
import com.example.ui.screens.PharbCreateContentScreen
import com.example.ui.screens.PharbDiscoverScreen
import com.example.ui.screens.PharbHomeFeedScreen
import com.example.ui.screens.PharbMessengerScreen
import com.example.ui.screens.PharbProfileScreen
import com.example.ui.viewmodel.HomeFeedTab
import com.example.ui.viewmodel.MainBottomTab
import com.example.ui.viewmodel.PharbUiState
import com.example.ui.viewmodel.PharbViewModel

/**
 * Sealed representation of the 5 core PHARB screen routes:
 * Home, Discover, Create, Messages, and Profile.
 */
sealed class AppScreen(val route: String, val titleAr: String, val titleEn: String) {
    data object Home : AppScreen(PharbRoutes.ROUTE_HOME, "الرئيسية", "Home")
    data object Discover : AppScreen(PharbRoutes.ROUTE_DISCOVER, "اكتشف", "Discover")
    data object Create : AppScreen(PharbRoutes.ROUTE_CREATE, "إنشاء", "Create")
    data object Messages : AppScreen(PharbRoutes.ROUTE_MESSAGES, "الرسائل", "Messages")
    data object Profile : AppScreen(PharbRoutes.ROUTE_PROFILE, "حسابي", "Profile")

    companion object {
        val items: List<AppScreen> = listOf(Home, Discover, Create, Messages, Profile)
    }
}

/**
 * Primary Jetpack Compose Navigation graph (`NavHost`) for PHARB.
 * Defines the composable routes for Home, Discover, Create, Messages, and Profile screens.
 */
@Composable
fun AppNavigation(
    viewModel: PharbViewModel,
    uiState: PharbUiState,
    currentUser: UserEntity,
    users: List<UserEntity>,
    visiblePosts: List<PostEntity>,
    allPosts: List<PostEntity>,
    drafts: List<PostEntity>,
    savedPosts: List<PostEntity>,
    comments: List<CommentEntity>,
    activeStories: List<StoryEntity>,
    shortVideos: List<ShortVideoEntity>,
    conversations: List<ConversationEntity>,
    messages: List<MessageEntity>,
    communities: List<CommunityEntity>,
    channels: List<ChannelEntity>,
    unreadNotificationsCount: Int,
    unreadMessagesCount: Int = 0,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppScreen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
            .fillMaxSize()
            .testTag("pharb_nav_host")
    ) {
        composable(route = AppScreen.Home.route) {
            PharbHomeFeedScreen(
                currentUser = currentUser,
                posts = visiblePosts,
                stories = activeStories,
                comments = comments,
                unreadNotificationsCount = unreadNotificationsCount,
                unreadMessagesCount = unreadMessagesCount,
                currentFeedTab = uiState.currentFeedTab,
                userInterests = uiState.userInterests,
                isArabic = uiState.isArabic,
                isOfflineMode = uiState.isOfflineMode,
                selectedPostForComments = uiState.selectedPostForComments,
                onSelectFeedTab = viewModel::selectFeedTab,
                onOpenSubScreen = viewModel::navigateToSubScreen,
                onOpenMessages = {
                    viewModel.selectBottomTab(MainBottomTab.MESSAGES)
                    navController.navigateToBottomBarRoute(AppScreen.Messages.route)
                },
                onOpenStory = viewModel::openStoryViewer,
                onCreateStoryQuick = {
                    viewModel.selectBottomTab(MainBottomTab.CREATE)
                    navController.navigateToBottomBarRoute(AppScreen.Create.route)
                },
                onReactToPost = viewModel::reactToPost,
                onVotePoll = viewModel::voteInPoll,
                onRepost = viewModel::repost,
                onToggleSave = { post -> viewModel.toggleSavePost(post) },
                onOpenComments = viewModel::openCommentsSheet,
                onSubmitComment = viewModel::submitComment,
                onReportPost = { post, reason ->
                    viewModel.submitReport("POST", post.id, post.content.take(80), reason)
                },
                onBlockOrMuteAuthor = viewModel::blockOrMuteUser,
                onToggleOffline = viewModel::toggleOfflineMode,
                onToggleLanguage = viewModel::toggleLanguage
            )
        }

        composable(route = AppScreen.Discover.route) {
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

        composable(route = AppScreen.Create.route) {
            PharbCreateContentScreen(
                drafts = drafts,
                isArabic = uiState.isArabic,
                onCreatePost = { content, type, link, polls, audioSec, file, tags, mentions, loc, aud, cat, asDraft ->
                    viewModel.createNewPost(
                        content = content,
                        postType = type,
                        linkUrl = link,
                        pollOptions = polls,
                        audioDurationSec = audioSec,
                        fileAttachmentName = file,
                        hashtags = tags,
                        mentions = mentions,
                        locationTag = loc,
                        audience = aud,
                        category = cat,
                        asDraft = asDraft
                    )
                },
                onPublishDraft = viewModel::publishDraft,
                onCreateStory = viewModel::createNewStory,
                onStartLiveStream = viewModel::startLiveStream
            )
        }

        composable(route = AppScreen.Messages.route) {
            PharbMessengerScreen(
                conversations = conversations,
                allMessages = messages,
                selectedConversationId = uiState.selectedConversationId,
                onSelectConversation = viewModel::openConversation,
                onSendMessage = viewModel::sendMessage,
                onReactMessage = viewModel::reactToMessage,
                onDeleteMessage = viewModel::deleteMessage,
                onCreateConversation = viewModel::createConversation,
                onStartCall = viewModel::startCall
            )
        }

        composable(route = AppScreen.Profile.route) {
            PharbProfileScreen(
                user = currentUser,
                myPosts = allPosts.filter { it.authorId == currentUser.id || it.authorUsername == currentUser.username },
                savedPosts = savedPosts,
                shortVideos = shortVideos,
                communities = communities,
                channels = channels,
                onUpdateProfile = { name, bio, loc, web, accType, isPriv ->
                    viewModel.updateProfileAndPrivacy(
                        fullName = name,
                        bio = bio,
                        location = loc,
                        website = web,
                        accountType = accType,
                        isPrivateAccount = isPriv,
                        showOnlineStatus = currentUser.showOnlineStatus,
                        showLastSeen = currentUser.showLastSeen,
                        allowMessagesFrom = currentUser.allowMessagesFrom,
                        allowTagging = currentUser.allowTagging,
                        searchableInDirectory = currentUser.searchableInDirectory
                    )
                },
                onRequestVerification = viewModel::requestVerification,
                onOpenSubScreen = viewModel::navigateToSubScreen,
                onLogout = viewModel::logoutCurrentSession
            )
        }
    }
}
