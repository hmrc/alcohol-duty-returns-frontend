#!/bin/bash

keysInCodeTemp=$(mktemp)
allUsedKeysTemp=$(mktemp)
keysTemp=$(mktemp)

./findMessageKeys.pl ../app | grep -v "\\$" | cut -f2 -d " " > $keysInCodeTemp
cat also_used_keys $keysInCodeTemp | sort | uniq > $allUsedKeysTemp
cat ../conf/messages.en | grep "=" | cut -f1 -d "=" | xargs -n1 | sort | uniq > $keysTemp

comm -23 $keysTemp $allUsedKeysTemp

rm -f $keysInCodeTemp
rm -f $allUsedKeysTemp
rm -f $keysTemp

