/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.search;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aale
 */
public class Query {

    String text;
    List<String> urls;

    public Query(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setURLs(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getURLs() {
        return urls;
    }

    public String getText() {
        return text;
    }

    public void addURL(String url) {
        if (urls == null) {
            urls = new ArrayList<>();
        }
        urls.add(url);
    }

}
