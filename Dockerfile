# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ARG APP_INSIGHTS_AGENT_VERSION=2.4.1

FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0
LABEL maintainer="https://github.com/hmcts/ccd-user-profile-api"

COPY build/libs/user-profile.jar  /opt/app/
COPY lib/AI-Agent.xml /opt/app
COPY lib/applicationinsights-agent-2.4.1.jar lib/AI-Agent.xml /opt/app/

EXPOSE 4453

CMD ["user-profile.jar"]

