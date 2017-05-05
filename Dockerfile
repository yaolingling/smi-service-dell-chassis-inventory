FROM openjdk:8-jre
VOLUME /tmp
ADD build/libs/service-chassis-inventory*.jar app.jar
COPY application.yml /application.yml
EXPOSE 46001
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

