#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$className;format="decap"$                  controllers.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /$className;format="decap"$                  controllers.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /change$className$                        controllers.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /change$className$                        controllers.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className$" >> ../conf/messages.en
echo "$className;format="decap"$.error.nonNumeric = Enter your $className;format="decap"$ using numbers" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Enter your $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.minimumRequired = $className;format="decap"$ must be 0.00 or more" >> ../conf/messages.en
echo "$className;format="decap"$.error.maximumRequired = $className;format="decap"$ must be 99999999.99 or less" >> ../conf/messages.en
echo "$className;format="decap"$.change.twoDecimalPlaces = $className$ must be a number to 2 decimal places" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
