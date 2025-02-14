#!/usr/bin/perl -w

use strict;

use Getopt::Long;
use Text::CSV;

my $inputMessagesFilename;
my $translationFilename;
my $outputFilename;

GetOptions("messages=s" => \$inputMessagesFilename, "translation=s" => \$translationFilename, "out=s" => \$outputFilename) && defined $inputMessagesFilename && $translationFilename && defined $outputFilename or die "Expected --messages messages.en --translation translation.csv --out messages.out"; 

my $lineNumber = 0;
my $linesProcessed = 0;
my $success = 0;
my $badDynamicText = 0;
my $notFoundError = 0;

my $csv = Text::CSV->new({ sep_char => ',' , binary => 1});

open(my $messagesEnInput, '<', $inputMessagesFilename) or die "Couldn't open English messages file $inputMessagesFilename\n";

my %messageToKeyMap = ();

while (my $line = <$messagesEnInput>) {
  chomp $line;

  if ($line =~ m/^[^#]/) {
    $line =~ m/(\S+)\s*=\s*(.+)/;
    my ($key, $value) = ($1, $2);

    $value =~ s/''/'/g;

    my $keysSoFar = $messageToKeyMap{$value};

    if (!defined $keysSoFar) {
      $keysSoFar = [$key]; 
    } else {
      push(@$keysSoFar, $key);
    }

    $messageToKeyMap{$value} = $keysSoFar;
  }
}

close $messagesEnInput;

open(my $translationInput, '<', $translationFilename) or die "Couldn't open translation file csv $translationFilename\n";

#initialise the output file

my %welshMessages = (); 

while (my $line = <$translationInput>) {
  chomp $line;
  $line =~ s/\R//;

  $lineNumber++;

  if ($csv->parse($line)) {
    my @fields = $csv->fields();

    my $english = $fields[1];
    my $welsh = $fields[2];

    $english =~ s/^\s+|\s+$//g;
    $welsh =~ s/^\s+|\s+$//g;

    if ($english ne "" && $english ne "English text" && $welsh ne "") {
      $linesProcessed++;

      my $dynamicCount = 0;

      my $checkEnglishDynamics = $english; 
      my $checkWelshDynamics = $welsh; 

      my $numDynamicSubs = ($checkEnglishDynamics =~ tr/\[//);

      if ($numDynamicSubs != $checkWelshDynamics =~ tr/\[//) {
        print "Number of English and Welsh dynamic substitions doesn't match for $english and $welsh\n";

	$badDynamicText = $badDynamicText + 1;
      } else {
        $english =~ s/(\w)(\[.*?\])/$1 $2/g; # ensure there is a space before dynamic content (welsh spreadsheet has issues)
        $welsh =~ s/(\w)(\[.*?\])/$1 $2/g;

        $english =~ y/\x{00A0}/ /; # handle non-breaking spaces
        $welsh =~ y/\x{00A0}/ /;

        for (my $i=0; $i<$numDynamicSubs; $i++) {
          $english =~ s/\[.*?\]/{$i}/;
          $welsh =~ s/\[.*?\]/{$i}/;
	}

        my $keys = $messageToKeyMap{$english}; 

        if (!defined $keys) {
          print "No entry in messages.en found: $english - Welsh was: $welsh\n";

          $notFoundError++;
        } else {
          foreach (@$keys) {
            $welshMessages{$_} = $welsh;
          }

          $success++;
        }
      }
    }

  } else {
    my $error = $csv->error_diag();
    warn "Unable to parse translation csv on line $lineNumber $error: $line";
  }
}

close($translationInput);

open(my $translationOutput, '>', $outputFilename) or die "Couldn't open translation file csv $outputFilename\n";

binmode($translationOutput, ":encoding(UTF-8)");

my $linesWritten = 0;

while ((my $key, my $value) = each(%welshMessages)) {
  $value =~ s/'/''/g;
  print $translationOutput "$key = $value\n";
  $linesWritten++;
}

close($translationOutput);

print "Processed $linesProcessed lines: $success successes (written $linesWritten), $notFoundError not matched, $badDynamicText dynamic problems\n";

1;
