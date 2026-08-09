---
name: word-crush-deployment
description: Local-first Word Crush client/backend integration, Spring Boot Docker deployment, SSH operations, and remote HTTPS verification. Use when changing, testing, deploying, or troubleshooting the Word Crush backend or its Android client/server integration.
---

# Word Crush Deployment

## Purpose

Use this workflow for Word Crush Android-client/backend changes, Spring Boot
deployment, Docker Compose maintenance, SSH operations, or production smoke
tests. The order is mandatory: local client/backend integration first, remote
deployment second, and remote HTTPS/API verification last.

Do not deploy merely because the backend image builds. A failed local gate is a
hard stop; fix it locally or report the blocker before touching the server.

## Project facts

- Development-stage compatibility policy: the project is currently under active development, so database migrations and compatibility between old and new interfaces are not required. New schema and API changes may be applied directly in development environments; do not add compatibility layers unless the user explicitly requests them.

- The client repository is this project; the backend repository is the sibling
  `word-crush-server` project.
- The backend is Spring Boot 3.x/Java 17 and is deployed with Docker Compose.
- The remote host is `ubuntu@txy.hejulian.org:22`; deploy under
  `/home/ubuntu/coding/word-crush-server`.
- The public API prefix is `https://txy.hejulian.org/word-crush/`.
- Reuse the existing Caddy HTTPS listener on ports 80/443. The application
  binds only `127.0.0.1:18080`; MySQL and Redis remain Docker-internal and must
  not publish host ports.
- The deployment overlay joins the app to the existing Docker network
  `relay_relay_internal`; Caddy proxies `/word-crush/*` to
  `wordcrush-app:8080` and strips the prefix.
- Keep `.env` out of Git. Generate production secrets on the server with
  restrictive permissions; never put secrets, private keys, passwords, or JWTs
  in this Skill, commits, logs, or tool output.

## Current SSH connection

Use these settings for the Tencent Cloud server:

- Host: `txy.hejulian.org`
- Port: `22`
- User: `ubuntu`
- Current local key file: `.codex-ssh/wordcrush_server_ed25519_admin`, relative
  to the client repository root. The private-key contents must never be copied
  into this Skill or printed.

From PowerShell in the client repository, connect interactively with:

```powershell
$sshKey = ".\.codex-ssh\wordcrush_server_ed25519_admin"
ssh -i $sshKey -o IdentitiesOnly=yes -o ConnectTimeout=10 -p 22 ubuntu@txy.hejulian.org
```

Use one-shot non-interactive commands for maintenance and deployment:

```powershell
ssh -i $sshKey -o IdentitiesOnly=yes -o BatchMode=yes -o ConnectTimeout=10 -p 22 ubuntu@txy.hejulian.org 'cd /home/ubuntu/coding/word-crush-server && docker compose --env-file .env -p wordcrush-server -f docker-compose.yml -f docker-compose.server.yml ps'
```

PowerShell quoting rule: keep the complete remote command in single quotes.
This prevents local expansion of remote expressions such as `$(...)` and
ensures that command substitution happens on Ubuntu. Do not use
`StrictHostKeyChecking=no`; preserve the accepted host key and stop if it
changes unexpectedly.

Do not use SCP as the normal source-code synchronization method. Versioned
client and backend files must be committed and pushed to their configured Git
remotes, then pulled on the server. Use SCP only for an explicitly approved
non-versioned operational artifact; never use it for `.env`, private keys, or
uncommitted source code.

The first connection check should confirm `whoami` is `ubuntu`, the hostname
is the expected Ubuntu VM, and the working directory is `/home/ubuntu`. Do not
reuse an interactive SSH session for automated commands; use separate one-shot
SSH calls.










## Mandatory workflow

### 1. Prove local integration first

Run backend tests and Compose validation from the backend repository: `.\mvnw.cmd -q test`, `docker compose --env-file .env.example -p wordcrush-local config --quiet`, then `docker compose --env-file .env.example -p wordcrush-local up -d --build`.

Verify `http://127.0.0.1:18080/actuator/health` returns `UP` with HTTP 200 and inspect `docker compose --env-file .env.example -p wordcrush-local ps`. The app must bind to `127.0.0.1:18080`; MySQL/Redis must not show host mappings such as `0.0.0.0:3306`.

