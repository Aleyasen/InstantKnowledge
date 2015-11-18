/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aale
 */
public class BoilerplateRemoval {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            URL url = new URL("http://www.imdb.com/title/tt1065073/");
            // NOTE: Use ArticleExtractor unless DefaultExtractor gives better results for you
            String text = ArticleExtractor.INSTANCE.getText(url);
            System.out.println(text);
        } catch (MalformedURLException | BoilerpipeProcessingException ex) {
            Logger.getLogger(BoilerplateRemoval.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
