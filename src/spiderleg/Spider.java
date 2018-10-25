/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spiderleg;

import java.io.FileNotFoundException;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
public class Spider
{
   
    public static Queue<String> urlList = new LinkedList<String>();
    public static Map<String, Integer> depthMap = new HashMap<>();
    public static Map<String, ArrayList<String>> previousLink = new HashMap<>();
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static Object[] stringKeys;

    public static void main(String[] args) throws Exception
    {

        /* Inputs the initial URL, Keyword, and Depth of the Search
         * The search must not extend the depth of D
         * int d, int that sets the depth of the search
         * string url, the starting url
         * string keyword, the keyword to search in the meta tag found on the websites
         *
         */
        Scanner input = new Scanner(System.in);

       
        System.out.println("Please Enter Seed URL: ");
        String url = input.nextLine();
        urlList.add(url);
        
        System.out.println("Please Enter Keyword");
        String keyword = input.nextLine();
        System.out.println("Please Enter Depth (d)");
        int d = input.nextInt();
        
        
//        Tries to crawl the web, which creates a list of visited nodes in the output txt file
//        If the file cannot be created, it throws a FileNotFoundException
        try
        {
            crawl(urlList, keyword, d);
        } catch (FileNotFoundException ex)
        {
            throw new FileNotFoundException("File cannot be found/created");
        }

        String[][] adjMatrix = createGraph();
        pageRank(adjMatrix);

    }

    public static void crawl(Queue<String> unvisited, String keyword, int d) throws FileNotFoundException
    {
        
//        Since the project is BFS, it uses a queue system. As files are found they are put into the queue and
//        then the first item in that queue is the next link to be searched.
        SpiderLeg leg = new SpiderLeg();
        Queue<String> visited = new LinkedList<String>();
        PrintWriter writer = new PrintWriter("output.txt");

        String currentLink = unvisited.peek();
        
        //depthMap is a hashmap that stores the currentLink and its depth
        depthMap.put(currentLink, 0);

        outerloop:
        while (!unvisited.isEmpty())
        {
            currentLink = unvisited.peek();
            writer.println(currentLink);
            ArrayList<String> outGoing = new ArrayList<>();
            System.out.println(ANSI_GREEN + "Visiting: " + currentLink + ANSI_RESET);

            Elements links = new Elements();
            
            //if a link is not found, or cannot be reached, outputs the sysout line
            try
            {
                links = leg.getHyperlink(currentLink);
            } catch (IOException e)
            {
                System.out.println(ANSI_CYAN + "Invalid web-address. Will be removed from queue." + ANSI_RESET);
                unvisited.remove();
            }
            
            
            //searches the list of elements, if there is no keyword set, it auto adds it to the queue and the hashmap
            //incrementing the depth of the webpage it is found on. (Example: if youtube.com is found on google.com, and google.com has a depth of 0
            //the depth of youtube.com is set to 1)
            for (Element link : links)
            {
                String absHref = link.attr("abs:href");
                outGoing.add(absHref);
                //if no keyword is set, it searches for everything listed
                if (keyword.equals(""))
                {
                    if (!absHref.isEmpty() && !visited.contains(absHref) && !link.toString().contains(currentLink) && !absHref.contains(currentLink) && !unvisited.contains(absHref))
                    {
                        if (depthMap.get(currentLink) <= d)
                        {
                            depthMap.put(absHref, depthMap.get(currentLink) + 1);
                        } else
                        {
                            System.out.println(ANSI_CYAN + "Depth is: " + (depthMap.get(currentLink) - 1) + ANSI_RESET);
                            break outerloop;
                        }
                        System.out.println("Added to queue: " + absHref);
                        unvisited.add(absHref);
                    }
                } 
                //otherwise, it only adds links that contain the meta keyword
                else
                {
                    Elements meta = leg.getMeta(currentLink);

                    boolean hasMetaWord = false;

                    for (Element metaList : meta)
                    {
                        if (metaList.toString().contains(keyword))
                        {
                            hasMetaWord = true;
                        }
                    }

                    if (!absHref.isEmpty() && !visited.contains(absHref)
                            && hasMetaWord && !link.toString().contains(currentLink) && !absHref.contains(currentLink) && !unvisited.contains(absHref))
                    {
                        if (depthMap.get(currentLink) <= d)
                        {
                            depthMap.put(absHref, depthMap.get(currentLink) + 1);
                        } else
                        {
                            //if the depth is reached, the entire while loop is broken
                            System.out.println(ANSI_CYAN + "Depth is: " + (depthMap.get(currentLink) - 1) + ANSI_RESET);
                            break outerloop;
                        }
                        System.out.println("Added to queue: (with keyword)" + absHref);
                        unvisited.add(absHref);
                    }
                }

            }
            previousLink.put(currentLink, outGoing);
            unvisited.remove(currentLink);
            visited.add(currentLink);

        }

        System.out.println("All websites have been visited, or depth has been reached");
        writer.close();

    }

    
    //creates the adjancency matrix
    //input: none
    //output a 2x2 adjacency matrix
    public static String[][] createGraph()
    {
        Set keys = previousLink.keySet();

        stringKeys = keys.toArray();

        String[][] adjMatrix = new String[stringKeys.length + 1][stringKeys.length + 1];

        for (int j = 0; j < stringKeys.length; j++)
        {
            adjMatrix[0][j + 1] = stringKeys[j].toString();
            adjMatrix[j + 1][0] = stringKeys[j].toString();
        }

        for (int k = 1; k < stringKeys.length + 1; k++)
        {
            for (int j = 1; j < stringKeys.length + 1; j++)
            {
                if (previousLink.get(adjMatrix[k][0]).contains(adjMatrix[0][j]))
                {
                    adjMatrix[k][j] = "1";
                } else
                {
                    adjMatrix[k][j] = "0";
                }
            }
        }

        return adjMatrix;
    }
    
