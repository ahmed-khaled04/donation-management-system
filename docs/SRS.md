# Software Requirements Specification: Impact Hub (Phase 1)

**Impact Hub: NGO Management System**
CSE336 Software Design Patterns · Major Task, Phase 1

| Member | GitHub | Part |
|---|---|---|
| Saifeldin (Sais) | @saifeldinsais | Foundation and people: `persistence`, `people`, `lookup` |
| Ahmed Khaled | @ahmed-khaled04 | Observer and notification core: `observer`, `notification` |
| Mohamed Dahy | @Mohamed-Dahy | Donations and notification channels: `donation`, `notification.channels` |
| Seif Eldeen | @seif-elden | Events and volunteer logic: `event`, `volunteer` |
| Youssif Sameh | @YoussifSameh2004 | Decorators and volunteer output: `donation.receipt`, `event.ticket`, `volunteer` |
| Tobgi | @tob-git | Beneficiaries and impact strategy: `beneficiary` |

---

## 1. Introduction

### 1.1 Purpose
This document specifies what Impact Hub does in Phase 1 and how its object-oriented design meets the
requirements using design patterns. It is written for the course staff who grade the project and for
the team members who build it.

### 1.2 Scope
Impact Hub helps a non-profit organization manage people, donations, events, volunteers, beneficiaries
and communication. Phase 1 delivers:

- the class diagram in [`docs/class-diagram/`](class-diagram/), and
- a Java implementation of exactly that diagram. It uses the Strategy, Observer, Decorator, Factory and
  Singleton patterns, organised with MVC, and has CRUD lookup tables.

The user interface is deliberately very simple: plain JavaFX windows with forms, buttons and a text
output area. Its only purpose is to show that every feature works.

### 1.3 Definitions
| Term | Meaning |
|---|---|
| Donor / Volunteer / Beneficiary | The three kinds of `Person` |
| Distribution | Resources handed to a beneficiary |
| Lookup table | A small editable list used by other data: `Skill` and `ResourceCategory` |
| Channel | How a person is contacted: `EMAIL`, `SMS` or `SOCIAL` |

---

## 2. Overall Description

### 2.1 Product perspective
Impact Hub is a self-contained desktop application (Java 21, JavaFX 21). Data is kept in a local SQLite
file, `impacthub.db`, through one shared JDBC connection. E-mail, SMS and social media delivery are
simulated: messages are printed to the terminal.

### 2.2 Users
| User | What they do |
|---|---|
| Staff | Record donations, run events, assign volunteers, hand out resources, edit lookup tables |
| Donor | Signs up, donates, receives thank-you messages and receipts |
| Volunteer | Signs up, is assigned to events, has hours logged, receives a certificate |
| Beneficiary | Is registered with their needs and receives distributions |

### 2.3 Constraints
- Java 21 and Maven; JavaFX for the UI; SQLite (`sqlite-jdbc`) for storage.
- The code contains only the classes in the class diagram. The one exception is `App`, the JavaFX entry
  point that wires the objects together and opens the main menu.

---

## 3. Functional Requirements

### 3.1 People (`people`)
| ID | Requirement | Classes |
|---|---|---|
| FR-P1 | A user signs up as a donor, volunteer or beneficiary with name, e-mail, phone and password | `PersonService.register`, `PersonController.signup` |
| FR-P2 | Passwords are stored only as SHA-256 hashes; login checks the hash | `Person.passwordHash`, `PersonService.authenticate` |
| FR-P3 | Staff search people by name and delete a person | `PersonService.searchByName / delete` |
| FR-P4 | Every person has a preferred contact channel | `Person.preferredChannel`, `Channel` |

### 3.2 Donations (`donation`, `donation.receipt`)
| ID | Requirement | Classes |
|---|---|---|
| FR-D1 | Record a donation of type Online, Check or In-kind for a donor and an amount | `DonationFactory`, `OnlineDonation`, `CheckDonation`, `InKindDonation` |
| FR-D2 | Track donor information, amounts and types; list all donations | `Donation`, `DonationService.getDonationsByDonor` |
| FR-D3 | Generate a receipt, optionally with tax information and/or a thank-you note | `Receipt`, `BasicReceipt`, `TaxInfoDecorator`, `ThankYouNoteDecorator` |
| FR-D4 | Produce a yearly tax statement for a donor | `DonationService.generateTaxStatement` |
| FR-D5 | Each new donation automatically updates totals, thanks the donor and prints a receipt | `StatsObserver`, `ThankYouObserver`, `ReceiptObserver` |

