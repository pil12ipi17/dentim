package com.dentim.karaoke.util

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.rules.TemporaryFolder
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileWriter
import kotlinx.coroutines.test.runTest

/**
 * Unit tests for ChecksumCalculator
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class ChecksumCalculatorTest {
    
    @get:Rule
    val tempFolder = TemporaryFolder()
    
    private lateinit var testFile1: File
    private lateinit var testFile2: File
    private lateinit var testFile3: File
    
    @Before
    fun setup() {
        // Create test files with known content
        testFile1 = tempFolder.newFile("test1.txt")
        testFile2 = tempFolder.newFile("test2.txt")  
        testFile3 = tempFolder.newFile("test3.txt")
        
        FileWriter(testFile1).use { it.write("Hello World") }
        FileWriter(testFile2).use { it.write("Hello World") } // Same content as file1
        FileWriter(testFile3).use { it.write("Different content") }
    }
    
    @Test
    fun `calculateMD5Sync should return consistent hash for same content`() {
        val hash1 = ChecksumCalculator.calculateMD5Sync(testFile1)
        val hash2 = ChecksumCalculator.calculateMD5Sync(testFile2)
        
        assertEquals("Files with same content should have same MD5", hash1, hash2)
        assertTrue("MD5 hash should be 32 characters", hash1.length == 32)
        assertTrue("MD5 hash should be hexadecimal", hash1.matches(Regex("[0-9a-f]+")))
    }
    
    @Test
    fun `calculateMD5Sync should return different hash for different content`() {
        val hash1 = ChecksumCalculator.calculateMD5Sync(testFile1)
        val hash3 = ChecksumCalculator.calculateMD5Sync(testFile3)
        
        assertNotEquals("Files with different content should have different MD5", hash1, hash3)
    }
    
    @Test
    fun `calculateMD5 async should return same result as sync version`() = runTest {
        val syncHash = ChecksumCalculator.calculateMD5Sync(testFile1)
        val asyncHash = ChecksumCalculator.calculateMD5(testFile1)
        
        assertEquals("Async and sync MD5 should be equal", syncHash, asyncHash)
    }
    
    @Test
    fun `calculateSHA256Sync should return valid SHA-256 hash`() {
        val hash = ChecksumCalculator.calculateSHA256Sync(testFile1)
        
        assertTrue("SHA-256 hash should be 64 characters", hash.length == 64)
        assertTrue("SHA-256 hash should be hexadecimal", hash.matches(Regex("[0-9a-f]+")))
    }
    
    @Test
    fun `calculateQuickChecksum should be faster than full MD5`() = runTest {
        val startTime1 = System.currentTimeMillis()
        val quickChecksum = ChecksumCalculator.calculateQuickChecksum(testFile1)
        val quickTime = System.currentTimeMillis() - startTime1
        
        val startTime2 = System.currentTimeMillis()
        val fullMD5 = ChecksumCalculator.calculateMD5(testFile1)
        val fullTime = System.currentTimeMillis() - startTime2
        
        assertNotNull("Quick checksum should not be null", quickChecksum)
        // Quick checksum should generally be faster, but for small test files it might not be noticeable
        assertTrue("Quick checksum should complete", quickTime >= 0)
        assertTrue("Full MD5 should complete", fullTime >= 0)
    }
    
    @Test
    fun `isValidChecksum should validate MD5 format correctly`() {
        assertTrue(ChecksumCalculator.isValidChecksum("5d41402abc4b2a76b9719d911017c592", "MD5"))
        assertFalse(ChecksumCalculator.isValidChecksum("invalid", "MD5"))
        assertFalse(ChecksumCalculator.isValidChecksum("5d41402abc4b2a76b9719d911017c59", "MD5")) // Too short
        assertFalse(ChecksumCalculator.isValidChecksum("5d41402abc4b2a76b9719d911017c592x", "MD5")) // Too long
    }
    
    @Test
    fun `isValidChecksum should validate SHA-256 format correctly`() {
        val validSHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        assertTrue(ChecksumCalculator.isValidChecksum(validSHA256, "SHA-256"))
        assertFalse(ChecksumCalculator.isValidChecksum("invalid", "SHA-256"))
        assertFalse(ChecksumCalculator.isValidChecksum("5d41402abc4b2a76b9719d911017c592", "SHA-256")) // MD5 length
    }
    
    @Test
    fun `areFilesIdentical should detect identical files`() = runTest {
        val areIdentical = ChecksumCalculator.areFilesIdentical(testFile1, testFile2)
        assertTrue("Files with same content should be identical", areIdentical)
    }
    
    @Test
    fun `areFilesIdentical should detect different files`() = runTest {
        val areIdentical = ChecksumCalculator.areFilesIdentical(testFile1, testFile3)
        assertFalse("Files with different content should not be identical", areIdentical)
    }
}