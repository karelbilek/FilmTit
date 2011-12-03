use strict;
use warnings;
use 5.010;
use Readonly;

Readonly my $MAX_MOVIES => 118557;
Readonly my $MAX_LINES => 20;

open my $export_file , '<', 'export_final_sorted_and_uniq.txt';

my $first_line = 1;

my $previous_id = 1;

my @filenames_czech;
my @filenames_english;

open my $pairs_file, ">", "pairs";
open my $stats_file, ">", "stats";
use IO::Handle;

$stats_file -> autoflush(1);

READ:
while (my $line = <$export_file>) {
    if ($first_line == 1) {
        $first_line = 0;
        next READ;
    }
    
    chomp $line;
    my ($id, $filename, $lang, undef, undef, $cds, $format) = split /\t/, $line;
    
    next READ if ($format ne 'srt');
    next READ if ($cds != 1);
    next READ if (!-e "files/$filename.gz");
    
    if ($previous_id != $id) {
        spust_srovnani($previous_id, \@filenames_czech, \@filenames_english);
        @filenames_czech = ();
        @filenames_english = ();
    }

    last READ if ($id > $MAX_MOVIES);



    if ($lang eq 'cze') {
        push @filenames_czech, $filename;
    } else {
        push @filenames_english, $filename;
    }
    $previous_id = $id;
}

sub read_first_times {
    Readonly my $titname => shift;
    Readonly my $count => shift;

    open my $tit_file, "gunzip files/$titname.gz --stdout |";
    
    my @res;

    SUBTITREAD:
    while (my $line = <$tit_file>) {
        chomp $line;

        #carriage return?
        $line =~ s/\r//;

        if ($line =~ /-->/) {
            push @res, $line;
            if (scalar @res == $count) {
                close $tit_file;
                return @res;
            }
        }
      
    }

}
sub spust_srovnani {
    Readonly my $id => shift;
    Readonly my $filenames_czech_ref => shift;
    Readonly my $filenames_english_ref => shift;

    my %english_file_with_first_time;

    for my $filename_english (@$filenames_english_ref) {
        Readonly my @first_times => 
           read_first_times($filename_english, $MAX_LINES);
        

        @english_file_with_first_time{@first_times} = 
           ($filename_english) x $MAX_LINES;


    }

    my $was_said = 0;

    for my $filename_czech (@$filenames_czech_ref) {
        Readonly my @first_times => 
           read_first_times($filename_czech, $MAX_LINES);

        TIMETEST:
        for my $first_time (@first_times) {
            if (exists $english_file_with_first_time{$first_time}) {
                my $filename_english = 
                    $english_file_with_first_time{$first_time};
                
                say $pairs_file $filename_czech, "\t", $filename_english;
           
                $was_said = 1;
                last TIMETEST;
            }
        }
    }
    if ($was_said) {
        say $stats_file "MOVIE $id -> :-)";
    } else {
        if (scalar @$filenames_czech_ref == 0) {
            say $stats_file "MOVIE $id -> :-(, but no czech";
        } else {
            say $stats_file "MOVIE $id -> :-(";
        }
    }
}