### 3.3 Events (`event`, `event.ticket`)
| ID | Requirement | Classes |
|---|---|---|
| FR-E1 | Create fundraisers, workshops and outreach programs | `EventFactory`, `Fundraiser`, `Workshop`, `Outreach` |
| FR-E2 | Register attendees (no double registration) and track attendance | `Event.register / markAttendance` |
| FR-E3 | Sell tickets with optional VIP seating and dinner, in any combination | `Ticket`, `BasicTicket`, `VipDecorator`, `MealDecorator` |
| FR-E4 | When an event's date or venue changes, or a reminder is sent, all attendees are notified | `EventService`, `AttendeeNotificationObserver` |
| FR-E5 | Each event has a status: planned, ongoing, completed or cancelled | `EventStatus` |

### 3.4 Volunteers (`volunteer`)
| ID | Requirement | Classes |
|---|---|---|
| FR-V1 | Keep volunteers' skills, availability and preferences | `Volunteer`, `Skill` |
| FR-V2 | Assign a volunteer to an event with a role | `VolunteerService.assign`, `Assignment` |
| FR-V3 | Log hours worked; the volunteer's total is updated | `VolunteerService.logHours` |
| FR-V4 | Find volunteers by skill | `VolunteerService.findBySkill` |
| FR-V5 | Issue a certificate showing the volunteer's total hours | `VolunteerCertificate` |

### 3.5 Beneficiaries (`beneficiary`)
| ID | Requirement | Classes |
|---|---|---|
| FR-B1 | Store beneficiaries with address, family size and needs | `Beneficiary`, `Need` |
| FR-B2 | Record distributions of resources; never more than the stock | `DistributionService.recordDistribution`, `Distribution`, `Resource` |
| FR-B3 | Stock goes down automatically after each distribution | `StockObserver` |
| FR-B4 | Evaluate a program's impact: food aid by people fed, education by needs met | `ImpactEvaluator`, `FoodAidImpactStrategy`, `EducationImpactStrategy`, `ImpactReport` |

### 3.6 Communication (`notification`, `notification.channels`)
| ID | Requirement | Classes |
|---|---|---|
| FR-C1 | Send messages by e-mail, SMS or social media | `EmailStrategy`, `SmsStrategy`, `SocialMediaStrategy` |
| FR-C2 | Each message goes through the recipient's preferred channel, with e-mail as the default | `NotificationService` |

### 3.7 Lookup tables (`lookup`)
| ID | Requirement | Classes |
|---|---|---|
| FR-L1 | Staff list, create, rename and delete skills and resource categories without a developer | `LookupController`, `LookupView`, `Skill`, `ResourceCategory` |

---

## 4. Client Requirements

These extend the basic brief with what the organization's staff asked for. Each is satisfied by the
diagram and the code.

| ID | Client requirement | Satisfied by |
|---|---|---|
| CR-01 | Every person is contacted through the channel they chose (e-mail, SMS or social) | Strategy: `NotificationService` + channel strategies |
| CR-02 | Donors are thanked automatically right after donating | Observer: `ThankYouObserver` |
| CR-03 | The donor chooses whether the receipt includes tax info and a thank-you note | Decorator: `TaxInfoDecorator`, `ThankYouNoteDecorator` |
| CR-04 | Event tickets can include VIP seating and dinner in any combination | Decorator: `VipDecorator`, `MealDecorator` |
| CR-05 | Attendees hear about date and venue changes without staff contacting each one | Observer: `AttendeeNotificationObserver` |
| CR-06 | A distribution can never exceed the stock, and stock is reduced automatically | `DistributionService`, Observer: `StockObserver` |
| CR-07 | Food aid and education programs are measured in different ways | Strategy: `FoodAidImpactStrategy`, `EducationImpactStrategy` |
| CR-08 | Admins edit skills and resource categories themselves | CRUD: `LookupController` |
| CR-09 | Passwords are never stored in plain text | `PersonService` (SHA-256 hash) |

