#!/bin/bash

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "section.$section$ = $label$" >> ../conf/messages.en

echo "Migration $section;format="snake"$ completed"
