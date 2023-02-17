package sorting


import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.io.Reader
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.Scanner
import kotlin.system.exitProcess

val modes = mutableMapOf<String, Regex>(
    "long" to  """-?\d+""".toRegex(),
    "line" to """.+""".toRegex(),
    "word" to """-?\w+""".toRegex())

val modesPrints = mutableMapOf<String, String>(
    "long" to "Total numbers: ",
    "line" to "Total lines: ",
    "word" to "Total words: "
)


fun MutableList<Long>.max(): Long? {
    if (this.isEmpty()) {
        return null
    }

    var max = this[0]
    for (i in 1..this.lastIndex) {
        if (this[i] > max) {
            max = this[i]
        }
    }
    return max
}

fun MutableList<String>.maxBy(relation: (input: String) -> Int): String? {
    if (this.isEmpty()) {
        return null
    }

    var max = this[0]
    for (i in 1..this.lastIndex) {
        if (relation(this[i]) > relation(max)) {
            max = this[i]
        }
    }
    return max
}

fun detectValues(mode: String, stream: InputStream = System.`in`): MutableList<String> {
    val regex = modes[mode]
    val scanner = Scanner(stream)
    val values = mutableListOf<String>()


    val notLongs = mutableListOf<String>()
    val notLongsRegex = """[a-zA-Z]+""".toRegex()
    while (scanner.hasNext()) {
        val input = scanner.nextLine()

        if (mode == "long") {
            val line = notLongsRegex.findAll(input)
            for (n in line) {
                notLongs.add(n.value)
            }
        }

        if (mode != "line") {
            val line = regex?.findAll(input)
            if (line != null) {
                for (n in line) {
                    values.add(n.value)
                }
            }
        } else {
            values.add(input)
        }


    }
    //Check that no word is passed
    if (mode == "long") {
        for (n in notLongs) {
            println("\"${n}\" is not a long. It will be skipped.")
        }
    }
    scanner.close()
    return values
}


// SORT BY COUNT -> order elements by frequency
fun <T> sortByCount(values: MutableList<T>, mode: String, outputStream: PrintStream = System.out) {
    val mapFrequency = values.groupBy { it }.mapValues { it.value.size }
    val sorted = mergeSort(values) { el1: T, el2: T ->
        if (mapFrequency[el1]!! == mapFrequency[el2]!!) {
            // How to break ties
            if (el1 is Long && el2 is Long) {
                return@mergeSort el1 <= el2
            } else if (el1 is String && el2 is String) {
                return@mergeSort lexOrderRelation(el1,el2)
            }
        }
        mapFrequency[el1]!! < mapFrequency[el2]!! }
    outputStream.println(modesPrints[mode] + "${values.size}.")
    sorted.distinct().forEach {
        outputStream.println("$it: ${mapFrequency[it]} time(s), ${(mapFrequency[it]?.times(100) ?: 0) /values.size}%")
    }
    outputStream.close()
}

fun sortNumbersNaturally(values: MutableList<Long>, mode: String, outputStream: PrintStream = System.out) {
    outputStream.println(modesPrints[mode] + "${values.size}.")
    val sorted = mergeSort(values) { el1: Long, el2: Long -> el1 <= el2 }
    outputStream.println("Sorted data: ${sorted.joinToString(" ")}")
    outputStream.close()
}

fun sortStringsNaturally(values: MutableList<String>, mode: String, outputStream: PrintStream = System.out) {
    outputStream.println(modesPrints[mode] + "${values.size}.")
    val sorted = mergeSort(values) { el1: String, el2: String -> lexOrderRelation(el1,el2) }
    sorted.forEach {
        if (mode == "line") {
            outputStream.println(it)
        } else if (mode == "word") {
            outputStream.print("$it ")
        }
    }
    outputStream.println()
    outputStream.close()
}

// Relations are ordering relations, in particular:
// LEX ORDER -> order string by lexicographic order (ascending 'a' first)
fun lexOrderRelation(string1: String, string2: String): Boolean {
    var i = 0
    while (i <= string1.lastIndex && i <= string2.lastIndex) {
        if (string1[i] < string2[i]) {
            return true
        } else if (string1[i] > string2[i]) {
            return false
        }
        i++
    }
    if (string1.length < string2.length) {
        return true
    }
    return false
}

fun <T> merge(array1: MutableList<T>, array2: MutableList<T>, relation: (T, T) -> Boolean): MutableList<T> {
    val result = mutableListOf<T>()

    var ind1 = 0
    var ind2 = 0

    while (ind1 <= array1.lastIndex && ind2 <= array2.lastIndex) {
        if (relation(array1[ind1], array2[ind2])) {
            result.add(array1[ind1])
            ind1++
        } else {
            result.add(array2[ind2])
            ind2++
        }
    }

    for (i in ind1..array1.lastIndex) {
        result.add(array1[i])
    }

    for (i in ind2..array2.lastIndex) {
        result.add(array2[i])
    }
    return result
}

fun <T> mergeSort(array: MutableList<T>, relation: (T, T) -> Boolean): MutableList<T> {
    if (array.size == 2) {
        if (relation(array[0], array[1])) {
            return mutableListOf(array[0], array[1])
        } else {
            return mutableListOf(array[1], array[0])
        }
    }

    if (array.size == 1) return array

    val middle = array.size / 2

    return merge(mergeSort(array.take(middle).toMutableList(), relation),
                 mergeSort(array.drop(middle).toMutableList(), relation), relation)
}





fun main(args: Array<String>) {
    val validArgs = setOf<String>("-sortingType", "-dataType","-inputFile", "-outputFile")
    val sortingModes = setOf<String>("natural", "byCount")
    val permittedTypes = setOf<String>("long", "word", "line")
    // write your code here
    var configuration = Pair<String, String>("natural", "word")
    var inputStream = System.`in`
    var outputStream = System.`out`



    if (args.isNotEmpty()) {
        if (args.contains("-sortingType")) {
            try {
                if (args[args.indexOf("-sortingType") + 1] in sortingModes) {
                    configuration = Pair(args[args.indexOf("-sortingType") + 1], configuration.second)
                }
            } catch (e: Exception) {
                println("No sorting type defined!")
                exitProcess(1)
            }
        }

        if (args.contains("-dataType")) {
            try {
                if (args[args.indexOf("-dataType") + 1] in permittedTypes) {
                    configuration = Pair(configuration.first, args[args.indexOf("-dataType") + 1])
                }
            } catch (e: Exception) {
                println("No data type defined!")
                exitProcess(1)
            }
        }

        for (arg in args) {
            if (arg !in validArgs && arg.first() == '-') {
                println("\"${arg}\" is not a valid parameter. It will be skipped.")
            }
        }

        if (args.contains("-inputFile")) {
            inputStream = FileInputStream(File(args[args.indexOf("-inputFile") + 1]))
        }

        if (args.contains("-outputFile")) {
            outputStream = PrintStream(File(args[args.indexOf("-outputFile") + 1]))
        }
    }


    val numbers: MutableList<Long>
    val values = detectValues(configuration.second, inputStream)

    if (configuration.first == "byCount") {
        if (configuration.second == "long") {
            numbers = values.map { it.toLong() }.toMutableList()
            sortByCount(numbers, configuration.second, outputStream)
        } else {
            sortByCount(values, configuration.second, outputStream)
        }
    } else if (configuration.first == "natural") {
        when (configuration.second) {
            "long" -> {
                numbers = values.map { it.toLong() }.toMutableList()
                sortNumbersNaturally(numbers, configuration.second, outputStream)
            }
            else -> sortStringsNaturally(values, configuration.second, outputStream)
        }
    }


}