#!/usr/bin/env bash

pushd () {
	command pushd "$@" > /dev/null
}

popd () {
	command popd "$@" > /dev/null
}

pushd "$(dirname "$0")"

./gradlew bootRun

popd
