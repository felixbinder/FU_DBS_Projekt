# -*- coding: utf-8 -*-

import psycopg2
from scipy.cluster.vq import kmeans2, whiten
from scipy.spatial.distance import euclidean
import numpy

def Clusterbildung(k_kmeans = 4, k_iterations = 30):
    #Datenbankverbindung
    conn = psycopg2.connect("dbname='Election' host='localhost' user='postgres' password='postgres'")
    cur = conn.cursor()

    #Aehnlichkeiten holen
    cur.execute("SELECT * FROM hashtags_aehnlichkeit")
    htaehnlichkeiten = cur.fetchall()

    #Alle Hashtags holen
    cur.execute("SELECT name FROM hashtag")
    hashtags = cur.fetchall()
    anzahl_hashtags = len(hashtags)
    for i, ht in enumerate(hashtags): #Wir kriegen die Hashtags also Tupel und wollen bloß den String haben
        hashtags[i] = ht[0]


    #Erstellen des Arrays mit den Beobachtungen - ein M*M Array, wobei M die Hashtags sind
    ae_matrix = numpy.zeros((anzahl_hashtags,anzahl_hashtags))        # [[.0 for col in range(anzahl_hashtags)] for row in range(anzahl_hashtags)]
    for cell in range(anzahl_hashtags): #Jeder Hashtag hat die Aehnlichkeit 1 zu sich selbst
        ae_matrix[cell][cell] 
    for i,aehnlichkeit in enumerate(htaehnlichkeiten): #Wir tragen die Aehnlichskeitswerte ein
        print("Ähnlichkeitspaar "+str(i)+"/"+str(len(htaehnlichkeiten)))
        ht_A = find_index(hashtags,aehnlichkeit[0])
        ht_B = find_index(hashtags,aehnlichkeit[1])
        ae_matrix[ht_A][ht_B] = aehnlichkeit[2] 
        ae_matrix[ht_B][ht_A] = aehnlichkeit[2] #Die Matrix ist ja symmetrisch
    print(ae_matrix)

    #K-Means ausfuehren
    # scipy.cluster.vq.whiten(ae_matrix) #Reskaliert die Werte jeder Dimension durch Division der Standardabweichung über alle Vorkommnisse. Das ist hier vermutlich nicht nötig, da die alle Dimensionen denselben Maßstab haben und vermutlich (aber denkbar!) nicht alle miteinander korrelieren
    kmeans_result = scipy.cluster.vq.kmeans2(ae_matrix,k_kmeans,iter = k_iterations,minit='points') #Wir muessen minit=points setzen, sonst kommt eine Beschwerde, dass die Matrix nicht "positiv-definit" sei
    print(kmeans_result)
    centroids = kmeans_result[0]
    ht_classifications = kmeans_result[1]

    #K-means Ergebnisse in Datenbank
    for i,ht in enumerate(ht_classifications):
        cur.execute("UPDATE hashtag SET cluster = " + str(ht) + " WHERE name = '" + hashtags[i] + "'")
    conn.commit()

    #Naechste Punkte zu Centroid finden
    cur.execute("UPDATE hashtag SET centroid = NULL") #Wir setzen zunächst alle Centroids auf NULL, den es gibt ja nur k viele
    for c_nr,centroid in enumerate(centroids): #Wir finden den nächsten Hashtag zu den Centroids (minimale euklidische Distanz)
        euklidabstand = [None]*anzahl_hashtags
        for i,ae_vektor in enumerate(ae_matrix):
            euklidabstand[i] = scipy.spatial.distance.euclidean(centroid,ae_vektor)
        closest_ht_nr = numpy.argmin(euklidabstand) # Minimales Element (bei gleichem Abstand erstes in Hashtagreihenfolge)
        cur.execute("UPDATE hashtag SET centroid = "+str(c_nr) + " WHERE name = '" + hashtags[closest_ht_nr] + "'") # Markiere den HT, der repraesentativ für den Centroid ist
    conn.commit()

    #Verbindung schließen
    conn.close()


def find_index(list, obj): #Wir koennen bei Strings ja kein .index() benutzen
    i = 0
    while i < len(list):
        if list[i] == obj:
            return i
        i+=1
    return None
