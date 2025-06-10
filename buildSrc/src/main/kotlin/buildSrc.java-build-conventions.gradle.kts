plugins {
    java
    id("buildSrc.build-caasd-props")
    id("org.springframework.boot")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    systemProperty("log4j2.configurationFile", "log4j2-cda.xml")
}

tasks.named<Test>("test") {
    systemProperty("log4j2.configurationFile", "log4j2-cda.xml")

    useJUnitPlatform {
        includeEngines("junit-jupiter")
        excludeEngines("junit-vintage")

        // ./gradlew test -pincludeIntegration
        unless(project.hasProperty("includeIntegration")) {
            excludeTags("INTEGRATION")
        }
    }

    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    reports.html.required.set(true)
    reports.junitXml.required.set(true)

    jvmArgs = listOf(
        // CDA and its dependencies add a lot of classpath bloat, add some space
        // to store the class references at compile/test time
        "-Djava.security.manager=allow",
        "-Xmx768m",
        "-XX:MaxMetaspaceSize=512m",
        // Open all the JDK17-sealed classes Spark/Crunch and friends are using
        // This need to also be supplied to the JVM when running jobs using CDA
        "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

tasks.getByName("caasdProps") {
    dependsOn("compileJava")
}

inline fun <T> unless(bool: Boolean, block: () -> T): T? = if (!bool) block() else null