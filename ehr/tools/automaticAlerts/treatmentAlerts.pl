#!/usr/bin/perl

=head1 DESCRIPTION

This script is designed to run as a cron job.  It will query a number of tables an email a report.
The report is designed to identify potential problems with the colony, primarily related to weights, housing
and assignments.


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
my @email_recipients = qw(bimber@wisc.edu cm@primate.wisc.edu wnprcvets@primate.wisc.edu);
@email_recipients = qw(bimber@wisc.edu);
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
my $timestr = sprintf("%02d:%02d", $tm->hour, $tm->min); 
my $timeOfDay = $tm->hour;

my $email_html = "This email contains any scheduled treatments not marked as completed.  It was run on: $datetimestr.<p>";
my $results;


#we find any rooms lacking obs for today
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'ehr',
    -queryName => 'RoomsWithoutObsToday',
    #-debug => 1,
);

if(@{$results->{rows}}){
	$email_html .= "<b>WARNING: The following rooms do not have any obs for today as of $timestr.</b><br>";
	#$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=ehr&query.queryName=RoomsWithoutObsToday"."'>Click here to view them</a><br>\n";

    foreach my $row (@{$results->{rows}}){
    	$email_html .= $row->{'room'}."<br>";				
    }

	$email_html .= "<hr>\n";			
}
else {
	
}	


#we find any treatments where the animal is not assigned to that project
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'treatmentSchedule',
    -filterArray => [    	
    	['Id/DataSet/Demographics/calculated_status', 'eq', 'Alive'],
		['projectStatus', 'isnonblank', ''],
		['date', 'dateeq', $datestr],		
    ],    
    #-debug => 1,
);

if(@{$results->{rows}}){
	$email_html .= "<b>WARNING: There are ".@{$results->{rows}}." scheduled treatments where the animal is not assigned to the project.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=treatmentSchedule&query.projectStatus~isnonblank&query.Id/DataSet/Demographics/calculated_status~eq=Alive&query.date~dateeq=$datestr"."'>Click here to view them</a><br>\n";
	$email_html .= "<hr>\n";			
}	

	
#we find treatments for each time of day:
my $areas = ['Charmany'];
$email_html .= "<b>Only the following areas are using online treatments: ".join(';', @$areas).", so this email will only include those areas.</b><p>";

processTreatments('AM', 9);
processTreatments('PM', 14);
processTreatments('Night', 15);


