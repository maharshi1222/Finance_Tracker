# CSE706 End-Semester Evaluation Report — Finance Tracker

**Framework:** Scrum / Agile  
**Product:** Finance Tracker Android application  
**Student:** Maharshi Brahmbhatt  
**Team member:** Ayush Parekh (currently unavailable due to illness; confirm with instructor how the team requirement should be documented)  
**Jira project:** [FTRACK board](https://nuv-team-mcwblmpq.atlassian.net/jira/software/projects/FTRACK/boards/67/backlog)  
**Report status:** Draft — update with actual implementation evidence, Jira screenshots, CI results, and sprint outcomes before submission.

## 1. Product vision and goal

The product vision is to provide an Android application that helps users record income and expenses, understand their financial position, manage financial reminders, and use account-related screens with clear validation.

The project goal is to deliver a working, testable prototype and demonstrate Scrum planning, iterative delivery, version control, automated testing, continuous integration, metrics, and retrospective-driven improvement.

## 2. Team and capacity

Maharshi Brahmbhatt is currently responsible for implementation, Jira updates, tests, integration, and submission preparation. Ayush Parekh is unavailable due to illness. Sprint capacity should therefore be estimated as one active contributor until the situation changes. The instructor should be informed and asked how to handle any requirement that explicitly expects two-person pair programming or independent code review. Do not record invented attendance or reviews.

## 3. Product backlog

The Jira backlog is the source of truth for issue status and acceptance criteria. Current issues were created as To Do and must be updated only when work is actually completed.

| Jira issue | Scope | Estimate |
|---|---|---:|
| FTRACK-2 | Add and view income/expense transactions | 5 |
| FTRACK-3 | Financial analysis and income-expense difference | 5 |
| FTRACK-4 | Financial reminders | 3 |
| FTRACK-5 | Login/sign-up validation | 3 |
| FTRACK-6 | TDD unit tests for two core logic areas | 5 |
| FTRACK-7 | GitHub repository and branching workflow | 3 |
| FTRACK-8 | GitHub Actions CI build and tests | 5 |
| FTRACK-9 | Scrum evidence, metrics, risk log and report | 5 |
| FTRACK-10 | Android UI test and Agile Testing Quadrants | 5 |

Estimates are provisional planning points, not historical velocity.

## 4. Sprint plan

### Sprint 1 — Core tracking and engineering foundation

**Goal:** establish core transaction/analysis behavior and the development quality workflow.

Planned items: FTRACK-2, FTRACK-3, FTRACK-5, FTRACK-6, FTRACK-7. Planned total: 21 points. This is a provisional target and should be reduced if it exceeds realistic one-person capacity.

### Sprint 2 — Reminders, CI, validation, and submission

**Goal:** complete reminders, automate build/test checks, and prepare evidence and the report.

Planned items: FTRACK-4, FTRACK-8, FTRACK-9, FTRACK-10. Planned total: 18 points. This is a provisional target and should be revised based on Sprint 1 outcomes.

**Important:** both sprints were recorded as future in the available Jira snapshot. Do not present planned points as completed velocity. Capture actual start/end dates, review decisions, retrospective outcomes, and screenshots as the work occurs.

## 5. Engineering and test strategy

### Unit testing and TDD

`FinanceRules.java` separates pure Java validation/calculation logic from Android screens. `FinanceRulesTest.java` covers valid/invalid transaction input, numeric parsing, balance difference, and surplus/deficit/balanced classification. For strict TDD evidence, use a test-first commit that fails, then an implementation commit that passes, then a refactor commit if appropriate. Existing files alone do not prove that historical red-green-refactor steps occurred.

A direct `javac` smoke program compiled the helper and passed six checks. This is limited smoke-test evidence, not a full JUnit/Gradle result. The full Gradle test task must be run locally or in GitHub Actions and the actual output recorded.

### Android functional testing

`ExampleInstrumentedTest` launches `MainActivity` and checks the app package and activity. It must be run on an Android emulator/device. Selenium is not the default for a native Android application; Espresso/AndroidX instrumentation is more suitable. Postman is relevant only if the product exposes an API.

### Continuous integration

`.github/workflows/android-ci.yml` defines a push/PR workflow to run `testDebugUnitTest`, build the debug APK, upload the APK artifact, and run an emulator instrumentation job. CI has not been verified until the project is pushed to GitHub and a workflow run finishes. Record the run URL, status, and any failures here:

- GitHub repository URL: **[add after creating repository]**
- Successful/failed Actions run URL: **[add after first run]**
- Unit-test result: **[record actual output]**
- Debug build result: **[record actual output]**
- Instrumentation result: **[record actual output]**

## 6. Agile Testing Quadrants

- **Q1 — Technology-facing, supports the team:** JUnit unit tests for transaction validation, amount parsing, and financial calculations.
- **Q2 — Business-facing, supports the team:** acceptance scenarios for adding income/expenses, viewing summaries, login/sign-up validation, and reminders.
- **Q3 — Business-facing, critiques the product:** exploratory testing of navigation, invalid inputs, empty data, labels, usability, and error messages on a device/emulator.
- **Q4 — Technology-facing, critiques the product:** reliability, compatibility, performance, and security checks appropriate to the prototype and its Firebase integration.

Record the test cases actually executed and results; the quadrant descriptions are a test strategy, not evidence that every test has already run.

## 7. SOLID principles

- **Single Responsibility:** `FinanceRules` is a separate class for validation and calculation logic, reducing the amount of business logic embedded in Activities.
- **Open/Closed:** apply only where a real extension point is useful; avoid introducing abstractions without a concrete need.
- **Liskov Substitution:** preserve behavioral contracts for any subclasses/interfaces used.
- **Interface Segregation:** keep interfaces focused if interfaces are introduced.
- **Dependency Inversion:** where practical, separate business rules from infrastructure such as Firebase so the logic can be tested without network services.

Only claim principles supported by actual code changes. The clearest current example is the extraction of pure business rules into `FinanceRules`.

## 8. Metrics and progress analysis

Use Jira to capture committed points, completed points, issue status, and daily remaining work. Velocity is the sum of story points completed during a sprint, not the planned estimate. The initial Jira snapshot showed both sprints in the future and the selected issues To Do, so there is no valid completed velocity or burndown trend yet.

| Metric | Current value | Update rule |
|---|---|---|
| Sprint 1 planned points | 21 | Revise at planning if capacity requires |
| Sprint 1 completed points | Not yet measured | Sum points of completed sprint issues |
| Sprint 2 planned points | 18 | Revise after Sprint 1 retrospective |
| Sprint 2 completed points | Not yet measured | Sum points of completed sprint issues |
| Build/test pass rate | Not yet verified | Record actual CI/local results |

## 9. Risks and mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| One active contributor due to teammate illness | High | Reduce WIP, prioritize core requirements, inform instructor, document capacity honestly |
| Gradle/dependency download or Firebase configuration fails in CI | High | Run a clean build, inspect logs/configuration, record failures and fixes |
| Business logic is tightly coupled to Activities | Medium | Extract small pure Java methods and unit-test them |
| Scrum evidence is incomplete | High | Record real daily notes, reviews, retrospectives, screenshots, and Jira changes as they occur |
| Scope exceeds available time | High | Prioritize core transactions, analysis, tests, CI, and required report over optional polish |

## 10. Scrum event log templates

### Daily Scrum — complete once per actual working day

- Date/time:
- What I completed since the previous check-in:
- What I will work on next:
- Blockers/risks:
- Jira issue keys:
- Commit, test, screenshot, or other evidence:

### Sprint Review — complete after the actual review/demo

- Sprint and date:
- Demonstrated features:
- Acceptance criteria passed/failed:
- Feedback received (identify source accurately):
- Follow-up backlog changes:
- Evidence link/screenshot:

### Sprint Retrospective — complete after the actual retrospective

- What went well:
- What did not go well:
- Root cause or learning:
- One measurable improvement for the next sprint:
- Owner and due date:

## 11. Submission checklist

- [ ] Working Android prototype builds and runs
- [ ] Jira product backlog and sprint backlogs updated
- [ ] Actual Sprint Planning, Daily Scrum, Review, and Retrospective evidence captured
- [ ] GitHub repository and meaningful commit history available
- [ ] JUnit tests executed and results attached
- [ ] Android instrumentation tests executed or limitation documented
- [ ] GitHub Actions run URL and results included
- [ ] Story-point metrics and charts based on real Jira data
- [ ] Test scripts/reports and Agile Testing Quadrants included
- [ ] SOLID practices described with code examples
- [ ] Risk register and mitigations updated
- [ ] Instructor informed about the temporary one-person workload and any review/pairing constraint

## 12. Current status and limitations

The source package includes a pure Java rules helper, JUnit test source, Android instrumentation test source, GitHub Actions workflow, and this report draft. Six direct Java smoke checks passed. Full Gradle/JUnit execution, emulator execution, and a GitHub Actions run remain unverified. No completed sprint velocity is claimed. Update this report after actual runs and sprint events before submitting it.
