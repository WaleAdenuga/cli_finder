import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

class FileFinder {

    fun searchByName(directoryPath: String, searchString: String, recursiveEnabled: Boolean = true) {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            println("Invalid directory path, does it exist?")
            // directory isn't valid
        }

        val allFiles = directory.walkTopDown().filter { it.isFile }.toList()
        val filesSize = allFiles.size
        println("Total number in directory: $filesSize")
        var currentIndex = 0

        directory.listFiles()?.forEach {
            if (it.isDirectory) {
                if (recursiveEnabled)searchByName(it.absolutePath, searchString)
            } else if (it.name.contains(searchString, ignoreCase = true)) {
                println(it.absolutePath)
            }
        }
    }

    fun searchByContent(directoryPath: String, searchString: String, recursiveEnabled: Boolean = true) {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            println("Invalid directory path, does it exist?")
            // directory isn't valid
        }

        directory.listFiles()?.forEach {
            if (it.isDirectory) {
                if (recursiveEnabled)searchByContent(it.absolutePath, searchString)
            } else {
                // will read entire file into a single string

                if (it.name.contains(searchString, ignoreCase = true)) {
                    println(it.absolutePath)
                }
                if (it.extension == "txt" || it.extension == "md" || it.extension == "xml") {

                    try {
                        val content = it.readText()
                        if (content.contains(searchString, ignoreCase = true)) {
                            println(it.absolutePath)
                        }
                    } catch (e: Exception) {
                        println("Error reading from: ${it.absolutePath}")
                    }
                }
            }
        }
    }

    fun printProgressBar(percentage: Int) {
        val progressBarWidth = 50
        val completed = (percentage * progressBarWidth) / 100
        val bar = "=".repeat(completed) + " ".repeat(progressBarWidth - completed)
        print("\r|$bar| $percentage%")
    }

}