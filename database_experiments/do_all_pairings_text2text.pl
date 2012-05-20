use warnings;
use strict;

use 5.010;

mkdir "pairs_wrong_encoding";
open my $f, "<", "vysledky_top";
while (my $line = <$f>) {
    my ($id, $s1, $s2) = split(/\t/, $line);
    system("perl pairing_text2text.pl $s1 $s2 >pairs_wrong_encoding/$id.txt");
    $|=1;
    say $id;
}
