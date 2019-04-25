package main.java.ranklib;

//import edu.unh.cs.treccar_v2.Data;
import main.java.Pair;
//import jdk.jfr.internal.BufferWriter;
//import jdk.nashorn.internal.parser.TokenStream;
//import main.java.QueryData;
import main.java.queryRetriever;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.util.*;

//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.ReentrantLock;

class paraContrainer implements Comparator<paraContrainer>{
    public String pid;
    public int qid;
    public   boolean isRelevant;
    public ArrayList<Double> features;
    public int docId;
    public double score;
    paraContrainer(String pid, int qid, boolean isRelevant, ArrayList<Double> features, int docId, double score)
    {
        this.pid = pid;
        this.qid = qid;
        this.isRelevant = isRelevant;
        this.features = features;
        this.docId = docId;
        this.score = score;
    }

    @Override
    public int compare(paraContrainer p1, paraContrainer p2)
    {
        if(p2.score > p1.score)
            return 1;
        else if(p2.score < p1.score)
            return -1;
        else
            return 0;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if(isRelevant)
            sb.append(1);
        else
            sb.append(0);
        sb.append(" ");
        sb.append("qid:");
        sb.append(qid);
        sb.append(" ");
        for(int i = 0; i < features.size(); i++)
        {
            sb.append(i);
            sb.append(":");
            sb.append(features.get(i));
            sb.append(" ");
        }
        return sb.toString();
    }



}


/**
 * Class: QueryContainer
 * Description: One is created for each of the query strings in the query .cbor file.
 *              Stores corresponding query string and TopDocs (obtained from BM25)
 */

class queryContainer{
    public String query;
    public TopDocs tops;
    public List<paraContrainer> paras;
    queryContainer(String query, TopDocs tops, List<paraContrainer> paras)
    {
        this.query = query;
        this.tops = tops;
        this.paras = paras;
    }

    public String getQuery(){
        return this.query;
    }

    public TopDocs getTops()
    {
        return  this.tops;
    }

    public List<paraContrainer> getParas()
    {
        return  this.paras;
    }
}


/**
 * Class: ranklibFormatter
 * Description: Used to apply scoring functions to queries (from .cbor file) and print results as features.
 *              The results file is compatible with RankLib.
 */

public class ranklibFormatter {
    public String qrelLoc;
    //private QueryData queryData;
    public IndexSearcher indexSearcher;
    public String queryLocation;
    public String indexPath;
    public List<queryContainer> qcontainers = new ArrayList<>();
    public queryRetriever qRetriever;
    public List<Pair<String, TopDocs>> queries;

