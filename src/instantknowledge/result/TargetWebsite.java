/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.result;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Aale
 */
public class TargetWebsite {

    String domain;
    Map<String, List<String>> wrappers;

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setWrappers(Map<String, List<String>> wrappers) {
        this.wrappers = wrappers;
    }

    public String getDomain() {
        return domain;
    }

    public Map<String, List<String>> getWrappers() {
        return wrappers;
    }

    public static void saveMetadata(List<TargetWebsite> data, String filepath) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filepath, false), "utf-8"));
            for (TargetWebsite tw : data) {
                for (String path : tw.getWrappers().keySet()) {
                    writer.write(tw.getDomain() + "\t" + path + "\n");
                }
            }

        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
    }

    public static Map<String, List<String>> readMetadata(String filepath) {
        Map<String, List<String>> wrappers = new HashMap<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filepath));
            String line = br.readLine();

            while (line != null) {
                String[] split = line.split("\\t", 2);
                String url = split[0];
                String path = split[1];
                if (wrappers.get(url) == null) {
                    wrappers.put(url, new ArrayList<String>());
                }
                wrappers.get(url).add(path);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return wrappers;
    }

}
