#!/usr/local/bin/php -q
<?
$fp=fopen($_SERVER["argv"][1],"r");

$lineN=2;
$lastRecordN=-1;
fgets($fp,1024);
while ( $r=fgetcsv($fp,1024) ) {
//	print_r($r); die();

	if ( 1 != $r[12]-$lastRecordN ) {
		printf("gap between line %d and %d\n",$lineN-1,$lineN);
	}

	$lastRecordN=$r[12];
	$lineN++;
}

fclose($fp);

?>
