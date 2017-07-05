<html>
   <head>
      <meta charset="utf-8"></meta>
      <title>Häufigkeit aller Hashtags</title>
      <script src="jquery.js"></script>
      <script src="jquery.flot.js"></script>
      <script src="jquery.flot.time.js"></script>
<?php

/* ******* Start Ausgabe nur bei ausgewähltem Hashtag 
 *         Dient dazu, die Daten für Flot zur Verfügung zu stellen.
 */
if ($_GET['hashtag']) {
    
?>

<script>$(function() {
      var tags = new Array(<?php 

$dbconn = pg_connect("dbname='Election' host='localhost' user='postgres' password='postgres'");

// Start- und Enddatum des Datensatzes auslesen.
$result = pg_query($dbconn, "select min(time), max(time) from tweet LIMIT 10");

$arr = pg_fetch_all($result);

pg_free_result($result);

// Format auf unsere Bedürfnisse anpassen.
$startdate = DateTime::createFromFormat('Y-m-d H:i:s', $arr[0]['min']);
$enddate = DateTime::createFromFormat('Y-m-d H:i:s', $arr[0]['max']);
$startdate = DateTime::createFromFormat('Y-m-d H:i:s', $startdate->format('Y-m-d 00:00:00'));
$enddate = DateTime::createFromFormat('Y-m-d H:i:s', $enddate->add(new DateInterval('P1D'))->format('Y-m-d 00:00:00'));

// Jeden einzelnen Tag dazwischen erhalten.
$interval = DateInterval::createFromDateString('1 day');
$period = new DatePeriod($startdate, $interval, $enddate);

// Für jeden Tag die Anzahl der Hashtags berechnen und im Javascript-Format ausgeben.
foreach ( $period as $date ) {
  print("[".($date->getTimestamp())*1000);
  
  $result = pg_query_params($dbconn, "SELECT sum(wie_oft) FROM Tweet, T_enth_H WHERE Tweet.ID = T_enth_H.Tweet_ID AND time >= $1 AND time < $2 AND h_name = $3", array($date->format('Y-m-d H:i:s'), $date->add(new DateInterval('P1D'))->format('Y-m-d H:i:s'), "#".$_GET['hashtag']));

  $arr = pg_fetch_all($result);
  
  pg_free_result($result);
  
  if ($arr[0]['sum'] == '') {
    $arr[0]['sum'] = 0;
    }

  if ($date == $enddate) {
    print(", " . $arr[0]['sum'] . "]"); }
  else { print(", " . $arr[0]['sum'] . "], "); }
  
  } // Ende Javascript-Format

?>);
$.plot("#barchart", [{data: tags, color: "grey"}],
                                        {bars: {show:true, barWidth: 2},
                                         xaxis: {
                                                mode: "time",
                                                timeformat: "%d.%m.%y"
                                                }});
         });
      </script>

<?php 
} /* ******* Ende Ausgabe nur bei ausgewähltem Hashtag */

?>
      
   </head>
<body>

<?php

$dbconn = pg_connect("dbname='Election' host='localhost' user='postgres' password='postgres'");

// Wähle alle Hashtags aus (für die Auswahlliste der Hashtags
$result = pg_query($dbconn, "SELECT name FROM Hashtag ORDER BY name");

$arr = pg_fetch_all($result);

pg_free_result($result);

?> 


<form action="a2_5.php"> <label>Hashtag: <select name="hashtag"> 
<?php 

for ($i = 0; $i < count($arr); $i++) {
    print ("<option value=\"". substr($arr[$i]['name'],1) . "\">".$arr[$i]['name'] . "</option>\n");
    }
    
?>
    <input value="Anzeigen" type="submit"></form>
    
    <?php
    if ($_GET['hashtag']) { ?>
    
    <h1>Übersicht Auftreten des Hashtags <?php print("#" . $_GET['hashtag']); ?></h1>
    
    <div id="barchart" style="width:1200px; height:400px;"></div>
    
    <?php } ?>
</form>
