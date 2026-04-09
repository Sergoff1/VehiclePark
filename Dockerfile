FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /build

COPY build.gradle.kts settings.gradle.kts ./

RUN gradle dependencies --no-daemon

COPY src ./src
RUN gradle war --no-daemon -x test

FROM tomcat:jre21

COPY opentelemetry-javaagent.jar /opt/agent/opentelemetry-javaagent.jar

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=builder /build/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war