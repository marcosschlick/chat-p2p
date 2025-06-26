# Chat P2P 

Este projeto inclui um servidor central para um sistema de chat P2P com banco de dados PostgreSQL e pgAdmin para gerenciamento.

## Pré-requisitos
- Maven e JDK(21)

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
cd server && mvn clean spring-boot:run && cd ..
```

### Terminal 2 - Cliente Desktop JavaFX:
```bash
cd desktop && mvn clean javafx:run && cd ..
```

Importante:
1. Execute o servidor primeiro (Terminal 1)
2. Aguarde o servidor inicializar completamente
3. Só então execute o cliente desktop (Terminal 2)
