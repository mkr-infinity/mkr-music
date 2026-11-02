
# MKR Music — AI Agent Instructions

## 1. Project Overview

- Project: MKR Music, an open-source, ad-free Android YouTube Music client written in Kotlin.
- Features include online streaming, offline playback, equalizer and a modern music-listening experience.
- Design identity: Expressive Material 3.
- Prioritize performance, battery efficiency, stability and maintainability.
- Preserve existing features, architecture and user changes.
- Never make unrelated changes or remove functionality without authorization.
- Keep implementations minimal, complete and maintainable.

## 2. Required Skills and Context

- MUST use the Ponytail skill for every coding task.
- If Ponytail is unavailable, install and use it from:
  https://github.com/DietrichGebert/ponytail/raw/refs/heads/main/skills/ponytail/SKILL.md
- Use relevant Kotlin, Android, Jetpack Compose and other domain-specific skills whenever applicable.
- Read applicable `AGENTS.md` instructions and project rules before editing.
- Inspect the actual implementation, architecture and execution paths before making assumptions.
- Follow Ponytail's minimal-change approach: solve the complete task with the smallest reliable implementation.
- Reuse existing components and patterns. Avoid placeholders, unnecessary abstractions and unfinished code.

## 3. Git Workflow

- Always work directly on the existing `main` branch.
- Never create or switch branches.
- Before editing, inspect the current branch, working tree and recent commits.
- Check whether `origin/main` has changes that need pulling.
- Fetch and compare with `origin/main` when necessary.
- Pull only when the working tree is clean and the local branch can safely fast-forward.
- Never overwrite, reset, discard, clean or revert existing user changes.
- If pulling is unsafe or branches have diverged, stop and report the situation.
- Never push or merge unless explicitly requested and authorized by the user.
- When committing is authorized, stage only task-related files and inspect the staged diff first.
- Never create empty commits, force-push or rewrite existing commit history.
- Never create pull requests unless explicitly authorized.

## 4. Commit Convention

When committing is explicitly authorized:

- Use short, genuine and meaningful commit messages.
- Prefer `type(scope): short meaningful description`.
- Examples:
  - `fix(player): restore playback progress`
  - `feat(search): improve music discovery`
  - `style(library): improve Material 3 layout`
  - `perf(player): reduce unnecessary updates`

## 5. Documentation and Version Restrictions

- Do not modify README files or any Markdown files, including `AGENTS.md`, without explicit human authorization and verification.
- Do not create unnecessary documentation or report files.
- Never bump or modify the application's version in any way unless explicitly requested and authorized by the user. 
- Do not modify unrelated project configuration or metadata.

## 6. Build and Testing Restrictions

- NEVER build or compile the application locally.
- NEVER run Gradle, `./gradlew` or local build commands.
- Do not attempt to bypass these restrictions using alternative build tools.
- Avoid heavy local operations, unnecessary downloads and resource-intensive tasks.
- Do not trigger GitHub Actions unless explicitly requested.
- Perform safe static checks,errors inspect references and review source changes.
- Never claim a build, test or runtime check succeeded without actually verifying it.
- Clearly identify anything that still requires runtime or physical-device testing.

## 7. Development Standards

- Follow idiomatic Kotlin and Android development practices.
- Reuse existing architecture, components, services and state management.
- Avoid duplicate logic, unnecessary dependencies and excessive background work.
- Handle errors, loading states, cancellation and lifecycle changes properly.
- Use appropriate coroutine scopes and never perform expensive operations on the main thread.
- Preserve existing playback, streaming, offline, queue and playlist functionality.
- Protect user data and avoid unnecessary database schema changes.
- Never perform destructive database operations or migrations without a genuine requirement.
- Use existing string-resource conventions and avoid unrelated localization changes.
- Add comments only for complex or non-obvious logic.

## 8. Battery and Performance

- Reuse existing architecture and components instead of introducing duplicate logic.
- Keep playback smooth and responsive without unnecessary battery drain.
- Keep animations and rendering lightweight.
- Respect Android lifecycle and background-execution restrictions.
- Never sacrifice playback reliability or functionality for superficial optimization.
- Avoid unnecessary services, polling, timers, wake locks and repeated operations.


## 9. UI and User Experience

- Follow MKR Music's Expressive Material 3 design identity.
- Maintain consistent typography, spacing, shapes, components and interactions.
- Support light and dark themes where applicable.
- Preserve dynamic artwork-based theming wherever implemented.
- Create responsive, accessible and polished interfaces.
- Provide meaningful accessibility descriptions, suitable touch targets and readable contrast.
- Handle loading, empty, error and success states properly.
- Avoid unnecessary visual effects, heavy animations and clutter.


## 11. Verification and Quality Assurance

- Double-check every requirement in the user's request.
- Inspect the final source code, not just the edited sections.
- Trace relevant execution paths and verify that changes are properly connected.
- Check imports, references, callbacks, state updates and lifecycle handling.
- Look for broken references, unused code, duplicate logic and regressions.
- Review battery, memory, CPU, network and data-safety implications.
- Run `git diff --check` and inspect the complete staged and unstaged diff.
- Fix incomplete or inconsistent implementations before requesting review.
- Never assume a task is complete simply because code was edited.
- Report incomplete work and unverified behaviour honestly.

## 12. Final Output Report

Provide a structured report directly in the terminal/chat response.

Include exactly:

1. **Changes Implemented:** What was actually completed.
2. **Files Modified:** Files created, modified or deleted.
3. **Verification:** Static checks and source inspections performed, with results.
4. **Testing Required:** Anything requiring a build, runtime or physical-device test.
5. **Git Status:** Commit hashes and messages, when authorized; current branch and working-tree status.

Do not create separate report files or additional documentation unless explicitly requested.

## 13. Priority and Decision Making

- The user's latest explicit instructions take priority over general project instructions, subject to higher-level safety requirements.
- Preserve existing project decisions and user changes.
- Never make unrelated changes or assumptions about important requirements.
- If requirements are unclear or a change could cause data loss or significant regressions, stop and ask the human contributor.
- Report blockers honestly instead of silently skipping requirements.

