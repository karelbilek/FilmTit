use strict;
use warnings;

use 5.010;

use File::Slurp;

my $which = $ARGV[0];

my $name = "named_ent_xml_simple.txt";

open my $file, "<:utf8", $name;

my @lines = <$file>;
chomp @lines;
@lines = grep {$_ !~ /^\s*$/} @lines;
@lines = map {if ($_ eq "<doc>" or $_ eq "</doc>") {$_} else {"<sentence>$_</sentence>"}} @lines;

use XML::LibXML;
my $doc = XML::LibXML->load_xml(string=>join("\n", @lines));


my %_conversions = (
    1=>["^[im]", "organization"],
    2=>["^[pP]", "person"],
    3=>["^g", "place"]
);

my $source = $_conversions{$which}[0];
my $target = $_conversions{$which}[1];


for my $sentence ($doc->getElementsByTagName("sentence")) {
    NE:
    for my $ne ($sentence->getElementsByTagName("ne")) {
        #next NE if (!define
        #my $father = $ne->parentNode;
        my $type = $ne->getAttribute("type");
            
        if ($type =~ /$source/) {
            my $content=$ne->textContent();
            my $newnode = $doc->createElement( $target );
            my $textnode = $doc->createTextNode($content);
            $newnode->addChild($textnode);
            $ne->replaceNode($newnode);
        }
    }
    
    for my $ne ($sentence->getElementsByTagName("ne")) {
        my $content=$ne->textContent();
        my $textnode = $doc->createTextNode($content);
        $ne->replaceNode($textnode);

    }
}

use Encode;
use HTML::Entities;

my $x = decode_utf8(decode_entities($doc->toString()));

binmode STDOUT, ":utf8";

$x =~s/<\?xml[^>]*>\n//;
$x =~s/<\/?sentence>//g;
$x =~s/<\/?doc>\n?//g;
$x =~ s/<$target>/<START:$target> /g;
$x =~ s/<\/$target>/ <END>/g;

print $x;
