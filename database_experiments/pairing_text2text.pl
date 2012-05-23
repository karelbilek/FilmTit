use strict;
use warnings;
use 5.010;
use Readonly;
use Pairing;

Readonly my $FIRST => $ARGV[0];
Readonly my $SECOND => $ARGV[1];


#input:arrays of first 100 times from a given movie for 2 translation
#output: sub of distances
sub get_best_pairs {
    Readonly my $first_ref => shift;
    Readonly my $second_ref => shift;

    my @res;

    do_for_all_shortest_distances ($first_ref, $second_ref, 
        sub {
            die "now";
        }, sub {
            push(@res, [@_]);
            return 0;
        }
    );
    return @res;
}

    
#read all start times
Readonly my @first_subs => 
    read_subs_from_file($FIRST);
    
Readonly my @second_subs => 
    read_subs_from_file($SECOND);

my @pairs = get_best_pairs(\@first_subs, \@second_subs);


my @res;
my @arena;
my $last_pair;
my $i=0;
for my $pair (@pairs) {
    $i++;
    my $start = $pair->[1]->{start};
    if ($pair == $pairs[-1] or 
            (defined $last_pair and $start ne $last_pair->[1]->{start})){
        #if there is more "left" subtitles
        #than "right" subtitles
        #they will fight in arena

        my $arena_winner;
        my $arena_winner_score=99**99**99;
        for my $subtitle (@arena) {
            my $score = lines_distance($subtitle, $last_pair->[1], 1);
            if ($score < $arena_winner_score) {
                $arena_winner_score = $score;
                $arena_winner = $subtitle;
            }
        }
        die if (!defined $arena_winner);
        push (@res, [$arena_winner, $last_pair->[1]]);
        @arena=();
    }
    push (@arena, $pair->[0]);
    
    $last_pair = $pair;
}

for my $pair (@res) {
    say $pair->[0]->{sub}."\t".$pair->[1]->{sub}if(lines_distance(@$pair)
    < 20000000);  
}



