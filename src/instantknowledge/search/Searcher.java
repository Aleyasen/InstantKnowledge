/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.search;

import instantknowledge.result.TargetWebsite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Aale
 */
public class Searcher {

    Map<String, List<String>> wrappers;

    public Searcher(String filepath) {
        wrappers = TargetWebsite.readMetadata(filepath);
    }

    public List<String> result(String url) {
        List<String> vals = new ArrayList<>();
        System.out.println("websites" + wrappers.keySet());
        for (String website : wrappers.keySet()) {
            if (url.startsWith(website)) {
                try {
                    int index = 0;
                    Document doc = Jsoup.connect(url).get();
                    final List<String> paths = wrappers.get(website);
                    for (String p : paths) {
                        final String text = Parser.getText(doc, p);
                        vals.add(text);
                        System.out.println(index++);
                        System.out.println(website + "\t" + p);
                        System.out.println(text);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return vals;
    }

    public static void main(String[] args) {
        Searcher searcher = new Searcher("data/movie.txt");
        String url = "http://www.rottentomatoes.com/m/the_dark_knight/";
//        String url = "http://www.imdb.com/title/tt0468569/?ref_=nv_sr_1";
        final List<String> result = searcher.result(url);
//        for (String val : result) {
//            System.out.println(val);
//        }

    }
}
