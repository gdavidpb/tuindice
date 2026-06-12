#!/usr/bin/env python3

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
from string import Template


SCRIPT_DIR = Path(__file__).resolve().parent
SKILL_DIR = SCRIPT_DIR.parent
TEMPLATE_DIR = SKILL_DIR / "assets" / "templates"

TEMPLATE_FILES = {
    "build-gradle": "build.gradle.kts.tpl",
    "module": "module.kt.tpl",
    "contract": "contract.kt.tpl",
    "viewmodel": "viewmodel.kt.tpl",
    "machine": "machine.kt.tpl",
    "internal-event": "internal-event.kt.tpl",
    "transitions": "transitions.kt.tpl",
    "error-messages": "error-messages.kt.tpl",
    "machine-contract-test": "machine-contract-test.kt.tpl",
    "repository-interface": "repository-interface.kt.tpl",
    "repository-implementation": "repository-implementation.kt.tpl",
    "observe-use-case": "observe-use-case.kt.tpl",
    "observe-use-case-error": "observe-use-case-error.kt.tpl",
    "update-use-case": "update-use-case.kt.tpl",
    "update-use-case-error": "update-use-case-error.kt.tpl",
    "update-exception-handler": "update-exception-handler.kt.tpl",
    "validator": "validator.kt.tpl",
    "destination": "destination.kt.tpl",
    "navigation": "navigation.kt.tpl",
    "route": "route.kt.tpl",
    "screen": "screen.kt.tpl",
    "smoke-test": "smoke-test.kt.tpl",
    "test-doubles": "test-doubles.kt.tpl",
    "strings": "strings.xml.tpl",
}

SCAFFOLD_LAYOUT = {
    "build-gradle": "{module_dir}/build.gradle.kts",
    "strings": "{module_dir}/src/commonMain/composeResources/values/strings.xml",
    "module": "{module_dir}/src/commonMain/kotlin/{package_path}/di/{MODULE_FILE_NAME}.kt",
    "contract": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/contract/{FEATURE_NAME}.kt",
    "viewmodel": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/viewmodel/{VIEWMODEL_NAME}.kt",
    "machine": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/machine/{MACHINE_NAME}.kt",
    "internal-event": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/machine/{INTERNAL_EVENT_NAME}.kt",
    "transitions": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/transition/{FEATURE_NAME}Transitions.kt",
    "error-messages": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/mapper/{FEATURE_NAME}ErrorMessages.kt",
    "machine-contract-test": "{module_dir}/src/commonTest/kotlin/{package_path}/presentation/machine/{MACHINE_CONTRACT_TEST_CLASS_NAME}.kt",
    "destination": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/navigation/{DESTINATION_NAME}.kt",
    "navigation": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/navigation/{FEATURE_NAME}Navigation.kt",
    "route": "{module_dir}/src/commonMain/kotlin/{package_path}/presentation/route/{ROUTE_NAME}.kt",
    "screen": "{module_dir}/src/commonMain/kotlin/{package_path}/ui/screen/{SCREEN_NAME}.kt",
    "repository-interface": "{module_dir}/src/commonMain/kotlin/{package_path}/domain/repository/{REPOSITORY_INTERFACE_NAME}.kt",
    "repository-implementation": "{module_dir}/src/commonMain/kotlin/{package_path}/data/repository/{DATA_REPOSITORY_NAME}.kt",
    "observe-use-case": "{module_dir}/src/commonMain/kotlin/{package_path}/domain/usecase/{OBSERVE_USE_CASE_NAME}.kt",
    "observe-use-case-error": "{module_dir}/src/commonMain/kotlin/{package_path}/domain/usecase/error/{OBSERVE_USE_CASE_ERROR_NAME}.kt",
    "update-use-case": "{module_dir}/src/commonMain/kotlin/{package_path}/domain/usecase/{UPDATE_USE_CASE_NAME}.kt",
    "update-use-case-error": "{module_dir}/src/commonMain/kotlin/{package_path}/domain/usecase/error/{UPDATE_USE_CASE_ERROR_NAME}.kt",
    "update-exception-handler": "{module_dir}/src/commonMain/kotlin/{package_path}/domain/usecase/exceptionhandler/{UPDATE_EXCEPTION_HANDLER_NAME}.kt",
    "smoke-test": "{module_dir}/src/commonTest/kotlin/{package_path}/di/{SMOKE_TEST_CLASS_NAME}.kt",
    "test-doubles": "{module_dir}/src/commonTest/kotlin/{package_path}/testing/{FEATURE_NAME}TestDoubles.kt",
}


