#!/usr/bin/env bash
# player-tracker 构建入口（Git Bash）
# 用法：
#   ./build.sh build          # 完整构建，产物 build/libs/playertracker-1.21.1-1.0.0.jar
#   ./build.sh runClient      # 启动测试客户端
#   ./build.sh clean build
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE="$(cd "$ROOT/.." && pwd)"
export JAVA_HOME="$WORKSPACE/.tools/jdk-21.0.12.1+1"
GRADLE="$WORKSPACE/.tools/gradle-8.10.2/bin/gradle"

# 关键：本机 Java 默认走 IPv6 会连接超时，必须强制 IPv4。
export JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"

cd "$ROOT"
exec "$GRADLE" "$@"
