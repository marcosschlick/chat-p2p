# Chat P2P 

Este projeto inclui um servidor central para um sistema de chat P2P com banco de dados PostgreSQL e pgAdmin para gerenciamento.

## Pré-requisitos
- Docker ou Podman

## Configuração Inicial

1. Clone o repositório:
```bash
git clone git@github.com:marcosschlick/chat-p2p.git
cd chat-p2p
```

2. Crie e edite o arquivo de ambiente:
```bash
cp .env.example .env
vim .env  # Edite com suas credenciais
```

## Iniciar os Containers
```bash
docker compose up -d
```
ou
```bash
podman compose up -d
```

## Acessar o pgAdmin
1. Abra: http://localhost:5050
2. Use as credenciais do seu `.env`:
   - Email: Valor de `PGADMIN_DEFAULT_EMAIL`
   - Senha: Valor de `PGADMIN_DEFAULT_PASSWORD`

## Configurar Conexão no pgAdmin
1. Clique em "Add New Server"
2. Na aba "General":
   - Name: `Local PostgreSQL`
3. Na aba "Connection":
   ```ini
   Host name/address: postgres
   Port: 5432
   Maintenance database: chatp2p  # Mesmo valor de SERVER_POSTGRES_DATABASE no .env
   Username: postgres             # Mesmo valor de SERVER_POSTGRES_USER no .env
   Password: sua_senha            # Mesmo valor de SERVER_POSTGRES_PASSWORD no .env
   ```
4. Clique em "Save"

## Criar Tabela de Usuários
Execute no pgAdmin (Query Tool):
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    online BOOLEAN NOT NULL DEFAULT false,
    profile_image_url VARCHAR(255)
);
```
