import java.io.File
import kotlin.system.exitProcess

class FileFinder(
    directoryPath: String,
    searchString: String,
    private var searchRecursive: String = "--true", private var searchFormat:String = "--shallow") {

    // The default nature for recursion is true
    private var recursiveEnabled: Boolean = true
    // The default nature for file content searching is false
    var searchDeep: Boolean = false
    private var currentDirectoryPath: String = ""
    var currentSearchString: String = ""

    // For mutableSet of results
    private var contentSearchResults: MutableSet<File> = mutableSetOf()

    init {
        // Handle recursive searching
        searchRecursive = if (searchRecursive.lowercase().startsWith("--"))  {
            searchRecursive.split("--")[1]
        } else searchRecursive.lowercase()

        recursiveEnabled = when (searchRecursive) {
            "true" -> true
            "false" -> false
            else -> true
        }

        // Handle search format
        searchFormat = if (searchFormat.lowercase().startsWith("--")) {
            searchFormat.split("--")[1]
        } else searchFormat.lowercase()

        searchDeep = when (searchFormat) {
            "deep" -> true
            "shallow" -> false
            else -> false
        }

        // Handle directory path -> where to search
        currentDirectoryPath = when(directoryPath.lowercase()) {
            "." -> { // treat as current directory
                File(System.getProperty("user.dir")).absolutePath
            }
            else -> {
                // call FileFinder class to handle this
                directoryPath
            }
        }

        currentSearchString = searchString

        println("Searching for \"$currentSearchString\" in $currentDirectoryPath")

    }

    fun searchByName() : Set<File> {
        val result = mutableSetOf<File>()
        val directory = File(currentDirectoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            println("Invalid directory path, does it exist?")
            // directory isn't valid
            return emptySet()
        }

        if (currentSearchString.isEmpty()) return emptySet()

        if (recursiveEnabled) {
            directory.walkTopDown().forEach {
                if (it.isFile && it.name.contains(currentSearchString, ignoreCase = true)) {
                    result.add(it)
                    println(it.absolutePath)
                }
            }
        } else {
            directory.listFiles()?.forEach {
                if (it.isFile && it.name.contains(currentSearchString, ignoreCase = true)) {
                    println(it.absolutePath)
                    result.add(it)
                }
            }
        }
        return result
    }

    fun searchByContent(): Set<File> {
        // Clear the results at the beginning so they remain consistent
        contentSearchResults.clear()

        val directory = File(currentDirectoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            println("Invalid directory path, does it exist?")
            // directory isn't valid
            return emptySet()
        }

        if (currentSearchString.isEmpty()) return emptySet()

        if (recursiveEnabled) {
            directory.walkTopDown().forEach {
                if (it.isFile) privateContentSearch(it)
            }
        } else {
            directory.listFiles()?.forEach {
                if (it.isFile) privateContentSearch(it)
            }
        }

        return contentSearchResults
    }

    private fun privateContentSearch(file: File) {
        if (file.name.contains(currentSearchString, ignoreCase = true)) {
            println(file.absolutePath)
            contentSearchResults.add(file)
        } else if (file.extension == "txt" || file.extension == "md" || file.extension == "xml") {

            try {
                val content = file.readText()
                if (content.contains(currentSearchString, ignoreCase = true)) {
                    println(file.absolutePath)
                    contentSearchResults.add(file)
                }
            } catch (e: Exception) {
                println("Error reading from: ${file.absolutePath}")
            }
        }
    }
}

fun main(args: Array<String>) {

    print("> ")
    if (args.isEmpty() || args.size < 2 || args.size > 4) printUsageAndExit()

    val directoryPath = args[0]
    val searchString = args[1]
    val searchRecursive = if (args.size >= 3) args[2] else "--true"
    val searchFormat = if (args.size >= 4) args[3] else "--shallow"


    val fileFinder = FileFinder(directoryPath, searchString, searchRecursive, searchFormat)

    when (fileFinder.searchDeep) {
        true -> fileFinder.searchByContent()
        false -> fileFinder.searchByName()
    }
}

fun printUsageAndExit() {
    println("Usage: cli_finder <directory_path> <search_value> [options] [search_form]")
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
        Example: cli_finder . node --true --shallow
        cli_finder would search the current directory's file names recursively for "node"
        ================================
        ==> cli_finder "C:\Users\name\Downloads" persona --false --deep
        cli_finder would search the contents of the text files in provided directory path non-recursively for "persona"
    """.trimIndent())

    // exit with 0 indicating successful termination
    exitProcess(0)
}