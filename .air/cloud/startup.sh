#!/usr/bin/env bash
set -euo pipefail

log() {
  printf '[startup] %s\n' "$*"
}

if [[ "${AIR_STARTUP_MODE:-}" == "warmup" ]]; then
  WARMUP=1
else
  WARMUP=
fi

export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
mkdir -p "$GRADLE_USER_HOME"

if [[ -n "${HTTPS_PROXY:-${HTTP_PROXY:-}}" ]]; then
  proxy_url="${HTTPS_PROXY:-${HTTP_PROXY}}"
  proxy_host="${proxy_url#http://}"
  proxy_host="${proxy_host#https://}"
  proxy_host="${proxy_host%%:*}"
  proxy_port="${proxy_url##*:}"
  export GRADLE_OPTS="${GRADLE_OPTS:-} -Dhttps.proxyHost=$proxy_host -Dhttps.proxyPort=$proxy_port -Dhttp.proxyHost=$proxy_host -Dhttp.proxyPort=$proxy_port"
fi

PROFILE_FILE=
for candidate in "$HOME/.bash_profile" "$HOME/.bash_login" "$HOME/.profile"; do
  if [[ -f "$candidate" ]]; then
    PROFILE_FILE="$candidate"
    break
  fi
done
if [[ -z "$PROFILE_FILE" ]]; then
  PROFILE_FILE="$HOME/.profile"
  touch "$PROFILE_FILE"
fi

ENV_FILE="$HOME/.kt-banking-app-env"
if [[ ! -f "$ENV_FILE" ]]; then
  cat > "$ENV_FILE" <<'EOF'
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
EOF
fi
SOURCE_LINE="[ -f \"$ENV_FILE\" ] && . \"$ENV_FILE\""
for shell_file in "$PROFILE_FILE" "$HOME/.bashrc"; do
  touch "$shell_file"
  if ! grep -Fqx "$SOURCE_LINE" "$shell_file"; then
    printf '\n%s\n' "$SOURCE_LINE" >> "$shell_file"
  fi
done

chmod +x ./gradlew
log 'Resolving dependencies and compiling the application'
./gradlew -Pkotlin.jvm.target.validation.mode=warning --no-daemon test build

log 'Starting Ktor server on port 8080'
nohup ./gradlew -Pkotlin.jvm.target.validation.mode=warning --no-daemon run > /tmp/kt-banking-app.log 2>&1 &
SERVER_PID=$!
printf '%s\n' "$SERVER_PID" > /tmp/kt-banking-app.pid

healthcheck() {
  log 'Waiting for the Ktor server and checking its page'
  while true; do
    if response=$(curl --silent --show-error --fail http://127.0.0.1:8080/ 2>/tmp/kt-banking-app-curl-error); then
      if grep -q 'Banking App' <<< "$response"; then
        log 'Healthcheck passed: Banking App page is served'
        return 0
      fi
      log 'Server answered, but the expected Banking App page was not found'
    else
      log "Server is not ready yet: $(tr '\n' ' ' < /tmp/kt-banking-app-curl-error)"
    fi
    if ! kill -0 "$SERVER_PID" 2>/dev/null; then
      log 'Ktor server exited unexpectedly'
      tail -n 80 /tmp/kt-banking-app.log || true
      return 1
    fi
    sleep 2
  done
}

if [[ -n "$WARMUP" ]]; then
  healthcheck
fi

log 'Startup complete'
