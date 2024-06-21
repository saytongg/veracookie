# Requirements
To develop and run the project, the following must be installed:
- Java 17
- maven

Additionally, the project requires a connection to a Redis instance to start, so Redis must also be installed on the machine.

# Profiles
The project includes two development profiles: `local` and `prod`. The `local` profile is for development and testing, while the `prod` profile is intended for deployment. Each profile has stage-specific variables: `src/main/resources/application-local.properties` for local development and `src/main/resources/application-prod.properties` for production.

# Installing the dependencies
The depedencies and plugins needed to run this application are indicated in `pom.xml`. To set up the project and install the dependencies, run the following command:
```bash
mvn clean install
```

# Running the application
To run the application, use the following commands:
```bash
# To run locally,
mvn spring-boot:run -Plocal

# To run in production,
mvn spring-boot:run -Pprod
```
