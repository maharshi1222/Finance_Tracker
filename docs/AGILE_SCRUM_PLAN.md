# CSE706 Agile/Scrum Plan — Finance Tracker

## Product vision
Provide an Android application that helps users record income and expenses, understand their financial position, manage financial reminders, and safely use account-related screens.

## Product goal
Deliver a working, testable Finance Tracker prototype and demonstrate Scrum planning, iterative delivery, version control, automated testing, CI, metrics, and retrospective-driven improvement.

## Team and capacity note
- Maharshi Brahmbhatt: currently responsible for implementation, Jira updates, tests, integration, and submission preparation.
- Ayush Parekh: teammate currently unavailable due to illness.
- Re-plan capacity conservatively while the team is effectively operating as one contributor. Record the real situation and actual attendance. Do not fabricate daily Scrum attendance, code reviews, pair programming, or contributions by an unavailable teammate. Ask the instructor how to handle the review/pair-programming requirement if a second participant remains unavailable.

## Scrum events and evidence
For each actual working day, record: date; what was completed since the last check-in; next work; blockers; Jira issue keys; commit/PR/test evidence. Do not backdate or invent logs.

For each sprint:
1. Sprint Planning: record goal, selected Jira issues, estimates, capacity assumption, and exclusions.
2. Daily Scrum: maintain short factual notes.
3. Sprint Review: demonstrate working behavior and record feedback.
4. Retrospective: record what went well, what did not, and one measurable improvement for the next sprint.

## Initial sprint plan (provisional; adjust after inspecting actual capacity)

### Sprint 1 — Core tracking and engineering foundation
Goal: establish the core transaction/analysis flow and the development quality workflow.
- FTRACK-2 — add/view income and expense transactions (5 points)
- FTRACK-3 — financial analysis and income-expense difference (5 points)
- FTRACK-5 — login/sign-up validation (3 points)
- FTRACK-6 — TDD unit tests for two business-logic modules (5 points)
- FTRACK-7 — GitHub repository, branches, PR/review workflow (3 points)

Planned estimate: 21 points. This is a provisional backlog estimate, not historical velocity. Reduce scope if one-person capacity cannot support it.

### Sprint 2 — Reminders, CI, validation, and submission
Goal: complete reminders, automate build/test checks, and prepare evidence/report.
- FTRACK-4 — reminders (3 points)
- FTRACK-8 — GitHub Actions CI (5 points)
- FTRACK-9 — Scrum logs, metrics, risks, and report (5 points)
- FTRACK-10 — Android UI functional tests and Agile Testing Quadrants (5 points)

Planned estimate: 18 points. Treat this as a provisional target; use actual completed work to report velocity.

## Acceptance criteria / Definition of Done
- Each story has testable acceptance criteria in Jira.
- Changes are built and tested; test results are recorded.
- Work is committed with descriptive messages and linked to Jira issue keys where practical.
- Pull requests are used when a second reviewer is available; otherwise document a self-review and the constraint honestly.
- Jira status reflects reality; do not mark unfinished work Done for presentation purposes.
- Review/demo and retrospective observations are recorded after each sprint.

## TDD and testing approach
Select two modules whose business logic can be tested without launching an Activity. Follow red → green → refactor and retain the test/implementation commits. Prefer JUnit for pure business logic and AndroidX instrumentation/Espresso for Android UI flows where appropriate. Selenium is for web UI and is not the right default for this native Android app. Postman applies only if there is an API to test.

## Agile Testing Quadrants (conceptual)
- Q1, technology-facing / supports the team: JUnit unit tests for transaction validation and financial calculations.
- Q2, business-facing / supports the team: acceptance tests and repeatable scenario checks for adding income/expenses, viewing analysis, and reminders.
- Q3, business-facing / critiques the product: exploratory testing of navigation, empty states, invalid inputs, and usability on emulator/device.
- Q4, technology-facing / critiques the product: reliability, compatibility, performance, and security checks appropriate to project scope.

## SOLID practices to evaluate
- Single Responsibility: separate calculation/validation from Activity UI where feasible.
- Open/Closed: extend behavior with small strategy/helper abstractions instead of repeated conditionals when justified.
- Liskov Substitution: preserve contracts for any subclasses/interfaces used.
- Interface Segregation: keep interfaces narrow if abstractions are introduced.
- Dependency Inversion: depend on abstractions for logic that needs substitution/testing where practical.
Document actual examples and avoid claiming principles that were not applied.

