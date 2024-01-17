#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$url$                  controllers.$section$.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /$url$                  controllers.$section$.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /change-$url$                        controllers.$section$.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /change-$url$                        controllers.$section$.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

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

echo "Adding page to the section page list"
awk '/sectionPages/ {\
      if(/Seq\(\)/)\
              sub(/\)\$/, "$className$Page&");\
      else\
              sub(/\)\$/, ", $className$Page&");\
} 1' ../app/pages/$section$/package.scala > tmp_file && mv tmp_file ../app/pages/$section$/package.scala

echo "Migration $className;format="snake"$ completed"
