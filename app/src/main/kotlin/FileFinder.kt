import java.io.File
import kotlin.system.exitProcess

class FileFinder {

    fun searchByName(directoryPath: String, searchString: String, recursiveEnabled: Boolean = true) {
        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            println("Invalid directory path, does it exist?")
            // directory isn't valid
        }

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

fun main(args: Array<String>) {
    val fileFinder = FileFinder()
    print("> ")
    if (args.isEmpty() || args.size < 2 || args.size > 4) printUsageAndExit()

    val directoryPath = args[0]
    val searchString = args[1]
    val searchRecursive = if (args.size >= 3) args[2] else "--true"
    val searchFormat = if (args.size >= 4) args[3] else "--shallow"

    // Handle recursive searching
    val recursiveEnabled = when (searchRecursive.lowercase().split("--")[1]) {
        "true" -> true
        "false" -> false
        else -> true
    }

    // Handle search format
    val searchDeep = when (searchFormat.lowercase().split("--")[1]) {
        "deep" -> true
        "shallow" -> false
        else -> false
    }

    // Handle directory path -> where to search
    val currentDirectoryPath = when(directoryPath.lowercase()) {
        "." -> { // treat as current directory
            File(System.getProperty("user.dir")).absolutePath
        }
        else -> {
            // call FileFinder class to handle this
            directoryPath
        }
    }

    println("Searching for \"$searchString\" in $currentDirectoryPath")

    when (searchDeep) {
        true -> fileFinder.searchByContent(currentDirectoryPath, searchString, recursiveEnabled)
        false -> fileFinder.searchByName(currentDirectoryPath, searchString, recursiveEnabled)
    }
}

fun printUsageAndExit() {
    println("Usage: tscfinder <directory_path> <search_value> [options] [search_form]")
    println("Use this tool to find files that match the provided file name")
    println("""
         search_form: --shallow: search each file in the directory by name and see if it matches
                      --deep: go within each text file to see if the content contains your search_value
                      default is shallow
         
         options:     --true: search recursively (within subdirectories) ==> might take extra time
                      --false: don't search recursively
                      default is true
    """.trimIndent())
    println("""
        ================================
        Example: tscfinder . node --true --shallow
        tscfinder would search the current directory's file names recursively for "node"
        ================================
        ==> tscfinder "C:\Users\name\Downloads" persona --false --deep
        tscfinder would search the contents of the text files in provided directory path non-recursively for "persona"
    """.trimIndent())

    // exit with 0 indicating successful termination
    exitProcess(0)
}