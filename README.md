# Chat P2P 

Este projeto inclui um servidor central para um sistema de chat P2P com banco de dados PostgreSQL (serviço Neon) e um cliente desktop usando JavaFX com banco de dados H2.

## Pré-requisitos
- Maven e JDK 21

## Configuração Inicial

Clone o repositório:
```bash
git clone git@github.com:marcosschlick/chat-p2p.git
cd chat-p2p
```

## Executando o Sistema

Para iniciar tanto o servidor quanto o cliente desktop, você precisará de dois terminais:

### Terminal 1 - Servidor Spring Boot:
```bash
cd server 
mvn clean spring-boot:run
```

### Terminal 2 - Cliente Desktop JavaFX:
```bash
cd desktop
mvn clean javafx:run
```

Importante:
1. Execute o servidor primeiro (Terminal 1)
2. Só então execute o cliente desktop (Terminal 2)

## Documentação da API (Swagger)

Após iniciar o servidor, você pode acessar a documentação interativa da API através dos seguintes endpoints:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- 

## Estrutura do Projeto
```
.
├── desktop
│   ├── database
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── com
│           │       └── chatp2p
│           │           ├── components
│           │           │   ├── FileMessageBubble.java
│           │           │   ├── ImageSelector.java
│           │           │   ├── MessageBubble.java
│           │           │   ├── SystemMessage.java
│           │           │   └── UserButton.java
│           │           ├── controllers
│           │           │   ├── ChatController.java
│           │           │   ├── LoginController.java
│           │           │   ├── OnlineUsersController.java
│           │           │   └── ProfileController.java
│           │           ├── core
│           │           │   └── App.java
│           │           ├── exceptions
│           │           │   ├── AppException.java
│           │           │   ├── ConnectionException.java
│           │           │   ├── NetworkException.java
│           │           │   └── UserNotFoundException.java
│           │           ├── managers
│           │           │   ├── AuthManager.java
│           │           │   ├── ConnectionManager.java
│           │           │   ├── DatabaseManager.java
│           │           │   ├── HttpManager.java
│           │           │   ├── PeerConnection.java
│           │           │   └── ServerManager.java
│           │           ├── models
│           │           │   ├── ImageOption.java
│           │           │   ├── Message.java
│           │           │   ├── MessageType.java
│           │           │   ├── OnlineUser.java
│           │           │   └── UserProfile.java
│           │           ├── repositories
│           │           │   └── MessageRepository.java
│           │           ├── services
│           │           │   └── UserService.java
│           │           └── utils
│           │               └── NetworkUtils.java
│           └── resources
│               └── com
│                   └── chatp2p
│                       ├── icons
│                       │   ├── chevron-left.png
│                       │   ├── paperclip.png
│                       │   ├── send.png
│                       │   └── user.png
│                       ├── images
│                       │   ├── bob_esponja.jpg
│                       │   ├── capitao_america.png
│                       │   ├── chaves.png
│                       │   ├── default_user.png
│                       │   ├── groot.jpg
│                       │   ├── sasuke.png
│                       │   ├── squirtle.png
│                       │   ├── suarez.png
│                       │   └── vegeta.png
│                       ├── styles.css
│                       └── views
│                           ├── ChatView.fxml
│                           ├── LoginView.fxml
│                           ├── OnlineUsers.fxml
│                           └── ProfileView.fxml
├── README.md
└── server
    ├── mvnw
    ├── mvnw.cmd
    ├── pom.xml
    └── src
        ├── main
        │   ├── java
        │   │   └── com
        │   │       └── chatp2p
        │   │           └── centralserver
        │   │               ├── CentralServerApplication.java
        │   │               ├── config
        │   │               │   ├── JwtUtil.java
        │   │               │   └── SecurityConfig.java
        │   │               ├── controllers
        │   │               │   ├── AuthController.java
        │   │               │   └── UserController.java
        │   │               ├── dtos
        │   │               │   ├── CreateUserDTO.java
        │   │               │   ├── LoginRequest.java
        │   │               │   ├── LoginResponse.java
        │   │               │   └── UpdateUserDTO.java
        │   │               ├── entities
        │   │               │   └── User.java
        │   │               ├── exceptions
        │   │               │   ├── AuthException.java
        │   │               │   └── GlobalExceptionHandler.java
        │   │               ├── repositories
        │   │               │   └── UserRepository.java
        │   │               └── services
        │   │                   └── UserService.java
        │   └── resources
        │       └── application.properties
        └── test
            └── java
                └── com
                    └── chatp2p
                        └── centralserver
                            └── CentralServerApplicationTests.java

43 directories, 68 files
```
