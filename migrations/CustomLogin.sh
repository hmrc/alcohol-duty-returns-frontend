#!/bin/bash

echo ""
echo "Applying migration CustomLogin"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        //custom-login                        controllers.SelectAppaId.CustomLoginController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       //custom-login                        controllers.SelectAppaId.CustomLoginController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /change-/custom-login                  controllers.SelectAppaId.CustomLoginController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /change-/custom-login                  controllers.SelectAppaId.CustomLoginController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "customLogin.title = CustomLoginTitle" >> ../conf/messages.en
echo "customLogin.heading = CustomLoginTitle" >> ../conf/messages.en
echo "customLogin.1 = 1" >> ../conf/messages.en
echo "customLogin.2 = 2" >> ../conf/messages.en
echo "customLogin.checkYourAnswersLabel = CustomLoginTitle" >> ../conf/messages.en
echo "customLogin.error.required = Select customLogin" >> ../conf/messages.en
echo "customLogin.change.hidden = CustomLogin" >> ../conf/messages.en

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomLogin: Arbitrary[SelectAppaId.CustomLogin] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(SelectAppaId.CustomLogin.values)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Adding page to the section page list"
awk '/sectionPages/ {\
      if(/Seq\(\)/)\
              sub(/\)$/, "CustomLoginPage&");\
      else\
              sub(/\)$/, ", CustomLoginPage&");\
} 1' ../app/pages/SelectAppaId/package.scala > tmp_file && mv tmp_file ../app/pages/SelectAppaId/package.scala

echo "Migration CustomLogin completed"
