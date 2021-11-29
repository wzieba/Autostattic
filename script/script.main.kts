@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("com.squareup.moshi:moshi-kotlin:1.12.0")
@file:DependsOn("com.squareup.moshi:moshi-adapters:1.12.0")
@file:DependsOn("eu.jrie.jetbrains:kotlin-shell-kts:0.2.1")
@file:CompilerOptions("-jvm-target", "11")
@file:Suppress("EXPERIMENTAL_API_USAGE")

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import eu.jrie.jetbrains.kotlinshell.shell.shell
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

val REPOSITORY_URL_ARGUMENT_ORDER = 0
val COMPILE_TASK_ARGUMENT_ORDER = 1
val REPOSITORY_URL = args[REPOSITORY_URL_ARGUMENT_ORDER]
val REPOSITORY_NAME = REPOSITORY_URL.substring(REPOSITORY_URL.lastIndexOf('/') + 1)
val REPOSITORY_DIR = "repo"

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .appendLiteral("/")
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral("/")
    .appendValue(ChronoField.YEAR)
    .toFormatter()

val STATS_FILE = File("../results/$REPOSITORY_NAME.csv")

if (STATS_FILE.exists().not()) {
    STATS_FILE.apply {
        createNewFile()
        writeText("date,outdated dependencies,all dependencies,kotlin lines,java lines,kotlin compiler warnings")
    }
}

val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

val LocalDateTime.asString: String
    get() = format(DATE_FORMATTER)

fun Long?.orEmpty() = this?.toString().orEmpty()

val dependenciesContainerAdapter: JsonAdapter<DependenciesContainer> = moshi.adapter(DependenciesContainer::class.java)

data class DependenciesContainer(val count: Int, val outdated: Outdated)
data class Outdated(val dependencies: List<Dependency>)
data class Dependency(val group: String, val name: String, val version: String) {
    val id = "$group:$name"
}

data class ClocResult(@Json(name = "code") val codeLines: Long)
data class ClocContainer(@Json(name = "Kotlin") val kotlin: ClocResult?, @Json(name = "Java") val java: ClocResult?)

val clocContainerAdapter: JsonAdapter<ClocContainer> = moshi.adapter(ClocContainer::class.java)

System.setProperty("java.awt.headless", "false")

val today: LocalDateTime = LocalDateTime.now()

shell {
    println("Setting up Init Gradle Plugins")
    val gradleInitDirectory = "/home/runner/.gradle/init.d/"

    "mkdir -p $gradleInitDirectory"()
    "cp add-versions-plugin.gradle $gradleInitDirectory"()
    "ls -ls $gradleInitDirectory"()

    println("Repository configuration")

    "git clone $REPOSITORY_URL $REPOSITORY_DIR"()

    cd(REPOSITORY_DIR)

    if (File("$REPOSITORY_DIR/gradle.properties-example").exists()) {
        "cp gradle.properties-example gradle.properties"()
    }

    if (File("$REPOSITORY_DIR/local.properties-example").exists()) {
        "cp local.properties-example local.properties"()
    }

    "ls"()

    "git show --summary"()

    println("Generating report")

    val clocReportFileName = "cloc.json"
    "cloc --json --include-lang=Kotlin,Java --report-file=$clocReportFileName ."()
    val clocReport = File("$REPOSITORY_DIR/$clocReportFileName").readText()
    val clocContainer = clocContainerAdapter.fromJson(clocReport)!!

    val out = StringBuilder()
    pipeline {
        "./gradlew ${args[COMPILE_TASK_ARGUMENT_ORDER]} --console=plain --rerun-tasks".process() pipe stringLambda {
            print(it)
            it to ""
        } pipe out
    }
    val kotlinCompilerWarningLines: Set<String> = out.toString().split("\n").filter { it.startsWith("w:") }.toSet()
    println("Counted ${kotlinCompilerWarningLines.size} Kotlin compiler warnings")

    "./gradlew dependencyUpdate --console=plain --refresh-dependencies"()

    val dependencyReport = File("$REPOSITORY_DIR/report.json").readText()
    val dependenciesContainer = dependenciesContainerAdapter.fromJson(dependencyReport)!!

    STATS_FILE.apply {
        if (STATS_FILE.readText().contains(today.asString).not()) {
            appendText(
                StringBuilder()
                    .append("\n")
                    .append(today.asString)
                    .append(",")
                    .append(dependenciesContainer.outdated.dependencies.size)
                    .append(",")
                    .append(dependenciesContainer.count)
                    .append(",")
                    .append(clocContainer.kotlin?.codeLines.orEmpty())
                    .append(",")
                    .append(clocContainer.java?.codeLines.orEmpty())
                    .append(",")
                    .append(kotlinCompilerWarningLines.size)
                    .toString()
            )
        }
    }
    "cat $STATS_FILE"()
}
