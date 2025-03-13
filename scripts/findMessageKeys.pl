#!/usr/bin/perl -w

use strict;
use File::Find;

# Directory to start the recursion
my $dir = shift @ARGV || '.';

sub process_file {
    my $fileName = $_;

    return unless $fileName =~ /(\.scala$|.html$)/;
    
    open my $fh, '<', $_ or die "Cannot open file $_: $!\n";

    while (my $line = <$fh>) {
        while ($line =~ /s?"([^"[ ()]*\.[^"[ ()]*)"/g) {
            my $key = $1;
	    my @keys_expanded = ();

	    if ($key =~ /\$transactionType/) {
              foreach $_ ("Return", "LPI", "RPI") {
  	        my $key1 = $key;
	        $key1 =~ s/\$transactionType/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$regime/) {
              foreach $_ ("Beer", "Cider", "Wine", "Spirits", "OtherFermentedProduct") {
  	        my $key1 = $key;
	        $key1 =~ s/\$regime/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$\{regime\.toString(\(\))?}/) {
              foreach $_ ("Beer", "Cider", "Wine", "Spirits", "OtherFermentedProduct") {
  	        my $key1 = $key;
	        $key1 =~ s/\$\{regime\.toString(\(\))?}/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$\{[^.]+\.alcoholType}/) {
              foreach $_ ("Beer", "Cider", "SparklingCider", "Wine", "Spirits", "OtherFermentedProduct") {
  	        my $key1 = $key;
	        $key1 =~ s/\$\{[^.]+\.alcoholType}/$_/;  
	        push @keys_expanded, $key1;
              }
            } elsif ($key =~ /adjustmentType\.\$\{value\.toString}/) {
              foreach $_ ("under-declaration", "over-declaration", "spoilt", "repackaged-draught-products", "drawback") {
  	        my $key1 = $key;
	        $key1 =~ s/adjustmentType\.\$\{value\.toString}/adjustmentType.$_/;  
	        push @keys_expanded, $key1;
	      }
             } elsif ($key =~ /\$adjustmentType/) {
              foreach $_ ("under-declaration", "over-declaration", "spoilt", "repackaged-draught-products", "drawback") {
  	        my $key1 = $key;
	        $key1 =~ s/\$adjustmentType/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$rateType/) {
              foreach $_ ("Core", "DraughtRelief", "SmallProducerRelief", "DraughtAndSmallProducerRelief") {
  	        my $key1 = $key;
	        $key1 =~ s/\$rateType/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$paymentType/) {
              foreach $_ ("unallocated", "historic") {
  	        my $key1 = $key;
	        $key1 =~ s/\$paymentType/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$spiritType/) {
              foreach $_ ("maltSpirits", "grainSpirits", "neutralAgriculturalOrigin", "neutralIndustrialOrigin", "beer", "wineOrMadeWine", "ciderOrPerry", "other") {
  	        my $key1 = $key;
	        $key1 =~ s/\$spiritType/$_/;  
	        push @keys_expanded, $key1;
	      }
            } elsif ($key =~ /\$\{returnAdjustmentsRow\.adjustmentTypeKey}/) {
              foreach $_ ("underdeclaration", "overdeclaration", "repackagedDraught", "spoilt", "drawback") {
  	        my $key1 = $key;
	        $key1 =~ s/\$\{returnAdjustmentsRow\.adjustmentTypeKey}/$_/;  
	        push @keys_expanded, $key1;
	      }
	    } elsif ($key =~ /\$\{section\.name}/) {
              foreach $_ ("adjustment", "dutySuspended", "spirits") {
                my $key1 = $key;
                $key1 =~ s/\$\{section\.name}/$_/;
                push @keys_expanded, $key1;
              }
       	    } else {
              push @keys_expanded, $key;
	    }

	    foreach $_ (@keys_expanded) {
              print "$fileName $_\n";  # Print the string inside the quotes
            }
        }
    }
    close $fh;
}

# Start the recursive search
find(\&process_file, $dir);
