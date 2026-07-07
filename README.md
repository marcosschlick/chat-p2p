# Chat P2P

## About

Chat P2P is a desktop peer-to-peer chat application built in Java.

The project uses a central Spring Boot server for user registration, authentication, profile management, and online user discovery. After users are authenticated, messages and files are exchanged directly between desktop clients through a P2P socket connection.

## Academic Context

This project was developed for the Object-Oriented Programming course in the Technology in Systems Analysis and Development program at IFSul Santana do Livramento.

## Features

- User registration and login
- JWT-based authentication
- Online user list
- Peer-to-peer text messaging
- Peer-to-peer file sharing
- Local chat history storage
- User profile image selection
- Swagger API documentation

## Technologies

- Java
- JavaFX
- Spring Boot
- Spring Security
- PostgreSQL
- H2 Database
- Maven
- Swagger / OpenAPI

## Project Structure

```text
.
├── desktop/       # JavaFX desktop client
├── server/        # Spring Boot central server
├── .env.example   # Example environment variables
└── README.md
```

## Prerequisites

- JDK 21
- Maven
- PostgreSQL database

## Environment Variables

The server requires environment variables for the PostgreSQL connection and JWT signing.

Use `.env.example` as a reference:

```bash
cp .env.example .env
```

Then fill `.env` with your own local or remote database values.

## Running the Project

Start the server first:

```bash
cd server
mvn clean spring-boot:run
```

Then start the desktop client in another terminal:

```bash
cd desktop
mvn clean javafx:run
```

## API Documentation

After starting the server, the API documentation is available at:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
