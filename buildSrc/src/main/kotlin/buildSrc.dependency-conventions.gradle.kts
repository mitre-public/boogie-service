plugins {
    `java-library`
}
val ghprToken: String? = project.properties["ghprToken"]?.toString() ?: System.getenv("ghprToken")

repositories {
    mavenCentral()
    maven {
        name = "cloudera-libs"
        url = uri("https://repository.cloudera.com/artifactory/libs-release-local/")
    }
    maven {
        name = "mitre-public"
        url = uri("https://maven.pkg.github.com/mitre-public/*")
        credentials(HttpHeaderCredentials::class) {
            name = "Authorization"
            value = "Bearer ${ghprToken ?: System.getenv("ghprToken")}"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}