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
        writeText("date,outdated dependencies,all dependencies")
    }
}

val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

val LocalDateTime.asString: String
    get() = format(DATE_FORMATTER)

data class DependenciesContainer(
    val count: Int,
    val outdated: Outdated,
)

val dependenciesContainerAdapter: JsonAdapter<DependenciesContainer> = moshi.adapter(DependenciesContainer::class.java)

data class Outdated(val dependencies: List<Dependency>)

data class Dependency(
    val group: String,
    val name: String,
    val version: String,
) {
    val id = "$group:$name"
}

System.setProperty("java.awt.headless", "false")

val today: LocalDateTime = LocalDateTime.now()

shellRun {
    changeWorkingDirectory(REPOSITORY_DIR)

    val hash = git.gitCommand(listOf("log", "--before=\"${today.asString}\"", "--format=\"%H\"", "-1"))

    println("Hash for ${today.asString} $hash")

    git.checkout(hash)

    println(command("git", listOf("status")))

    println(command("./gradlew", listOf("dependencyUpdate", "--console=plain", "--refresh-dependencies")))

    val report = File("$REPOSITORY_DIR/report.json").readText()
    val container = dependenciesContainerAdapter.fromJson(report)!!

    STATS_FILE.apply {
        if (STATS_FILE.readText().contains(today.asString).not()) {
            appendText("\n${today.asString},${container.outdated.dependencies.size},${container.count}")
        }
    }
    ""
}


println("Committing stats update")

shellRun {
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
