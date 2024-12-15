FROM maven:3-eclipse-temurin-17-alpine
LABEL author="Alisher Aliev"

EXPOSE 8080

WORKDIR /backend

RUN adduser -D -h /home/backender -s /bin/bash backender && \
    chown -R backender:backender /backend 

USER backender

COPY --chown=backender:backender pom.xml .
COPY --chown=backender:backender src ./src
CMD ["mvn", "spring-boot:run"]
