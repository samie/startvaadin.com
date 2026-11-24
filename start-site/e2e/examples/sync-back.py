#!/usr/bin/env python3
"""Copy edits made in a generated template project back into the template.

Polish a template in the real generated project (start-site/target/example-projects/
<id>/<artifactId>, e.g. with `./mvnw spring-boot:run` and live reload), then sync the
template-owned files back to start-site/src/main/resources/examples/<id>/: the
package path becomes `__pkg__` and the package name becomes `{{package}}`.

Usage:
  sync-back.py <id>             show what would change in the template (dry run)
  sync-back.py <id> --apply     write the changes to the template
  sync-back.py <id> --snapshot  record the generated project's state (check.sh does this)
  sync-back.py <id> --check     exit 1 if the project was edited since the snapshot

Synced: everything under src/main/java and src/test/java (except the generated
application class and its test), src/main/resources (except application.*), and
src/main/frontend (except generated/). Files missing from the project are removed
from the template. In text files the project's package, description and name (from
its pom) go back to `{{package}}`, `{{description}}` and `{{name}}`; binary files are
copied as-is.
"""

import difflib
import hashlib
import json
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[3]
OUTPUT = REPO / "start-site/target/example-projects"
EXAMPLES = REPO / "start-site/src/main/resources/examples"
EXCLUDED_RESOURCES = re.compile(r"^application(-[^/]*)?\.(properties|ya?ml)$|^vaadin-featureflags\.properties$")


def project_dir(template_id):
    candidates = [p for p in (OUTPUT / template_id).glob("*/pom.xml")]
    if len(candidates) != 1:
        sys.exit(f"No generated project for '{template_id}' in {OUTPUT / template_id}; run check.sh {template_id} first")
    return candidates[0].parent


def application_class(project):
    for path in (project / "src/main/java").rglob("*.java"):
        text = path.read_text()
        if "@SpringBootApplication" in text:
            package = re.search(r"^package\s+([\w.]+);", text, re.MULTILINE).group(1)
            return package, path.stem
    sys.exit(f"No @SpringBootApplication class in {project}")


def project_files(project, package, app_class):
    """Template-owned files of the project, as {template-relative path: Path}."""
    package_path = package.replace(".", "/")
    files = {}
    for root in ("src/main/java", "src/test/java"):
        base = project / root / package_path
        for path in sorted(base.rglob("*")) if base.is_dir() else []:
            if path.is_file() and path.name not in (f"{app_class}.java", f"{app_class}Tests.java"):
                files[f"{root}/__pkg__/{path.relative_to(base)}"] = path
    for root, excluded in (("src/main/resources", EXCLUDED_RESOURCES), ("src/main/frontend", re.compile(r"^generated/"))):
        base = project / root
        for path in sorted(base.rglob("*")) if base.is_dir() else []:
            relative = path.relative_to(base).as_posix()
            if path.is_file() and not excluded.search(relative):
                files[f"{root}/{relative}"] = path
    return files


def pom_value(project, tag):
    match = re.search(rf"<{tag}>([^<]+)</{tag}>", (project / "pom.xml").read_text().split("<dependencies>")[0])
    return match.group(1) if match else None


def to_template(text, project, package):
    text = re.sub(rf"\b{re.escape(package)}\b", "{{package}}", text)
    for tag in ("description", "name"):
        value = pom_value(project, tag)
        if value:
            text = text.replace(value, "{{" + tag + "}}")
    return text


def read(path):
    data = path.read_bytes()
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return data


def staged(template_id):
    project = project_dir(template_id)
    package, app_class = application_class(project)
    files = {}
    for target, path in project_files(project, package, app_class).items():
        content = read(path)
        if isinstance(content, bytes):
            files[target] = content
            continue
        text = to_template(content, project, package)
        if app_class in text:
            print(f"warning: {target} references {app_class}, which is named after the user's project", file=sys.stderr)
        files[target] = text
    return project, files


def digest(project):
    package, app_class = application_class(project)
    return {target: hashlib.sha256(path.read_bytes()).hexdigest()
            for target, path in project_files(project, package, app_class).items()}


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    template_id, mode = sys.argv[1], (sys.argv[2] if len(sys.argv) > 2 else "")
    template = EXAMPLES / template_id
    snapshot = OUTPUT / f"{template_id}.snapshot.json"

    if mode == "--snapshot":
        snapshot.write_text(json.dumps(digest(project_dir(template_id)), indent=1))
        return
    if mode == "--check":
        if not snapshot.exists() or not (OUTPUT / template_id).exists():
            return
        if json.loads(snapshot.read_text()) != digest(project_dir(template_id)):
            sys.exit(f"The generated '{template_id}' project was edited since it was generated. "
                     f"Run sync-back.py {template_id} --apply to keep the edits, or delete {OUTPUT / template_id} to discard them.")
        return
    if not template.is_dir():
        sys.exit(f"No template '{template_id}' in {EXAMPLES}")
    if any(template.rglob("*.tmpl")):
        sys.exit(f"{template} uses .tmpl files, which sync-back does not support")

    _, files = staged(template_id)
    existing = {p.relative_to(template).as_posix() for p in template.rglob("*") if p.is_file()}
    changed = False
    for target in sorted(existing | files.keys()):
        old = read(template / target) if target in existing else ""
        new = files.get(target, "")
        if old == new:
            continue
        changed = True
        label = "deleted" if target not in files else ("added" if target not in existing else "modified")
        print(f"--- {label}: {target}")
        if isinstance(old, str) and isinstance(new, str):
            sys.stdout.writelines(difflib.unified_diff(old.splitlines(True), new.splitlines(True), "template", "project", n=2))
        else:
            print("(binary)")
        if mode == "--apply":
            path = template / target
            if target in files:
                path.parent.mkdir(parents=True, exist_ok=True)
                if isinstance(new, bytes):
                    path.write_bytes(new)
                else:
                    path.write_text(new)
            else:
                path.unlink()
    if mode == "--apply":
        for directory in sorted((p for p in template.rglob("*") if p.is_dir()), reverse=True):
            if not any(directory.iterdir()):
                directory.rmdir()
        if changed:
            snapshot.write_text(json.dumps(digest(project_dir(template_id)), indent=1))
    if not changed:
        print(f"Template '{template_id}' is in sync with the generated project.")
    elif mode != "--apply":
        print(f"\nDry run. Apply with: {Path(sys.argv[0]).name} {template_id} --apply")


if __name__ == "__main__":
    main()
