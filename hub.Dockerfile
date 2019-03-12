FROM gradle:5.2.1-jdk8 as builder
LABEL maintainer="https://github.com/hmcts/ccd-user-profile-api"

COPY . /home/gradle/src
USER root
RUN chown -R gradle:gradle /home/gradle/src
USER gradle

WORKDIR /home/gradle/src
RUN gradle assemble

ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0.1

COPY --from=builder /home/gradle/src/build/libs/user-profile.jar /opt/app/
COPY lib/AI-Agent.xml /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4453/status/health

EXPOSE 4453

CMD ["user-profile.jar"]
