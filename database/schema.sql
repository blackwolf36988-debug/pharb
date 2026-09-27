-- ============================================================================
-- PHARB SOCIAL NETWORK — PRODUCTION POSTGRESQL DATABASE SCHEMA (Section 29)
-- Supports 1,000 -> 100,000 -> 1,000,000+ concurrent users with indexing
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 1. USERS & AUTHENTICATION
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID DEFAULT uuid_generate_v4() UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(32) UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL,
    account_type VARCHAR(24) NOT NULL DEFAULT 'PERSONAL', -- PERSONAL, CREATOR, BUSINESS, EDUCATION
    role VARCHAR(20) NOT NULL DEFAULT 'USER', -- USER, MODERATOR, ADMIN
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    is_suspended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. PROFILES
CREATE TABLE profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(120) NOT NULL,
    bio TEXT DEFAULT '',
    avatar_url TEXT,
    cover_url TEXT,
    location VARCHAR(120),
    website VARCHAR(255),
    followers_count INT NOT NULL DEFAULT 0,
    following_count INT NOT NULL DEFAULT 0,
    show_online_status BOOLEAN NOT NULL DEFAULT TRUE,
    show_last_seen BOOLEAN NOT NULL DEFAULT TRUE,
    allow_messages_from VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    allow_tagging BOOLEAN NOT NULL DEFAULT TRUE,
    searchable_in_directory BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. DEVICES & SESSIONS
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_name VARCHAR(120) NOT NULL,
    platform VARCHAR(32) NOT NULL DEFAULT 'ANDROID',
    push_token TEXT,
    last_ip VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id BIGINT REFERENCES devices(id) ON DELETE SET NULL,
    jwt_jti VARCHAR(128) UNIQUE NOT NULL,
    refresh_token_hash TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. POSTS
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    post_type VARCHAR(24) NOT NULL DEFAULT 'TEXT', -- TEXT, IMAGE, MULTI_IMAGE, VIDEO, GIF, LINK, POLL, AUDIO, QUESTION, FILE
    media_urls JSONB DEFAULT '[]'::JSONB,
    poll_options JSONB DEFAULT '[]'::JSONB,
    file_metadata JSONB,
    hashtags TEXT[] DEFAULT '{}',
    mentions TEXT[] DEFAULT '{}',
    location_tag VARCHAR(120),
    audience VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    category VARCHAR(48) NOT NULL DEFAULT 'general',
    is_repost BOOLEAN NOT NULL DEFAULT FALSE,
    original_post_id BIGINT REFERENCES posts(id) ON DELETE SET NULL,
    quote_text TEXT,
    comments_count INT NOT NULL DEFAULT 0,
    reactions_count INT NOT NULL DEFAULT 0,
    repost_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_author_created ON posts(author_id, created_at DESC);
CREATE INDEX idx_posts_category_created ON posts(category, created_at DESC);

-- 5. COMMENTS, LIKES & REACTIONS (Like, Love, Laugh, Wow, Sad, Angry)
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    likes_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reactions (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reaction_type VARCHAR(16) NOT NULL, -- LIKE, LOVE, LAUGH, WOW, SAD, ANGRY
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(post_id, user_id)
);

-- 6. FOLLOWERS & FOLLOWING
CREATE TABLE follows (
    follower_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED', -- PENDING, ACCEPTED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id)
);

-- 7. STORIES & STORY VIEWS (24h + Archive)
CREATE TABLE stories (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
    media_url TEXT,
    caption TEXT,
    music_track VARCHAR(120),
    interactive_sticker JSONB,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    views_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '24 hours'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE story_views (
    story_id BIGINT NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    viewer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    viewed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (story_id, viewer_id)
);

-- 8. SHORT VIDEOS (PHARB SHORTS)
CREATE TABLE videos (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    caption TEXT NOT NULL,
    hls_manifest_url TEXT NOT NULL,
    thumbnail_url TEXT NOT NULL,
    audio_track VARCHAR(160),
    hashtags TEXT[] DEFAULT '{}',
    duration_seconds INT NOT NULL DEFAULT 60,
    bitrate_kbps INT NOT NULL DEFAULT 2400,
    likes_count INT NOT NULL DEFAULT 0,
    comments_count INT NOT NULL DEFAULT 0,
    shares_count INT NOT NULL DEFAULT 0,
    saves_count INT NOT NULL DEFAULT 0,
    views_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. CONVERSATIONS & MESSAGES (PHARB MESSENGER)
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120),
    is_group BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT', -- TEXT, IMAGE, VIDEO, VOICE, FILE, STICKER, GIF
    content TEXT NOT NULL,
    media_url TEXT,
    reply_to_id BIGINT REFERENCES messages(id) ON DELETE SET NULL,
    reaction_emoji VARCHAR(16),
    delivery_status VARCHAR(16) NOT NULL DEFAULT 'SENT', -- SENT, DELIVERED, SEEN
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. COMMUNITIES & CHANNELS
CREATE TABLE communities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) UNIQUE NOT NULL,
    category VARCHAR(48) NOT NULL,
    description TEXT NOT NULL,
    rules TEXT NOT NULL,
    creator_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    members_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE community_members (
    community_id BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER', -- MEMBER, MODERATOR, ADMIN
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (community_id, user_id)
);

CREATE TABLE channels (
    id BIGSERIAL PRIMARY KEY,
    handle VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(120) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    owner_type VARCHAR(24) NOT NULL DEFAULT 'EDUCATION', -- CREATOR, EDUCATION, BUSINESS
    category VARCHAR(48) NOT NULL,
    description TEXT NOT NULL,
    subscribers_count INT NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE channel_members (
    channel_id BIGINT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subscribed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (channel_id, user_id)
);

-- 11. SAVED POSTS & COLLECTIONS
CREATE TABLE collections (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE saved_posts (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    collection_id BIGINT REFERENCES collections(id) ON DELETE SET NULL,
    saved_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

-- 12. LIVE STREAMS, NOTIFICATIONS, REPORTS, BLOCKS & VERIFICATION REQUESTS
CREATE TABLE live_streams (
    id BIGSERIAL PRIMARY KEY,
    host_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    category VARCHAR(48) NOT NULL,
    cover_url TEXT,
    is_live BOOLEAN NOT NULL DEFAULT TRUE,
    viewers_count INT NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    actor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    notif_type VARCHAR(24) NOT NULL, -- LIKE, COMMENT, FOLLOW, MESSAGE, MENTION, REPOST, JOIN_REQUEST, LIVE, SECURITY
    title VARCHAR(160) NOT NULL,
    body TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(24) NOT NULL, -- POST, USER, COMMENT, MESSAGE
    target_id BIGINT NOT NULL,
    reason VARCHAR(32) NOT NULL, -- Spam, Harassment, Hate, Impersonation, Scam, Copyright, Illegal Content, Other
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    moderator_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE blocks (
    blocker_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    relation_type VARCHAR(16) NOT NULL DEFAULT 'BLOCK', -- BLOCK, MUTE, RESTRICT
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blocker_id, blocked_id)
);

CREATE TABLE verification_requests (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_type VARCHAR(24) NOT NULL,
    document_reference TEXT NOT NULL,
    justification TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