sub processTreatments {
	my $timeofday = shift;
	my $minTime = shift;

	$results = Labkey::Query::selectRows(
	    -baseUrl => $baseUrl,
	    -containerPath => $studyContainer,
	    -schemaName => 'study',
	    -queryName => 'treatmentSchedule',
	    -columns => 'Id,CurrentArea,CurrentRoom,CurrentCage,projectStatus,treatmentStatus,treatmentStatus/Label,meaning,code,,volume2,conc2,route,amount2,remark,performedby',
	    -sort => 'CurrentArea,CurrentRoom',
	    -filterArray => [
	    	['date', 'dateeq', $datestr],
	    	['timeofday', 'eq', $timeofday],
	    	['CurrentArea', 'in', join(';', @$areas)],
			['Id/DataSet/Demographics/calculated_status', 'eq', 'Alive'],   	
	    ],    	   	          
	    #-debug => 1,
	);
	
	$email_html .= "<b>$timeofday Treatments:</b><br>";
	
	if(!@{$results->{rows}}){
		$email_html .= "There are no scheduled $timeofday treatments as of $timestr. Treatments could be added after this email was sent, so please check online closer to the time.<hr>";	
	}		
	else {
		my $complete = 0;
		my $incomplete = 0;
		my $summary = {};
	    foreach my $row (@{$results->{rows}}){    	
			if($row->{'treatmentStatus/Label'} && $row->{'treatmentStatus/Label'} eq 'Completed'){
				$complete++;
#				if(!$$summary{$row->{'CurrentArea'}}){
#					$$summary{$row->{'CurrentArea'}} = {};				
#				}	
#				if(!$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}){
#					$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}} = {complete=>0,incomplete=>0, incompleteRecords=>[]};				
#				}	
#				
#				$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}{complete}++;
			}   
			else {
				if(!$$summary{$row->{'CurrentArea'}}){
					$$summary{$row->{'CurrentArea'}} = {};				
				}	
				if(!$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}){
					$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}} = {complete=>0,incomplete=>0,incompleteRecords=>[]};				
				}	
				
				$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}{incomplete}++;
				push(@{$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}{incompleteRecords}}, $row);
				
				$incomplete++;				
			}     
	    };
	
		my $url = "<a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=treatmentSchedule&query.timeofday~eq=$timeofday&query.date~dateeq=$datestr&query.Id/DataSet/Demographics/calculated_status~eq=Alive"."'>Click here to view them</a></p>\n";
		$email_html .= "There are ".@{$results->{rows}}." scheduled $timeofday treatments.  $complete have been completed.  $url<p>\n";		
	
		if($timeOfDay >= $minTime){
			if(!$incomplete){
				$email_html .= "All scheduled $timeofday treatments have been marked complete as of $datetimestr.<p>\n";		
			}
			else {
				$email_html .= "The following $timeofday treatments have not been marked complete as of $datetimestr:<p>\n";
		
				my $prevRoom = '';
				foreach my $area (sort(keys %$summary)){
					my $rooms = $$summary{$area};			
					$email_html .= "<b>$area:</b><br>\n";
					foreach my $room (sort(keys %$rooms)){
						if($$rooms{$room}{incomplete}){
							$email_html .= "$room: ".$$rooms{$room}{incomplete}."<br>\n";
							$email_html .= "<table border=1><tr><td>Id</td><td>Treatment</td><td>Route</td><td>Concentration</td><td>Amount To Give</td><td>Volume</td><td>Instructions</td><td>Ordered By</td></tr>";
							
							foreach my $rec (@{$$rooms{$room}{incompleteRecords}}){
								$email_html .= "<tr><td>".$$rec{Id}."</td><td>".($$rec{meaning} ? $$rec{meaning} : '')."</td><td>".($$rec{route} ? $$rec{route} : '')."</td><td>".($$rec{conc2} ? $$rec{conc2} : '')."</td><td>".($$rec{amount2} ? $$rec{amount2} : '')."</td><td>".($$rec{volume2} ? $$rec{volume2} : '')."</td><td>".($$rec{remark} ? $$rec{remark} : '')."</td><td>".($$rec{userid} ? $$rec{userid} : '')."</td></tr>";
							}
							
							$email_html .= "</table><p>\n";	    	
						}
	
					}
					$email_html .= '<p>';	
				}
			}
		}
		else {
			$email_html .= "It is too early in the day to send warnings about incomplete treatments\n";
		}
		
		$email_html .= '<hr>';
	}
}


#then any treatments from today that different from the order:
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'TreatmentsThatDiffer',
    -columns => '*',
    -filterArray => [
    	['date', 'dateeq', $datestr],	
    ],    	   	          
    #-debug => 1,
);

$email_html .= "<b>Treatments that differ from what was ordered:</b><br>";

