@echo off
echo [1/3] Budowanie aplikacji i obrazów...
docker-compose build --no-cache

echo [2/3] Uruchamianie kontenerów (Java 21 + MySQL)...
docker-compose up -d

echo [3/3] Gotowe! Aplikacja startuje...
echo Twoja aplikacja bedzie dostepna pod adresem: http://localhost:8080
echo Baza danych MySQL (wewnatrz sieci) na porcie: 3306
echo.
echo Aby zatrzymac aplikacje, zamknij to okno i wpisz 'docker-compose down' w terminalu.
pause