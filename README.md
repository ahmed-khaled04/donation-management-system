# Impact Hub: NGO Management System

CSE336 Software Design Patterns, Phase 1. Java 21 · JavaFX 21 · SQLite · Maven

- Requirements and design: [docs/SRS.pdf](docs/SRS.pdf) (source: `docs/srs/SRS.html`)
- Class diagram: [docs/class-diagram/](docs/class-diagram/)

## Run

```bash
mvn javafx:run    # start the app (creates impacthub.db with sample data on first run)
mvn test          # run the unit tests
```

Sample logins use password `1234`: `sara@mail.com`, `omar@mail.com` (donors), `ali@mail.com`,
`mona@mail.com` (volunteers), `fatma@mail.com`, `hassan@mail.com` (beneficiaries).
Notifications and receipts are printed in the terminal. Delete `impacthub.db` to reset the data.

## Packages

| Package | Owner |
|---|---|
| `persistence`, `people`, `lookup` | Sais (@saifeldinsais) |
| `observer`, `notification` | Khaled (@ahmed-khaled04) |
| `donation`, `notification.channels` | Dahy (@Mohamed-Dahy) |
| `event`, `volunteer` (service, assignment, controller) | Seif (@seif-elden) |
| `donation.receipt`, `event.ticket`, `volunteer` (view, certificate) | Yousef (@YoussifSameh2004) |
| `beneficiary` | Tobgi (@tob-git) |
