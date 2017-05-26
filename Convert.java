import java.io.*;

/**
* Konvertiert die Datei "american-election-tweets.csv" in UTF-8.
*/

public class Convert {
	
	public static void main(String args[]) {
		
		// Pfad der Quell- und Zieldatei
		File file = new File("./american-election-tweets.csv");
		File ziel = new File("american-election-tweets-utf8.csv");
		String charset = "windows-1252";
		
        try {
            // Öffnen der Dateien mit der jeweiligen Codieerung zum Lesen bzw. Schreiben.
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ziel), "UTF-8"));
            String puffer;
            int gelesen;
            
            // Übertragen aus der Quell- in die Zieldatei in der neuen Codierung.
            while ( (puffer = input.readLine()) != null) {
                output.write(puffer);
                output.newLine();
                }
            
            // Datei schließen.
            if (input != null) {
                    input.close(); }
                if (output != null) {
                    output.close(); }
            }
            catch (FileNotFoundException e) { e.printStackTrace();}
            catch (UnsupportedEncodingException e) { e.printStackTrace();}
            catch (IOException e) { e.printStackTrace();}
		} // Ende Main-Funktion
	
	}
