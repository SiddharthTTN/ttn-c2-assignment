.PHONY: help install frontend-install backend-install \
	frontend-build frontend-test frontend-lint \
	backend-test backend-package \
	test build run run-h2 run-postgres \
	package prod-build clean clean-static \
	docker-up docker-down ollama-pull

ROOT := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))
FRONTEND := $(ROOT)/frontend
BACKEND := $(ROOT)/backend

help:
	@echo "Targets:"
	@echo "  install          npm ci (frontend) — Maven deps resolved on first backend build"
	@echo "  test             backend unit/integration tests + frontend vitest"
	@echo "  build            frontend production build only"
	@echo "  package          frontend build + stage static + backend mvn package"
	@echo "  prod-build       alias for package"
	@echo "  run-h2           Spring Boot with default h2 profile (in-memory/file demo)"
	@echo "  run-postgres     Spring Boot with postgres profile (requires docker-up + ollama-pull)"
	@echo "  docker-up        start PostgreSQL/pgvector and Ollama via docker compose"
	@echo "  docker-down      stop compose services"
	@echo "  ollama-pull      pull default chat and embedding models into Ollama container"
	@echo "  clean            remove staged static, frontend dist, backend target"

install: frontend-install

frontend-install:
	cd "$(FRONTEND)" && npm ci

backend-install:
	cd "$(BACKEND)" && ./mvnw -q -DskipTests dependency:go-offline

frontend-build: frontend-install
	cd "$(FRONTEND)" && npm run build

frontend-test: frontend-install
	cd "$(FRONTEND)" && npm run test

frontend-lint: frontend-install
	cd "$(FRONTEND)" && npm run lint && npm run typecheck

backend-test:
	cd "$(BACKEND)" && ./mvnw -q test

backend-package:
	cd "$(BACKEND)" && ./mvnw -q package -DskipTests

test: backend-test frontend-test

build: frontend-build

package: frontend-build
	"$(ROOT)/scripts/copy-frontend-static.sh"
	cd "$(BACKEND)" && ./mvnw -q package -DskipTests

prod-build: package

run-h2:
	cd "$(BACKEND)" && ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=h2

run-postgres:
	@set -a && [ -f "$(ROOT)/.env" ] && . "$(ROOT)/.env"; set +a; \
	cd "$(BACKEND)" && ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=postgres

docker-up:
	docker compose up -d

docker-down:
	docker compose down

ollama-pull:
	docker compose exec ollama ollama pull nomic-embed-text
	docker compose exec ollama ollama pull llama3.2:3b

clean: clean-static
	rm -rf "$(FRONTEND)/dist" "$(BACKEND)/target"

clean-static:
	"$(ROOT)/scripts/clean-frontend-static.sh"
