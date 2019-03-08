# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0
LABEL maintainer="https://github.com/hmcts/ccd-user-profile-api"

COPY build/libs/user-profile.jar  /opt/app/
COPY lib/AI-Agent.xml /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4453/status/health

EXPOSE 4453

CMD ["user-profile.jar"]

