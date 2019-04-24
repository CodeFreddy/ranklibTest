package main.java.ranklib;

import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import info.debatty.java.stringsimilarity.interfaces.StringDistance;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.*;

public class ranklibTrainer {
    private String indexPath;
    private String queryPath;
    private String qrelPath;
    private ranklibFormatter formatter;
    public ranklibTrainer(String indexPath, String queryPath, String qrelPath, String flag) throws IOException {
        this.indexPath = indexPath;
        this.queryPath = queryPath;
        this.qrelPath = qrelPath;

        formatter = new ranklibFormatter(this.qrelPath, this.queryPath, this.indexPath, flag);

    }

    /**
     * Function: retrieveSequence
     * Description: For qiven query string, filters out numbers (and enwiki) and retruns a list of tokens
     */

    public List<String> retrieveSequence(String query) throws IOException {
        String reg = "(\\d+|enwiki:)";
        List<String> sequence = new ArrayList<>();
        String res = query.replaceAll(reg, "");
        sequence = formatter.qRetriever.createTokenList(res);

        return sequence;
    }

    /**
     * Function: addStringDistanceFunction
     * Description: In this method, I try to the distance (or similarity) between the terms (after splitting)
     *              and the entities in each document.
     * @params dist: StingDistance interface (from debatty stringsimilarity library)
     */

    public List<Double> addStringDistanceFunction(String query, TopDocs tops, StringDistance distance) throws IOException {
        List<String> tokens = retrieveSequence(query);
        List<Double> res = new ArrayList<>();
        ScoreDoc[] scoreDoc = tops.scoreDocs;

        for (int i = 0; i < scoreDoc.length; i++) {
            List<Double> temp = new ArrayList<>();
            ScoreDoc score = scoreDoc[i];
            Document doc = formatter.indexSearcher.doc(score.doc);
            String[] entities = doc.getValues("spotlight");
            double sum = 0.0;
            double mean = 0.0;
            for(String str: entities)
            {
                str.replace("_", " ");
            }

            for(String token: tokens)
            {
                for(String entity: entities)
                {
                    double curVal = 0.0;
                    curVal = distance.distance(token, entity);
                    temp.add(curVal);
                }
            }

            for(int j = 0; j < temp.size(); j++)
            {
                sum += temp.get(j);
            }

            mean = sum / temp.size();
            res.add(mean);

        }
        return res;

    }

    /**
     * Function: addAverageQueryScore
     * Description: In this method, I tokenize the query and treat each token as an individual query.
     *              I then get the BM25 score of each query to each document and average the results.
     */

    public List<Double> addAverageQueryScore(String query, TopDocs tops, IndexSearcher indexSearcher) throws IOException {
        List<String> tokens = retrieveSequence(query);
        List<BooleanQuery> termQueries = new ArrayList<>();
        List<Double> results = new ArrayList<>();
        for(String token: tokens)
        {
            BooleanQuery.Builder res = new BooleanQuery.Builder();
            BooleanQuery bq;
            Query q = new TermQuery(new Term("content", token));
            res.add(q, BooleanClause.Occur.SHOULD);
            bq = res.build();
            termQueries.add(bq);
        }

        ScoreDoc[] scoreDoc = tops.scoreDocs;
        for (int i = 0; i < scoreDoc.length; i++){
            ScoreDoc score = scoreDoc[i];
            List<Double> tempList = new ArrayList<>();
            double sum = 0.0;
            double mean = 0.0;
            for(BooleanQuery booleanQuery: termQueries)
            {
                double temp = 0.0;
                temp = (double)indexSearcher.explain(booleanQuery, score.doc).getValue();
                tempList.add(temp);
            }
            for(int j = 0; j < tempList.size(); j++)
            {
                sum += tempList.get(j);
            }
            mean = sum / tempList.size();
            results.add(mean);
        }
        return results;
    }

    /**
     * Function: addEntityQueries
     * Description: This method is supposed to consider query only the
     */




    /**
     * Function: useLucSim
     * Description: Takes a Lucene similarity function and uses it to rescore documents.
     */

