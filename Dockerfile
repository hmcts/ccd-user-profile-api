# Keep hub.Dockerfile aligned to this file as far as possible
FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-1.0
LABEL maintainer="https://github.com/hmcts/ccd-user-profile-api"

COPY build/libs/user-profile.jar /opt/app/
COPY lib/applicationinsights-agent-2.3.1.jar lib/AI-Agent.xml /opt/app/

ENV JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom -javaagent:/opt/app/applicationinsights-agent-2.3.1.jar"

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4453/status/health

EXPOSE 4453

ENTRYPOINT exec java ${JAVA_OPTS} -jar user-profile.jar

