package org.firstinspires.ftc.threedrd.ftc

import java.io.*
import java.nio.file.*
import org.junit.Assert.*
import org.junit.*

class StorageTests {
    @Test
    fun readsExistingSettingsAndIgnoresMissingSettings() {
        val directory = Files.createTempDirectory("storage-tests").toFile()
        val file = directory.resolve("settings/config.json")
        val storage = Storage(file)

        assertNull(storage.read())
        storage.write("settings")
        assertEquals(true, file.parentFile.isDirectory)
        assertEquals("settings", storage.read())
        assertEquals(file, storage.file)
        assertEquals(
            "${File.separator}sdcard${File.separator}FIRST${File.separator}settings${File.separator}config.json",
            Storage("config.json").file.path
        )
    }
}
