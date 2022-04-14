#!/usr/bin/env bash
set -e

echo "deploy maven artifact"
PROJECT_VERSION=`./mvnw -U org.apache.maven.plugins:maven-help-plugin:3.1.1:evaluate -Dexpression=project.version | grep '^[[:digit:]].[[:digit:]].[[:digit:]]\$'`
GIT_COMMIT_VERSION=`git log --pretty=format:'%h' -n 1`
NEW_PROJECT_VERSION=${PROJECT_VERSION}-${BUILD_ID}-${GIT_COMMIT_VERSION}
./mvnw versions:set -DnewVersion=${NEW_PROJECT_VERSION}

./mvnw deploy -U -DupdateReleaseInfo=true -DdeployAtEnd=true
