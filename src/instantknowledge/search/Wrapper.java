/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.search;

import java.util.List;

/**
 *
 * @author Aale
 */
public class Wrapper {

    String website;
    List<WElement> patterns;

    public Wrapper() {
    }

    public Wrapper(String website, List<WElement> patterns) {
        this.website = website;
        this.patterns = patterns;
    }

    public List<WElement> getPatterns() {
        return patterns;
    }

    public String getWebsite() {
        return website;
    }

    public void setPatterns(List<WElement> patterns) {
        this.patterns = patterns;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
