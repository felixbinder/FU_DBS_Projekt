import java.io.*;
import java.sql.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.LinkedList;

public class Insert {
    
    public static void main (String[] args) {
        
        Connection c = null;
        int datensätze = 6126;
        String[] datensatz = new String[11];
        String temp;
        PreparedStatement pst = null;
        Pattern patt = Pattern.compile("(#\\w+)\\b");
        Matcher match;
        int hashtags = 0;
        int tweet_id = 0, paar_id = 0;
        String tag;
        ResultSet result, result_hashtags_in_tweets;
        LinkedList<String> list;
        String hashtag1, hashtag2;
        
        try {
            // Datenbankverbindung vorbereiten, Datei öffnen.
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost/Election",
            "testuser", "testpass");
            File file = new File("./american-election-tweets-utf8.csv");
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            // Die erste Zeile ist zu überspringen, da sie keine relevanten Daten enthält.
            input.readLine();
            for (int i = 1; i <= datensätze; ++i) {
            	// Auslesen aus Datei
            	temp = input.readLine();
            	datensatz = temp.split(";");
            	
            	// Ersetzungen aus der Dateikonvertierung rückgängig machen
            	datensatz[1] = datensatz[1].replaceAll("&amp,","&amp;");
                datensatz[1] = datensatz[1].replaceAll("&gt,","&gt;");
                datensatz[1] = datensatz[1].replaceAll("women who work:","women who work;");
                datensatz[1] = datensatz[1].replaceAll("just for some Americans:", "just for some Americans;");
                datensatz[1] = datensatz[1].replaceAll("up to dictators:","up to dictators;");
                datensatz[1] = datensatz[1].replaceAll("We can’t contain ISIS:", "We can’t contain ISIS;");
                datensatz[1] = datensatz[1].replaceAll("daunting the odds:", "daunting the odds;");
                datensatz[1] = datensatz[1].replaceAll("to summon what’s best in us:", "to summon what’s best in us;");
                datensatz[1] = datensatz[1].replaceAll("We don’t fear the future:", "We don’t fear the future;");
                datensatz[1] = datensatz[1].replaceAll("&lt,", "&lt;");
                datensatz[1] = datensatz[1].replaceAll("7,546,980, @tedcruz 5,481,737,", "7,546,980; @tedcruz 5,481,737;");
                datensatz[1] = datensatz[1].replaceAll("Endorses Donald Trump for president,", "Endorses Donald Trump for president;");
            	
            	// Eintragen in Datenbank vorbereiten
            	pst = c.prepareStatement("INSERT INTO Tweet(id,handle,text,is_retweet,is_quote_status,retweet_count,favorite_count,time) VALUES (?,?,?,?,?,?,?,?)"); 
            	pst.setInt(1, (i-1)); // ID
            	pst.setString(2, datensatz[0]); // handle
            	pst.setString(3, datensatz[1]); // text
            	pst.setBoolean(4, datensatz[2].contains("True")); // is_retweet
            	pst.setBoolean(5, datensatz[6].contains("True")); // is_quote_status
            	pst.setInt(6, Integer.parseInt(datensatz[7])); // retweet_count
            	pst.setInt(7, Integer.parseInt(datensatz[8])); // favorite_count
            	pst.setTimestamp(8, Timestamp.valueOf(datensatz[4].replace("T"," "))); // time
            	
            	// Eintragen in Datenbank (Durchführung)
            	pst.executeUpdate();
            	
            	
            	// Suche nach Hashtags
            	if (datensatz[1].contains("#")) {
            		match = patt.matcher(datensatz[1]);
            		// Solange Hashtags vorhanden, füge sie in die Hashtag-Tabelle ein.
            		while(match.find()){
        			tag = match.group(1);
        			//System.out.println(tag);
        			pst = c.prepareStatement("SELECT COUNT(name) FROM hashtag WHERE name=?");
        			pst.setString(1, tag);
        			result = pst.executeQuery();
        			if (result.next()){
	        			if (result.getInt(1) == 0) {
	        				// Neueintrag
	        				pst = c.prepareStatement("INSERT INTO Hashtag(name, anzahl_global) VALUES (?, ?)");
	        				pst.setString(1, tag);
	        				pst.setInt(2, 1);
	        				pst.executeUpdate();
	        				}
	        			else {
	        				// Anzahl erhöhen
	        				pst = c.prepareStatement("UPDATE Hashtag SET anzahl_global = anzahl_global+1 WHERE name=?"); // Einfach add?
	        				pst.setString(1,tag);
	        				pst.executeUpdate();
	        				}
	        			
	        			// Füge Information über gefundene Hashtags in T_enth_H-Tabelle ein.
	        			// Frage: Eintrag schon vorhanden?
	        			pst = c.prepareStatement("SELECT count(tweet_id) FROM t_enth_h WHERE tweet_id=? AND h_name=?");
	        			pst.setInt(1,(i-1));
	        			pst.setString(2, tag);
	        			result = pst.executeQuery();
	        			if (result.next()){
						if (result.getInt(1) == 0) {
							// Neueintrag
							pst = c.prepareStatement("INSERT INTO T_enth_H(tweet_id, h_name, wie_oft) VALUES (?, ?,?)");
							pst.setString(2, tag);
							pst.setInt(1, (i-1));
							pst.setInt(3, 1);
							pst.executeUpdate();
							}
						else {
							// Anzahl erhöhen
							pst = c.prepareStatement("UPDATE T_enth_H SET wie_oft = wie_oft+1 WHERE tweet_id=? AND h_name=?"); // Einfach add?
							pst.setInt(1,(i-1));
							pst.setString(2, tag);
							pst.executeUpdate();
							}
						}
	        			}
        			++hashtags;
    				}
            		}
            	
            	System.out.print("Datensätze importiert: "+i+"/"+datensätze+"; außerdem: "+hashtags+" Hashtags gefunden.\r");
            	}
            System.out.print("\n");
            
            // Variable nun benutzen für das Zählen der Hashtag-Paare.
            hashtags = 0;
            
            
            // ******* Hashtag-Paare finden *********
            pst = c.prepareStatement("SELECT Tweet_ID, COUNT(Tweet_ID) FROM t_enth_h GROUP BY Tweet_ID HAVING COUNT(Tweet_ID) > 1 ORDER BY COUNT(*) DESC");
            result = pst.executeQuery();
            
            while (result.next()){
            	
            	list = new LinkedList<String>();
            	tweet_id = result.getInt(1);
            
            	
            	pst = c.prepareStatement("SELECT h_name FROM T_enth_H WHERE Tweet_ID = ?");
            	pst.setInt(1, tweet_id); // Tweet-ID aus der Abfrage für die neue Abfrage nutzen.
            	
            	// Ergebnis in Liste einlesen
            	result_hashtags_in_tweets = pst.executeQuery();
            	while (result_hashtags_in_tweets.next()) {
            		list.add(result_hashtags_in_tweets.getString(1));
            		}
            	
            	// Alle Tupel der Hashtags bilden.
            	while (list.size() > 1) {
            		//System.out.print(list.get(0)+" "+list.get(1));
            		hashtag1 = list.get(0);
            		hashtag2 = list.get(1);
            		
            		// Sind beide Hashtags identisch, sind sie kein Paar in unserem Sinne.
            		if (hashtag1.equals(hashtag2)) {
            			list.pop();
            			continue;
            			}
            		// richtige Ordnung herstellen, um Doubletten zu vermeiden.
            		if (hashtag1.toLowerCase().compareTo(hashtag2.toLowerCase()) > 0) {
            			temp = hashtag1;
            			hashtag1 = hashtag2;
            			hashtag2 = temp;
            			}
            		
            		// Paar in der Datenbank schon vorhanden?
            		pst = c.prepareStatement("SELECT count(ID), ID FROM Hashtag_Paare WHERE name1=? AND name2=? GROUP BY ID");
            		pst.setString(1,hashtag1);
            		pst.setString(2,hashtag2);
            		result_hashtags_in_tweets = pst.executeQuery();
            		if ( result_hashtags_in_tweets.next()) {
            			// Falls ja, aktualisiere diesen Eintrag
            			if (result_hashtags_in_tweets.getInt(1) == 1) {
            				paar_id = result_hashtags_in_tweets.getInt(2);
            				pst = c.prepareStatement("UPDATE Hashtag_Paare SET Anzahl_global = Anzahl_global +1 WHERE ID = ?");
            				pst.setInt(1,paar_id);
            				pst.executeUpdate();
            				}
            			}
            		// Falls nein, trage Datensatz neu ein (und auch gleich Verweis in Relation Hashtags_bilden_HP).
            		else {
            			pst = c.prepareStatement("INSERT INTO Hashtag_Paare(name1, name2, Anzahl_global) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            			pst.setString(1,hashtag1);
            			pst.setString(2,hashtag2);
            			pst.setInt(3,1);
            			pst.executeUpdate();
            			
            			// Was war die ID beim Einfügen in Hashtag_Paare?
            			ResultSet generatedKeys = pst.getGeneratedKeys();
		                if (generatedKeys.next()) {
			                paar_id = generatedKeys.getInt(1);
			                }
        	    		
            			// Update Hashtags_bilden_HP
            			pst = c.prepareStatement("INSERT INTO Hashtags_bilden_HP(Hash_Name, HP_ID) VALUES (?,?)");
            			pst.setString(1, hashtag1);
            			pst.setInt(2,paar_id);
            			pst.executeUpdate();
            			
            			pst = c.prepareStatement("INSERT INTO Hashtags_bilden_HP(Hash_Name, HP_ID) VALUES (?,?)");
            			pst.setString(1, hashtag2);
            			pst.setInt(2,paar_id);
            			pst.executeUpdate();
            			}
            		
            		// Eintrag in T_enth_HP schon enthalten?
            		pst = c.prepareStatement("SELECT COUNT(Tweet_ID) FROM T_enth_HP WHERE Hashpaar_ID = ? AND Tweet_ID = ?");
            		pst.setInt(1,paar_id);
            		pst.setInt(2,tweet_id);
            		result_hashtags_in_tweets = pst.executeQuery();
            		
            		// Wenn ja, erhöhe Zähler in der Datenbank
            		if (result_hashtags_in_tweets.next()) {
            			if (result_hashtags_in_tweets.getInt(1) == 1) {
            				pst = c.prepareStatement("UPDATE T_enth_HP SET wie_oft = wie_oft+1 WHERE Hashpaar_ID = ? AND Tweet_ID = ?");
            				pst.setInt(1,paar_id);
		            		pst.setInt(2,tweet_id);
		            		pst.executeUpdate();
            				}
            			}
            		//Andernfalls füge Eintrag hinzu.
            		pst = c.prepareStatement("INSERT INTO T_enth_HP(Tweet_ID, Hashpaar_ID, wie_oft) VALUES (?,?,?)");
            		pst.setInt(2,paar_id);
            		pst.setInt(1,tweet_id);
            		pst.setInt(3,1);
            		pst.executeUpdate();
		            		
            		
            		// Kopf der Liste entfernen, um die restlichen Tupel zu bilden
            		list.pop();
            		++hashtags;
            		System.out.print(hashtags+" Hashtag-Tupel gefunden.\r");
            		}
            	
            	list = null;
            	}
            System.out.println("\nFertig!");
            
            }
        catch (FileNotFoundException e) { e.printStackTrace();}
        catch (UnsupportedEncodingException e) { e.printStackTrace();}
        catch (IOException e) { e.printStackTrace();}
        catch (ClassNotFoundException e) {
		System.out.println("Klasse nicht gefunden: " + e.getMessage());
		}
        catch (SQLException ex) {
		 System.out.println("SQLException: " + ex.getMessage());
		 System.out.println("SQLState: " + ex.getSQLState());
		 System.out.println("VendorError: " + ex.getErrorCode());
		 }
        
        
        }

    }
