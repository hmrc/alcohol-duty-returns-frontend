# Welsh translation process

This is instructions for translating the English messages file (../conf/messages.en) to the Welsh messages file (../conf/messages.cy).
All file paths will be relative to the scripts directory where the discussed scripts are located.

To run the scripts you'll need perl installed (and the path to be /usr/bin/perl) as well as the Text::CSV library/module.

## Welsh translation spreadsheet

This is provided by the Welsh translation team and is 'all' the screen messages in both English and Welsh.
This was supplied to the team in English and the Welsh translations added by them.

This will be saved to the shared drive.
On opening the spreadsheet, each tab should be saved to csv format in the ../welsh-translation directory and should be called welsh1.csv, welsh2.csv...
where the number is the index (from 1) of the sheet from left to right.

Note: Do not attempt to translate (temporarily or otherwise) anything that is missing using Google Translate etc, or fill with joke / unprofessional placeholders
as there is a risk of it accidentally ending in production.
In the messages.cy file the value NOT_FOUND will be used to indicate clearly something is missing which can easily be found in the file or counted automatically.

## Creating key to message files from the csvs

For each Welsh csv file:

./create_translated_message_file.pl --messages ../conf/messages.en --translation ../welsh-translation/welsh1.csv --out ../welsh-translation/welsh1.keys

You may want to save any output to help manual translation (what couldn't be translated).
Append to the end of the command to redirect stdout and stderr to a log file:   > welsh1.log 2>&1

## Create manual translations

First time skip this step and came back.

Once run through first time, look at ../welsh-translation/messages.cy

Any messages that couldn't translated (translation team didn't do, messages.en was since updated, unused messages, dynamic values, incorrectly specified vs messages.en)
will have a value of NOT_FOUND assigned.

Looking at the log files if saved or scripts' output (e.g. welsh1.log etc and create.log) will give a help on which things couldn't be done
automatically and sometimes why.
Also by looking at the Excel spreadsheet, vs what gets output by the server will give more clues to what is missing.
Beware of the same word having different spellings in different contexts where there are multiple keys or translations.

Use curly single quotes instead of double quotes and ASCII apostrophe (see ../conf/messages.en for examples) for both quoting, apostrophe and contractions.
Use ASCII space instead of tab or other spaces.

To avoid having to repeat this process over and over for the same messages each time the translation spreadsheet is updated,
create your own .keys files e.g. welsh\_manual\_adjustments.keys in the same form as welsh1.keys etc.
Then use these files in the combine step below. Thus the automatic and manual translations are both combined and used to create messages.cy.

## Combine the keys files

cat ../welsh-translation/*.keys > ../welsh-translation/welsh_keys.all

If you have files you do not wish to include, you can manually specify which ones e.g.
  cat ../welsh-translation/welsh1.keys ../welsh-translation/welsh2.keys > ../welsh-translation/welsh_keys.all

## Create the message file

./create_messages_cy.pl --messages ../conf/messages.en --keys ../welsh-translation/welsh_keys.all --output ../welsh-translation/messages.cy

Again to save the output to a file e.g.: > create.log 2>&1

At this point, iterate back to the create manual translation step until all NOT_FOUND Welsh messages are fixed.

## When ready to test or push

Overwrite the server's Welsh messages file with the latest:

cp ../welsh-translation/messages.cy ../conf/messages.cy

In ../conf/app.config there is a feature toggle to allow Welsh to be selected. To view the translations, set this to true and re-run the server.
Note that production (and/or the environments) will have this switched off, so when ready to deploy to users, this will need to also be toggled.

## Handling new translations from the Welsh team

It is unclear at the moment how updates and new translations will be presented.
It might be possible to start again (keeping the manual files that were creates: welsh_manual...).
If the create_messages_cy.pl script detects multiple translations for the same thing
(e.g. the Welsh team now have translated something, but you got it wrong, or they changed something) it will flag it as an issue and the entry in
the messages.cy file will be NOT_FOUND until resolved (usually by deleted the line in the manual file, maybe after asking the Welsh team for guidance).
