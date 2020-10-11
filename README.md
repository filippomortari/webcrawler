### How to run the application locally

You will need `java11` and `docker`. `Gradle` is already distributed via Gradle Wrapper. The app depends on Redis but no particular setup is needed for that as Docker Compose takes care of it.

- ensure you are running java 11.
```
➜  webcrawler git:(master) ✗ java -version
openjdk version "11.0.3" 2019-04-16 LTS
OpenJDK Runtime Environment Zulu11.31+11-CA (build 11.0.3+7-LTS)
OpenJDK 64-Bit Server VM Zulu11.31+11-CA (build 11.0.3+7-LTS, mixed mode)
```
- build the fat jar (this will also run the test suite).
```
./gradlew clean build
```
- run it  through Docker Compose
```
docker-compose up
```