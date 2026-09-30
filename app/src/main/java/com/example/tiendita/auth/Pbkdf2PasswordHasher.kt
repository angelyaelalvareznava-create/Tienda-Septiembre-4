package com.example.tiendita.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Platform implementation only. Never falls back to a different algorithm. */
class Pbkdf2PasswordHasher internal constructor(
    private val dispatcher: CoroutineDispatcher,
    private val secureRandom: SecureRandom
) : PasswordHasher {
    /** Production API always uses the CPU dispatcher. Injection is internal for tests. */
    constructor() : this(Dispatchers.Default, SecureRandom())

    companion object {
        const val PARAMETERS_VERSION = 1
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS_V1 = 600_000
        const val SALT_BYTES = 16
        const val KEY_BYTES = 32
        // Bound corrupt/untrusted work factors; future policies must update this explicitly.
        const val MAX_SUPPORTED_ITERATIONS = 2_000_000
    }

    override suspend fun hash(password: CharArray): PasswordHash = withPasswordCopy(password) { characters ->
        var salt: ByteArray? = null
        var derived: ByteArray? = null
        try {
            salt = ByteArray(SALT_BYTES)
            secureRandom.nextBytes(salt)
            derived = derive(characters, salt, ITERATIONS_V1)
            PasswordHash(ALGORITHM, ITERATIONS_V1, encode(salt), encode(derived))
        } finally {
            derived?.fill(0)
            salt?.fill(0)
        }
    }

    override fun isSupported(stored: PasswordHash): Boolean {
        if (stored.algorithm != ALGORITHM || stored.iterations !in ITERATIONS_V1..MAX_SUPPORTED_ITERATIONS) return false
        if (stored.salt.length > 24 || stored.hash.length > 44) return false
        var salt: ByteArray? = null
        var expected: ByteArray? = null
        return try {
            salt = decode(stored.salt)
            expected = decode(stored.hash)
            salt.size == SALT_BYTES && expected.size == KEY_BYTES
        } catch (_: IllegalArgumentException) {
            false
        } finally {
            expected?.fill(0)
            salt?.fill(0)
        }
    }

    override suspend fun verify(password: CharArray, stored: PasswordHash): Boolean = withPasswordCopy(password) { characters ->
        if (!isSupported(stored)) return@withPasswordCopy false
        var salt: ByteArray? = null
        var expected: ByteArray? = null
        var actual: ByteArray? = null
        try {
            salt = decode(stored.salt)
            expected = decode(stored.hash)
            actual = derive(characters, salt, stored.iterations)
            MessageDigest.isEqual(expected, actual)
        } finally {
            actual?.fill(0)
            expected?.fill(0)
            salt?.fill(0)
        }
    }

    /** Borrow caller's array; wipe our copy even if dispatch is cancelled or work throws. */
    private suspend fun <T> withPasswordCopy(password: CharArray, block: (CharArray) -> T): T {
        val characters = password.copyOf()
        try {
            return withContext(dispatcher) { block(characters) }
        } finally {
            characters.fill('\u0000')
        }
    }

    private fun derive(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BYTES * 8)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
    private fun decode(value: String): ByteArray = Base64.getDecoder().decode(value)
}
