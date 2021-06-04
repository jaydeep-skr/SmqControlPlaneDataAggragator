FROM maven:3.6.3-openjdk-11 as MAVEN_BUILD
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
COPY --from=MAVEN_BUILD /build/target/controller-*.jar controller.jar
ENTRYPOINT ["java","-jar","controller.jar"]