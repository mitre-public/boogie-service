# Runtime image for boogie-service on a Docker Hardened Image (DHI) base — tdp-roadmap #529.
# Follows the merged maestro DHI pattern (mitre-tdp/maestro#812).
#
# The runtime base is the DHI eclipse-temurin Java 21 (alpine) image, pinned by digest. It is
# distroless/nonroot: no shell, no keytool, no update-ca-trust. Renovate's dockerfile manager
# tracks the tag@digest pairs below.
#
#   - runtime : DHI eclipse-temurin 21 (alpine), pinned by digest. Distroless, fixed nonroot user.
#   - certs   : DHI eclipse-temurin 21 JDK dev (has a shell + keytool), used only to bake the MITRE
#               CA roots into a temurin truststore. Not shipped; the PKCS12/JKS truststore it emits
#               is portable to the runtime (same temurin 21).
#
# NOTE(dhi): the pre-DHI base (eclipse-temurin:21-jre-alpine) did NOT seed the MITRE CA roots — it
#   shipped only the public temurin truststore. The cert stage below ADDS the MITRE roots (matching
#   the maestro pattern). This is a net improvement and is required for any outbound TLS to
#   MITRE-internal endpoints; it does not regress prior behavior for public endpoints.
ARG TEMURIN_JDK_VERSION=21-jdk-alpine3.24-dev
ARG TEMURIN_JDK_DIGEST=sha256:b60eaaef4af660b6b9476c612b77e3c8c998a166869797f53f444c7a5cc14f82

ARG BUILD_INIT=harbor.cre.gov.aws.mitre.org/cre/docker-init:20260429.1415.47-adc03cd-mitre
FROM $BUILD_INIT AS docker-init

# ---- cert stage: bake the MITRE CA roots into a temurin truststore --------------------------------
# The distroless runtime has no shell/keytool/update-ca-trust, so seed a truststore from this JDK's
# cacerts (public roots) and add the docker-init MITRE roots here, then COPY it into the runtime.
FROM harbor.cre.gov.aws.mitre.org/dhi/eclipse-temurin:${TEMURIN_JDK_VERSION}@${TEMURIN_JDK_DIGEST} AS certs
USER root
RUN --mount=type=bind,from=docker-init,src=/caasd,dst=/init \
    cp "${JAVA_HOME}/lib/security/cacerts" /tmp/truststore \
    && for crt in /init/certs/*.crt; do \
         keytool -importcert -noprompt -storepass changeit -keystore /tmp/truststore \
           -alias "mitre-$(basename "$crt" .crt)" -file "$crt"; \
       done

# ---- runtime: distroless DHI JRE ------------------------------------------------------------------
FROM harbor.cre.gov.aws.mitre.org/dhi/eclipse-temurin:21-alpine@sha256:470f6223a1cdc3cdc3627f4cc11e31c5b6c2a8c3ca81f802d049b1a17e54e397 AS deploy

WORKDIR /boogie-service

EXPOSE 8080
EXPOSE 8081

# MITRE-trusted truststore from the cert stage (replaces the keytool/update-ca-trust path the
# distroless runtime cannot run). Consumed via -Djavax.net.ssl.trustStore in the ENTRYPOINT — a
# fixed path avoids depending on the JRE's version-specific JAVA_HOME. Contains the JDK public roots
# plus the MITRE CAs.
COPY --from=certs /tmp/truststore /boogie-service/certs/cacerts

# DHI runs as a fixed nonroot user and the JAR only needs to be world-readable, so no --chown.
COPY build/libs/boogie-service.jar .

# /tmp must be writable at runtime; the helm chart mounts an emptyDir at /tmp with
# readOnlyRootFilesystem. -Djava.io.tmpdir=/tmp keeps the JVM temp dir explicit.
ENTRYPOINT ["java", \
    "-Djava.io.tmpdir=/tmp", \
    "-Djavax.net.ssl.trustStore=/boogie-service/certs/cacerts", \
    "-Djavax.net.ssl.trustStorePassword=changeit", \
    "-Djava.security.manager=allow", \
    "-XX:MaxRAMPercentage=75.0", \
    "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", \
    "--add-opens=java.base/java.io=ALL-UNNAMED", \
    "--add-opens=java.base/java.lang=ALL-UNNAMED", \
    "--add-opens=java.base/java.nio=ALL-UNNAMED", \
    "--add-opens=java.base/java.util=ALL-UNNAMED", \
    "-jar", "./boogie-service.jar"]
