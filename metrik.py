import psycopg2
import math


def cosine_similarity(v1,v2):
	"Cosinus-Ähnlichkeit der beiden Vektoren v1 und v2 berechnen"
	uu, uv, vv = 0, 0, 0
	# Skalarprodukte berechnen
	for i in range(len(v1)):
		u = v1[i]; v = v2[i]
		uu += u*u
		vv += v*v
		uv += u*v
	return uv/math.sqrt(uu*vv)

def main():
	# Verbinden mit der Datenbank
	connection = psycopg2.connect("dbname='Election' host='localhost' user='postgres' password='postgres'")

	cursor = connection.cursor();

	# Datenbankabfrage zur Berechnung der Gesamtzahl an Tweets, also der Dimensionen des Vektorraums, den wir betrachten.
	cursor.execute("SELECT count(id) FROM Tweet");
	tupel = cursor.fetchall();
	dimensionen = tupel[0][0] * 3 # zusätzliche Dimensionen für Anzahl der Retweets und Favorisierungen
	
	cursor.execute("SELECT B.name, A.Name FROM Hashtag AS A, Hashtag AS B WHERE A.name != B.name")

	tupel = cursor.fetchall()
	
	i = 0
	for t in tupel:
		hashtag1 = t[0]
		hashtag2 = t[1]
		if (hashtag1.lower() >= hashtag2.lower()):
			continue
	
		# Vektoren für Cosinus-Abstand bilden
		vektor1 = [0] * dimensionen
		vektor2 = [0] * dimensionen
	
		# Vektor 1 berechnen
		cursor.execute("SELECT Tweet_ID, wie_oft, retweet_count, favorite_count FROM T_enth_H, Tweet WHERE H_Name = '"+hashtag1+"' AND Tweet.ID = T_enth_H.Tweet_ID");
		#cursor.execute("SELECT Tweet_ID, wie_oft FROM T_enth_H WHERE H_Name = '"+hashtag1+"'");
		vektordaten = cursor.fetchall()
		#print (vektordaten)
		for d in vektordaten:
			vektor1[d[0]] = d[1]
			vektor1[d[0]+1] = d[2]
			vektor1[d[0]+2] = d[3]
			#print(d[0],d[1])	
		#print (vektor1)
	
		# Vektor 2 berechnen
		cursor.execute("SELECT Tweet_ID, wie_oft FROM T_enth_H WHERE H_Name = '"+hashtag2+"'");
		vektordaten = cursor.fetchall()
		for d in vektordaten:
			vektor2[d[0]] = d[1]
			#print(d[0],d[1])
		result = cosine_similarity(vektor1, vektor2)
		
		cursor.execute("INSERT INTO Hashtags_Aehnlichkeit(name1,name2,aehnlichkeit) VALUES('"+hashtag1+"','"+hashtag2+"',"+str(result)+")")
	
	
		i += 1
		print (str(i)+"/91774", end="\r")
	connection.commit()
	print (str(i)+"/91774")
	

if __name__ == '__main__':
	main()
