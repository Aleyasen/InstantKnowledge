package instantknowledge.search;

import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

//Could not do functionality for multiple page results so max results is capped at 10
/**
 *
 * @author Aale
 */
public class SearchHelper {

    /**
     *
     * @param query
     * @param numberOfUrls
     * @return
     */
    public static List<String> search(String query, int numberOfUrls) {

        try {
            List<String> listOfURLs = new ArrayList<String>();

            // Setting up the google API URL
//            String APIkey = "AIzaSyDNZ6K2wI2z2-j1tTSaZrpgUQJpOV2DRv8";
            String APIkey = "AIzaSyBfV669lB2cSkFyZv7EcU6pdBxYcCoaTUw";
            query = URLEncoder.encode(query, "UTF-8");

            // Using google developer API key to use custom search
            String stringURL = "https://www.googleapis.com/customsearch/v1?key=" + APIkey + "&cx=013036536707430787589:_pqjad5hr1a&q=" + query
                    + "&alt=json";
            URL url = new URL(stringURL);

            // System.out.println(stringURL);
            // Opening a connection and sending a request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            BufferedReader buffer;

            // To check for potential errors
            if (connection.getResponseCode() >= 400) {
                buffer = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
                String bufferLine;
                while ((bufferLine = buffer.readLine()) != null) {
                    System.out.println("Error:: " + bufferLine);
                }
            } else {
                buffer = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                String bufferLine;
                int countOfURLs = 0;
                while ((bufferLine = buffer.readLine()) != null && countOfURLs < numberOfUrls) {

                    if (bufferLine.contains("\"link\": \"")) {
                        String link = bufferLine.substring(bufferLine.indexOf("\"link\": \"") + ("\"link\": \"").length(),
                                bufferLine.indexOf("\","));
                        listOfURLs.add(link); // Will add the google search return links
                        // to the our URL list.
                        countOfURLs++;
                    }
                }
            }

            connection.disconnect();
            return listOfURLs;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // Main function to test functionality of code.
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        List<String> urls = search("the god father", 10);
        System.out.println(urls);
    }
}