    ranklibFormatter(String qrelLoc, String queryLocation,String indexPath, String flag) throws IOException {
        this.qrelLoc = qrelLoc;
        try {
            this.indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open((new File(indexPath).toPath()))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.queryLocation = queryLocation;
        this.indexPath = indexPath;
        this.qRetriever  = new queryRetriever(this.indexPath);

        if(flag.equals("pages"))
        {
            this.queries = qRetriever.getPageQueries(this.queryLocation);
        }else if(flag.equals("sections"))
        {
            this.queries = qRetriever.getSectionQueries(this.queryLocation);
        }

        ranklibFormat(qrelLoc, this.queryLocation);
        //

    }

    interface Function{
        List<Double> func(String s, TopDocs tops, IndexSearcher indexSearcher) throws IOException;

    }



    public void ranklibFormat(String qrelLoc, String queryLocation) throws IOException {


        // If a qrel filepath was given, reads file and creates a set of query/paragraph pairs for relevancies
        List<Pair<String, String>> relevances = new ArrayList<>();
       if(qrelLoc != "")
       {
           BufferedReader bf = new BufferedReader(new FileReader(qrelLoc));
           String line = bf.readLine();
           while(line != null)
           {
               String[] strs = line.split(" ");
               Pair<String, String> newPair = new Pair<>(strs[0], strs[2]);
               relevances.add(newPair);
               line = bf.readLine();
           }

       }
        // Maps queries into query containers (stores paragraph and feature information)

        for(int i = 0; i < queries.size(); i++)
        {
            String query = queries.get(i).getKey();
            TopDocs tp = queries.get(i).getValue();
            ScoreDoc[] scoreDoc = tp.scoreDocs;
            List<paraContrainer> containers = new ArrayList<>();
            for (int j = 0; j < scoreDoc.length; j++){
                ScoreDoc score = scoreDoc[j];
                Document doc = indexSearcher.doc(score.doc);
                String paraId = doc.getField("paraid").stringValue();

                boolean isRelevant = false;
                if(relevances != null)
                {
                    if(relevances.contains(new Pair<>(query, paraId)))
                    {
                        isRelevant = true;
                    }else
                        isRelevant = false;

                }
                paraContrainer pc = new paraContrainer(paraId, i + 1, isRelevant, new ArrayList<Double>(), score.doc,0.0);
                containers.add(pc);
            }
            queryContainer qc = new queryContainer(query, tp, containers);
            qcontainers.add(qc);

        }



    }

    public List<Double> normSum(List<Double> values)
    {
        double sum = 0.0;
        for(Double num: values)
        {
            sum += num;
        }

        for(int i = 0; i < values.size(); i++)
        {
            double val = values.get(i);
            values.set(i, val / sum);
        }
        return  values;
    }


    /**
     * Function: normZscore
     * Description: Calculates zscore for doubles in list

     */

    public List<Double> normZscore(List<Double> values)
    {
        double mean = 0.0;
        double sum = 0.0;
        double std = 0.0;
        double tempSum = 0.0;
        for(Double num: values)
        {
            sum += num;
        }
        mean = sum / values.size();
        for(Double num: values)
        {
            tempSum += Math.pow(num - mean, 2.0);
        }
        std = Math.sqrt(tempSum);

        for(int i = 0; i < values.size(); i++)
        {
            double temp = values.get(i);
            values.set(i, (temp - mean)/std);
        }
        return values;
    }



    /**
     * Function: normLinear
     * Description: Linearizes each value: (value - min) / (max - min)

     */

    public  List<Double> normLinear(List<Double> values)
    {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for(Double num: values)
        {
            if(num > max)
            {
                max = num;
            }
            if(num < min)
            {
                min = num;
            }
        }

        for(int i = 0; i < values.size(); i++)
        {
            double temp = values.get(i);
            values.set(i, (temp - min) / (max - min));
        }

        return values;
    }

    /**
     * Enum: NormType
     * Description: Determines if the the values of an added feature should be normalized
     */

    public enum normType{
        NONE,           // No normalization should be used
        SUM,            // Value is divided by total sum of all values in query
        ZSCORE,         // Zscore is calculated for all values in query
        LINEAR          // Value is normalized to: (value - min) / (max - min)
    }

    /**
     * Function: normalizeResults
     * Description: Normalizes a list of doubles according to the NormType

     */

    public List<Double> normalizeResults(List<Double> values, normType ntype)
    {
        switch (ntype)
        {
            case NONE:
                return values;

            case SUM:
                return normSum(values);

            case LINEAR:
                return normLinear(values);

            case ZSCORE:
                return normZscore(values);

             default:
                 break;
        }
        return new ArrayList<Double>();
    }


    /**
     *
     * @param f
     * @param weight
     * @param ntype
     */

    public void addFeatures(Function f, double weight, normType ntype) throws IOException {
//        System.out.println("addfeatures");
//        System.out.println(qcontainers.size());
        for(int i = 0; i < qcontainers.size(); i++)
        {
            String query = qcontainers.get(i).getQuery();
            //System.out.println("Here we go");
            TopDocs tps = qcontainers.get(i).getTops();
            List<paraContrainer> paras = qcontainers.get(i).getParas();
            /*
            这个位置第三个参数不确定 debug的时候记得回来检查
             */
            List<Double> curList = f.func(query, tps, indexSearcher);
            List<Double> curRes = normalizeResults(curList, ntype);
            List<Pair<Double, paraContrainer>> combination = new ArrayList<>();
            int minLen  = curRes.size();


            if(minLen > paras.size())
                minLen = paras.size();
            for(int j = 0; j < minLen; j++)
            {
                double curScore = curRes.get(j);
                paraContrainer curPara = paras.get(j);
                Pair<Double, paraContrainer> newComb = new Pair<>(curScore, curPara);
                combination.add(newComb);
            }

            for(Pair<Double, paraContrainer> entry: combination)
            {
                double score = entry.getKey();
                paraContrainer paragraph = entry.getValue();
                paragraph.features.add(score * weight);
            }

        }
    }

    // Convenience function (turns NaN and infinite values into 0.0)
    public double transDouble(double d)
    {
        if(Double.isInfinite(d) || Double.isNaN(d))
        {
            d = 0.0;
        }
        return d;
    }

    /**
     * Function: addBM25
     * Description: Adds results of the BM25 query as a feature. Since the scores are already contained in the TopDocs,
     *              this simply extracts them as a list of doubles.

     */

    public List<Double> bm25(String query, TopDocs tops, IndexSearcher indexSearcher)
    {
        ScoreDoc[] scoreDoc = tops.scoreDocs;
        List<Double> res = new ArrayList<>();
        for (int i = 0; i < scoreDoc.length; i++) {
            ScoreDoc score = scoreDoc[i];
            double s = (double) score.score;
            res.add(s);
        }
        return res;
    }
    public void addBM25(Double weight, normType ntype) throws IOException {
        addFeatures(this::bm25, weight, ntype);

    }


    /**
     * Function: rerankQueries
     * Description: Sums current weighted features together and reranks documents according to their new scores.

     */

    public void rerankQueries(){
        for(queryContainer qc: qcontainers)
        {
            List<paraContrainer> sortedParaContainer = new ArrayList<>();
            for(paraContrainer pc : qc.paras)
            {
                double total = 0.0;
                for(int i = 0; i < pc.features.size(); i++)
                {
                    double curNum = pc.features.get(i);
                    total += transDouble(curNum);
                }
                pc.score = total;
                sortedParaContainer.add(pc);
            }
            qc.paras = sortedParaContainer;

            for(int i = 0; i < sortedParaContainer.size(); i++)
            {
                paraContrainer para = sortedParaContainer.get(i);
                qc.tops.scoreDocs[i].doc = para.docId;
                qc.tops.scoreDocs[i].score = (float) para.score;
            }

        }
    }

    /**
     * Function: writeToRankLibFile
     * Desciption: Writes features to a RankLib-compatible file.

     */

    public void writeToRankLibFile(String output) throws IOException {
        BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(output));
        System.out.println("Out put path: " + output);
        for(queryContainer qc: qcontainers)
        {
            for(paraContrainer pc: qc.paras)
            {
                bufferWriter.write(pc.toString());
                bufferWriter.newLine();
            }
            bufferWriter.flush();
        }
        bufferWriter.close();

    }



    /**
     * Function: writeQueriesToFile
     * Desciption: Uses query formatter to write current queries to trec-car compatible file

     */

    public void writeQuriesToFile(String output) throws IOException {
        queryRetriever qr = new queryRetriever(indexPath);
        qr.writeQueriesFile(queries, output);
    }





}