    public List<Double> useLucSim(String query, TopDocs tops, IndexSearcher indexSearcher, Similarity sim) throws IOException {
        List<String> entityQuery = retrieveSequence(query);
        BooleanQuery.Builder res = new BooleanQuery.Builder();
        List<Double> results = new ArrayList<>();
        BooleanQuery bq;
        for(String token: entityQuery)
        {

            Query q = new TermQuery(new Term("content", token));
            res.add(q, BooleanClause.Occur.SHOULD);

        }
        bq = res.build();
        Similarity curSim = indexSearcher.getSimilarity(true);
        indexSearcher.setSimilarity(sim);

        ScoreDoc[] scoreDoc = tops.scoreDocs;
        for (int i = 0; i < scoreDoc.length; i++){
            ScoreDoc score = scoreDoc[i];
            double curVal = (double) indexSearcher.explain(bq, score.doc).getValue();
            results.add(curVal);
        }
        indexSearcher.setSimilarity(curSim);
        return results;
    }
    /**
     * Function: querySimilarity
     * Description: Score with weighted combination of BM25 and string similarity functions (trained using RankLib).
     */


    public void querySimilarity() throws IOException {
        formatter.addBM25(0.884669653, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> addStringDistanceFunction(query, tops, new JaroWinkler()), -0.001055, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> addStringDistanceFunction(query, tops, new Jaccard()), 0.11427, ranklibFormatter.normType.ZSCORE);

    }


    public List<Double> sectionSplit(String query, TopDocs tops, IndexSearcher indexSearcher, int secIndex) throws IOException {
        List<String> tokens = retrieveSequence(query);
        List<BooleanQuery> termQueries = new ArrayList<>();
        List<Double> results = new ArrayList<>();
        for(String token: tokens)
        {
            BooleanQuery.Builder res = new BooleanQuery.Builder();
            BooleanQuery bq;
            Query q = new TermQuery(new Term("content", token));
            res.add(q, BooleanClause.Occur.SHOULD);
            bq = res.build();
            termQueries.add(bq);
        }
        ScoreDoc[] scoreDoc = tops.scoreDocs;
        if(termQueries.size() < secIndex + 1)
        {
            for(int i = 0; i < tops.scoreDocs.length; i++)
            {
                results.add(0.0);

            }
            return results;
        }

        BooleanQuery booleanQuery = null;
        if(termQueries.get(secIndex) != null)
            booleanQuery = termQueries.get(secIndex);

        for (int i = 0; i < scoreDoc.length; i++) {
            ScoreDoc score = scoreDoc[i];
            double curVal = (double) indexSearcher.explain(booleanQuery, score.doc).getValue();
            results.add(curVal);
        }

        return results;
    }


    /**
     * Function: querySimilarity
     * Description: Score with weighted combination of BM25 and average_query (trained using RankLib).
     */

