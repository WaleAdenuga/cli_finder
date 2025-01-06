import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class FileFinderTest {

    private lateinit var tempDir: File

    @BeforeEach
    fun setUp() {
        /***
         * The test layout looks like
         *
         * tempDir/
         * -- cli_file.txt
         * -- cli_finder.txt
         * -- subDir/
         *    -- cli_text.txt
         *    -- search_string.txt
         * -- unique.txt
         *
         * OR
         *
         * tempDir/
         * -- cli_file.txt
         * -- cli_finder.txt
         * -- subDir/
         *    -- cli_text.txt
         *    -- search_string.txt
         * -- test.txt
         *
         */
        tempDir = Files.createTempDirectory("testDir").toFile()
    }

    @AfterEach
    fun tearDown() {
        // Delete the temporary directory and all its contents
        tempDir.deleteRecursively()
    }

    @Test
    fun testTempDirectoryIsProperlySetup() {
        println(tempDir.absolutePath)
        assertTrue(tempDir.exists())
        assertTrue(tempDir.isDirectory)
    }

    @Test
    fun testInvalidDirectoryPath() {
        val fileFinder = FileFinder("invalid", "arsenal")
        val result = fileFinder.searchByName()
        assertTrue(result.isEmpty())

        val result2 = fileFinder.searchByContent()
        assertTrue(result2.isEmpty())
    }

    @Test
    fun testValidDirectoryPathInvalidString() {
        val fileFinder = FileFinder(tempDir.absolutePath, "random:,:bvy")
        val result = fileFinder.searchByName()
        assertTrue(result.isEmpty())

        val result2 = fileFinder.searchByContent()
        assertTrue(result2.isEmpty())
    }

    @Test
    // Should return 0 for the search since the search string is not the name of the file created
    // Should return 1 for the search since the search string is the name of the file created
    // The content of the file contains the search_string, also make sure that it's not duplicated since the search is shallow
    fun testSearchShallowNonRecursiveFalse() {
        // Write a file to the tempDir - have the search string
        val file = tempDir.resolve("cli_file.txt")
        assertTrue(file.createNewFile(), "File should be created")
        assertTrue(file.exists(), "File should exist")
        file.writeText("This text is for the cli_finder")

        val fileFinder = FileFinder(tempDir.absolutePath, "cli_finder", "false", "shallow")
        var result = fileFinder.searchByName()
        assertEquals(0, result.size)

        val file2 = tempDir.resolve("cli_finder.txt")
        assertTrue(file2.createNewFile(), "File should be created")
        assertTrue(file2.exists(), "File should exist")
        file2.writeText("This text is for the cli_finder")

        result = fileFinder.searchByName()
        assertEquals(1, result.size)
    }

    @Test
    // Test to check recursive nature - verify that a subdirectory contains the search_string
    fun testSearchShallowRecursive() {
        // Write a file to the tempDir - have the search string
        val file = tempDir.resolve("cli_file.txt")
        assertTrue(file.createNewFile(), "File should be created")
        assertTrue(file.exists(), "File should exist")
        file.writeText("This text is for the cli_finder")

        val file2 = tempDir.resolve("cli_finder.txt")
        assertTrue(file2.createNewFile(), "File should be created")
        assertTrue(file2.exists(), "File should exist")
        file2.writeText("This text is for the cli_finder")

        // create subdirectory
        val directory1 = tempDir.resolve("subDir")
        assertTrue(directory1.mkdir(), "Subdirectory should be created successfully")
        assertTrue(directory1.exists(), "Subdirectory should exist")

        // Create file in subdirectory
        val file3 = directory1.resolve("cli_text.txt")
        assertTrue(file3.createNewFile(), "File should be created")
        assertTrue(file3.exists(), "File should exist")
        file3.writeText("This text contains search_string")

        val file4 = directory1.resolve("search_string.txt")
        assertTrue(file4.createNewFile(), "File should be created")
        assertTrue(file4.exists(), "File should exist")
        file4.writeText("This text contains search_string")

        val fileFinder = FileFinder(tempDir.absolutePath, "search_string", "true", "shallow")
        var result = fileFinder.searchByName()
        assertEquals(1, result.size)

        val file5 = tempDir.resolve("search_string.txt")
        assertTrue(file5.createNewFile(), "File should be created")
        assertTrue(file5.exists(), "File should exist")
        file5.writeText("This text contains search_string")

        result = fileFinder.searchByName()
        assertEquals(2, result.size)
    }

    // Searching deep looks at the contents (of text files) as well as the file names
    @Test
    fun testSearchDeepNonRecursive() {
        // Write a file to the tempDir - have the search string
        val file = tempDir.resolve("cli_file.txt")
        assertTrue(file.createNewFile(), "File should be created")
        assertTrue(file.exists(), "File should exist")
        file.writeText("This text contains search_string")

        val file2 = tempDir.resolve("cli_finder.txt")
        assertTrue(file2.createNewFile(), "File should be created")
        assertTrue(file2.exists(), "File should exist")
        file2.writeText("This text contains manchester")

        // create subdirectory
        val directory1 = tempDir.resolve("subDir")
        assertTrue(directory1.mkdir(), "Subdirectory should be created successfully")
        assertTrue(directory1.exists(), "Subdirectory should exist")

        // Create file in subdirectory
        val file3 = directory1.resolve("cli_text.txt")
        assertTrue(file3.createNewFile(), "File should be created")
        assertTrue(file3.exists(), "File should exist")
        file3.writeText("This text also contains manchester")

        val file4 = directory1.resolve("search_string.txt")
        assertTrue(file4.createNewFile(), "File should be created")
        assertTrue(file4.exists(), "File should exist")
        file4.writeText("This text also contains search_string")

        // Non-recursive test - searching deep should search both file names and their contents
        val fileFinder = FileFinder(tempDir.absolutePath, "cli", "false", "deep")
        var result = fileFinder.searchByContent()
        assertEquals(2, result.size) // first 2 files contain cli

        fileFinder.currentSearchString = "manchester"
        result = fileFinder.searchByContent()
        assertEquals(1, result.size)

        fileFinder.currentSearchString = "text"
        result = fileFinder.searchByContent()
        assertEquals(2, result.size)

        val file5 = tempDir.resolve("unique.txt")
        assertTrue(file5.createNewFile(), "File should be created")
        assertTrue(file5.exists(), "File should exist")
        file5.writeText("cli text contains unique")

        // Test uniqueness - let file name and text both contain the search _string
        fileFinder.currentSearchString = "unique"
        result = fileFinder.searchByContent()
        assertEquals(1, result.size)

        // Test uniqueness further - first 2 files start with cli in their file name, and the third file has cli in its content
        fileFinder.currentSearchString = "cli"
        result = fileFinder.searchByContent()
        assertEquals(3, result.size)
    }

    @Test
    fun testSearchDeepRecursive() {
        // Write a file to the tempDir - have the search string
        val file = tempDir.resolve("cli_file.txt")
        assertTrue(file.createNewFile(), "File should be created")
        assertTrue(file.exists(), "File should exist")
        file.writeText("This text contains search_string")

        val file2 = tempDir.resolve("cli_finder.txt")
        assertTrue(file2.createNewFile(), "File should be created")
        assertTrue(file2.exists(), "File should exist")
        file2.writeText("This text contains manchester")

        // create subdirectory
        val directory1 = tempDir.resolve("subDir")
        assertTrue(directory1.mkdir(), "Subdirectory should be created successfully")
        assertTrue(directory1.exists(), "Subdirectory should exist")

        // Create file in subdirectory
        val file3 = directory1.resolve("cli_text.txt")
        assertTrue(file3.createNewFile(), "File should be created")
        assertTrue(file3.exists(), "File should exist")
        file3.writeText("This text also contains manchester")

        val file4 = directory1.resolve("search_string.txt")
        assertTrue(file4.createNewFile(), "File should be created")
        assertTrue(file4.exists(), "File should exist")
        file4.writeText("This text also contains search_string")

        // Non-recursive test - searching deep should search both file names and their contents
        val fileFinder = FileFinder(tempDir.absolutePath, "cli", "true", "deep")
        var result = fileFinder.searchByContent()
        assertEquals(3, result.size) // first 2 files contain cli + one in subdirectory

        // Recursively deep search for manchester should return 2 files
        fileFinder.currentSearchString = "manchester"
        result = fileFinder.searchByContent()
        assertEquals(2, result.size)

        val file5 = directory1.resolve("test.txt")
        assertTrue(file5.createNewFile(), "File should be created")
        assertTrue(file5.exists(), "File should exist")
        file5.writeText("This text contains cli and test")

        // search for cli should return 4 files - 2 files names in temp, 1 file name in subDir and 1 in content of subDir
        fileFinder.currentSearchString = "cli"
        result = fileFinder.searchByContent()
        assertEquals(4, result.size)

        // "test" appears both as a file name and part of the content in a subDir, a search should return 1 result
        println("========================")
        fileFinder.currentSearchString = "test"
        result = fileFinder.searchByContent()
        assertEquals(1, result.size)
    }


}