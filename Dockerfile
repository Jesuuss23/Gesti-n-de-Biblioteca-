FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src src
RUN mvn -B package && mvn -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy -Dartifact=com.mysql:mysql-connector-j:8.4.0 -DoutputDirectory=/tmp/driver

FROM quay.io/wildfly/wildfly:35.0.1.Final-jdk17
COPY --from=build /tmp/driver/mysql-connector-j-8.4.0.jar /tmp/mysql.jar
COPY infra/wildfly.cli /tmp/wildfly.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/wildfly.cli
COPY --from=build /src/target/biblioteca.war /opt/jboss/wildfly/standalone/deployments/
