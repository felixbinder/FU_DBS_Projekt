import psycopg2
import math


def cosine_similarity(v1,v2):
    "compute cosine similarity of v1 to v2: (v1 dot v2)/{||v1||*||v2||)"
    sumxx, sumxy, sumyy = 0, 0, 0
    for i in range(len(v1)):
        x = v1[i]; y = v2[i]
        sumxx += x*x
        sumyy += y*y
        sumxy += x*y
    return sumxy/math.sqrt(sumxx*sumyy)

def main():
	dimensionen = 6126 # Entspricht der Gesamtzahl an Tweets

	 
	# Verbinden mit der Datenbank
	connection = psycopg2.connect("dbname='Election' host='localhost' user='postgres' password='postgres'")


	cursor = connection.cursor();

	# Datenbankabfrage: 
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
		cursor.execute("SELECT Tweet_ID, wie_oft FROM T_enth_H WHERE H_Name = '"+hashtag1+"'");
		vektordaten = cursor.fetchall()
		for d in vektordaten:
			vektor1[d[0]] = d[1]
			#print(d[0],d[1])
		#print (vektor1)
	
		# Vektor 2 berechnen
		cursor.execute("SELECT Tweet_ID, wie_oft FROM T_enth_H WHERE H_Name = '"+hashtag2+"'");
		vektordaten = cursor.fetchall()
		for d in vektordaten:
			vektor2[d[0]] = d[1]
			#print(d[0],d[1])
        # Cosinus-Abstand berechnen
		result = 1 - cosine_similarity(vektor1, vektor2)
		
		cursor.execute("INSERT INTO Hashtags_Aehnlichkeit(name1,name2,aehnlichkeit) VALUES('"+hashtag1+"','"+hashtag2+"',"+str(result)+")")
	
	
		i += 1
		print (str(i)+"/91774", end="\r")
	connection.commit()
	print (str(i)+"/91774")
	

if __name__ == '__main__':
	main()
