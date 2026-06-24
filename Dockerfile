# DHI migration (stub) — tdp-roadmap #529. Runtime base swapped to the Docker Hardened Image
# eclipse-temurin Java 21 (alpine) ref, pinned by digest. Renovate's dockerfile manager should
# track this tag@digest pair (follow-up).
#
# TODO(dhi, maestro#812): the DHI base is distroless/nonroot with no shell, keytool, or
#   update-ca-trust. Follow the merged maestro pattern (mitre-tdp/maestro#812) to complete this:
#     - Add a cert-seeding build stage on the JDK -dev image (has keytool) that bakes the MITRE CA
#       roots into a temurin truststore, then COPY it into this runtime stage and reference it via
#       -Djavax.net.ssl.trustStore in the ENTRYPOINT.
#     - Writable /tmp and /run: helm hardening (readOnlyRootFilesystem + emptyDir mounts at /tmp
#       and /run); add -Djava.io.tmpdir=/tmp to the ENTRYPOINT.
#     - Drop any --chown (DHI runs as a fixed nonroot user; world-readable bundle is sufficient).
FROM harbor.cre.gov.aws.mitre.org/dhi/eclipse-temurin:21-alpine@sha256:470f6223a1cdc3cdc3627f4cc11e31c5b6c2a8c3ca81f802d049b1a17e54e397 AS deploy

WORKDIR /boogie-service

EXPOSE 8080
EXPOSE 8081

COPY build/libs/boogie-service.jar .

ENTRYPOINT ["java","-Djava.security.manager=allow","-XX:MaxRAMPercentage=75.0","--add-opens=java.base/sun.nio.ch=ALL-UNNAMED","--add-opens=java.base/java.io=ALL-UNNAMED","--add-opens=java.base/java.lang=ALL-UNNAMED","--add-opens=java.base/java.nio=ALL-UNNAMED","--add-opens=java.base/java.util=ALL-UNNAMED","-jar","./boogie-service.jar"]