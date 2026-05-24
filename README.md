# Motel Management

A Java 21 Swing MVC desktop application for turn-based motel/hotel administration (Spanish UI only).

## Overview

Desktop app for managing check-ins, check-outs, item sales, room inventory, and turn-based financial tracking in a motel/hotel environment. Uses a card-based panel navigation system with 19 views.
Built on Netbeans as a educational project for a specific usecase present on the author's environment, with ease of testing and better udnerstanding for software development.

## Tech Stack

- **Java 21** with records, sealed interfaces, pattern matching
- **Swing** UI with MigLayout, custom renders/helpers
- **Maven** wrapper (no system Maven required)
- **JSON** persistence (no database)
- **iText 7** (PDF receipts), **Apache POI** (XLSX reports)
- **JUnit 5 + AssertJ** (152 tests)

## Build & Run

| Command | Purpose |
|---------|---------|
| `.\mvnw.cmd clean compile` | Compile |
| `.\mvnw.cmd test` | Run all tests |
| `.\mvnw.cmd clean package` | Build fat JAR |
| `java -jar target/Motel_management-0.1.3.1.jar` | Run app |

## Architecture

MVC with manual dependency injection wired in `App.java`. A `MotelManagement` facade delegates to service objects (`RoomManager`, `SellingService`, `TurnService`, `HistoryService`, `ProgramConfig`, `Register`, `Printer`, `FileManager`). A single `Controller` orchestrates 9 sub-controllers and 4 timers (clock, backup, floor rotation, overtime warning).

**Packages:** `controller`, `controller.sub`, `model`, `model.modelManagers`, `model.dto`, `model.turn`, `view`, `view.helpers`, `view.customListRenderes`, `view.interfaces`

A detailed [sequence diagram](docs/DIAGRAMS/v0.1.3.1.md) covering all 10 defensive validation layers across the MVC stack is available in the [diagrams index](docs/DIAGRAMS/DIAGRAMS.md).

## Functionality

The program targets a **touchscreen** interface, with touchscreen related control for list and Management
A main Floor view is present showing status of the program, a warning for overtime room booking is also present for multiple tower / floors configurations
Each room has 3 different selection for time, with a personalized modification for each room present on the configuration


## Future features / roadmap

Features to be added, in no particular order of progress, WIP will be marked and are actively being worked on

- [x] **Initial setup flow:** allow the program to do a initial setup flow
- [ ] **Localization/Internationalization:** Once the program reaches a mature point, a i18n localization will be implemented (Currently in progress)
- [ ] **External door interface:** On a separate project it's meant to link external hardware door opening and closing for tracking of each room linked to it (Currently in progress, future github linking to the project will be available)
- [ ] **Personalization, encryption of history:** To apply encryption and data safety, future implementation for all data to be locally encrypted to the user requirements is being testes (Currently in progress)
- [ ] **Report exportation:** Currently a local report generation is present, implementation for custom email endpoints, or implementation of API is being evaluated
- [ ] **Printer page personalization:** For convienence, a fixed calculation for a 90mm thermal priter is the only implementation currently available for printing, a more customizable option is being setup
- [ ] **Date and time customization:** On the effort to localization, a date and time customization will be managed for data saving and related
- [ ] **Database integration:** A customizable database integration and base is being considered to not rely heavily on JSON data structures for consulting

## Known issues

- **Hardcode 25 grid limitation:** Due to some requirements of legibility, there was a limit made on the grid for the floor, a touch friendly floor navigation is currently being developed
- **Hardcoded long values:** The logic is heavily based upon hardcoded long values for pricing or related, due to the COP nature is working as a whole long, implementation of doubles or floating points are being done for the project scope as a customizable option for currencies that manage decimals
- **History review broken:** The in program history review is currently broken.

## Versions:
- **0.1.2:** Up until commits done to september 27th 2025, basic functionality, requires existing applicationProperties to properly function
- **0.1.3:** Commits done since start of 2026 comprise the current program version, better implementation, separation, and management options prepared, still requires a existing applicationProperties with basic data to function, known conflicts and issues are being actively worked on
- **0.1.3.1:** Refactoring and initial setup finished, making the program fully functional for basic details, advanced data output configuration and related will come at a later date

## **DISCLAIMER:**
This program has used LLM tools such as OpenCode, Claude, Qwen, and related for code development, refinement and optimization (Though test creation was a godsend).

**Design decissions, feature planning, validation, and implementation are still done by me, the repository author**.

This is a educational project made on a simple understanding of MVC, UML, and Object oriented design, meant to apply, develop, and improve on those concepts on a real use case scenario with actual requirements, this is a highly personalized project for the usecase.

Design files for the basic concept can be found on github: [Project designs](https://github.com/TheAbsdag/New_Project_Designs)
