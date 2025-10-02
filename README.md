https://bbw-it.github.io/450_main_rupe/assets/12_Mocken/22_9_SpringBootDatenbankUnittestMockFallStudie.pdf

# Anwendung Mockito für DB Service

## Projekt Employee and Department

1. Projekt öffnen
2. Datenbank __employeedepartment__ in MariaDB Server erstellen
3. SQL aus __employeedepartment.sql__ importieren
4. Projekt starten
5. Im Browser localhost:8080 starten und ausprobieren

## Testing

Das Projekt ist gedacht, den Umgang mit Unit-Tests zu üben. Es wird Mockito verwendet, um die Datenbank zu mocken.

- Erstelle Unittests für den EmployeeService.
- Erstelle Unittests für den BusinessService.
- Erstellen Unittests für den ViewController

Um die Tests laufen zu lassen und ich Java 19 verwende (+MacOS), musste ich `JAVA_HOME=$(/usr/libexec/java_home -v 19) mvn clean test` ausführen.

(c) 2024 by Peter Rutschmann BBW