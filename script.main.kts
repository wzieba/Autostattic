@file:Repository("https://repo.kotlin.link")
@file:DependsOn("com.squareup.moshi:moshi-kotlin:1.12.0")
@file:DependsOn("com.squareup.moshi:moshi-adapters:1.12.0")
@file:DependsOn("com.lordcodes.turtle:turtle:0.5.0")
@file:CompilerOptions("-jvm-target", "11")

import com.lordcodes.turtle.shellRun
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

println("Setting up Init Gradle Plugins")

shellRun {
    val gradleInitDirectory = "/home/runner/.gradle/init.d/"

    command("mkdir", listOf("-p", gradleInitDirectory))
    command("cp", listOf("add-versions-plugin.gradle", gradleInitDirectory))
    println(command("ls", listOf("-la", gradleInitDirectory)))
    ""
}

println("Repository configuration")

val REPOSITORY_URL_ARGUMENT_ORDER = 0
val REPOSITORY_URL = args[REPOSITORY_URL_ARGUMENT_ORDER]
val REPOSITORY_NAME = REPOSITORY_URL.substring(REPOSITORY_URL.lastIndexOf('/') + 1)
val REPOSITORY_DIR = "repo"

shellRun {
    git.clone(REPOSITORY_URL, REPOSITORY_DIR)

    changeWorkingDirectory(REPOSITORY_DIR)

    if (File("$REPOSITORY_DIR/gradle.properties-example").exists()) {
        command("cp", listOf("gradle.properties-example", "gradle.properties"))
    }

    if (File("$REPOSITORY_DIR/local.properties-example").exists()) {
        command("cp", listOf("local.properties-example", "local.properties"))
    }
    ""
}

println("Generating report")

val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .appendLiteral("/")
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral("/")
    .appendValue(ChronoField.YEAR)
    .toFormatter()

val STATS_FILE = File("$REPOSITORY_NAME.csv")

if (STATS_FILE.exists().not()) {
    STATS_FILE.apply {
        createNewFile()
        writeText("date,outdated dependencies,all dependencies,kotlin lines,java lines")
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

shellRun {
    changeWorkingDirectory(REPOSITORY_DIR)

    println(command("git", listOf("show", "--summary")))

    val clocReportFileName = "cloc.json"
    println(command("cloc", listOf("--json", "--include-lang=Kotlin,Java", "--report-file=$clocReportFileName", ".")))
    val clocReport = File("$REPOSITORY_DIR/$clocReportFileName").readText()
    val clocContainer = clocContainerAdapter.fromJson(clocReport)!!

    println(command("./gradlew", listOf("dependencyUpdate", "--console=plain", "--refresh-dependencies")))
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
                    .toString()
            )
        }
    }
    ""
}


println("Committing stats update")

shellRun {
    println(command("cat", listOf("$STATS_FILE")))
    command("git", listOf("pull"))
    command("git", listOf("add", "-A"))
    command("git", listOf("config", "--local", "user.email", "\"action@github.com\""))
    command("git", listOf("config", "--local", "user.name", "\"Github Action\""))
    command(
        "git",
        listOf("commit", "-m", "Report update for $REPOSITORY_NAME on ${DATE_FORMATTER.format(LocalDate.now())}")
    )
    command("git", listOf("push"))
    ""
}
