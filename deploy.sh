#!/bin/bash

set -e

BRANCH_DEPLOY=gh-pages
BRANCH_NOW=`git rev-parse --abbrev-ref HEAD`

git switch $BRANCH_DEPLOY
cp client/yagd-client/dist/yagd*.exe docs
git add docs/*yagd*.exe && git commit -m "update `date`"
git push origin $BRANCH_DEPLOY

git switch $BRANCH_NOW