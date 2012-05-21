package Pairing;
use warnings;
use strict;
use Perl6::Export::Attrs;
use Readonly;
use 5.010;
#checks, if string looks like number
#(perl util's doesn't work that well)
sub mylooks_like_number{
    return $_[0] =~ /^-?(\d+\.?\d*|\.\d+)$/;
}

#converting time to milliseconds
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

#see sum_of_distances
#takes one line from one file
#and whole array fron second one
#and finds the one with best distance & returns it
sub shift_to_best_distance {
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
            return ($last_line, @arr);
        }
    }
    return ($last_line);
}

#input:arrays of first 100 times from a given movie for 2 translation
#output: sub of distances
sub do_for_all_shortest_distances :Export(:DEFAULT) {
    Readonly my $first_ref => shift;
    Readonly my $second_ref => shift;
    
    my $sanitycheck_sub = shift;
    my $every_pair_sub = shift;

    #copies
    my @first = @$first_ref;
    my @second = @$second_ref;

    if (scalar @first ==0 or scalar @second==0) {
        $sanitycheck_sub->();
    } else {

        SUBTIT:
        #for every line in first array
        while (scalar @first) {
            #forget it if we are already worse than a file before
            
            #take one time from file A and array of the times from file B
            #takes the subtitle from B which has shortest distance from A, returns
            #the distance to $distance, shifts the one with shortest disance and
            #the ones before from @second and returns
            #the resulting array to @remaining
            @second = shift_to_best_distance($first[0], @second);

            my $last = $every_pair_sub->($first[0], $second[0]);
            shift @first;
            
            last SUBTIT if $last;
        }
    }
}

sub read_subs_from_file :Export(:DEFAULT){
    Readonly my $titname => shift;
    Readonly my $count => shift // 99**99**99;

    open my $tit_file, "gunzip /tmp/database_experiments/files/$titname.gz --stdout |";
    
    #warn "Ctu $titname.\n";
    my @res;

    my %previous;
    SUBTITREAD:
    while (my $line = <$tit_file>) {
        chomp $line;

        #carriage return?
        $line =~ s/\r//;
        
        #Yes, there can be negative numbers AND there is sometimes --> in the
        #text itself >:-(
 
        if ($line =~ /^(-?\d?\d:-?\d?\d:-?\d?\d[.,]-?\d+)\s*-->\s*(-?\d?\d:-?\d?\d:-?\d?\d[.,]-?\d+)/) {
            if (exists $previous{start}){
            #warn "Pridavam $line.\n";
                $previous{sub} =~ s/\n/\|/g;
                $previous{sub} =~ s/\|*$//g;
                
                push @res, {%previous}; 
                if (scalar @res == $count) {
                    close $tit_file;
                    @res = sort {($a->{start}+$a->{end})<=>($b->{start}+$b->{end})} @res;
                    return @res;
                }
            }
            $previous{start} = time_to_number($1);
            $previous{end} = time_to_number($2);
            $previous{sub}="";
        } else {
            $previous{sub}.= $line."\n" if ($line !~ /^\d*$/);
        }
        
      
    }

    #when there are LESS titles than $count
    close $tit_file;
    @res = sort {($a->{start}+$a->{end})<=>($b->{start}+$b->{end})} @res;
    return @res;

}
 

#distance of the lines
sub lines_distance :Export(:DEFAULT) {
    Readonly my $first => shift;
    Readonly my $second => shift;

use Data::Dumper;
#print Dumper($first);
    Readonly my $first_start => $first->{start};
    Readonly my $second_start => $second->{start};
    Readonly my $first_end => $first->{end};
    Readonly my $second_end => $second->{end};
return (abs(($first_start+$first_end)/2 -
    ($second_start+$second_end)/2))**2;
}

1;
