#!/usr/bin/env bash
# Compiles <name>.cs here (all of them without arguments) to language/<name>.dll - the library of the language test
# <name>.cs, against the .NET 10 mock SDK
set -euo pipefail
here="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
sdk="$here/../../../../mockSdk/net10.0"
csc="$(ls -d "$(dirname "$(readlink -f "$(command -v dotnet)")")"/sdk/*/Roslyn/bincore/csc.dll | tail -1)"
references=()
for library in "$sdk"/*.dll; do
    references+=("-reference:$library")
done
for name in "${@:-}"; do
    for source in "$here"/${name:-*}.cs; do
        dotnet exec "$csc" -nologo -target:library -nostdlib -noconfig -deterministic -langversion:latest -nullable:enable \
            "${references[@]}" -out:"$here/../resources/language/$(basename "$source" .cs).dll" "$source"
    done
done
