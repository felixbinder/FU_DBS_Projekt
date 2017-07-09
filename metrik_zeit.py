# -*- coding: utf-8 -*-
#Achtung, braucht eine Weile zum Ausführen!
import psycopg2
import math
from datetime import datetime

connection = psycopg2.connect("dbname='Election' host='localhost' user='postgres' password='postgres'")
cursor = connection.cursor();


def metrik():
	# Verbinden mit der Datenbank
	cursor.execute("DELETE FROM hashtags_aehnlichkeit;") #Erstmal alles löschen
	connection.commit()

	# Datenbankabfrage: 
	cursor.execute("SELECT name FROM Hashtag;")
	hashtags = cursor.fetchall()
	for htA in hashtags:
		for htB in hashtags:
			print("Berechne: "+str(htA[0])+" und "+str(htB[0]))
		#Berechne Abstand htA und htB 
			final_ae = aehnlichtkeit(htA,htB) + aehnlichtkeit(htA,htB)
			cursor.execute("INSERT INTO hashtags_aehnlichkeit(name1,name2,aehnlichkeit) VALUES('"+str(htA[0])+"','"+str(htB[0])+"',"+str(final_ae)+");")
	connection.commit()


def aehnlichtkeit(htA, htB):
	aehnlichkeit = 0
	count = 0
	cursor.execute("SELECT time FROM tweet, t_enth_h WHERE tweet.ID = t_enth_h.tweet_id AND h_name = '" + str(htA[0]) + "';")
	atweets = cursor.fetchall() #[alle Tweets, die Hashtag A enthalten]
	cursor.execute("SELECT time FROM tweet, t_enth_h WHERE tweet.ID = t_enth_h.tweet_id AND h_name = '" + str(htB[0]) + "';")
	btweets = cursor.fetchall() #[alle Tweets, die Hashtag B enthalten]
	for at in atweets:
		k=0
		# a_date = datetime.strptime(at[0], '%Y-%m-%d %-H:%M:%S')
		distance = []
		for bt in btweets:
			# b_date = datetime.strptime(bt[0], '%Y-%m-%d %-H:%M:%S')
			# distance[n] = abs(a_date - b_date)
			distance.append(float(abs((at[0] - bt[0]).total_seconds())))
		md = min(distance)
		if md == 0:
			aehnlichkeit+100 #Wert wählen!
		else: 
			aehnlichkeit += 1000/md
	return aehnlichkeit