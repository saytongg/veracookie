FROM eclipse-temurin:17.0.10_7-jdk-alpine
RUN apk update \
&& apk add --no-cache firefox \
&& apk add --no-cache curl \
&& curl -O -L https://github.com/mozilla/geckodriver/releases/download/v0.34.0/geckodriver-v0.34.0-linux64.tar.gz \
&& tar -xvzf geckodriver* \
&& chmod +x geckodriver \
&& mv geckodriver /usr/local/bin/ \
&& rm -rf geckodriver*
WORKDIR /app
COPY target/veracookie-0.0.1-SNAPSHOT.jar /app/veracookie.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "veracookie.jar"]