If client or API code changed, run the narrowest applicable client check, for example `.\gradlew.bat :app:compileDebugKotlin`, and exercise one safe real API path locally. Never print login passwords, JWTs, or response tokens. If any required local test, health check, or client compile fails, stop and do not open SSH or modify the server.

### 2. Commit and synchronize the verified change

Only after the local gate passes, inspect both repositories again with `git status --short` and `git diff --check`. Stage and commit only the intended client/backend files. Never stage `.env`, SSH keys, build output, generated secrets, or unrelated user changes.

Push the commit to the configured Git remote before deploying. Client and backend are separate repositories, so commit and push them separately when both changed. Deploy only the backend commit to the server; the Android client is not copied into the server directory.

On the server, if the backend checkout is absent, use Git clone. If it exists, use `git fetch` followed by `git pull --ff-only` for the intended branch. Verify the expected commit with `git rev-parse --short HEAD` and require a clean working tree. If the remote working tree is dirty or the fast-forward pull cannot proceed, stop and preserve it; never use `git reset --hard` as a sync shortcut.

### 3. Preflight the remote host

Only after the local gate passes, connect as `ubuntu@txy.hejulian.org` on port 22 with the configured key and one-shot non-interactive commands. Check existing containers, listeners, Docker, and the `relay_relay_internal` network. If absent, clone the backend into `/home/ubuntu/coding/word-crush-server`; otherwise pull the committed backend revision. Do not upload local `.env`, private keys, caches, or unrelated edits.

Generate the remote `.env` with strong random values, set mode 600, and validate only that required values are non-empty. Reuse existing Caddy HTTPS on ports 80/443; do not request a new Tencent Cloud security-group port unless a new public port is genuinely required and the user approves it.

### 4. Deploy the remote stack

In `/home/ubuntu/coding/word-crush-server`, validate the merged files with `docker compose --env-file .env -p wordcrush-server -f docker-compose.yml -f docker-compose.server.yml config --quiet`, then run the same command with `up -d --build`.

Require healthy MySQL/Redis and a running app. Verify `http://127.0.0.1:18080/actuator/health`, and use `ss -ltn` to reject any public MySQL, Redis, or application listener. Never use `down -v` or delete data volumes as routine recovery.

### 5. Update and verify HTTPS

Back up `/home/ubuntu/ssh_mobile/relay/Caddyfile` before changing it. Add `/word-crush/*` while keeping the Relay fallback unchanged; run `caddy validate` before reload. If a bind-mounted upload leaves the running Caddy container with the old inode, recreate only Caddy with the Relay Compose project using `--no-deps --force-recreate`; do not recreate Relay, the backend, MySQL, or Redis solely to refresh Caddy.

Verify `GET https://txy.hejulian.org/word-crush/actuator/health` returns HTTP 200 and `UP`, a safe `POST https://txy.hejulian.org/word-crush/api/user/login` returns HTTP 200, and `GET https://txy.hejulian.org/` still returns the existing Relay response. Confirm the original Relay containers remain running and inspect recent app logs for startup errors. Do not print passwords or JWTs.

## Client API rules

- Keep the Retrofit base URL trailing slash at `https://txy.hejulian.org/word-crush/`.
- Use relative paths such as `api/user/login` and `api/getTopNRecord`; a leading slash discards the `/word-crush/` prefix.
- Keep production traffic on HTTPS. Do not weaken Android cleartext policy to make a production test pass.
- Update client documentation when the server URL, API paths, deployment commands, or ports change.

## Failure, rollback, and routine operations

- Local failure: stop before SSH and report the exact failed command.
- Remote app failure: preserve existing services and data volumes; inspect logs and health instead of opening a new port.
- Caddy failure: restore the timestamped backup, validate, reload, and confirm the existing Relay root path before retrying.
- Never use broad destructive commands such as `rm -rf`, `down -v`, database deletion, or reset of unrelated repositories.
- Use the same Compose project name and both files for later status/log checks: `docker compose --env-file .env -p wordcrush-server -f docker-compose.yml -f docker-compose.server.yml ps` and `... logs --tail=100 app`.

Every later code deployment repeats the full order: local tests and Docker integration first, Git commit/push second, remote Git pull and deployment third, and remote HTTPS/API verification last.
