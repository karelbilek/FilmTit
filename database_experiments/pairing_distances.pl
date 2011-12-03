use strict;
use warnings;
use 5.010;
use Readonly;

Readonly my $MAX_MOVIES =>2000000;
Readonly my $MAX_LINES => 30;
Readonly my $FIRST_MOVIE => 0;

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
    
    next READ if ($id < $FIRST_MOVIE);

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
#use Scalar::Util qw(looks_like_number);
sub mylooks_like_number{
    return $_[0] =~ /^-?(\d+\.?\d*|\.\d+)$/;
}

sub time_to_number {
    Readonly my $time => shift;

    my ($hour, $minute, $second, $mili) = split /[,:.]/, $time;

    if (!defined $hour) {
        die "OUCH - $time";
    }
    if (!defined $mili) {
        die "OUCH 3 - $time";
    }
    if (scalar( grep {!mylooks_like_number($_)} ($hour,$minute,$second,$mili))!=0) {
        say "OUCH4";
        
        my $what = join "- ", map {mylooks_like_number($_) ? "[$_] je number." :
            "[$_] neni number."} ($hour, $minute, $second, $mili);
        
        die "OUCH4 - [$time] - $what";
    }
    my $number = $hour*3600*1000+$minute*60*1000+$second*1000+$mili;
    return $number;
}

sub first_time {
   $_[0] =~ /^(\S*)\s*-->\s?/ or die "OUCH2 - ".$_[0];
    return $1;
}

sub best_distance {
    my $first = shift;
    my @arr = @_;

    my $last_distance = 99**99;
    my $last_line;
    while (scalar @arr) {
        my $distance = lines_distance($first, $arr[0]);
        if ($distance < $last_distance) {
            $last_distance = $distance;
            $last_line = shift @arr;
        } else {
            return ($last_distance, $last_line, @arr);
        }
    }
    return ($last_distance, $last_line);
}

sub sum_of_distances {
    Readonly my $first_ref => shift;
    Readonly my $second_ref => shift;
    Readonly my $best_so_far => shift;


    #copies
    my @first = @$first_ref;
    my @second = @$second_ref;

    if (scalar @first ==0 or scalar @second==0) {
        return 99**99**99;
    }

    my $sum = 0;

    while (scalar @first) {
        if ($sum >= $best_so_far) {
            return $best_so_far+1;
        }
        
        my ($distance, @remaining) = best_distance($first[0], @second);
        shift @first;

        $sum += $distance;
        @second = @remaining;
    }

    return $sum;
    
}

sub stupid_sum_of_distances {
    Readonly my $first_ref => shift;
    Readonly my $second_ref => shift;

    my $res = 0;

    for my $first_sub_time (@$first_ref) {
        

        my $minimal_distance = 99**99;
        for my $second_sub_time (@$second_ref) {
            my $distance = lines_distance($first_sub_time, $second_sub_time); 
            if ($distance < $minimal_distance) {
                $minimal_distance = $distance;
            }
        }
        $res += $minimal_distance;
    }

    return $res;
}

sub lines_distance {
    Readonly my $first => shift;
    Readonly my $second => shift;

    Readonly my $first_start => time_to_number(first_time($first));
    Readonly my $second_start => time_to_number(first_time($second));

  
    return abs($first_start - $second_start);
}

sub read_first_times {
    Readonly my $titname => shift;
    Readonly my $count => shift;

    open my $tit_file, "gunzip files/$titname.gz --stdout |";
    
    #warn "Ctu $titname.\n";
    my @res;

    SUBTITREAD:
    while (my $line = <$tit_file>) {
        chomp $line;

        #carriage return?
        $line =~ s/\r//;
        
        #Yes, there can be negative numbers AND there is sometimes --> in the
        #text itself >:-(
 
        if ($line =~ /^(-?\d?\d:-?\d?\d:-?\d?\d[.,]-?\d+)\s?-->/) {
            #warn "Pridavam $line.\n";
            push @res, $line;
            if (scalar @res == $count) {
                close $tit_file;
                return @res;
            }
        }
      
    }

    #when there are LESS titles than $count
    close $tit_file;
    return @res;

}
sub spust_srovnani {
    Readonly my $id => shift;
    Readonly my $filenames_czech_ref => shift;
    Readonly my $filenames_english_ref => shift;

    my %subtitle_times_in_file;

    for my $filename_english (@$filenames_english_ref) {
        Readonly my @first_times => 
           read_first_times($filename_english, $MAX_LINES);
        
        $subtitle_times_in_file{$filename_english} =
            \@first_times;
    }


    my %distances;

    my $best_so_far = 99**99**99;

    for my $filename_czech (@$filenames_czech_ref) {
        Readonly my @first_times => 
           read_first_times($filename_czech, $MAX_LINES);

        for my $filename_english (keys %subtitle_times_in_file) {
            #warn "!!! $filename_english $filename_czech\n";
            my $distance = sum_of_distances(
                    \@first_times,
                    $subtitle_times_in_file{$filename_english},
                    $best_so_far
               );
            if ($distance < $best_so_far) {
                $best_so_far = $distance;
            }
            $distances{$filename_english."\t".$filename_czech} = $distance;

        }

    }

    my $best = (sort {$distances{$a} <=> $distances{$b}} keys %distances)[0];
    
    say $id."\t".$best."\t".$distances{$best};
    
}




