package org.firstinspires.ftc.threedrd.nextftc.config

import org.junit.Assert.*
import org.junit.*

class PanelsConfigTests {
    class CurrentApi {
        companion object {
            var refreshed: Class<*>? = null

            @JvmStatic
            fun refreshClass(type: Class<*>) {
                refreshed = type
            }

            @JvmStatic
            fun refreshClass() = Unit
        }
    }

    object LegacyApi {
        var refreshed: Any? = null

        fun refreshClass(value: Any) {
            refreshed = value
        }
    }

    class UnsupportedApi {
        fun refreshClass(value: Int) = value

        companion object {
            @JvmStatic
            fun refreshClass(value: String) = value
        }
    }

    @Test
    fun supportsTheLegacyPanelsApi() {
        PanelsConfig.refresher(LegacyApi::class.java)(this)

        assertSame(this, LegacyApi.refreshed)
    }

    @Test
    fun supportsTheCurrentSlothPanelsApi() {
        PanelsConfig.refresher(CurrentApi::class.java)(this)

        assertSame(javaClass, CurrentApi.refreshed)
    }

    @Test
    fun rejectsAnUnsupportedPanelsApi() {
        val exception = runCatching {
            PanelsConfig.refresher(UnsupportedApi::class.java)
        }.exceptionOrNull()

        assertEquals("Panels refreshClass API is unavailable", exception?.message)
    }
}
