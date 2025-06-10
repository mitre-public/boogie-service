FROM harbor-cre-ops.cre.gov.aws.mitre.org/aws-ecr/docker-init:latest-mitre AS docker-init

FROM eclipse-temurin:21-jre-alpine AS deploy

RUN --mount=type=bind,src=/caasd,from=docker-init,dst=/init \
    /init/scripts/copy-certs.sh --setup=java

WORKDIR /boogie-service

EXPOSE 8080
EXPOSE 8081

COPY build/libs/boogie-service.jar .

ENTRYPOINT ["java","-Djava.security.manager=allow","-XX:MaxRAMPercentage=75.0","--add-opens=java.base/sun.nio.ch=ALL-UNNAMED","--add-opens=java.base/java.io=ALL-UNNAMED","--add-opens=java.base/java.lang=ALL-UNNAMED","--add-opens=java.base/java.nio=ALL-UNNAMED","--add-opens=java.base/java.util=ALL-UNNAMED","-jar","./boogie-service.jar"]