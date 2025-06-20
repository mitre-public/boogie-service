plugins {
    `maven-publish`
}
group = "org.mitre.tdp"

tasks.withType<GenerateModuleMetadata> {
    suppressedValidationErrors.add("enforced-platform")
}

applyPublishing("java")

fun Project.applyPublishing(componentName: String) {
    publishing {
        publications {
            create<MavenPublication>(project.name) {
                artifactId = project.name
                from(components[componentName])

                pom {
                    name.set(project.name)
                    organization {
                        name.set("The MITRE Corporation")
                        url.set("https://github.com/mitre-public")
                    }
                    scm {
                        connection.set("scm:git:ssh://git@github.com:mitre-public/boogie-service.git")
                        developerConnection.set("scm:git:ssh://git@github.com:mitre-public/boogie-service.git")
                        url.set("https://github.com/mitre-public/boogie-service")
                        tag.set("HEAD")
                    }
                }
            }
        }

        val ghprToken: String? = project.properties["ghprToken"]?.toString() ?: System.getenv("ghprToken")
        repositories {
            maven {
                name = "boogie-service"
                url = uri("https://maven.pkg.github.com/mitre-public/boogie-service")
                credentials(HttpHeaderCredentials::class) {
                    name = "Authorization"
                    value = "Bearer ${ghprToken ?: System.getenv("ghprToken")}"
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
}
