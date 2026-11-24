# Vaadin UI end-to-end tests

[Playwright](https://playwright.dev/) validation suite for the start.spring.io
server-side Vaadin UI (`io.spring.start.site.ui`). It drives a real browser against
a running instance and covers the three primary flows: **adding a dependency**,
**generating a project**, and **exploring** the generated sources.

## Prerequisites

- Node.js 18+ and the app running at `http://localhost:8080`.

Start the app (either works):

```bash
# dev mode
cd start-site && ../mvnw spring-boot:run

# or a production jar
cd start-site && ../mvnw -Pproduction -pl . -am package -DskipTests
java -Dvaadin.productionMode=true -jar target/start-site-exec.jar
```

## Install & run

```bash
cd start-site/e2e
npm install
npx playwright install chromium
npm test                 # run all specs
npx playwright test explore.spec.js   # a single spec
npm run report           # open the HTML report
```

Point the suite at a different instance with `START_SITE_BASE_URL`:

```bash
START_SITE_BASE_URL=http://localhost:9000 npm test
```

## Layout

- `tests/helpers.js` — navigation + a reusable `addDependency()` flow and the
  `dialog()` scope (Vaadin dialog content is teleported into a zero-size host, so we
  scope to `vaadin-dialog-content:visible`).
- `tests/dependencies.spec.js` — picker search, add, chip, remove, no-match placeholder,
  filter retained after toggle.
- `tests/a11y.spec.js` — accessible names on the theme toggle and chip remove buttons,
  and the labelled "More" menu.
- `tests/generate.spec.js` — Generate downloads a non-empty `demo.zip` and shows a
  confirmation notification.
- `tests/explore.spec.js` — Explore tree renders, placeholder before selection, file
  content loads, dialog closes.

## Template smoke tests (`examples/`)

`examples/` holds a separate suite for the projects **generated** from the UI templates, not
for the start site itself. Run it through the gate script from the repo root:

```bash
start-site/e2e/examples/check.sh all          # generate + mvnw verify + boot + smoke, every template
start-site/e2e/examples/check.sh crud --skip-build   # reuse the last generated build
```

Each `<id>.spec.js` runs against the generated app (`EXAMPLE_BASE_URL`) and saves a 640×400
screenshot to `start-site/target/example-projects/<id>.png`. Add
`--update-screenshots` to write the catalog card images instead (they are hand-made illustrations).

To polish a template, edit the generated project in `start-site/target/example-projects/<id>/`,
then copy the edits back with `examples/sync-back.py <id>` (a dry-run diff) and
`examples/sync-back.py <id> --apply`.

## Findings

UX/a11y behaviors these tests surfaced are tracked in
[`../../TEST_FINDINGS.md`](../../TEST_FINDINGS.md). All previously-recorded findings are
now fixed and each is guarded by a test here — the suite is fully green with no
`test.fail()` annotations.
