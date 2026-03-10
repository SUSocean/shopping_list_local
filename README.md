To succesefully run the program you need the folowing requirements:
- Java 17+
- Docker

Instructions:
- Downloand the project from the repository
- Open console and navigate to the project folder
- Run this command to create a local database:
  - docker compose up
- Run this command to launch the project:
  - win | mvnw.cmd spring-boot:run
  - mac/linux | ./mvnw spring-boot:run
- Done. Now go to your browser and navigate to http://localhost:8080