    public void queryAverage() throws IOException {
        formatter.addBM25(0.5, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures(this::addAverageQueryScore, 0.5, ranklibFormatter.normType.ZSCORE);
    }

    /**
     * Function: querySplit
     * Description: Score with weighted combination of BM25 and separate section scores (trained using RankLib).
     */

    public void querySplit() throws IOException {
        formatter.addBM25(0.4824247, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 0), 0.069, ranklibFormatter.normType.ZSCORE);
        System.out.println("Split!!!");
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 1), -0.1845, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 2), -0.25063, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 3),  0.0134, ranklibFormatter.normType.ZSCORE);

    }


    /**
     * Function: queryDirichlet
     * Description: Score with weighted combination of BM25 and LM_Dirichlet method (trained using RankLib)
     */

    public void queryDirichlet() throws IOException {
        System.out.println("Dirichlet");
        formatter.addBM25( 0.80067, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSeacher) -> useLucSim(query, tops, indexSeacher, new LMDirichletSimilarity()), 0.19932975, ranklibFormatter.normType.ZSCORE);

    }

    /**
     * Function: queryMercer
     * Description: Score with weighted combination of BM25 and LM_Dirichlet method (trained using RankLib)
     */

    public void queryMercer() throws IOException {
        formatter.addBM25( 0.82, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSeacher) -> useLucSim(query, tops, indexSeacher, new LMJelinekMercerSimilarity(new LMSimilarity.DefaultCollectionModel(), 0.5f)), 0.1798988, ranklibFormatter.normType.ZSCORE);
    }

    /**
     * Function: queryCombined
     * Description: Score with weighted combination of BM25, Jaccard string similarity, LM_Dirichlet, and second/third
     *              section headers (trained using RankLib).
     */

    public void queryCombined() throws IOException {
        double[] weights = {0.3106317698753524,-0.025891305471130843,
                0.34751201103557083, -0.2358113441529167, -0.08015356975284649};
        formatter.addBM25(weights[0], ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> addStringDistanceFunction(query, tops, new Jaccard()), weights[1], ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> useLucSim(query, tops, indexSearcher, new LMDirichletSimilarity()), weights[2], ranklibFormatter.normType.ZSCORE);

        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 1),  weights[3], ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 2),  weights[4], ranklibFormatter.normType.ZSCORE);
    }

    // Runs associated query method

    public void runRanklibQuery(String method, String output) throws IOException {
        switch (method)
        {
            case "average_query":
                queryAverage();
                break;
            case "split_sections":
                querySplit();
                break;
            case  "lm_mercer":
                queryMercer();
                break;
            case  "lm_dirichlet":
                queryDirichlet();
                break;
            case "combined":
                queryCombined();
                break;
            default:
                break;
        }

        formatter.rerankQueries();
        formatter.qRetriever.writeQueriesFile(formatter.queries, output);
    }


    /**
     * Function: trainSplit
     * Description: training for section_split method.
     * @see
     */

    public void trainSplit() throws IOException {
        formatter.addBM25(1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 0),  1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 1),  1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 2),  1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 3),  1.0, ranklibFormatter.normType.ZSCORE);
    }

    /**
     * Function: trainAverageQuery
     * Description: training for average_query method.

     */

    public void trainAverageQuery() throws IOException {
        formatter.addBM25(1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures(this::addAverageQueryScore, 1.0, ranklibFormatter.normType.ZSCORE);
    }


    /**
     * Function: trainDirichSim
     * Description: training for lm_dirichlet method.

     */

    public void trainDirichSim() throws IOException {
        formatter.addBM25(1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> useLucSim(query, tops, indexSearcher, new LMDirichletSimilarity()),1.0, ranklibFormatter.normType.ZSCORE);
    }

    /**
     * Function: trainJelinekMercerSimilarity
     * Description: training for lm_mercer method.

     */

    public void trainJelinekMercerSim() throws IOException {
        formatter.addBM25(1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSeacher) -> useLucSim(query, tops, indexSeacher, new LMJelinekMercerSimilarity(new LMSimilarity.DefaultCollectionModel(), 0.5f)), 1.0, ranklibFormatter.normType.ZSCORE);

    }


    /**
     * Function: trainCombined
     * Description: training for combined method.

     */

    public void trainCombined() throws IOException {
        formatter.addBM25(1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> addStringDistanceFunction(query, tops, new Jaccard()), 1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> useLucSim(query, tops, indexSearcher, new LMDirichletSimilarity()), 1.0, ranklibFormatter.normType.ZSCORE);

        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 1),  1.0, ranklibFormatter.normType.ZSCORE);
        formatter.addFeatures((query, tops, indexSearcher) -> sectionSplit(query, tops, indexSearcher, 2),  1.0, ranklibFormatter.normType.ZSCORE);


    }

    /**
     * Function: train
     * Description: Add features associated with training method and then writes scored features to a RankLib compatible
     *              file for later use in training weights.
     */

    public void train(String method, String out) throws IOException {
        switch (method){
            case "average_query":
                trainAverageQuery();
                break;
            case "split_sections":
                trainSplit();
                break;
            case  "lm_mercer":
                trainJelinekMercerSim();
                break;
            case  "lm_dirichlet":
                trainDirichSim();
                break;
            case "combined":
                trainCombined();
                break;
            default:
                System.out.println("Unkown method!");
                break;
        }
        formatter.writeToRankLibFile(out);
    }

}
