# Notes & History API (Java 21 + MySQL + Envers)

Aplikacja do zarządzania notatkami z pełną historią zmian (audytem) realizowaną przez Hibernate Envers. Projekt jest w pełni skonteneryzowany.

## 🚀 Szybki start (Docker)

Najprostszym sposobem na uruchomienie aplikacji jest użycie przygotowanego skryptu:

1. Upewnij się, że masz zainstalowany **Docker Desktop**.
2. Kliknij dwukrotnie plik `run.bat`.

Aplikacja będzie dostępna pod adresem: `http://localhost:8080`

### Ręczne sterowanie kontenerami:
* **Budowanie i start:** `docker-compose up --build -d`
* **Zatrzymanie:** `docker-compose down`
* **Podgląd logów:** `docker logs -f notes-app`

## 🗄️ Baza danych (MySQL)

Baza danych jest automatycznie konfigurowana w Dockerze.
Jeśli chcesz połączyć się z nią zewnętrznym narzędziem (np. HeidiSQL):

- **Host:** `127.0.0.1`
- **Port:** `3306`
- **Użytkownik:** `root`
- **Hasło:** `root`
- **Database:** `notes_db`

## 🛠️ Technologie
- **Java 21** (Virtual Threads ready)
- **Spring Boot 3.x**
- **MySQL 8.3**
- **Hibernate Envers** (Audytowanie zmian)
- **Lombok** (Record Builders)
- **Flyway** (Migracje bazy danych)

## 📝 Funkcje API
- `POST /items` - Utworzenie nowej notatki (inicjalizuje `version = 0`).
- `PATCH /items/{id}` - Edycja notatki (wymaga podania aktualnej wersji w celu blokady optymistycznej).
- `GET /items/{id}/history` - Pobranie pełnej historii zmian notatki z tabeli audytowej.

## ⚠️ Uwagi dot. wersji
W tym projekcie zrezygnowano z adnotacji `@Version` na rzecz ręcznego zarządzania polem `version`. Gwarantuje to poprawny zapis numeru wersji w tabelach audytowych `items_aud` przy współpracy z Hibernate Envers.
Do modelu danych została też dodana dodatkowa rola `OWNER` oraz do tabeli items dodatkowe pole `created_by` sugerujące kto dokonał zmian  