package main.java;

//import edu.stanford.nlp.ling.CoreAnnotations;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import main.java.Pair;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class queryRetriever {
    private EnglishAnalyzer analyzer;
    private IndexSearcher indexSearcher;
    private static final int max_results = 100;
    public queryRetriever(String indexPath)
    {
        try {
            this.indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(indexPath).toPath()))));
            analyzer = new EnglishAnalyzer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EnglishAnalyzer getAnalyzer(){
        return this.analyzer;
    }


    public List<String> createTokenList(String query) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(query));
        List<String> res = new ArrayList<>();
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while(tokenStream.incrementToken())
        {
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            res.add(charTermAttribute.toString());
        }
        tokenStream.end();
        tokenStream.close();
        return res;
    }


    /**
     * Function: createQueryString
     * Description: Returns string to be used in querying.
     * @param sectionPath: If non-empty, collapses sections into a single query string
     */

    public String createQueryString(Data.Page page, List<Data.Section> sectionPath)
    {
        String res = null;
        String sectionStr = null;
        for(Data.Section section: sectionPath)
        {
            sectionStr += section.getHeading();
        }
        res = page.getPageName() + " " + sectionStr;
        return res;
    }


    /**
     * Class: createQuery
     * Description: Given a query string, will create a boolean query by breaking it into tokens.
     * @return BooleanQuery: (tokens joined with OR clauses)
     */

    public BooleanQuery createQuery(String query) throws IOException {
        List<String> tokenLists = createTokenList(query);
        BooleanQuery.Builder res = new BooleanQuery.Builder();
        for(String token: tokenLists)
        {
            Query q = new TermQuery(new Term("content", token));
            res.add(q, BooleanClause.Occur.SHOULD);
        }
        return res.build();
    }

    /**
     * Function: getPageQueries
     * Description: Given a query location (.cbor file), queries Lucene index with page names.
     * @return List of pairs (query string and the Top 100 documents obtained by doing the query)
     */

    public List<Pair<String, TopDocs>> getPageQueries(String queryLocation) throws IOException {
        List<Pair<String, TopDocs>> res = new ArrayList<>();
        FileInputStream fis = new FileInputStream(new File(queryLocation));
        for(Data.Page page : DeserializeData.iterableAnnotations(fis))
        {

            String queryId = page.getPageId();
            String queryStr = createQueryString(page, new ArrayList<>());
            TopDocs tops = indexSearcher.search(createQuery(queryStr), max_results);
            Pair<String, TopDocs> newPair = new Pair<>(queryId, tops);
            res.add(newPair);
        }
        return res;
    }


    /**
     * Function: getSectionQueries
     * Description: Given a query location (.cbor file), queries Lucene index with page name and section names.
     * @return List of pairs (query string and the Top 100 documents obtained by doing the query)
     */

    public List<Pair<String, TopDocs>> getSectionQueries(String queryLocation) throws IOException {
        List<Pair<String, TopDocs>> res = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        FileInputStream fis = new FileInputStream(new File(queryLocation));



        for(Data.Page page : DeserializeData.iterableAnnotations(fis))
        {

            for (List<Data.Section> sectionPath : page.flatSectionPaths())
            {
                String queryId = Data.sectionPathId(page.getPageId(), sectionPath);
                String queryStr = createQueryString(page, sectionPath);
                TopDocs tops = indexSearcher.search(createQuery(queryStr), max_results);
                Pair<String, TopDocs> newPair = null;
                if(map.put(queryId, "") == null)
                {
                    newPair = new Pair<>(queryId, tops);
                    //System.out.println("Query ID: " + queryId);
                }
                if(newPair != null)
                {
                    res.add(newPair);
                }
            }
        }
        return  res;

    }

    /**
     * Function: writeRankingsToFile
     * Description: Writes formatted query results to a file (for use with trec_eval)
     */

    public void writeRankingFile(TopDocs tops, String queryId, BufferedWriter writer, int qNumber) throws IOException {
        ScoreDoc[] scoreDocs = tops.scoreDocs;
        for(int i = 0; i < scoreDocs.length; i++)
        {
            ScoreDoc score = scoreDocs[i];
            Document doc = indexSearcher.doc(score.doc);
            String paraId = doc.getField("paraid").stringValue();
            float rankScore = score.score;
            int rank = i + 1;
            String runStr = queryId + " Q" + qNumber + " " + paraId + " " + rank + " " + rankScore + " " + "team 3";
            writer.write(runStr);
            writer.newLine();
        }

    }

    /**
     * Function: writeQueriesToFile
     * Description: For each pair of query name and top 100 documents, write the results to a file.

     */

    public void writeQueriesFile(List<Pair<String, TopDocs>> queries, String output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        for(int i = 0; i < queries.size(); i++)
        {
            Pair<String, TopDocs> cur = queries.get(i);
            //System.out.println(cur.getKey());
            writeRankingFile(cur.getValue(), cur.getKey(), writer, i);
        }
        writer.flush();
        writer.close();

    }

    
}
