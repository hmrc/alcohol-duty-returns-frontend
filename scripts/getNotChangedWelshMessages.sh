#!/bin/bash

englishTemp=$(mktemp)
welshTemp=$(mktemp)

git log "$1"^..HEAD -p -- ../conf/messages.en | grep ^+ | grep "=" | cut -f1 -d "=" | cut -f2 -d "+" | sort > $englishTemp
git log "$1"^..HEAD -p -- ../conf/messages.cy | grep ^+ | grep "=" | cut -f1 -d "=" | cut -f2 -d "+" | sort > $welshTemp

comm -23 $englishTemp $welshTemp

rm -f $englishTemp
rm -f $welshTemp



