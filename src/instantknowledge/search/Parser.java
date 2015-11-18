/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package instantknowledge.search;

import instantknowledge.result.TargetWebsite;
import instantknowledge.utils.LCS;
import instantknowledge.utils.MapUtil;
import instantknowledge.utils.URLUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

/**
 *
 * @author Aale
 */
public class Parser {

    static int numberOfSeedUrls = 20;
    static int numberOfUrlsForEachPattern = 5;

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        List<String> queries = Arrays.asList("Fight Club", "The Dark Knight", "Forrest Gump", "American Beauty");
        final List<TargetWebsite> results = process(queries);
        String filepath = "data/movie.txt";
        TargetWebsite.saveMetadata(results, filepath);

    }

    public static List<TargetWebsite> process(List<String> queries) {
//        List<String> queries = Arrays.asList("jiawei han", "david forsyth", "hector garcia-molina");
        List<TargetWebsite> websites = new ArrayList<>();
        List<Query> queryObjs = new ArrayList<>();
        for (String qStr : queries) {
            queryObjs.add(new Query(qStr));
        }
        final Map<String, String> patternsMap = getUrlPattern(queryObjs);
        final Map<String, String> refinedPatternsMap = refinePatterns(patternsMap);
        //filter urls for each query
        for (Query q : queryObjs) {
            List<String> filteredURLs = new ArrayList<>();
            for (String url : q.getURLs()) {
                final String domain = URLUtil.getDomainName(url);
                if (refinedPatternsMap.keySet().contains(domain)) {
                    filteredURLs.add(url);
                }
            }
            q.setURLs(filteredURLs);
        }
        MapUtil.printMap(refinedPatternsMap);
        for (String domain : refinedPatternsMap.keySet()) {
            List<Document> pages = getSampledPages(refinedPatternsMap.get(domain));
            for (Document p : pages) {
                System.out.println("baseURI: " + p.baseUri());
            }
            final Map<String, List<String>> wrapper = generateWrapper(pages);
            final Map<String, List<String>> filteredWrapper = getFilterWrapper(wrapper);
            System.out.println("hello");
            TargetWebsite twebsite = new TargetWebsite();
            twebsite.setDomain(refinedPatternsMap.get(domain));
            twebsite.setWrappers(filteredWrapper);
            websites.add(twebsite);
        }
        findCommonInfo(websites, queryObjs);
        return websites;
    }

    /**
     *
     * @param sampleQueries
     * @return Map contains key:domain, value:url pattern
     */
    public static Map<String, String> getUrlPattern(List<Query> sampleQueries) {
        Map<String, String> domains = new HashMap<>();
        Map<String, Integer> domainCounts = new HashMap<>();

        for (Query query : sampleQueries) {

            final List<String> urls = SearchHelper.search(query.getText(), numberOfSeedUrls);
            for (String url : urls) {
                query.addURL(url);
                final String domain = URLUtil.getDomainName(url);
                domains.put(url, domain);
                Integer currentCount = domainCounts.get(domain);
                if (currentCount == null) {
                    domainCounts.put(domain, 1);
                } else {
                    domainCounts.put(domain, currentCount + 1);
                }
            }
        }
        final Map<String, Integer> sortedDomainCounts = MapUtil.sortByValue(domainCounts);
        Integer top = sampleQueries.size();
        System.out.println("Domain Counts");
        MapUtil.printMap(sortedDomainCounts);
        System.out.println();
        List<String> selectedDomains = new ArrayList<>();
        Map<String, String> selectedDomainsPatterns = new HashMap<>();
        for (String key : sortedDomainCounts.keySet()) {
            final Integer val = sortedDomainCounts.get(key);
            if (val >= (top - 1)) { // TODO: maybe top-1 threshold is not good. 
                selectedDomains.add(key);
                String pattern = getCommonPattern(domains, key);
                selectedDomainsPatterns.put(key, pattern);
            } else {
                break;
            }
            // System.out.println(key + "\t" + sortedDomainCounts.get(key));
        }
        System.out.println(selectedDomains);
        return selectedDomainsPatterns;
    }

    private static String getCommonPattern(Map<String, String> domains, String domain) {
        List<String> myUrls = new ArrayList<>();
        for (String url : domains.keySet()) {
            if (domains.get(url).equals(domain)) {
                myUrls.add(url);
            }
        }
        String result = myUrls.get(0);
        for (int i = 1; i < myUrls.size(); i++) {
            result = LCS.longestSubstr(result, myUrls.get(i));
        }
        return result;
    }

    private static List<Document> getSampledPages(String urlPattern) {
        List<String> urls = SearchHelper.search("site:" + urlPattern, numberOfUrlsForEachPattern);
        List<Document> docs = new ArrayList<>();
        for (String url : urls) {
            try {
                Document doc = Jsoup.connect(url).get();
                docs.add(doc);
            } catch (IOException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return docs;
    }

    private static Map<String, List<String>> generateWrapper(final List<Document> pages) {
        final Map<String, List<String>> candidateTextVals = new HashMap<>();
        if (pages == null || pages.isEmpty()) {
            return null;
        }
        final Document first = pages.get(0);
        final Map<String, String> paths_map = new LinkedHashMap<>();
        final Stack<String> path = new Stack<>();
        first.body().traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
//                System.out.println("PUSH >> SIZE=" + path.size());
                String current_element = getBestRepresentative(node, path);
                path.push(current_element);
                String node_text;
                if (node instanceof TextNode) {
                    node_text = node.toString().trim();
                    if (node_text != null && node_text.length() > 0) {
                        Stack<String> candidate_path = SimplifyPath(path);
                        candidate_path.pop();
                        boolean isAccept = evaluatePath(pages, first, candidate_path);
                        if (isAccept) {
                            final String serializeStack = serializeStack(candidate_path);
                            candidateTextVals.put(serializeStack, generateTextValsForPaths(pages, candidate_path));
                            paths_map.put(serializeStack, node_text);
                            System.out.println("PUT key>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>:" + serializeStack + " \tValue:" + node_text);
                        }
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
//                System.out.println("POP >> SIZE=" + path.size());
                if (!path.isEmpty()) {
                    path.pop();
                }
            }

        });
        return candidateTextVals;
    }

    private static boolean hasVal(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return true;
    }

    private static String serializeStack(Stack<String> stack) {
        StringBuilder strbuilder = new StringBuilder();
        for (String element : stack) {
            strbuilder.append(element);
        }
        return strbuilder.toString();
    }

    private static String getBestRepresentative(Node node, Stack<String> currentPath) {
        //TODO: use also title
        String best = " *";
        if (node instanceof Element) {
            Element elem = (Element) node;
            final String css_class = elem.classNames().iterator().next();
            if (hasVal(elem.id()) && isAcceptedId(elem.id())) {
                best = " #" + elem.id();
            } else if (hasVal(elem.attr("itemprop"))) {
                best = " " + elem.tagName() + "[itemprop=" + elem.attr("itemprop") + "]";
            } else if (hasVal(css_class)) {
                best = " ." + css_class;
            } else if (hasVal(elem.attr("title"))) {
                best = " " + elem.tagName() + "[title=" + elem.attr("title") + "]";
            } //                else if (hasVal(elem.attr("href"))) {
            //                best = " " + elem.tagName() + "[href=" + elem.attr("href") + "]";
            //            } 
            else {
                best = " " + elem.tagName();
            }
        }
        if (currentPath.size() > 0) {
            best = " >" + best;
        }
//        System.out.println("best::::::::::::::::::::::: " + best);
        return best;
    }

    public static int getCount(Document doc, String selector) {
        System.out.println("Inside Count: " + doc.baseUri() + " Selector:" + selector);
        Elements elements = doc.select(selector);
        if (elements == null) {
            return 0;
        } else {
            return elements.size();
        }
    }

    public static String getText(Document doc, String selector) {
        Elements elements = doc.select(selector);
        if (elements == null) {
            return null;
        } else if (elements.size() > 1) {
            System.out.println("WARNING: MORE THAN ONE INSTANCE FOR SELECTOR: " + selector + " IN " + doc.baseUri());
            int count = Math.min(elements.size(), 3);
            String concat = "";
            for (int i = 0; i < count; i++) {
                concat += elements.get(i).text() + ",";
            }
            concat = concat.substring(0, concat.length() - 1);
            return concat;
        } else if (!elements.isEmpty()) {
            return elements.get(0).text();
        } else {
            return null;
        }
    }

    private static boolean evaluatePath(List<Document> pages, Document first, Stack<String> path) {
        String pathStr = serializeStack(path);
        final int count = getCount(first, pathStr);
        if (count > 1) {
            System.out.println("PATH HAS MORE THAN 1 INSTANCE IN PAGE " + pathStr);
            return false;
        }
        int zero_count = 0;
        int multiple_count = 0;
        for (Document doc : pages) {
            int count_elements = getCount(doc, pathStr);
            if (count_elements == 0) {
                zero_count++;
                System.out.println("pattern not found in " + doc.baseUri() + "\t pattern: " + pathStr);
            }
            if (count_elements > 1) {
                multiple_count++;
                System.out.println("multiple instance found in " + doc.baseUri() + "\t pattern: " + pathStr);
            }
        }
        if (zero_count > 1) {
            System.out.println("zero_count=" + zero_count + " => not accept");
            return false;
        }
        if (multiple_count > 3) {
            System.out.println("multiple_count=" + multiple_count + " => not accept");
            return false;
        }
        return true;
    }

    private static List<String> generateTextValsForPaths(List<Document> pages, Stack<String> path) {
        List<String> textValsForPath = new ArrayList<>();
        String pathStr = serializeStack(path);
        for (Document doc : pages) {
            String text = getText(doc, pathStr);
            textValsForPath.add(text);
        }
        return textValsForPath;
    }

    private static Map<String, List<String>> getFilterWrapper(Map<String, List<String>> wrapper) {
        Map<String, List<String>> filteredWrapper = new HashMap<>();
        for (String key : wrapper.keySet()) {
            boolean isDifferent = areTheyDifferent(wrapper.get(key));
            if (isDifferent) {
                filteredWrapper.put(key, wrapper.get(key));
            }
        }
        return filteredWrapper;
    }

    private static boolean areTheyDifferent(List<String> texts) {
        int matches = 0;
        String first = null;
        for (String t : texts) {
            if (t != null & t.length() > 0) {
                first = t;
                break;
            }
        }
        if (first != null) {
            for (String t : texts) {
                if (first.equals(t)) {
                    matches++;
                }
            }
            System.out.println("matches:" + matches + " textSize=" + texts.size());

            if (matches == texts.size()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    static List<String> ignoreList = Arrays.asList("en.wikipedia.org");

    private static Map<String, String> refinePatterns(Map<String, String> patternsMap) {
        Map<String, String> refinedPatternsMap = new HashMap<>();
        for (String website : patternsMap.keySet()) {
            if (ignoreList.contains(website)) {
                continue;
            }
            String val = patternsMap.get(website);
            if (!val.endsWith("/") && !val.endsWith("=")) {
                int lastBackslashIndex = val.lastIndexOf("/");
                val = val.substring(0, lastBackslashIndex + 1);
            }
            refinedPatternsMap.put(website, val);
        }
        return refinedPatternsMap;
    }

    private static boolean isAcceptedId(String id) {
        if (id.contains("|")) {
            return false;
        }
        return true;
    }

    private static void findCommonInfo(List<TargetWebsite> websites, List<Query> queries) {
        for (Query q : queries) {
            try {
                String firsturl = q.getURLs().get(0);
                Document firstDoc = Jsoup.connect(firsturl).get();
                String securl = q.getURLs().get(1);
                Document secDoc = Jsoup.connect(securl).get();

            } catch (IOException ex) {
                Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void compareUs(TargetWebsite t1, TargetWebsite t2, Document d1, Document d2, Set<String> acceptedPaths1, Set<String> acceptedPaths2) {
        final Set<String> paths1 = t1.getWrappers().keySet();
        final Set<String> paths2 = t2.getWrappers().keySet();
        for (String path1 : paths1) {
            String val1 = getText(d1, path1);
            for (String path2 : paths2) {
                String val2 = getText(d2, path2);
                if (isSimilar(val1, val2)) {
                    acceptedPaths1.add(path1);
                    acceptedPaths2.add(path2);
                }
            }
        }
    }

    private boolean isSimilar(String val1, String val2) {
        if (val1.equals(val2)) {
            return true;
        }
        return false;
    }

    private static Stack<String> SimplifyPath(Stack<String> path) {
        Stack<String> simplePath = new Stack<>();
        int indexLastStar = -1;
        int index = 0;
        for (String element : path) {
            if (element.contains("#")) {
                indexLastStar = index;
            }
            index++;
        }
        if (indexLastStar == -1) {
            return path;
        } else {
            for (int i = indexLastStar; i < path.size(); i++) {
                simplePath.add(path.get(i));
            }
            if (simplePath.get(0).startsWith(" >")) {
                simplePath.set(0, simplePath.get(0).substring(2));
            }
            return simplePath;
        }
    }

}