---

## 5. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 Flexibility | New donation types, event types, channels, ticket extras and impact measures are added as new classes, without changing existing ones |
| NFR-2 Maintainability | MVC keeps the screens (views), the screen logic (controllers) and the business rules (services) apart. Each package belongs to one module |
| NFR-3 Reusability | One `GenericDAO<T>` stores every kind of object, so no table needs its own data-access class |
| NFR-4 Persistence | Data survives restarts in `impacthub.db` |
| NFR-5 Usability | Every window shows the result or error of each action in its output area. Bad input (e.g. text in an id field) shows a message instead of crashing |

---

## 6. System Design

### 6.1 Architecture: MVC

| Layer | Classes |
|---|---|
| View | `View` (interface) ← `PersonView`, `DonationView`, `EventView`, `VolunteerView`, `BeneficiaryView`, `LookupView` |
| Controller | `PersonController`, `DonationController`, `EventController`, `VolunteerController`, `BeneficiaryController`, `LookupController` |
| Model | Services (`PersonService`, `DonationService`, `EventService`, `VolunteerService`, `DistributionService`), entities, `GenericDAO`, `DatabaseManager` |

Each view creates its controller and only talks to it (1-to-1). The controller calls the services and
tells the view what to show.

### 6.2 Class diagram
The full diagram is in [`docs/class-diagram/`](class-diagram/) as Mermaid source (`.mmd`), Graphviz
source (`.dot`) and rendered images (`.png`, `.svg`).

![Class diagram](class-diagram/NGO_UML_class_diagram.png)

### 6.3 Design patterns

| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `NotificationService` → `NotificationStrategy` (`EmailStrategy`, `SmsStrategy`, `SocialMediaStrategy`) | Pick the delivery channel per recipient at runtime |
| **Strategy** | `ImpactEvaluator` → `ImpactEvaluationStrategy` (`FoodAidImpactStrategy`, `EducationImpactStrategy`) | Each program measures impact differently |
| **Observer** | Subjects `DonationService`, `EventService`, `DistributionService`; observers `ReceiptObserver`, `ThankYouObserver`, `StatsObserver`, `AttendeeNotificationObserver`, `StockObserver`; message `ChangeEvent` | Reactions to a change (thank-you, stats, receipts, notices, stock) are added without touching the service |
| **Decorator** | `Receipt` ← `BasicReceipt`, `ReceiptDecorator` (`TaxInfoDecorator`, `ThankYouNoteDecorator`) | Receipt sections are combined freely |
| **Decorator** | `Ticket` ← `BasicTicket`, `TicketDecorator` (`VipDecorator`, `MealDecorator`) | Ticket extras add to price and description in any combination |
| **Factory** | `DonationFactory.create(type, ...)`, `EventFactory.create(type, ...)` | Create the right subclass from the type the user picked |
| **Singleton** | `DatabaseManager.getInstance()` | One shared database connection for the whole app |
| **MVC** | Section 6.1 | Separate screens, screen logic and business rules |

### 6.4 CRUD lookup tables
`Skill` (used by `Volunteer`) and `ResourceCategory` (used by `Need` and `Resource`) are lookup tables.
`LookupView` / `LookupController` list, create, rename and delete their rows through `GenericDAO`.

### 6.5 Persistence
`DatabaseManager` (Singleton) opens one JDBC connection to `impacthub.db`. `GenericDAO<T>` gives
`insert`, `findById`, `findAll`, `update` and `delete` for any class. Each class gets its own table, named
after the class, holding the serialized objects. On first run the app adds sample data: skills,
categories, resources, two donors, two volunteers, two beneficiaries and two events.

---

## 7. Running and Testing

```bash
mvn javafx:run    # start the app
mvn test          # run the unit tests (use target/test.db, never impacthub.db)
```

Sample logins all use password `1234`, e.g. `sara@mail.com` (donor), `ali@mail.com` (volunteer),
`fatma@mail.com` (beneficiary). Delete `impacthub.db` to start again with fresh sample data.
