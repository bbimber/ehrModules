#!/usr/bin/perl

=head1 DESCRIPTION

This script is designed to run as a cron job.  It will query a number of tables an email a report.
The report is focused on TB tests


=head1 LICENSE

This package and its accompanying libraries are free software; you can
redistribute it and/or modify it under the terms of the GPL (either
version 1, or at your option, any later version) or the Artistic
License 2.0.

=head1 AUTHOR

Ben Bimber

=cut

#config options:
my $baseUrl = 'https://ehr.primate.wisc.edu/';
my $studyContainer = 'WNPRC/EHR/';

#whitespace separated list of emails
my @email_recipients = qw(bimber@wisc.edu);
my $mail_server = 'smtp.primate.wisc.edu';

#emails will be sent from this address
my $from = 'ehr-no-not-reply@primate.wisc.edu';


############Do not edit below this line
use strict;
use warnings;
use Labkey::Query;
use Net::SMTP;
use Data::Dumper;
use Time::localtime;

# Find today's date
my $tm = localtime;
my $datetimestr=sprintf("%04d-%02d-%02d at %02d:%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday, $tm->hour, $tm->min);
my $datestr=sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);
my $timeOfDay = $tm->hour;

my $email_html = "This email contains reports on TB Tests.  It was run on: $datetimestr.<p>";
my $results;
my $sendEmail = 0;



#find any animals TB tested 24 hours ago, but lacking result1
$tm = localtime( time() - ( 1 * 24 * 60 * 60 ) );
my $startDate =sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);  
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'tb',
    -filterArray => [    	
    	['Id/Dataset/Demographics/calculated_status', 'eq', 'Alive'],
		['date', 'dateeq', $startDate],
		['result1', 'isblank', ''],		
    ],      
    #-debug => 1,
);

if(@{$results->{rows}}){
	$sendEmail = 1;
	$email_html .= "<b>WARNING: The following animals were TB tested 24H ago (".$startDate."), but are missing the 24H result.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=TB Tests&query.date~dateeq=$startDate&query.Id/Dataset/Demographics/calculated_status~eq=Alive&query.result1~isblank"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'Id'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
		
}


#we find any animals TB tested 48 hours ago, but lacking result2
$tm = localtime( time() - ( 2 * 24 * 60 * 60 ) );
$startDate =sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);
  
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'tb',
    -filterArray => [    	
    	['Id/Dataset/Demographics/calculated_status', 'eq', 'Alive'],
		['date', 'dateeq', $startDate],
		['result2', 'isblank', ''],		
    ],      
    #-debug => 1,
);

if(@{$results->{rows}}){
	$email_html .= "<b>WARNING: The following animals were TB tested 48H ago (".$startDate."), but are missing results.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=TB Tests&query.date~dateeq=$startDate&query.Id/Dataset/Demographics/calculated_status~eq=Alive&query.result2~isblank"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'Id'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
	
}	


#we find any animals TB tested 72 hours ago, but lacking any result
$tm = localtime( time() - ( 3 * 24 * 60 * 60 ) );
$startDate =sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);
  
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'tb',
    -filterArray => [    	
    	['Id/Dataset/Demographics/calculated_status', 'eq', 'Alive'],
		['date', 'dateeq', $startDate],
		['missingResults', 'eq', 'true'],		
    ],      
    #-debug => 1,
);

if(@{$results->{rows}}){
	$email_html .= "<b>WARNING: The following animals were TB tested 72H ago (".$startDate."), but are missing results.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=TB Tests&query.date~dateeq=$startDate&query.Id/Dataset/Demographics/calculated_status~eq=Alive&query.missingResults~eq=true"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'Id'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
	
}	


#we find any 24H results of 4 or 5
$tm = localtime( time() - ( 1 * 24 * 60 * 60 ) );
$startDate =sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);
  
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'tb',
    -filterArray => [    	
    	['Id/Dataset/Demographics/calculated_status', 'eq', 'Alive'],
		['date', 'dateeq', $startDate],
		['result1', 'in', '4;5'],		
    ],      
    #-debug => 1,
);

if(@{$results->{rows}}){
	$sendEmail = 1;
	$email_html .= "<b>WARNING: The following animals have 24H results (".$startDate.") of 4 or 5.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=TB Tests&query.date~dateeq=$startDate&query.Id/Dataset/Demographics/calculated_status~eq=Alive&query.result1~in=4;5"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'Id'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
		
}


#we find any 48H results of 3, 4 or 5
$tm = localtime( time() - ( 2 * 24 * 60 * 60 ) );
$startDate =sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);
  
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'tb',
    -filterArray => [    	
    	['Id/Dataset/Demographics/calculated_status', 'eq', 'Alive'],
		['date', 'dateeq', $startDate],
		['result2', 'in', '3;4;5'],		
    ],      
    #-debug => 1,
);

if(@{$results->{rows}}){
	$sendEmail = 1;
	$email_html .= "<b>WARNING: The following animals have 48H results (".$startDate.") of 3, 4 or 5.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=TB Tests&query.date~dateeq=$startDate&query.Id/Dataset/Demographics/calculated_status~eq=Alive&query.result2~in=3;4;5"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'Id'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
		
}


#we find any 72H results of 3, 4 or 5
$tm = localtime( time() - ( 3 * 24 * 60 * 60 ) );
$startDate =sprintf("%04d-%02d-%02d", $tm->year+1900, ($tm->mon)+1, $tm->mday);
  
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'tb',
    -filterArray => [    	
    	['Id/Dataset/Demographics/calculated_status', 'eq', 'Alive'],
		['date', 'dateeq', $startDate],
		['result3', 'in', '3;4;5'],		
    ],      
    #-debug => 1,
);

if(@{$results->{rows}}){
	$sendEmail = 1;
	$email_html .= "<b>WARNING: The following animals have 72H results (".$startDate.") of 3, 4 or 5.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=TB Tests&query.date~dateeq=$startDate&query.Id/Dataset/Demographics/calculated_status~eq=Alive&query.result3~in=3;4;5"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'Id'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
		
}


open(HTML, ">", "C:\\Users\\Admin\\Desktop\\test.html");
print HTML $email_html;
close HTML;

#my $smtp = Net::SMTP->new($mail_server,
#    Timeout => 30,
#    Debug   => 0,
#);
#$smtp->mail( $from );
#$smtp->recipient(@email_recipients, { Notify => ['FAILURE'], SkipBad => 1 });  
#
#$smtp->data();
#$smtp->datasend("Subject: Daily TB Alerts: $datestr\n");
#$smtp->datasend("Content-Transfer-Encoding: US-ASCII\n");
#$smtp->datasend("Content-Type: text/html; charset=\"US-ASCII\" \n");
#$smtp->datasend("\n");
#$smtp->datasend($email_html);
#$smtp->dataend();
#
#$smtp->quit;