def escape_kotlin_string(value: str) -> str:
    return '"' + value.replace("\\", "\\\\").replace('"', '\\"') + '"'


def escape_xml(value: str) -> str:
    return (
        value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
        .replace("'", "&apos;")
    )


def normalize_module_name(value: str) -> str:
    if not re.fullmatch(r"[a-z][a-z0-9]*", value):
        raise ValueError("`--module` must be lowercase alphanumeric and start with a letter.")
    return value


def infer_feature_name(module_name: str) -> str:
    return module_name[:1].upper() + module_name[1:]


def lower_first(value: str) -> str:
    return value[:1].lower() + value[1:] if value else value


def build_context(args: argparse.Namespace) -> dict[str, str]:
    module_name = normalize_module_name(args.module)
    feature_name = args.feature_name or infer_feature_name(module_name)
    if not re.fullmatch(r"[A-Z][A-Za-z0-9]*", feature_name):
        raise ValueError("`--feature-name` must be PascalCase.")

    feature_lower_camel = lower_first(feature_name)
    package_prefix = args.package_prefix.rstrip(".")
    package_name = f"{package_prefix}.{module_name}"
    package_path = package_name.replace(".", "/")
    screen_title = args.screen_title or feature_name
    top_bar_config = args.top_bar_config
    bottom_bar_visible = not args.hide_bottom_bar

    with_persistence = getattr(args, "with_persistence", False)
    with_device_tests = getattr(args, "with_device_tests", False)

    with_persistence_block = ""
    if with_persistence:
        with_persistence_block = '\n\t\t\t\timplementation(project(":persistence"))'

    android_device_setup_block = ""
    android_device_source_set_block = ""
    if with_device_tests:
        android_device_setup_block = (
            "\t\twithDeviceTest {\n"
            '\t\t\tinstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"\n'
            "\t\t}\n"
        )
        android_device_source_set_block = (
            "\n\t\tval androidDeviceTest by getting {\n"
            "\t\t\tdependencies {\n"
            "\t\t\t\timplementation(project.dependencies.platform(libs.compose.bom))\n"
            "\t\t\t\timplementation(libs.bundles.testing.android)\n"
            "\t\t\t\timplementation(libs.test.ext.junit)\n"
            "\t\t\t\timplementation(libs.compose.ui.test.junit4)\n"
            "\t\t\t}\n"
            "\t\t}\n"
        )

    context = {
        "MODULE_NAME": module_name,
        "FEATURE_NAME": feature_name,
        "FEATURE_LOWER_CAMEL": feature_lower_camel,
        "PACKAGE_PREFIX": package_prefix,
        "PACKAGE": package_name,
        "PACKAGE_PATH": package_path,
        "SCREEN_TITLE_LITERAL": escape_kotlin_string(screen_title),
        "SCREEN_TITLE_XML": escape_xml(screen_title),
        "READY_MESSAGE_LITERAL": escape_kotlin_string(f"{screen_title} listo"),
        "TOP_BAR_CONFIG": top_bar_config,
        "BOTTOM_BAR_VISIBLE_LITERAL": "true" if bottom_bar_visible else "false",
        "WITH_PERSISTENCE_DEPENDENCY_BLOCK": with_persistence_block,
        "ANDROID_DEVICE_TEST_SETUP_BLOCK": android_device_setup_block,
        "ANDROID_DEVICE_TEST_SOURCESET_BLOCK": android_device_source_set_block,
        "MODULE_FILE_NAME": f"{feature_name}Module",
        "MODULE_VAR_NAME": f"{feature_lower_camel}Module",
        "VIEWMODEL_NAME": f"{feature_name}ViewModel",
        "MACHINE_NAME": f"{feature_name}Machine",
        "INTERNAL_EVENT_NAME": f"{feature_name}InternalEvent",
        "TRANSITIONS_FUNCTION_NAME": f"{feature_lower_camel}Transitions",
        "MACHINE_CONTRACT_TEST_CLASS_NAME": f"{feature_name}StateMachineContractTest",
        "CONTENT_OBSERVED_EVENT_NAME": f"{feature_name}ContentObserved",
        "OBSERVATION_FAILED_EVENT_NAME": f"{feature_name}ObservationFailed",
        "REFRESH_STARTED_EVENT_NAME": f"{feature_name}RefreshStarted",
        "REFRESH_FAILED_EVENT_NAME": f"{feature_name}RefreshFailed",
        "OBSERVE_ACTION_NAME": f"Observe{feature_name}",
        "REFRESH_ACTION_NAME": f"Refresh{feature_name}",
        "REFRESH_METHOD_NAME": f"refresh{feature_name}Action",
        "OBSERVE_USE_CASE_NAME": f"Observe{feature_name}UseCase",
        "OBSERVE_USE_CASE_PARAM_NAME": f"observe{feature_name}UseCase",
        "OBSERVE_USE_CASE_ERROR_NAME": f"Observe{feature_name}UseCaseError",
        "UPDATE_USE_CASE_NAME": f"Update{feature_name}UseCase",
        "UPDATE_USE_CASE_PARAM_NAME": f"update{feature_name}UseCase",
        "UPDATE_USE_CASE_ERROR_NAME": f"Update{feature_name}UseCaseError",
        "UPDATE_EXCEPTION_HANDLER_NAME": f"Update{feature_name}ExceptionHandler",
        "REPOSITORY_INTERFACE_NAME": f"{feature_name}Repository",
        "REPOSITORY_PARAM_NAME": f"{feature_lower_camel}Repository",
        "DATA_REPOSITORY_NAME": f"{feature_name}DataRepository",
        "DESTINATION_NAME": f"{feature_name}Destination",
        "ROUTE_NAME": f"{feature_name}Route",
        "SCREEN_NAME": f"{feature_name}Screen",
        "NAVIGATION_FUNCTION_NAME": f"{feature_lower_camel}Navigation",
        "SMOKE_TEST_CLASS_NAME": f"{feature_name}ModuleKoinSmokeTest",
        "RECORDING_REPOSITORY_NAME": f"Recording{feature_name}Repository",
        "GENERATED_RESOURCES_PACKAGE": f"tuindice.{module_name}.generated.resources",
        "VALIDATOR_NAME": f"{feature_name}ParamsValidator",
        "PARAMS_NAME": f"{feature_name}Params",
        "MODULE_DIR_BASENAME": module_name,
    }
    return context


