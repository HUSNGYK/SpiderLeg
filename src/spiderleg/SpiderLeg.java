/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spiderleg;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author qzt0471
 */
public class SpiderLeg {
    
    //spiderLeg pulls the information from the given site using jSoup
    public static Elements getTitle(String url)
    {
        Document doc1 = new Document("");
        
        try {
            doc1 = Jsoup.connect(url).get();
        } catch (IOException ex) {
         
        }
        
        
       Elements title = doc1.select("title");
       return title;
    }
    
    public static Elements getHyperlink(String url) throws IOException
    {
        Document doc1 = new Document("");
        
        try {
            doc1 = Jsoup.connect(url).get();
        } catch (IOException ex) {
           throw new IOException("Invalid Host");
        }
        
        Elements links = doc1.select("a[href]");
        return links;
    }
    
    public static Elements getImages(String url)
    {
         Document doc1 = new Document("");
        
        try {
            doc1 = Jsoup.connect(url).get();
        } catch (IOException ex) {
           
        }
        
        Elements media = doc1.select("[src]");
     
        if(media!=null)
        return media;
        else
        return null;
    }
    
    public Elements getMeta(String url)
    {
       Document doc1 = new Document("");
        
        try {
            doc1 = Jsoup.connect(url).get();
        } catch (IOException ex) {
           
        }
        
        Elements meta = doc1.select("meta");
        return meta;
    }
}
