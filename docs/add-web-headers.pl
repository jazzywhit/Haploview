#opens all the files in the directory ending in .php and strips <html> and
#<body> tags and replaces 'em with the php header and footer for the web
#page table of contents.

$dir = $ARGV[0];

opendir (THISDIR, $dir);
@files = readdir(THISDIR);
closedir(THISDIR);

foreach $file (@files){
    if ($file =~ /php$/){
	print "adding web headers: $file\n";
	my $lines;
	open (FILE, "$dir/$file");
	while (<FILE>){
	    $lines = $lines . $_;
	}
	close FILE;

	open (OUT, ">$dir/$file");
	print OUT "<?php \$documentation=true;\nrequire(\"header.php\"); ?>\n";
	$lines =~ s/(<html[.\n]*<body.*?>)//;
	$lines =~ s/<\/body><\/html>//;
	print OUT $lines;
	print OUT "<? require(\"footer.html\"); ?>\n";
	close OUT;
    }
}

