use strict;
use warnings;
use 5.010;
use Readonly;

Readonly my $movie_id => $ARGV[0];
Readonly my $MAX_READ => $ARGV[1] // 5;
binmode(STDOUT, ":utf8");
use utf8;

say "CZECH";
say "===========";

for my $titname (all_subtitles_of_movie(1)) {
    say $titname;
    read_first_subtitles($titname, $MAX_READ);
    say "--------";
}

say "ENGLISH";
say "===========";

for my $titname (all_subtitles_of_movie(0)) {
    say $titname;
    read_first_subtitles($titname, $MAX_READ);
    say "--------";
}


sub all_subtitles_of_movie {
    Readonly my $czech => shift;

    Readonly my $language_mark => $czech ? "cze" : "eng";
    
    open my $pipe, qq{grep "^$movie_id\\s[0-9]*\\s$language_mark" }.
                   qq{export/export_final_sorted_and_uniq.txt |}.
                   qq{ cut -f2 |};

    my @filenames = (<$pipe>);
    chomp(@filenames);
    close $pipe;
    return @filenames;
}

sub read_first_subtitles {
    Readonly my $titname => shift;
    Readonly my $count => shift;

    open my $tit_file, "gunzip export/files/$titname.gz --stdout |".
                        "iconv -f 'cp1250' -t 'utf8' |";
    binmode($tit_file, ":utf8");
    
    my $subs_read_so_far;

    SUBTITREAD:
    while (my $line = <$tit_file>) {
        chomp $line;

        #carriage return?
        $line =~ s/\r//;
        

        if ($line =~ /-->/) {
            $subs_read_so_far++;
            if ($subs_read_so_far > $count) {
                close $tit_file;
                return;
            }
        }
        
        say $line;
      
    }

}

