#FROM openjdk:21-jdk-slim
#
#WORKDIR /app
#
#COPY mvnw .
#
#COPY .mvn ./.mvn
#
#COPY pom.xml .
#
#COPY src ./src
#
#RUN ./mvnw clean package -Dmaven.test.skip=true
#
#EXPOSE 8090
#
#ENTRYPOINT ["java", "-jar", "target/auth-project.jar"]