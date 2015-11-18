/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.search;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Aale
 */
public class Test {

    public static void main(String[] args) {
        try {
            String url = "http://en.wikipedia.org/wiki/List_of_people_from_Frankfurt";
            String selector = ".mediawiki > #footer > #footer-places > #footer-places-about > a[title='Wikipedia:About']";
//            String selector = "body > *";
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select(selector);
            System.out.println(elements.size());
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
