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
| `java -jar target/Motel_management-0.1.3.jar` | Run app |

## Architecture

MVC with manual dependency injection wired in `App.java`. A `MotelManagement` facade delegates to service objects (`RoomManager`, `SellingService`, `TurnService`, `HistoryService`, `ProgramConfig`, `Register`, `Printer`, `FileManager`). A single `Controller` orchestrates 9 sub-controllers and 4 timers (clock, backup, floor rotation, overtime warning).

**Packages:** `controller`, `controller.sub`, `model`, `model.modelManagers`, `model.dto`, `model.turn`, `view`, `view.helpers`, `view.customListRenderes`, `view.interfaces`

## Functionality

The program targets a **touchscreen** interface, with touchscreen related control for list and Management
A main Floor view is present showing status of the program, a warning for overtime room booking is also present for multiple tower / floors configurations
Each room has 3 different selection for time, with a personalized modification for each room present on the configuration 

## Project Stats

- **82** source files, **~14,138** lines (main)
- **8** test classes, **152** tests, **~1,871** lines (test)
- **19** view cards, **23** panel views
- Single-instance via `FileLock`
- Persistent state: JSON in `data/`, history in `history/`, backups in `backup/`

## Future features

- **Localization/Internationalization:** Once the program reaches a mature point, a i18n localization will be implemented
- **External door interface:** On a separate project it's meant to link external hardware door opening and closing for tracking of each room linked to it

## Known issues

- **No initialization:** Actively being worked to prepare an initial program data interface for first start
- **Conflicts on data:** Due to personalization for each room management, current report and design for room needs to be revised, WIP

## Versions:
- **0.1.2:** Up until commits done to september 27th 2025, basic functionality, requires existing applicationProperties to properly function
- **0.1.3:** Commits done since start of 2026 comprise the current program version, better implementation, separation, and management options prepared, still requires a existing applicationProperties with basic data to function, known conflicts and issues are being actively worked on

## **DISCLAIMER:**
This program has used LLM tools such as OpenCode, Claude, and related for code development, **Design, management, and final decission are still done by the author**, This is a original project prepared on a simple understanding of MVC, UML, and Object oriented design, it can be found on github: [Project designs](https://github.com/TheAbsdag/New_Project_Designs) , 
