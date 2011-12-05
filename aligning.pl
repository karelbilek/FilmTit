use strict;
use warnings;
use 5.010;
use Readonly;

Readonly my $first_sub => $ARGV[0];
Readonly my $second_sub=> $ARGV[1];

binmode(STDOUT, ":utf8");

spust($first_sub, $second_sub);

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

sub find_best_fit {
    my $first = shift;
    my @arr = @_;

    my $last_distance = 99**99;
    my $last_line;
    while (scalar @arr) {
        my $distance = times_distance($first, $arr[0]);
        if ($distance < $last_distance) {
            $last_distance = $distance;
            $last_line = shift @arr;
        } else {
            return ($last_line, @arr);
        }
    }
    return ($last_line);
}

sub best_align {
    Readonly my $first_ref => shift;
    Readonly my $second_ref => shift;
    Readonly my $best_so_far => shift;


    #copies
    my @first = @$first_ref;
    my @second = @$second_ref;

    if (scalar @first ==0 or scalar @second==0) {
       return (); 
    }

    #this "czech" and "english" can be anything
    #I just used it for better imagining of the languages
    #(It is actually switched when copying from distances file)
    my @czech_results;
    my @english_results;
    
    while (scalar @first) {
        my @remaining = find_best_fit($first[0], @second);
        my $best_english_fit = $remaining[0];

        my $previous_english_fit = scalar @english_results ? $english_results[-1] : {count=>-50};
        
        if ($previous_english_fit->{count} != $best_english_fit->{count}) {
            push @english_results, $best_english_fit;
            push @czech_results, $first[0];
        } else {
            my $previous_czech_fit = $czech_results[-1];

            if (times_distance($best_english_fit, $first[0]) < 
               times_distance($best_english_fit, $previous_czech_fit)){
            
                pop @czech_results;
                push @czech_results, $first[0];
            }
       }

       @second = @remaining;
       shift @first;
    }

    if (scalar @czech_results != scalar @english_results) {
        die "OUCH 6";
    } 

    my @res = map {[$czech_results[$_], $english_results[$_], 
    times_distance( $czech_results[$_], $english_results[$_])]}
                                            (0..$#czech_results);
   
   return @res;
}

sub times_distance {
    Readonly my $first => shift;
    Readonly my $second => shift;

    Readonly my $first_start => time_to_number(first_time($first->{time}));
    Readonly my $second_start => time_to_number(first_time($second->{time}));

  
    return abs($first_start - $second_start);
}

sub read_subs {
    Readonly my $titname => shift;
    Readonly my $count => shift;

    open my $tit_file, "gunzip files/$titname.gz --stdout | iconv -f \"cp1250\" -t \"utf8\" |";
    binmode ($tit_file, ":utf8"); 
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
            
            my $number = scalar @res ? $res[-1]->{count}+1 : 1;
            my $new_hash = {count=>$number, time=>$line};
            push @res, $new_hash;
        } else {
            if (scalar @res) {
                if ($line!~/^\d*$/) {
                    $res[-1]->{subtitle}.=$line."\n";
                }
            }

        }
      
    }

    #when there are LESS titles than $count
    close $tit_file;
    return @res;

}
sub spust {
    Readonly my $first_sub_num => shift;
    Readonly my $second_sub_num => shift;

    Readonly my @first_subtitles => read_subs($first_sub_num);
    Readonly my @second_subtitles=> read_subs($second_sub_num);


    my @aligns = best_align(\@first_subtitles, \@second_subtitles);
    @aligns = sort {$a->[2]<=>$b->[2]} @aligns;
    for (@aligns) {
        #note - by my mistake, czech and english are switched in best_align
        #subroutine
        say "ENGLISH:";

        say $_->[0]->{subtitle};
        say "CZECH:";
        say $_->[1]->{subtitle};
        say "DISTANCE:";
        say $_->[2];
        say "==================";
    }
}




