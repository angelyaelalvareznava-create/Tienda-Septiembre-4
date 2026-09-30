package com.example.tiendita.auth

import java.util.Base64
import java.security.SecureRandom
import java.security.ProviderException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    private val hasher = Pbkdf2PasswordHasher()

    @Test fun productionConstructorUsesDefaultDispatcher() {
        // Explicitly guard the production API's dispatcher configuration.
        val field = Pbkdf2PasswordHasher::class.java.getDeclaredField("dispatcher")
        field.isAccessible = true
        assertSame(Dispatchers.Default, field.get(Pbkdf2PasswordHasher()))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun internalDispatcherAndCleanupOnRandomFailure() = runTest {
        var generatedSalt: ByteArray? = null
        val failingRandom = object : SecureRandom() {
            override fun nextBytes(bytes: ByteArray) {
                generatedSalt = bytes
                bytes.fill(42)
                throw ProviderException("Test random provider failure")
            }
        }
        val controlled = Pbkdf2PasswordHasher(StandardTestDispatcher(testScheduler), failingRandom)
        val callerPassword = "unchanged".toCharArray()
        val operation = async {
            try {
                controlled.hash(callerPassword)
                fail("Random failure must not produce a hash")
            } catch (_: ProviderException) {
                // Propagation fails closed; no credential result is returned.
            }
        }
        assertNull(generatedSalt)
        runCurrent()
        operation.await()
        assertNotNull(generatedSalt)
        assertTrue(generatedSalt!!.all { it == 0.toByte() })
        assertArrayEquals("unchanged".toCharArray(), callerPassword)
        callerPassword.fill('\u0000')
    }

    @Test fun malformedHashAfterValidSaltFailsClosed() = runBlocking {
        val callerPassword = "password".toCharArray()
        try {
            val malformed = PasswordHash("PBKDF2WithHmacSHA256", 600_000,
                "AAECAwQFBgcICQoLDA0ODw==", "invalid!")
            assertFalse(hasher.isSupported(malformed))
            assertFalse(hasher.verify(callerPassword, malformed))
            assertArrayEquals("password".toCharArray(), callerPassword)
        } finally {
            callerPassword.fill('\u0000')
        }
    }

    @Test fun correctAndIncorrectPassword() = runBlocking {
        val stored = hasher.hash(" Password123! ".toCharArray())
        assertTrue(hasher.verify(" Password123! ".toCharArray(), stored))
        assertFalse(hasher.verify("Password123!".toCharArray(), stored))
        assertFalse(hasher.verify("wrong".toCharArray(), stored))
    }

    @Test fun individualSaltAndApprovedParameters() = runBlocking {
        val first = hasher.hash("same".toCharArray())
        val second = hasher.hash("same".toCharArray())
        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
        assertEquals("PBKDF2WithHmacSHA256", first.algorithm)
        assertEquals(600_000, first.iterations)
        assertEquals(16, Base64.getDecoder().decode(first.salt).size)
        assertEquals(32, Base64.getDecoder().decode(first.hash).size)
    }

    @Test fun independentlyGeneratedSha256Vector() = runBlocking {
        // Python hashlib.pbkdf2_hmac('sha256', b'password', bytes(range(16)), 600000, 32).
        val vector = PasswordHash("PBKDF2WithHmacSHA256", 600_000,
            "AAECAwQFBgcICQoLDA0ODw==", "O8NxGOYlCT6bee0Ikw6nr3OJWRIz/dkt3fNpNx5g28A=")
        assertTrue(hasher.verify("password".toCharArray(), vector))
    }

    @Test fun invalidMetadataNeverFallsBack() = runBlocking {
        val valid = PasswordHash("PBKDF2WithHmacSHA256", 600_000,
            "AAECAwQFBgcICQoLDA0ODw==", "O8NxGOYlCT6bee0Ikw6nr3OJWRIz/dkt3fNpNx5g28A=")
        listOf(valid.copy(algorithm = "PBKDF2WithHmacSHA1"), valid.copy(iterations = 0),
            valid.copy(iterations = 10_000), valid.copy(iterations = Int.MAX_VALUE),
            valid.copy(salt = "invalid!"), valid.copy(hash = "AA==")).forEach {
            assertFalse(hasher.isSupported(it))
            assertFalse(hasher.verify("password".toCharArray(), it))
        }
    }
}