if(!@{$results->{rows}}){
	$email_html .= "All entered treatments given match what was ordered.<hr>";	
}		
else {
	my $summary = {};
    foreach my $row (@{$results->{rows}}){
		if(!$$summary{$row->{'CurrentArea'}}){
			$$summary{$row->{'CurrentArea'}} = {};				
		}	
		if(!$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}){
			$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}} = [];				
		}	
			
		push(@{$$summary{$row->{'CurrentArea'}}{$row->{'CurrentRoom'}}}, $row);				     
    };	
	
	my $prevRoom = '';
	foreach my $area (sort(keys %$summary)){
		my $rooms = $$summary{$area};			
		$email_html .= "<b>$area:</b><br>\n";
		foreach my $room (sort(keys %$rooms)){
			$email_html .= "$room: ".@{$$rooms{$room}}."<br>\n";
				
			foreach my $rec (@{$$rooms{$room}}){
				$email_html .= '<table border=1><tr><td>';
				$email_html .= 'Id: '.$$rec{id}."<br>\n";
				$email_html .= 'Treatment: '.$$rec{meaning}."<br>\n";
				$email_html .= 'Ordered By: '.$$rec{performedby}."<br>\n";
				$email_html .= 'Performed By: '.$$rec{drug_performedby}."<br>\n";
				

				if($$rec{route} && $$rec{route} ne $$rec{drug_route}){
					$email_html .= 'Route Ordered: '.$$rec{route}."<br>\n";					
					$email_html .= 'Route Entered: '.$$rec{drug_route}."<br>\n";
				}
				if($$rec{concentration} && ($$rec{concentration} != $$rec{drug_concentration} || $$rec{conc_units} ne $$rec{drug_conc_units})){
					$email_html .= 'Concentration Ordered: '.$$rec{concentration}.' '.$$rec{conc_units}."<br>\n";					
					$email_html .= 'Concentration Entered: '.$$rec{drug_concentration}.' '.$$rec{drug_conc_units}."<br>\n";
				}	
				if($$rec{dosage} && ($$rec{dosage} != $$rec{drug_dosage} || $$rec{dosage_units} ne $$rec{drug_dosage_units})){
					$email_html .= 'Dosage Ordered: '.$$rec{dosage}.' '.$$rec{dosage_units}."<br>\n";					
					$email_html .= 'Dosage Entered: '.$$rec{drug_dosage}.' '.$$rec{drug_dosage_units}."<br>\n";
				}
				if($$rec{amount} && ($$rec{amount} != $$rec{drug_amount} || $$rec{amount_units} ne $$rec{drug_amount_units})){
					$email_html .= 'Amount Ordered: '.$$rec{amount}.' '.$$rec{amount_units}."<br>\n";					
					$email_html .= 'Amount Entered: '.$$rec{drug_amount}.' '.$$rec{drug_amount_units}."<br>\n";
				}
				if($$rec{volume} && ($$rec{volume} != $$rec{drug_volume} || $$rec{vol_units} ne $$rec{drug_vol_units})){
					$email_html .= 'Volume Ordered: '.$$rec{volume}.' '.$$rec{vol_units}."<br>\n";					
					$email_html .= 'Volume Entered: '.$$rec{drug_volume}.' '.$$rec{drug_vol_units}."<br>\n";
				}
			}
			$email_html .= '</td></tr></table>';	
			$email_html .= "<p>\n";	    	
		}
					
		$email_html .= '<p>';	
	}
}


#we find any treatments where the animal is not alive
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'Treatment Orders',
    -filterArray => [
    	['Id/DataSet/Demographics/calculated_status', 'neq', 'Alive'],
		['enddate', 'isblank', ''],    			    	
    ],    
    #-debug => 1,
);

if(@{$results->{rows}}){
	$email_html .= "<b>WARNING: There are ".@{$results->{rows}}." active treatments for animals not currently at WNPRC.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=Treatment Orders&query.enddate~isblank&query.Id/DataSet/Demographics/calculated_status~neq=Alive"."'>Click here to view and update them</a><br>\n";
	$email_html .= "<hr>\n";			
}	

#we find any problems where the animal is not alive
$results = Labkey::Query::selectRows(
    -baseUrl => $baseUrl,
    -containerPath => $studyContainer,
    -schemaName => 'study',
    -queryName => 'Problem List',
    -filterArray => [
    	['Id/DataSet/Demographics/calculated_status', 'neq', 'Alive'],
		['enddate', 'isblank', ''],    			    	
    ],    
    #-debug => 1,
);

if(@{$results->{rows}}){
	$email_html .= "<b>WARNING: There are ".@{$results->{rows}}." unresolved problems for animals not currently at WNPRC.</b><br>";
	$email_html .= "<p><a href='".$baseUrl."query/".$studyContainer."executeQuery.view?schemaName=study&query.queryName=Problem List&query.enddate~isblank&query.Id/DataSet/Demographics/calculated_status~neq=Alive"."'>Click here to view and update them</a><br>\n";
	$email_html .= "<hr>\n";			
}


#open(HTML, ">", "C:\\Users\\Admin\\Desktop\\test.html");
#print HTML $email_html;
#close HTML;

my $smtp = Net::SMTP->new($mail_server,
    Timeout => 30,
    Debug   => 0,
);
$smtp->mail( $from );
$smtp->recipient(@email_recipients, { Notify => ['FAILURE'], SkipBad => 1 });  

$smtp->data();
$smtp->datasend("Subject: Daily Colony Alerts\n");
$smtp->datasend("Content-Transfer-Encoding: US-ASCII\n");
$smtp->datasend("Content-Type: text/html; charset=\"US-ASCII\" \n");
$smtp->datasend("\n");
$smtp->datasend($email_html);
$smtp->dataend();

$smtp->quit;