def render_template(template_name: str, context: dict[str, str]) -> str:
    template_file = TEMPLATE_DIR / TEMPLATE_FILES[template_name]
    template = Template(template_file.read_text())
    return template.safe_substitute(context)


def insert_before(text: str, marker: str, snippet: str, error_message: str) -> tuple[str, bool]:
    if snippet in text:
        return text, False
    index = text.find(marker)
    if index == -1:
        raise ValueError(error_message)
    return text[:index] + snippet + text[index:], True


def read_required_file(path: Path) -> str:
    if not path.exists():
        raise ValueError(f"Missing file: {path}")
    return path.read_text()


def write_text_if_changed(path: Path, content: str, dry_run: bool) -> bool:
    current = read_required_file(path)
    if current == content:
        return False
    if not dry_run:
        path.write_text(content)
    return True


def write_file(path: Path, content: str, force: bool) -> None:
    if path.exists() and not force:
        raise FileExistsError(f"Refusing to overwrite existing file without --force: {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content)


def run_scaffold_feature(args: argparse.Namespace) -> int:
    context = build_context(args)
    root = Path(args.root).resolve()
    module_dir = root / context["MODULE_NAME"]
    expanded_context = {
        **context,
        "module_dir": str(module_dir),
        "package_path": context["PACKAGE_PATH"],
    }

    created_files = []
    for template_name, target_pattern in SCAFFOLD_LAYOUT.items():
        target_path = Path(target_pattern.format(**expanded_context))
        content = render_template(template_name, context)
        write_file(target_path, content, args.force)
        created_files.append(target_path)

    print(f"Created {len(created_files)} files under {module_dir}")
    print("Next integration steps:")
    print("- add the module to settings.gradle.kts")
    print("- add dependencies in maincore/build.gradle.kts and app/build.gradle.kts if needed")
    print("- register the feature in maincore/.../SharedModules.kt when it is part of the shared runtime")
    print("- wire navigation from maincore/.../TuIndiceNavHost.kt when the feature is reachable")
    print("- adjust README.md if architectural boundaries change")
    return 0


def run_render_template(args: argparse.Namespace) -> int:
    context = build_context(args)
    content = render_template(args.template, context)

    if args.output:
        output = Path(args.output).resolve()
        write_file(output, content, args.force)
        print(output)
        return 0

    sys.stdout.write(content)
    return 0


def integrate_settings_gradle(text: str, context: dict[str, str]) -> tuple[str, bool]:
    module_literal = f'":{context["MODULE_NAME"]}"'
    if module_literal in text:
        return text, False

    match = re.search(r"include\(\n(?P<body>.*?)\n\)", text, re.S)
    if not match:
        raise ValueError("Could not find include(...) block in settings.gradle.kts")

    body = match.group("body").rstrip()
    new_body = f'{body},\n\t":{context["MODULE_NAME"]}"'
    new_text = text[: match.start("body")] + new_body + text[match.end("body") :]
    return new_text, True


def integrate_project_dependency(
    text: str,
    module_name: str,
    indentation: str,
    insert_before_marker: str | None = None,
) -> tuple[str, bool]:
    snippet = f'{indentation}implementation(project(":{module_name}"))\n'
    dependency_literal = f'implementation(project(":{module_name}"))'
    if dependency_literal in text:
        return text, False

    if insert_before_marker is not None:
        return insert_before(
            text=text,
            marker=insert_before_marker,
            snippet=snippet,
            error_message=f"Could not find dependency anchor `{insert_before_marker.strip()}`.",
        )

    matches = list(re.finditer(r'^[ \t]+implementation\(project\(":[^"]+"\)\)\n?', text, re.M))
    if not matches:
        raise ValueError("Could not find an existing project dependency block to extend.")

    last_match = matches[-1]
    return text[: last_match.end()] + snippet + text[last_match.end() :], True


def integrate_shared_modules(text: str, context: dict[str, str]) -> tuple[str, bool]:
    changed = False

    import_snippet = f'import com.gdavidpb.tuindice.{context["MODULE_NAME"]}.di.{context["MODULE_VAR_NAME"]}\n'
    text, import_changed = insert_before(
        text=text,
        marker="import org.koin.core.module.Module\n",
        snippet=import_snippet,
        error_message="Could not find org.koin import anchor in SharedModules.kt",
    )
    changed = changed or import_changed

    entry = f'\t\t{context["MODULE_VAR_NAME"]}'
    if entry not in text:
        match = re.search(
            r"(fun featureModules\(\): List<Module> \{\n\treturn listOf\(\n)(?P<body>.*?)(\n\t\)\n\})",
            text,
            re.S,
        )
        if not match:
            raise ValueError("Could not find featureModules() list in SharedModules.kt")

        body = match.group("body").rstrip()
        new_body = f"{body},\n{entry}"
        text = text[: match.start("body")] + new_body + text[match.end("body") :]
        changed = True

    return text, changed


def integrate_nav_host(text: str, context: dict[str, str]) -> tuple[str, bool]:
    changed = False

    import_snippet = (
        f'import com.gdavidpb.tuindice.{context["MODULE_NAME"]}.presentation.navigation.'
        f'{context["NAVIGATION_FUNCTION_NAME"]}\n'
    )
    text, import_changed = insert_before(
        text=text,
        marker="import com.gdavidpb.tuindice.ui.MaincoreUiTags\n",
        snippet=import_snippet,
        error_message="Could not find UI import anchor in TuIndiceNavHost.kt",
    )
    changed = changed or import_changed

    navigation_call = (
        f'\n\t\t{context["NAVIGATION_FUNCTION_NAME"]}(\n'
        '\t\t\tonViewStateChanged = onViewStateChanged,\n'
        '\t\t\tshowSnackBar = showSnackBar\n'
        '\t\t)\n'
    )
    text, navigation_changed = insert_before(
        text=text,
        marker="\n\t\tbrowserNavigation(",
        snippet=navigation_call,
        error_message="Could not find browserNavigation anchor in TuIndiceNavHost.kt",
    )
    changed = changed or navigation_changed

    return text, changed


def run_integrate_feature(args: argparse.Namespace) -> int:
    context = build_context(args)
    root = Path(args.root).resolve()
    module_dir = root / context["MODULE_NAME"]

    if not args.skip_module_check and not module_dir.exists():
        raise ValueError(
            f"Expected module directory to exist before integration: {module_dir}. "
            "Run scaffold-feature first or pass --skip-module-check."
        )

    file_paths = {
        "settings.gradle.kts": root / "settings.gradle.kts",
        "maincore/build.gradle.kts": root / "maincore" / "build.gradle.kts",
        "app/build.gradle.kts": root / "app" / "build.gradle.kts",
        "SharedModules.kt": root / "maincore" / "src" / "commonMain" / "kotlin" / "com" / "gdavidpb" / "tuindice" / "di" / "SharedModules.kt",
        "TuIndiceNavHost.kt": root / "maincore" / "src" / "commonMain" / "kotlin" / "com" / "gdavidpb" / "tuindice" / "ui" / "screen" / "TuIndiceNavHost.kt",
    }

    changed_files: list[str] = []

    settings_text = read_required_file(file_paths["settings.gradle.kts"])
    settings_text, _ = integrate_settings_gradle(settings_text, context)
    if write_text_if_changed(file_paths["settings.gradle.kts"], settings_text, args.dry_run):
        changed_files.append("settings.gradle.kts")

    maincore_build_text = read_required_file(file_paths["maincore/build.gradle.kts"])
    maincore_build_text, _ = integrate_project_dependency(
        text=maincore_build_text,
        module_name=context["MODULE_NAME"],
        indentation="\t\t\t\t",
        insert_before_marker='\t\t\t\timplementation(libs.koin.compose)\n',
    )
    if write_text_if_changed(file_paths["maincore/build.gradle.kts"], maincore_build_text, args.dry_run):
        changed_files.append("maincore/build.gradle.kts")

    app_build_text = read_required_file(file_paths["app/build.gradle.kts"])
    app_build_text, _ = integrate_project_dependency(
        text=app_build_text,
        module_name=context["MODULE_NAME"],
        indentation="\t",
    )
    if write_text_if_changed(file_paths["app/build.gradle.kts"], app_build_text, args.dry_run):
        changed_files.append("app/build.gradle.kts")

    shared_modules_text = read_required_file(file_paths["SharedModules.kt"])
    shared_modules_text, _ = integrate_shared_modules(shared_modules_text, context)
    if write_text_if_changed(file_paths["SharedModules.kt"], shared_modules_text, args.dry_run):
        changed_files.append("maincore/.../SharedModules.kt")

    if not args.skip_nav_host:
        nav_host_text = read_required_file(file_paths["TuIndiceNavHost.kt"])
        nav_host_text, _ = integrate_nav_host(nav_host_text, context)
        if write_text_if_changed(file_paths["TuIndiceNavHost.kt"], nav_host_text, args.dry_run):
            changed_files.append("maincore/.../TuIndiceNavHost.kt")

    if changed_files:
        mode_label = "Would update" if args.dry_run else "Updated"
        print(f"{mode_label} {len(changed_files)} files:")
        for changed_file in changed_files:
            print(f"- {changed_file}")
    else:
        print("No integration changes were needed.")

    print("Manual follow-up still required:")
    print("- review TuIndiceNavHost wiring if the feature navigation signature differs from the scaffold baseline")
    print("- update BottomBarConfig.kt and TuIndiceScreen.kt if the feature should be a top-level tab")
    print("- update root build.gradle.kts verification task lists if the module should be included there")
    print("- update README.md if architectural boundaries changed")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Scaffold TuIndice module boilerplate.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    subparsers.add_parser("list-templates", help="List available template keys.")

    scaffold = subparsers.add_parser("scaffold-feature", help="Scaffold a baseline shared feature module.")
    scaffold.add_argument("--module", required=True)
    scaffold.add_argument("--feature-name")
    scaffold.add_argument("--screen-title")
    scaffold.add_argument("--package-prefix", default="com.gdavidpb.tuindice")
    scaffold.add_argument("--top-bar-config", choices=["Summary", "Record"], default="Summary")
    scaffold.add_argument("--hide-bottom-bar", action="store_true")
    scaffold.add_argument("--with-persistence", action="store_true")
    scaffold.add_argument("--with-device-tests", action="store_true")
    scaffold.add_argument("--root", default=".")
    scaffold.add_argument("--force", action="store_true")

    render = subparsers.add_parser("render-template", help="Render a single component template.")
    render.add_argument("template", choices=sorted(TEMPLATE_FILES))
    render.add_argument("--module", required=True)
    render.add_argument("--feature-name")
    render.add_argument("--screen-title")
    render.add_argument("--package-prefix", default="com.gdavidpb.tuindice")
    render.add_argument("--top-bar-config", choices=["Summary", "Record"], default="Summary")
    render.add_argument("--hide-bottom-bar", action="store_true")
    render.add_argument("--with-persistence", action="store_true")
    render.add_argument("--with-device-tests", action="store_true")
    render.add_argument("--output")
    render.add_argument("--force", action="store_true")

    integrate = subparsers.add_parser(
        "integrate-feature",
        help="Integrate a scaffolded shared feature into the repo wiring.",
    )
    integrate.add_argument("--module", required=True)
    integrate.add_argument("--feature-name")
    integrate.add_argument("--screen-title")
    integrate.add_argument("--package-prefix", default="com.gdavidpb.tuindice")
    integrate.add_argument("--top-bar-config", choices=["Summary", "Record"], default="Summary")
    integrate.add_argument("--hide-bottom-bar", action="store_true")
    integrate.add_argument("--root", default=".")
    integrate.add_argument("--skip-module-check", action="store_true")
    integrate.add_argument("--skip-nav-host", action="store_true")
    integrate.add_argument("--dry-run", action="store_true")

    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()

    if args.command == "list-templates":
        for template_name in sorted(TEMPLATE_FILES):
            print(template_name)
        return 0

    try:
        if args.command == "scaffold-feature":
            return run_scaffold_feature(args)
        if args.command == "render-template":
            return run_render_template(args)
        if args.command == "integrate-feature":
            return run_integrate_feature(args)
    except (ValueError, FileExistsError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1

    parser.print_help()
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