## Metrics
Track story points committed and completed, issue counts by status, and a daily remaining-work series for a burndown chart. Velocity is completed story points per sprint, not planned points. At the time of writing, both sprints are future and the issues are To Do; therefore completed velocity is not yet available. Capture Jira screenshots after real progress.

## Risk register
| Risk | Likelihood / impact | Mitigation |
|---|---|---|
| One contributor due to teammate illness | High / High | Reduce WIP, prioritize must-have scope, inform instructor, and document capacity assumptions. |
| Android build or Firebase configuration fails in CI | Medium / High | Run a local clean build first, inspect config files/rules, and keep CI logs as evidence. |
| Business logic is tightly coupled to Activities | Medium / Medium | Extract small pure Java calculation/validation methods before adding tests. |
| Insufficient evidence for Scrum events | Medium / High | Capture real notes/screenshots as events happen; never fabricate past events. |
| Scope exceeds available time | High / High | Prioritize core transactions, analysis, two TDD modules, and CI before optional polish. |

## Submission checklist from the course brief
- Working software prototype
- Product backlog, sprint backlog, and sprint progress charts
- Summary of Scrum roles/events actually followed
- Design and testing practices adopted
- Test scripts and test reports
- Metrics analysis and interpretation
- Risks and mitigation actions
- Optional sprint demo video


## Implementation update — 25 October 2026

- Added `FinanceRules`, a pure-Java helper used by the transaction form to validate amount/type/category and by the Analysis screen to parse Firestore amount values consistently.
- Added `FinanceRulesTest` JUnit cases for valid/invalid transaction input, zero/negative/non-finite values, missing type/category, amount parsing, balance difference, and surplus/deficit/balanced outcomes.
- Updated the Android instrumentation smoke test to launch `MainActivity` and assert the application package.
- Added a GitHub Actions emulator job to run `connectedDebugAndroidTest` after the unit-test/build job.
- Verification actually completed in the preparation environment: `FinanceRules.java` compiled with `javac`, and five direct Java smoke checks passed. Full Gradle/JUnit/build/emulator execution is **not yet verified** because the environment could not download Gradle from `services.gradle.org` (`UnknownHostException`). Do not report the full suite as passing until CI or a local Android Studio run confirms it.
- The test cases were added and the implementation integrated in the same preparation pass; to demonstrate the strict red → green → refactor TDD cycle for the course, preserve separate test-first and implementation commits when carrying out the next changes in Git.

## Test scenario matrix

| Area | Scenario | Expected result | Evidence to capture |
|---|---|---|---|
| Transaction validation | Positive amount, valid type and category | Accepted | JUnit result + emulator screenshot |
| Transaction validation | Empty, malformed, zero, negative, NaN or infinite amount | Rejected with a useful validation message | JUnit result |
| Transaction validation | Missing transaction type or placeholder category | Rejected | JUnit result |
| Analysis | Numeric Firestore amount and currency-formatted string | Amount parsed consistently | JUnit result |
| Analysis | Income 100 and expense 40 | Difference 60; surplus classification | JUnit result |
| Analysis | Income below expense / equal to expense | Deficit / balanced classification | JUnit result |
| App launch | Main activity starts and package is correct | Instrumentation smoke test passes | GitHub Actions emulator log |
| CI | Push or pull request | Unit tests, debug build and instrumentation test run | Actions run URL and screenshots |

## Daily Scrum note template (complete only after the real check-in)

- Date/time:
- Completed since last check-in (issue keys and evidence):
- Next planned work:
- Blockers/risks:
- Capacity note: Maharshi is currently the sole active contributor; Ayush is unavailable due to illness.

## Sprint Review template

- Sprint and date:
- Issues actually completed (include links/commits):
- Demonstrated user-visible behavior:
- Test/build results (include exact run and outcome):
- Feedback received from actual reviewer/instructor, or “no external feedback yet”:
- Accepted work / work carried forward:

## Sprint Retrospective template

- What went well (with example):
- What did not go well (with example):
- One concrete improvement for the next sprint:
- How the improvement will be measured:

Do not backdate or fill these templates with invented events. Since this is currently a one-contributor effort, record solo planning and self-review honestly and ask the instructor how to satisfy any requirement that explicitly expects a second participant.
