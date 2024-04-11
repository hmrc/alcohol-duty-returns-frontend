#!/bin/bash

echo ""
echo "Applying migration BeforeStartReturn"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /before-you-start-your-return                       controllers.section.BeforeStartReturnController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "beforeStartReturn.title = beforeStartReturn" >> ../conf/messages.en
echo "beforeStartReturn.heading = beforeStartReturn" >> ../conf/messages.en

echo "Migration BeforeStartReturn completed"
