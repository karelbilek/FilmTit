use 5.010;
use warnings;
use strict;


my $all = 0;
my %repetitions_of;
 
for my $srtname (<en/*.srt>) {
	open my $infile, "<", $srtname;
	while (my $line = <$infile>) {
		chomp $line;
		$line=~s/\r//;
		if ($line!~/^\s*$/ and $line!~/^[0-9]+$/ and $line!~/-->/) {
			$repetitions_of{$line}++;
			$all++;
		}
	} 
}

my $doubles_every_time;
my $doubles_once;
for my $line (sort {$repetitions_of{$b} <=> $repetitions_of{$a}} keys %repetitions_of) {
	if ($repetitions_of{$line} > 1) {
		$doubles_every_time += $repetitions_of{$line};
		say "Line $line is repeated ".$repetitions_of{$line}."-times.";
		$doubles_once++;
	}
}

say "All = $all";
say "More than once (counted every time) = $doubles_every_time";
say "counted only once per repeated line = $doubles_once";
say "Part = ", ($doubles_every_time/$all)*100;
