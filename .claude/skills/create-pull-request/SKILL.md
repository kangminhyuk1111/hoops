---
name: create-pull-request
description: Create a GitHub pull request following project conventions. Use when the user asks to create a PR, submit changes for review, or open a pull request. Handles commit analysis, branch management, and PR creation using the gh CLI tool.
---

# Create Pull Request

This skill guides you through creating a well-structured GitHub pull request that follows project conventions and best practices.

## Prerequisites Check

Before proceeding, verify the following:

### 1. Check if `gh` CLI is installed

```bash
gh --version
```

If not installed, inform the user:
> The GitHub CLI (`gh`) is required but not installed. Please install it:
> - macOS: `brew install gh`
> - Other: https://cli.github.com/

### 2. Check if authenticated with GitHub

```bash
gh auth status
```

If not authenticated, guide the user to run `gh auth login`.

### 3. Verify clean working directory

```bash
git status
```

If there are uncommitted changes, ask the user whether to:
- Commit them as part of this PR
- Stash them temporarily
- Discard them (with caution)

### 4. **[필수] Cucumber Acceptance Test 실행**

> ⚠️ **PR 생성 전 반드시 모든 Cucumber 테스트를 통과해야 합니다.**
>
> **테스트 실패 시 PR 생성을 절대 진행하지 마세요!**

```bash
./gradlew test
```

#### 테스트 우선순위 (ATDD 기반)
| 우선순위 | 테스트 종류 | 위치 | 필수 여부 |
|---------|-----------|------|---------|
| 1 | **Cucumber Acceptance Tests** | `backend/src/test/java/com/hoops/acceptance/` | **필수** |
| 2 | Integration Tests | `backend/src/test/java/com/hoops/integration/` | 필수 |
| 3 | Unit Tests | 각 패키지 test 디렉토리 | 필수 |

#### 테스트 실패 시 대응
1. **실패한 시나리오 확인**: 어떤 기능이 깨졌는지 파악
2. **코드 수정**: 기존 기능이 깨지지 않도록 수정
3. **테스트 재실행**: `./gradlew test`
4. **모든 테스트 통과 확인 후** PR 생성 진행

#### Cucumber 시나리오 커버리지 목표
- **90% 이상** 시나리오 커버리지 유지
- 새로운 기능 추가 시 해당 기능의 Cucumber 시나리오 작성 필수

## Gather Context

### 1. Identify the current branch

```bash
git branch --show-current
```

Ensure you're not on `main` or `master`. If so, ask the user to create or switch to a feature branch.

### 2. Find the base branch

```bash
git remote show origin | grep "HEAD branch"
```

This is typically `main` or `master`.

### 3. Analyze recent commits relevant to this PR

```bash
git log origin/main..HEAD --oneline --no-decorate
```

Review these commits to understand:
- What changes are being introduced
- The scope of the PR (single feature/fix or multiple changes)
- Whether commits should be squashed or reorganized

### 4. Review the diff

```bash
git diff origin/main..HEAD --stat
```

This shows which files changed and helps identify the type of change.

## Information Gathering

Before creating the PR, you need the following information. Check if it can be inferred from:
- Commit messages
- Branch name (e.g., `fix/issue-123`, `feature/new-login`)
- Changed files and their content

If any critical information is missing, use `ask_followup_question` to ask the user:

### Required Information

1. **Related Issue Number**: Look for patterns like `#123`, `fixes #123`, or `closes #123` in commit messages
2. **Description**: What problem does this solve? Why were these changes made?
3. **Type of Change**: Bug fix, new feature, breaking change, refactor, cosmetic, documentation, or workflow
4. **Test Procedure**: How was this tested? What could break?

### Example clarifying question

If the issue number is not found:
> I couldn't find a related issue number in the commit messages or branch name. What GitHub issue does this PR address? (Enter the issue number, e.g., "123" or "N/A" for small fixes)

## Git Best Practices

Before creating the PR, consider these best practices:

### Commit Hygiene

1. **Atomic commits**: Each commit should represent a single logical change
2. **Clear commit messages**: Follow conventional commit format when possible
3. **No merge commits**: Prefer rebasing over merging to keep history clean

### Branch Management

1. **Rebase on latest main** (if needed):
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Squash if appropriate**: If there are many small "WIP" commits, consider interactive rebase:
   ```bash
   git rebase -i origin/main
   ```
   Only suggest this if commits appear messy and the user is comfortable with rebasing.

### Push Changes

Ensure all commits are pushed:
```bash
git push origin HEAD
```

If the branch was rebased, you may need:
```bash
git push origin HEAD --force-with-lease
```

## Create the Pull Request

**IMPORTANT**: Read and use the PR template at `.github/pull_request_template.md`. The PR body format must **strictly match** the template structure. Do not deviate from the template format.

When filling out the template:
- Replace `#XXXX` with the actual issue number, or keep as `#XXXX` if no issue exists (for small fixes)
- Fill in all sections with relevant information gathered from commits and context
- Mark the appropriate "Type of Change" checkbox(es)
- Complete the "Pre-flight Checklist" items that apply

### Create PR with gh CLI

```bash
gh pr create --title "PR_TITLE" --body "PR_BODY" --base main
```

Alternatively, create as draft if the user wants review before marking ready:
```bash
gh pr create --title "PR_TITLE" --body "PR_BODY" --base main --draft
```

## Post-Creation

After creating the PR:

1. **Display the PR URL** so the user can review it
2. **Remind about CI checks**: Tests and linting will run automatically
3. **Suggest next steps**:
   - Add reviewers if needed: `gh pr edit --add-reviewer USERNAME`
   - Add labels if needed: `gh pr edit --add-label "bug"`

## Error Handling

### Common Issues

1. **No commits ahead of main**: The branch has no changes to submit
   - Ask if the user meant to work on a different branch

2. **Branch not pushed**: Remote doesn't have the branch
   - Push the branch first: `git push -u origin HEAD`

3. **PR already exists**: A PR for this branch already exists
   - Show the existing PR: `gh pr view`
   - Ask if they want to update it instead

4. **Merge conflicts**: Branch conflicts with base
   - Guide user through resolving conflicts or rebasing

## Summary Checklist

Before finalizing, ensure:
- [ ] `gh` CLI is installed and authenticated
- [ ] **Cucumber Acceptance Tests 통과** ← 최우선 필수!
- [ ] **Integration Tests 통과**
- [ ] **Unit Tests 통과**
- [ ] Working directory is clean
- [ ] All commits are pushed
- [ ] Branch is up-to-date with base branch
- [ ] Related issue number is identified, or placeholder is used
- [ ] PR description follows the template exactly
- [ ] Appropriate type of change is selected
- [ ] Pre-flight checklist items are addressed

> ❌ **Cucumber 테스트 미통과 시 PR 생성 절대 금지**
>
> 테스트 실행 명령: `./gradlew test`