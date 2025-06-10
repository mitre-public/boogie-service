pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}


dependencyResolutionManagement.versionCatalogs.create("buildLibs") {
    library("spring.boot", "org.springframework.boot", "spring-boot-gradle-plugin").version("3.1.3")
}
