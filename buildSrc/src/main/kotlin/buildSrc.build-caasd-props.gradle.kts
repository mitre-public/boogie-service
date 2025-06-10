import java.io.IOException

fun runCommand(command: String): String {
    try {
        val parts = command.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(1, TimeUnit.SECONDS)
        return proc.inputStream.bufferedReader().readText().trim()
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
}

val caasdProps by tasks.registering {
    // always run if something depends on this -- we want the latest timestamp
    outputs.upToDateWhen { false }

    doLast {
        val propsDirPath = "${project.projectDir}/src/main/resources/META-INF/"
        val propsDirFile = File(propsDirPath)
        if (!propsDirFile.exists())
            propsDirFile.mkdirs()
        val propsFile = File("$propsDirPath/caasd.properties")
        if (propsFile.exists())
            propsFile.delete()
        propsFile.createNewFile()

        val props = java.util.Properties()
        props.setProperty("caasd.project.groupId", "${project.group}")
        props.setProperty("caasd.project.artifactId", project.name)
        props.setProperty("caasd.project.version", "${project.version}")

        val timeFormat = "yyyy-MM-dd HH:mm Z"
        props.setProperty("maven.build.timestamp.format", timeFormat)
        props.setProperty("build.timestamp.format", timeFormat)

        val buildInstant = java.time.Instant.now()
        val localDateTime = java.time.ZonedDateTime.ofInstant(buildInstant, java.time.ZoneOffset.UTC)
        val formattedTimestamp = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern(timeFormat))
        props.setProperty("caasd.project.timestamp", formattedTimestamp)

        val userName = System.getProperty("user.name") ?: "UNKNOWN"
        props.setProperty("caasd.project.builtBy", userName)


        // Check we have access to git before filling out caasd props git info
        if (!runCommand("git --version").isNullOrEmpty()) {
            props.setProperty("git.revision", runCommand("git rev-parse HEAD"))
            props.setProperty("git.commitDate", runCommand("git log -1 --format=%cd"))
            props.setProperty("git.shortRevision", runCommand("git rev-parse --short HEAD"))
            props.setProperty("git.branch", runCommand("git rev-parse --abbrev-ref HEAD"))
            props.setProperty("git.tag", runCommand("git tag --points-at HEAD"))
        }

        val outputStream = java.io.FileOutputStream(propsFile)
        props.store(outputStream, null)
    }
}
