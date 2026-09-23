# Motel Management

A Java 25 Swing MVC desktop application for turn-based motel/hotel administration (Spanish UI only).

## Overview

Desktop app for managing check-ins, check-outs, item sales, room inventory, and turn-based financial tracking in a motel/hotel environment. Uses a card-based panel navigation system with 27 views.
Built on Netbeans as a educational project for a specific usecase present on the author's environment, with ease of testing and better understanding for software development.

## Tech Stack

- **Java 25** with records, sealed interfaces, pattern matching
- **Swing** UI with MigLayout, custom renders/helpers
- **Maven** wrapper (no system Maven required)
- **JSON** persistence (no database)
- **iText 7** (PDF receipts), **Apache POI** (XLSX reports)
- **JUnit 5 + AssertJ** (No GUI tests done)

## Build & Run

| Command | Purpose |
|---------|---------|
| `.\mvnw.cmd clean compile` | Compile |
| `.\mvnw.cmd test` | Run all tests |
| `.\mvnw.cmd clean package` | Build fat JAR |
| `java -jar target/Motel_management-0.1.5.jar` | Run app |

## Architecture

MVC with manual dependency injection wired in `App.java`. A `MotelManagement` facade delegates to service objects (`RoomManager`, `SellingService`, `TurnService`, `HistoryService`, `EmailConfigurationService`, `ProgramConfig`, `Register`, `Printer`, `FileManager`). A single `Controller` orchestrates 13 sub-controllers and 4 timers (clock, backup, floor rotation, overtime warning).

**Packages:** `controller`, `controller.sub`, `model`, `model.modelManagers`, `model.dto`, `model.email.config`, `model.email.dto`, `model.email.exception`, `model.email.service`, `model.json`, `model.print`, `model.turn`, `view`, `view.helpers`, `view.customListRenderes`, `view.interfaces`

A detailed [sequence diagram](docs/DIAGRAMS/v0.1.3.1.md) covering all 10 defensive validation layers across the MVC stack is available in the [diagrams index](docs/DIAGRAMS/DIAGRAMS.md).

## Functionality

The program targets a **touchscreen** interface, with touchscreen related control for list and Management
A main Floor view is present showing status of the program, a warning for overtime room booking is also present for multiple tower / floors configurations
Each room has 3 different selection for time, with a personalized modification for each room present on the configuration, as well to the price of each one (Currently only in COP)

### Printing

The four printed documents (room receipt, sale receipt, turn summary, turn detail) are laid out from JSON templates. A type without a customized template prints with the layout built into the program, so a fresh installation needs nothing configured; layouts are edited, previewed at the paper width of the configured printer and test-printed from *Options → Printer configuration → CONFIGURAR IMPRESION*. Format, field keys and styles: [print templates](docs/PRINT_TEMPLATES.md).


## Future features / roadmap

Features to be added, in no particular order of progress, WIP will be marked and are actively being worked on

- [x] **Initial setup flow:** allow the program to do a initial setup flow
- [ ] **Localization/Internationalization:** Once the program reaches a mature point, a i18n localization will be implemented (Currently in progress)
- [ ] **External door interface:** On a separate project it's meant to link external hardware door opening and closing for tracking of each room linked to it (Currently in progress, future github linking to the project will be available)
- [ ] **Personalization, encryption of history:** To apply encryption and data safety, future implementation for all data to be locally encrypted to the user requirements is being testes (Currently in progress)
- [x] **Report exportation:** Partially implemented for email report, future WhatsApp integration for reporting is in progress (email initial release: v0.1.4)
- [x] **Printer page personalization:** Receipts and turn reports are laid out from editable JSON templates (defaults tuned for a thermal printer). The template editor is reachable from *Options → Printer configuration → CONFIGURAR IMPRESION*; a type without a custom template keeps using the built-in layout. The editor previews each line on a sheet as wide as the paper the configured printer declares (so a line that wraps on paper wraps in the preview), with zoom, a paper selector for comparison and a page counter; **IMPRIMIR PRUEBA** prints the unsaved template with sample data through the same print job receipts use, without touching the turn or any stored data
- [ ] **Date and time customization:** On the effort to localization, a date and time customization will be managed for data saving and related (Currently in progress with localization)
- [ ] **Database integration:** A customizable database integration and base is being considered to not rely heavily on JSON data structures for consulting

## Known issues

- [x] PARTIALLY FIXED: Based on screen size, the grid can go past 25, visibility is affected ~~**Hardcode 25 grid limitation:** Due to some requirements of legibility, there was a limit made on the grid for the floor, a touch friendly floor navigation is currently being developed~~
- [x] **FIXED:** Fixed values to use a similar to BigDecimal approach, but with customazibility for different currencies and even custom values ~~**Hardcoded long values:** The logic is heavily based upon hardcoded long values for pricing or related, due to the COP nature is working as a whole long, implementation of doubles or floating points are being done for the project scope as a customizable option for currencies that manage decimals~~
- **History review broken:** The in program history review is currently broken.
- **Java Swing integration:** Development of view related elements started with Java Swing due to simplicity, swap to JavaFX for a touch friendly approach without using broken helpers is currently being tested for implementation

## Versions major changes:
- **0.1.5:** Receipts and turn reports are laid out from editable JSON templates, with a template editor, a preview at the paper size the printer declares and a test print. Customizing is optional: a type without a saved layout keeps using the built-in one, and no data or schema change is involved. Requires Java 25.
- **0.1.4.1:** Modified encryption type for potential sensitive data on e-mail to use UUID linked to the machine itself. Added a .csv for better static analysis on exportation options.
- **0.1.4:** Added email reporting to specific addresses (meant for motel manager, accounting, or related),
- **0.1.3.1:** Refactoring and initial setup finished, making the program fully functional for basic details, advanced data output configuration and related will come at a later date
- **0.1.3:** Commits done since start of 2026 comprise the current program version, better implementation, separation, and management options prepared, still requires a existing applicationProperties with basic data to function, known conflicts and issues are being actively worked on
- **0.1.2:** Up until commits done to September 27th 2025, basic functionality, requires existing applicationProperties to properly function

## **DISCLAIMER:**
This program has used LLM tools such as OpenCode, Claude, With open weight models such as Qwen, Deepseek, and related for code development, refinement and optimization (Though test creation was a godsend).

**Design decisions, feature planning, validation, and implementation are still done by me, TheAbsdag, the repository author**.

This is a educational project made on a simple understanding of MVC, UML, and Object oriented design, meant to apply, develop, and improve on those concepts on a real use case scenario with actual requirements, this is a highly personalized project for the use case.

Design files for the basic concept can be found on GitHub: [Project designs](https://github.com/TheAbsdag/New_Project_Designs)
