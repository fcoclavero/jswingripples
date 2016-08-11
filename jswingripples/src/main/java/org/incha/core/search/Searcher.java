package org.incha.core.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.incha.ui.classview.ClassTreeView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fcocl_000 on 05-05-2016.
 * Class that manages user searches using the indexes in the index Directory.
 */
public class Searcher {
    /**
     * Class instance for singleton pattern.
     */
    private static Searcher instance = null;
    /**
     * Lucene class which handles user input and creates a Lucene query.
     */
    private QueryParser queryParser;
    /**
     * Lucene class for searching the created indexes.
     */
    private IndexSearcher indexSearcher;
    /**
     * Contains all search hits.
     */
    private List<String> results = new ArrayList<>();
    /**
     * Class view UI.
     */
    private ClassTreeView classTreeView;
    
    /**
     * Contains information about the part of the file where 
     * the word was found. 
     */
    private List<Object []> res_information = new ArrayList<Object []>();

    /**
     * Returns the current instance.
     * @return the current Indexer instance.
     */
    public static Searcher getInstance() {
        if (instance == null) {
            try {
                instance = new Searcher();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Default constructor.
     * @throws IOException
     */
    private Searcher() throws IOException {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
        queryParser = new QueryParser(Version.LUCENE_36, LuceneConstants.CONTENTS, analyzer);
    }

    /**
     * Closes the IndexSearcher.
     * @throws IOException
     */
    public void close() throws IOException {
        indexSearcher.close();
    }

    /**
     * Set current class view.
     * @param classTreeView the current class view.
     */
    public void setClassTreeView(ClassTreeView classTreeView) { this.classTreeView = classTreeView; }

    /**
     * Searches the indexes in the Directory.
     * @param searchQuery the user's query.
     * @return the query results.
     * @throws IOException
     * @throws ParseException
     */
    private TopDocs searchIndexes(String searchQuery) throws IOException, ParseException {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, LuceneConstants.MAX_RESULTS_ITEMS);
    }

    /**
     * Retrieves Lucene Document from ScoreDoc.
     * @param scoreDoc the document to be retrieved.
     * @return the corresponding Document.
     * @throws IOException
     */
    private Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    /**
     * Handles complete search process.
     * @param searchQuery the user's query string.
     * @throws IOException
     * @throws ParseException
     */
    public void search(String searchQuery) throws IOException, ParseException {
        // The directory containing the indexes.
        Directory indexDirectory = FSDirectory.open(new File(LuceneConstants.INDEX_DIRECTORY_PATH));
        indexSearcher = new IndexSearcher(IndexReader.open(indexDirectory));
        TopDocs topDocs = searchIndexes(searchQuery);
        results = new ArrayList<>();
        for(ScoreDoc doc : topDocs.scoreDocs) {
            String aux = removeJavaExtension(getDocument(doc).get(LuceneConstants.FILE_NAME));
            results.add(aux);
            int index_query = results.indexOf(aux);
            readFileAndLook(getDocument(doc).get(LuceneConstants.FILE_PATH), searchQuery, index_query);
        }
        // Refresh analysis table
        classTreeView.repaint();
    }

    /**
     * Calculates the proportion of times the last searched term appears in the specified file
     * to the total number of search hits for the same term.
     * @param fileName the file's name.
     * @return the number of hits.
     */
    double searchHits(String fileName) {
        return results.size() == 0 ? 0 : Collections.frequency(results, fileName) * 1.0  / results.size();
    }

    /**
     * Removes the .java extension from the given filename.
     * @param fileName the filename to modify.
     * @return the filename without the .java extension.
     */
    private String removeJavaExtension(String fileName) {
        return fileName.replace(".java", "");
    }
    
    /**
     * Returns the results of the query
     * @return
     */
    public List<String> getResults() {
    	return results;
    }
    
    /**
     * Return a list of the lines where the words were found
     */
    public List<Object []> getResInfo() {
    	return res_information;
    }
    
    /**
     * Deletes the elements of the list res_information
     */
    public void clearResInfo() {
    	res_information.clear();
    }
    
    /**
     * Reads the file where the word was found to provide information
     * @throws FileNotFoundException 
     * 
     */
    public void readFileAndLook(String path, String searchQuery, int index_query) throws FileNotFoundException {
        RandomAccessFile file = new RandomAccessFile(path.replace('\\', '/'), "r");    	
    	String line; 
    	int num_line = 0;    	
    	try{    	      	   
           while ((line = file.readLine()) != null) {   
        	  ++num_line; 
              if (line.contains(" " + searchQuery.trim() + " ")) {
            	  Object [] new_info = {line, index_query, num_line};
            	  res_information.add(new_info);
            	  break; //Sólo se toma la primera línea
              }
            }   
         }catch(Exception e){
            e.printStackTrace();            
         }
    	try {
    		if (file != null) file.close();     		
    	}
        catch(Exception e){
            e.printStackTrace();            
        }
    }
    
}
