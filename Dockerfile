FROM maven:3.6.1 as builder
COPY . /project
WORKDIR /project
RUN mvn clean install -Dmaven.test.skip=true
FROM openjdk:8-jdk-alpine
COPY --from=builder /project/target/qrcode-scan-login-1.0-SNAPSHOT.jar /
ENTRYPOINT ["java", "-jar", "/qrcode-scan-login-1.0-SNAPSHOT.jar"]
EXPOSE 9999
