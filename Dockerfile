FROM hmcts/cnp-java-base:openjdk-8u181-jre-alpine3.8-1.0
LABEL maintainer="https://github.com/hmcts/ccd-user-profile-api"

ENV APP user-profile.jar
ENV APPLICATION_TOTAL_MEMORY 854M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

ENV JAVA_OPTS "-Dspring.config.location=classpath:/application.properties -Djava.security.egd=file:/dev/./urandom"

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4453/status/health

EXPOSE 4453
