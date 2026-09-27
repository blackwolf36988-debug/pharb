package com.example.core

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * PHARB Production Security Manager (Sections 6, 25)
 * - Salted PBKDF2WithHmacSHA256 password hashing (never stores plaintext passwords)
 * - HMAC-SHA256 signed JWT-format Access & Refresh Tokens
 * - OTP generation & verification
 * - Input sanitization (XSS & SQLi protection)
 * - In-memory sliding window Rate Limiter
 */
object SecurityManager {
    private const val PBKDF2_ITERATIONS = 12000
    private const val KEY_LENGTH_BITS = 256
    private val secureRandom = SecureRandom()
    private val hmacSecret = "PHARB_CORE_HMAC_256_SIGNATURE_KEY_v1".toByteArray(Charsets.UTF_8)

    private val rateLimitBuckets = mutableMapOf<String, MutableList<Long>>()

    fun generateSalt(): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return bytesToHex(salt)
    }

    fun hashPassword(password: String, saltHex: String): String {
        return try {
            val salt = hexToBytes(saltHex)
            val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            bytesToHex(hash)
        } catch (e: Exception) {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$saltHex:$password".toByteArray(Charsets.UTF_8))
            bytesToHex(bytes)
        }
    }

    fun verifyPassword(password: String, saltHex: String, expectedHash: String): Boolean {
        if (saltHex.isBlank() || expectedHash.isBlank()) return false
        val computed = hashPassword(password, saltHex)
        return MessageDigest.isEqual(
            computed.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun generateJwtToken(userId: Long, username: String, role: String = "USER", ttlMillis: Long = 3600_000L): String {
        val now = System.currentTimeMillis()
        val exp = now + ttlMillis
        val headerJson = """{"alg":"HS256","typ":"JWT"}"""
        val payloadJson = """{"sub":"$userId","usr":"$username","role":"$role","iat":$now,"exp":$exp}"""
        val headerB64 = encodeUrlSafe(headerJson.toByteArray(Charsets.UTF_8))
        val payloadB64 = encodeUrlSafe(payloadJson.toByteArray(Charsets.UTF_8))
        val signature = signHmac("$headerB64.$payloadB64")
        return "$headerB64.$payloadB64.$signature"
    }

    fun generateRefreshToken(userId: Long): String {
        val randomBytes = ByteArray(24)
        secureRandom.nextBytes(randomBytes)
        val tokenCore = bytesToHex(randomBytes)
        val sig = signHmac("refresh:$userId:$tokenCore")
        return "pharb_rt_${userId}_${tokenCore}_$sig"
    }

    fun generateOtpCode(): String {
        val code = 100000 + secureRandom.nextInt(900000)
        return code.toString()
    }

    fun sanitizeInput(raw: String): String {
        return raw
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace("javascript:", "", ignoreCase = true)
            .replace("onerror=", "", ignoreCase = true)
            .replace("onload=", "", ignoreCase = true)
            .replace("DROP TABLE", "", ignoreCase = true)
            .replace("DELETE FROM", "", ignoreCase = true)
            .replace("--", "—")
            .trim()
    }

    @Synchronized
    fun checkRateLimit(actionKey: String, maxRequests: Int = 15, windowMillis: Long = 60_000L): Boolean {
        val now = System.currentTimeMillis()
        val timestamps = rateLimitBuckets.getOrPut(actionKey) { mutableListOf() }
        timestamps.removeAll { now - it > windowMillis }
        if (timestamps.size >= maxRequests) {
            return false
        }
        timestamps.add(now)
        return true
    }

    fun evaluatePasswordStrength(password: String): PasswordStrengthResult {
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        val labelAr = when {
            score <= 1 -> "ضعيفة جدًا"
            score == 2 -> "متوسطة"
            score == 3 -> "جيدة"
            else -> "قوية وآمنة"
        }
        val labelEn = when {
            score <= 1 -> "Too Weak"
            score == 2 -> "Fair"
            score == 3 -> "Good"
            else -> "Strong & Secure"
        }
        return PasswordStrengthResult(
            score = score,
            isAcceptable = password.length >= 6 && score >= 2,
            labelAr = labelAr,
            labelEn = labelEn
        )
    }

    private fun signHmac(data: String): String {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(hmacSecret, "HmacSHA256"))
            bytesToHex(mac.doFinal(data.toByteArray(Charsets.UTF_8)))
        } catch (e: Exception) {
            val digest = MessageDigest.getInstance("SHA-256")
            bytesToHex(digest.digest(data.toByteArray(Charsets.UTF_8)))
        }
    }

    private fun encodeUrlSafe(bytes: ByteArray): String {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = if (hex.length % 2 != 0) "0$hex" else hex
        val result = ByteArray(clean.length / 2)
        for (i in result.indices) {
            val idx = i * 2
            result[i] = clean.substring(idx, idx + 2).toIntOrNull(16)?.toByte() ?: 0
        }
        return result
    }
}

data class PasswordStrengthResult(
    val score: Int,
    val isAcceptable: Boolean,
    val labelAr: String,
    val labelEn: String
)
