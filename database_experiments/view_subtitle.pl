#!/usr/bin/perl
system('gunzip files/'.$ARGV[0].'.gz --stdout | iconv -f "cp1250" -t "utf8" | less')
