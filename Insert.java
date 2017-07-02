import java.io.*;
import java.sql.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Objects;

public class Insert {
    
    public static void main (String[] args) {
        
        Connection c = null;
        int datensätze = 6126;
        String[] datensatz = new String[11];
        ArrayList<String> temp_datensatz = new ArrayList<String>();
		ArrayList<String> temp_datensatz2 = new ArrayList<String>();
        ArrayList<String> hashtag = new ArrayList<String>();
        ArrayList<Integer> hashtagPaare = new ArrayList<Integer>();
        String temp;
        PreparedStatement pst = null;
        Pattern patt = Pattern.compile("(#\\w+)\\b");
        Matcher match;
        int hashtags = 0;
        String tag;
        ResultSet result;
        ResultSet result_temp;
        
        try {
            // Datenbankverbindung vorbereiten, Datei öffnen.
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost/Election",
            "postgres", "postgres");
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

                    while(temp_datensatz.size() > 0) {
                        temp_datensatz.remove(0);
                    }

            		// Solange Hashtags vorhanden, füge sie in die Hashtag-Tabelle ein.
            		while(match.find()) {
                        tag = match.group(1);


        			temp_datensatz.add(tag); // Hashtags des aktuellen Tweets werden für die Generierung der Hashtag Paare zwischengespeichert

        			//System.out.println(temp_datensatz.get(0));

        			pst = c.prepareStatement("SELECT COUNT(name) FROM Hashtag WHERE name=?");
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

					// Füge Information über gefundene Hashtags in Hashtag-Paare-Tabelle ein.

                    // 1. Schritt Hashtag-Paare bilden
                    while(temp_datensatz2.size() > 0) {          // temporäres Array 2 für Hashtag-Paare wird geleert
                        temp_datensatz2.remove(0);
                    }
                        int e = 1;
						while(e < temp_datensatz.size()){

							for (int z=1; z<temp_datensatz.size(); z++){
                                            if(!(Objects.equals(temp_datensatz.get(0),temp_datensatz.get(z)))) {
                                                temp_datensatz2.add(temp_datensatz.get(0));            // Schreibe hashtag paar
                                                temp_datensatz2.add(temp_datensatz.get(z));
                                            }
									}
                            temp_datensatz.remove(0);
								}



                    // 2. Schritt : Prüfen ob Hashtag-Paare bereits vorhanden, falls nicht, dann einfügen, sonst Anzahl_global erhöhen.
                while(temp_datensatz2.size() > 1) {

							pst = c.prepareStatement("SELECT count(name1) FROM Hashtag_Paare WHERE (name1=? AND name2=?) OR (name1=? AND name2=?)");
							pst.setString(1,temp_datensatz2.get(0));
							pst.setString(2, temp_datensatz2.get(1));
                            pst.setString(3,temp_datensatz2.get(1));
                            pst.setString(4, temp_datensatz2.get(0));
							result = pst.executeQuery();
							if (result.next()){
								if (result.getInt(1) == 0) {
									// Neueintrag

                                    // Primary ID MAX abfragen
                                    pst = c.prepareStatement("SELECT max(ID) from Hashtag_Paare");
                                    result = pst.executeQuery();

                                    if (result.next()){
                                    int temp2 = result.getInt(1)+1;

									pst = c.prepareStatement("INSERT INTO Hashtag_Paare(ID, name1, name2, Anzahl_global) VALUES (?,?,?,?)"); //ID wird automatisch ohne Eingabe laufend erhöht
                                    pst.setInt(1, temp2);
                                    if (temp_datensatz2.get(0).toLowerCase().compareTo(temp_datensatz2.get(1).toLowerCase()) < 0){
                                        pst.setString(2,temp_datensatz2.get(0));
        	                            pst.setString(3, temp_datensatz2.get(1));
        	                            }
        	                    else {
   	                    		    pst.setString(3,temp_datensatz2.get(0));
        	                            pst.setString(2, temp_datensatz2.get(1));
        	                    		}
									pst.setInt(4, 1);
									pst.executeUpdate();
                                    }
								}
                                else {
                                    // Anzahl_global erhöhen
                                    pst = c.prepareStatement("UPDATE Hashtag_Paare SET Anzahl_global = Anzahl_global+1 WHERE (name1=? AND name2=?) OR (name1=? AND name2=?)");
                                    pst.setString(1,temp_datensatz2.get(0));
                                    pst.setString(2, temp_datensatz2.get(1));
                                    pst.setString(3, temp_datensatz2.get(1));
                                    pst.setString(4,temp_datensatz2.get(0));
                                    pst.executeUpdate();
                                }
							}
							// T_enth_HP befüllen

                            pst = c.prepareStatement("SELECT max(ID) from Hashtag_Paare"); // Hashtag Paar ID des eben eingefügten Paars abfragen
                            result = pst.executeQuery();
                            if (result.next()){
                            // Prüfen, ob Eintrag bereits vorhanden

                            pst = c.prepareStatement("SELECT count(wie_oft) FROM T_enth_HP WHERE (Tweet_ID=? AND Hashpaar_ID=?)");
                            pst.setInt(1,(i-1));
                            pst.setInt(2, result.getInt(1));
                            result_temp = pst.executeQuery();

                            if(result_temp.next()){
                                if (result_temp.getInt(1) == 0){
                                     pst = c.prepareStatement("INSERT INTO T_enth_HP(Tweet_ID, Hashpaar_ID, wie_oft) VALUES (?,?,?)"); //ID wird automatisch ohne Eingabe laufend erhöht
                                     pst.setInt(1, (i-1)); // Tweet ID
                                     pst.setInt(2, result.getInt(1)); // Hashtag-Paar ID
                                     pst.setInt(3, 1); // WIE_OFT
                                     pst.executeUpdate();
                                }else{
                                    pst = c.prepareStatement("UPDATE T_enth_HP SET wie_oft = wie_oft+1 WHERE Tweet_ID=? AND Hashpaar_ID=?");
                                    pst.setInt(1, (i-1)); // Tweet ID
                                    pst.setInt(2, result.getInt(1)); // Hashtag-Paar ID
                                    pst.executeUpdate();
                                }

                             }
                            }
                            temp_datensatz2.remove(0);
                            temp_datensatz2.remove(0);
                }


						}







            	
            	System.out.print("Datensätze importiert: "+i+"/"+datensätze+"; außerdem: "+hashtags+" Hashtags gefunden.\r");
            	}
            // 3. Schritt Relationstabelle Hashtags_bilden_HP befüllen. Dies wird komplett aus den bereits vorhandenen Daten via SQL Befehlen geleistet.
            pst = c.prepareStatement("SELECT Name FROM Hashtag");  // Projektion aller Hashtags
            result = pst.executeQuery();
            while(hashtag.size() > 0) {          // Hashtag Liste wird geleert
                 hashtag.remove(0);
            }

            while(result.next()){                                  // Hashtags werden in Liste eingelesen
                hashtag.add(result.getString(1));
            }

            while(hashtag.size()>0){
            pst = c.prepareStatement("SELECT ID FROM Hashtag_Paare WHERE name1=? OR name2=?");
            pst.setString(1,hashtag.get(0));
            pst.setString(2,hashtag.get(0));
            result = pst.executeQuery();
            while(result.next()) {
                hashtagPaare.add(result.getInt(1));    // Hashtag Paare, die Hashtag enthalten werden in Liste eingelesen
            }
            while(hashtagPaare.size()>0) {
                pst = c.prepareStatement("INSERT INTO Hashtags_bilden_HP(Hash_Name, HP_ID) VALUES (?,?)");
                pst.setString(1, hashtag.get(0));
                pst.setInt(2, hashtagPaare.get(0));
                pst.executeUpdate();
                hashtagPaare.remove(0);
            }
            hashtag.remove(0);
            }

           




            System.out.print("\n");
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
