use warnings;
use strict;

mkdir "utf8_corrected";

$| = 1;

for my $filename (<utf8/*>) {
	print $filename."\n";
	my $new_fname = $filename;
	$new_fname =~ s/utf8/utf8_corrected/;
	system("iconv -f utf8 -t iso-8859-2 $filename | iconv -f windows-1250 -t utf8 > $new_fname");
}
