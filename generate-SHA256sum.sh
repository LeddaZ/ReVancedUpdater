#!/bin/bash

# Generate SHA-256 hash
sha256sum app/build/outputs/apk/release/app-release.apk > build/updater-SHA-256.txt
