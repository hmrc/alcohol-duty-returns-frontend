#!/usr/bin/perl -w

use strict;

use Getopt::Long;

my $englishMessagesFilename;
my $keysFilename;
my $welshMessagesFilename;

GetOptions("messages=s" => \$englishMessagesFilename, "keys=s" => \$keysFilename, "output=s" => \$welshMessagesFilename) && defined $englishMessagesFilename && $keysFilename && defined $welshMessagesFilename or die "Expected --messages messages.en --keys combinded_keys_outfile --output messages.cy";

my %welshMessages = ();

open(my $keysFile, '<', $keysFilename) or die "Couldn't open keys file $keysFilename\n";

my $lineNumber = 0;

my %translatedKeyToMessageMap = ();

while (my $line = <$keysFile>) {
  chomp $line;
  $lineNumber++;

  if ($line =~ m/(\S+) = (.+)/) {
    my ($key, $value) = ($1, $2);

    my $existingMessage = $translatedKeyToMessageMap{$key};

    if (defined $existingMessage && $existingMessage ne $value) {
      warn "For key $key found message $value and earlier $existingMessage\n";
    } else {
      $translatedKeyToMessageMap{$key} = $value;
    }
  } else {
    warn "Unexpected line $line in message file at line $lineNumber - expected key = value\n";
  }
}

close $keysFile;

open(my $englishMessagesFile, '<', $englishMessagesFilename) or die "Couldn't open english messages file $englishMessagesFilename\n";
open(my $welshMessagesFile, '>', $welshMessagesFilename) or die "Couldn't open welse messages file $welshMessagesFilename\n";

while (my $line = <$englishMessagesFile>) {
  chomp $line;
 
  if ($line =~ m/(\S+)\s*=\s*.+/) {
    my $key = $1; 

    my $translation = $translatedKeyToMessageMap{$key};

    if (defined $translation) {
      $translation =~ s/'/''/g;
      print $welshMessagesFile "$key = $translation\n";
    } else {
      print $welshMessagesFile "$key = NOT_FOUND\n";
    }
  } else {
    print $welshMessagesFile "$line \n";
  }
}

close $englishMessagesFile;
close $welshMessagesFile;

1;
