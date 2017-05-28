import java.io.*;
import java.sql.*;

public class Insert {
    
    public static void main (String[] args) {
        
        Connection c = null;
        int datensätze = 6126;
        String[] datensatz = new String[11];
        String temp;
        PreparedStatement pst = null;
        
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
            	
            	System.out.print("Datensätze importiert: "+i+"/"+datensätze+"\r");
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
