package com.example.tiendita.auth

import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordHasherAndroidTest {
    @Test fun platformSha256CompatibilityAndDevicePerformance() = runBlocking {
        val hasher = Pbkdf2PasswordHasher()
        val samples = mutableListOf<Long>()
        repeat(3) {
            val started = System.nanoTime()
            val hash = hasher.hash(" Password123! ".toCharArray())
            samples += (System.nanoTime() - started) / 1_000_000
            assertEquals(600_000, hash.iterations)
            assertTrue(hasher.verify(" Password123! ".toCharArray(), hash))
            assertFalse(hasher.verify("Password123!".toCharArray(), hash))
        }
        val sorted = samples.sorted()
        val minimum = sorted.first()
        val maximum = sorted.last()
        val median = sorted[sorted.size / 2]
        Log.i("AuthInfrastructureTest", "Hash API=${Build.VERSION.SDK_INT}, model=${Build.MODEL}, " +
            "ms=$samples, minMs=$minimum, maxMs=$maximum, medianMs=$median")
        // 2,500 ms is this project's regression limit, based on Nothing A024 measurements.
        // It is not a universal cryptographic recommendation; never reduce iterations to meet it.
        assertTrue("Hash reached the 2500 ms regression limit on API ${Build.VERSION.SDK_INT}: $samples ms",
            samples.all { it < 2_500 })
    }
}