    //computes the pagerank (and outputs them in order)
    //inputs 2x2 adjMatrix
    //outputs: none
    public static void pageRank(String[][] adjMatrix)
    {

        Double[][] pageR = new Double[adjMatrix.length - 1][adjMatrix.length - 1];

        for (int k = 1; k < adjMatrix.length; k++)
        {
            boolean isSink = false;
            for (int j = 1; j < adjMatrix.length; j++)
            {
                if (adjMatrix[k][j].equals("1"))
                {
                    isSink = false;
                    break;
                } else
                {
                    isSink = true;
                }
            }
            if (isSink)
            {
                for (int j = 0; j < pageR.length; j++)
                {
                    pageR[k - 1][j] = 1.0 / pageR.length;
                }
            }
        }

        for (int k = 1; k < adjMatrix.length; k++)
        {
            for (int j = 1; j < adjMatrix.length; j++)
            {
                //this gives incoming as outgoing is horizontal, incoming is vertical
                if (adjMatrix[j][k].equals("1"))
                {
                    pageR[j - 1][k - 1] = 1.0 / previousLink.get(adjMatrix[j][0]).size();
                }
            }
        }

        for (int k = 1; k < adjMatrix.length; k++)
        {
            for (int j = 1; j < adjMatrix.length; j++)
            {
                //this any other numbers 1/n
                if (pageR[k - 1][j - 1] == (null))
                {
                    pageR[k - 1][j - 1] = 1.0 / pageR.length;
                }
            }
        }

        for (int k = 0; k < pageR.length; k++)
        {
            for (int j = 0; j < pageR[0].length; j++)
            {
                System.out.print(pageR[k][j] + " ");
            }
            System.out.println("\n");
        }

        powerIteration(pageR, 1.0 / 1000);

    }

    //poweriteration algorithm to compute the page rank
    //input: the initial page rank matrix
    //input: the double "t" which is the difference between each iteration needed to be found
    public static void powerIteration(Double[][] pageR, double t)
    {
        Double[][] iterations = new Double[stringKeys.length][1];

        for (int k = 0; k < iterations.length; k++)
        {
            iterations[k][0] = (1.0 / iterations.length);
        }

        for (int k = 0; k < 20; k++)
        {
            Double[][] iteration2 = multiplyArrays(pageR, iterations);
            iterations = iteration2;
            if (Math.abs(iterations[0][0] - iteration2[0][0]) < t)
            {

                System.out.println("Different is smaller than T");
                break;
            }

        }

        for (int k = 0; k < iterations.length; k++)
        {
            System.out.println(stringKeys[k].toString() + " has a page rank of: " + iterations[k][0]);
        }

        sortMe(iterations, 0 ,iterations.length-1);
        System.out.println("\n\n\n\n");
        
        for (int k = 0; k < iterations.length; k++)
        {
            System.out.println(stringKeys[k].toString() + " has a page rank of: " + iterations[k][0]);
        }
    }

    
    //basic mergesort that sorts the pageRankings
    public static void sortMe(Double[][] iteration, int low, int high)
    {

        if (low < high)
        {
            int middle = (low + high) / 2;
            sortMe(iteration, low, middle);
            sortMe(iteration, middle + 1, high);
            merge(iteration, low, middle, high);

        }
    }

    public static void merge(Double[][] array, int low, int middle, int high)
    {
        Double[][] helper = new Double[array.length][array.length];
        String[] linkHelper = new String[stringKeys.length];
        for (int i = low; i <= high; i++)
        {
            helper[i][0] = array[i][0];
            linkHelper[i] = stringKeys[i].toString();
        }

        int helperLeft = low;
        int helperRight = middle + 1;
        int current = low;

        while (helperLeft <= middle && helperRight <= high)
        {
            if (helper[helperLeft][0] <= helper[helperRight][0])
            {
                array[current][0] = helper[helperLeft][0];
                stringKeys[current] = linkHelper[helperLeft];
                helperLeft++;

            } else
            {
                array[current][0] = helper[helperRight][0];
                stringKeys[current] = linkHelper[helperRight];
                helperRight++;
            }
            current++;
        }

        int remaining = middle - helperLeft;
        for (int i = 0; i <= remaining; i++)
        {
            array[current + i][0] = helper[helperLeft + i][0];
            stringKeys[current + i] = linkHelper[helperLeft+i];
        }
    }
    
    //basic array multiplicity algorithm
    public static Double[][] multiplyArrays(Double[][] A, Double[][] B)
    {

        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;

        if (aColumns != bRows)
        {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        Double[][] C = new Double[aRows][bColumns];
        for (int i = 0; i < aRows; i++)
        {
            for (int j = 0; j < bColumns; j++)
            {
                C[i][j] = 0.00000;
            }
        }

        for (int i = 0; i < aRows; i++)
        { // aRow
            for (int j = 0; j < bColumns; j++)
            { // bColumn
                for (int k = 0; k < aColumns; k++)
                { // aColumn
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }
}
