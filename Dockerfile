FROM openjdk:8-jre

COPY build/libs/user-profile-*-all.jar /app.jar

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4453/status/health

EXPOSE 4453

CMD java ${JAVA_OPTS} -Dspring.config.location=/application.properties -Djava.security.egd=file:/dev/./urandom -jar /app.jar
