#!/bin/bash

englishTemp=$(mktemp)
welshTemp=$(mktemp)

cat ../conf/messages.en | grep "=" | cut -d "=" -f1 | sort | xargs -n1 > $englishTemp 
cat ../conf/messages.cy | grep "=" | cut -d "=" -f1 | sort | xargs -n1 > $welshTemp

comm -3 $englishTemp $welshTemp

rm -f $englishTemp
rm -f $welshTemp
