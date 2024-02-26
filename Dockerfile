# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
ARG APP_INSIGHTS_AGENT_VERSION=3.4.13
ARG PLATFORM=""

FROM hmctspublic.azurecr.io/base/java${PLATFORM}:17-distroless
USER hmcts
LABEL maintainer="https://github.com/hmcts/ccd-user-profile-api"

COPY build/libs/user-profile.jar  /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 4453

CMD ["user-profile.jar"]
