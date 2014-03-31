#! /usr/bin/perl

use strict;
use warnings;

use IO::File;


my ($title,$title1)=0;
my $c=0;

my $filename = 'Data_18_19.txt';
my $filename1= 'Process_data_asso_18_19.txt'; 
open(my $fh,'>',$filename1) or die "could not open file '$filename1'$!";
open(my $info,$filename) or die "could not open file '$filename'$!";
	
	while(my $line=<$info>)
			{
				print $line;
				$line =~ /^(.*?)\,(.*)$/;
				$title=$2;
				print "\n"."$2";
			#	printf$1." ".$2." ".$3;
				
				printf $fh $2."\n";
				#exit;
			#	else
			#		{
			#		printf $fh"\n";
			#		}
	       		#	my $line1 = <$info1>;
			#	$line1 =~ /^(.*?)\>(.*?)\>(.*)$/;
			#	$title1=$3;
			}
