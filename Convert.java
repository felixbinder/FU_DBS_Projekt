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
                
                // Ersetze im Datensatz alle Semikola, die keine Trennzeichen sind.
                puffer = puffer.replaceAll("&amp;", "&amp,");
                puffer = puffer.replaceAll("&gt;", "&gt,");
                puffer = puffer.replaceAll("women who work;", "women who work:");
                puffer = puffer.replaceAll("just for some Americans;", "just for some Americans:");
                puffer = puffer.replaceAll("up to dictators;", "up to dictators:");
                puffer = puffer.replaceAll("We can’t contain ISIS;", "We can’t contain ISIS:");
                puffer = puffer.replaceAll("daunting the odds;", "daunting the odds:");
                puffer = puffer.replaceAll("to summon what’s best in us;", "to summon what’s best in us:");
                puffer = puffer.replaceAll("We don’t fear the future;", "We don’t fear the future:");
                puffer = puffer.replaceAll("&lt;", "&lt,");
                puffer = puffer.replaceAll("7,546,980; @tedcruz 5,481,737;", "7,546,980, @tedcruz 5,481,737,");
                puffer = puffer.replaceAll("Endorses Donald Trump for president;", "Endorses Donald Trump for president,");
                
                output.write(puffer);
                
                // Entferne alle Umbrüche in den Tweets und ersetze sie so, daß sie wiederherstellbar sind.
                if (puffer.endsWith("True") || puffer.endsWith("False") || puffer.endsWith("truncated")) {
                    output.newLine();
                    }
                else { output.write("<br />"); }
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
