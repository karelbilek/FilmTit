#!/usr/bin/perl
system('gunzip export/files/'.$ARGV[0].'.gz --stdout | iconv -f "cp1250" -t "utf8" | less')
