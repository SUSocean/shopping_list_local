To succesefully run the program you need the folowing requirements:
- Java 17+
- Docker

Instructions:
- Downloand the project from the repository
- Openg console and navigate to the project folder
- run this command to create a local database:
  - docker compose up
- run this command to launch the project:
  - win | mvnw.cmd spring-boot:run
  - mac/linux | ./mvnw spring-boot:run
- done. Now go to your browser and navigate to http://localhost:8080
