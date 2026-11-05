# Impact Hub: NGO Management System

CSE336 Software Design Patterns, Phase 1. Java 25 · JavaFX 21 · SQLite · Maven

- Requirements and design: [docs/SRS.pdf](docs/SRS.pdf) (source: `docs/srs/SRS.html`)
- Class diagram: [docs/class-diagram/](docs/class-diagram/)

## Run

Requires a **JDK 25 or newer**. The Maven Wrapper downloads Maven and the
project dependencies on the first run, so an internet connection is needed.

**macOS:** double-click `start-app.command`. It starts the JavaFX desktop
application and locates Homebrew Java even when launched from Finder. Keep
the terminal window open while using the app.

**Terminal:**

```bash
cd "/path/to/donation-management-system"
./mvnw javafx:run   # opens the desktop app; creates impacthub.db on first run
```

On Windows, use `mvnw.cmd javafx:run`. If Maven is already installed,
`mvn javafx:run` also works. In an IDE, import `pom.xml` as a Maven project
and run the `javafx:run` Maven goal.

To run the existing tests:

```bash
./mvnw test
```

Tests use `target/test.db`, separately from your application data.

On the dashboard, click **Log in**. Sample accounts use password `1234`:
`sara@mail.com`, `omar@mail.com` (donors), `ali@mail.com`, `mona@mail.com`
(volunteers), `fatma@mail.com`, `hassan@mail.com` (beneficiaries).
After login, click **Donations** to open donation management.

Notifications and receipts are printed in the terminal. Application records
are saved in `impacthub.db` in this project folder. To reset the sample data,
close the app and delete that database file.

## Packages

| Package | Owner |
|---|---|
| `persistence`, `people`, `lookup` | Sais (@saifeldinsais) |
| `observer`, `notification` | Khaled (@ahmed-khaled04) |
| `donation`, `notification.channels` | Dahy (@Mohamed-Dahy) |
| `event`, `volunteer` (service, assignment, controller) | Seif (@seif-elden) |
| `donation.receipt`, `event.ticket`, `volunteer` (view, certificate) | Yousef (@YoussifSameh2004) |
| `beneficiary` | Tobgi (@tob-git) |
