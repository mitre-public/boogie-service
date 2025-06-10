plugins {
    `kotlin-dsl`
}

val ghprToken: String? = project.properties["ghprToken"]?.toString() ?: System.getenv("ghprToken")

repositories {
    gradlePluginPortal()
    maven {
        name = "mitre-public"
        url = uri("https://maven.pkg.github.com/mitre-public/*")
        credentials(HttpHeaderCredentials::class) {
            name = "Authorization"
            value = "Bearer ${ghprToken}"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

dependencies {
    api(buildLibs.spring.boot)
}
