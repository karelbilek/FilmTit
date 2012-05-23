use strict;
use warnings;
use 5.010;
use utf8;

#stupid trick - if file has a letter "í", it is unicode
sub recognize_encoding {
    my $filename = shift;
    my $count = `cat $filename | grep "í" | wc -l` + 0;
    if ($count == 0) {
        return 0;
    } else {
        return 1;
    }
}

mkdir "/tmp/database_experiments/pairs_utf8";
for my $fn (</tmp/database_experiments/pairs_wrong_encoding/*>) {
    my $w = recognize_encoding($fn);
    my $new_fn = $fn;
    $new_fn =~ s/pairs_wrong_encoding/pairs_utf8/;
    if ($w) {
        system "cp $fn $new_fn";
    } else {
        
        system "iconv -f 'cp1250' -t 'utf8' -c $fn > $new_fn";
    }
}


