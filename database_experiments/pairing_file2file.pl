use strict;
use warnings;
use 5.010;
use Readonly;
use Pairing;

#how many movies to take
Readonly my $MOVIE_COUNTS => 888888888888888888;

#how man lines to take from each file
Readonly my $MAX_LINES => 50;

#what is number of the first movie
Readonly my $FIRST_MOVIE => 0;

#filenames of czech/english subs for current movie
my @filenames_czech;
my @filenames_english;

#I am reading line by line so I need to remember previous movie ID
my $previous_id = undef;
my $counted=0;

#file with all the movie<->subfile pairing
open my $export_file , '<', 'export/export_final_sorted_and_uniq.txt' or die $!;

#takes line, makes it subtitle hash
sub export_line_to_subtitle {
    my $line = shift;
    my ($id, $filename, $lang, undef, undef, $cds, $format) = split /\t/, $line;
    return (id=>$id, filename=>$filename, lang=>$lang, cds=>$cds,
        format=>$format);
}

#tests if subtitle is valid
sub is_subtitle_bad {
    my %subtitle = @_;
    return 1 if ($subtitle{id} < $FIRST_MOVIE);
    return 1 if ($subtitle{format} ne 'srt');
    return 1 if ($subtitle{cds} != 1);
    return 1 if (!-e "export/files/".$subtitle{filename}.".gz");
    return 0;
}

#for each line
SUBTITLE:
while (my $line = <$export_file>) {
    
    chomp $line;
    
    my %subtitle = export_line_to_subtitle($line);
    
    next SUBTITLE if is_subtitle_bad(%subtitle);

    #When I am changing movie, I compare all files and 
    #print them out
    if (defined $previous_id and $previous_id != $subtitle{id}) {
        compare_files($previous_id, \@filenames_czech, \@filenames_english);
        @filenames_czech = ();
        @filenames_english = ();
        $counted++;
        
        #exiting if we read too much movies;
        last READ if ($counted >= $MOVIE_COUNTS);
    }

    #adding to array of files
    if ($subtitle{lang} eq 'cze') {
        push @filenames_czech, $subtitle{filename};
    } else {
        push @filenames_english, $subtitle{filename};
    }

    #remembering last id
    $previous_id = $subtitle{id};
}



#input:arrays of first 100 times from a given movie for 2 translation
#output: sub of distances
sub sum_of_distances {
    
    Readonly my $first_ref => shift;
    Readonly my $second_ref => shift;
    Readonly my $best_so_far => shift;
    
    my $sum=0;
    do_for_all_shortest_distances ($first_ref, $second_ref, 
        #sanity check
        sub {
            $sum = 99**99**99;
        }, 
        #adding the distances
        sub {
            if ($sum > $best_so_far) {
                $sum = $best_so_far+1;
                return 1;
            }
            my ($f, $s) = @_;
            die "ouch" if !defined $f;
            my $distance = lines_distance($f,$s);
            $sum += $distance;
            return 0;
        }
    );

    return $sum;
}


#Compares all files for one movie
sub compare_files {
    Readonly my $id => shift;
    Readonly my $filenames_czech_ref => shift;
    Readonly my $filenames_english_ref => shift;

    #subtitles, indexe by filename, as array
    my %english_subtitles_in_file;

    #for each file in english
    for my $filename_english (@$filenames_english_ref) {
        #read all start times
        Readonly my @subtitles => 
           read_subs_from_file($filename_english, $MAX_LINES);
        
        #put them to hash indexed by filename
        $english_subtitles_in_file{$filename_english} =
            \@subtitles;
    }
   
    #best distance so far
    my $best_so_far = 99**99**99;

    #best pair so far
    my $best_pair_so_far = undef;
    
    #take all czech filenames
    for my $filename_czech (@$filenames_czech_ref) {
        #read all start times
        Readonly my @czech_subtitles => 
           read_subs_from_file($filename_czech, $MAX_LINES);

        #for every czech file for every english file...
        for my $filename_english (keys %english_subtitles_in_file) {
            
            #sum all distances for the czech-english pair
            my $distance = sum_of_distances(
                    \@czech_subtitles,
                    $english_subtitles_in_file{$filename_english},
                    $best_so_far
               );

            #put them to best_so_far
            if ($distance < $best_so_far) {
                $best_so_far = $distance;
                $best_pair_so_far = $filename_english."\t".$filename_czech;
            }

        }

    }
    
    say $id."\t".$best_pair_so_far."\t".$best_so_far;
    
}




