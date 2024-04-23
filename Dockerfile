FROM amazoncorretto:17

COPY ./build/libs/ledger-0.1.0-SNAPSHOT.jar /app/ledger.jar

ENV DB_USERNAME=""
ENV DB_PASSWORD=""

CMD ["java", "-jar", "-Xms512m", "-Xmx512m", "/app/ledger.jar", "--spring.datasource.username=${DB_USERNAME}", "--spring.datasource.password=${DB_PASSWORD}"]