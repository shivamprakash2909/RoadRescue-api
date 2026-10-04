.PHONY: up down logs build infra-up infra-down clean restart ps server-build client-build

up:
	docker compose up -d

down:
	docker compose down

restart:
	docker compose down && docker compose up -d

logs:
	docker compose logs -f

build:
	docker compose build

infra-up:
	docker compose up -d postgres redis kafka pgadmin

infra-down:
	docker compose stop postgres redis kafka pgadmin

pgadmin-up:
	docker compose up -d pgadmin

pgadmin-down:
	docker compose stop pgadmin

server-build:
	cd server && ./mvnw clean install -DskipTests

client-build:
	cd client && npm run build

ps:
	docker compose ps

clean:
	docker compose down -v --remove-orphans
