package main.java;

//import edu.unh.cs.treccar_v2.Data;
//import main.java.QueryExpansion.QueryExpansion;
//import main.java.QueryExpansion.QueryExpansionLDA;
//import main.java.QueryExpansion.QueryExpansionQueryEntity;
import main.java.ranklib.*;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static private String INDEX_DIRECTORY = "C:\\CS953\\ranklibTest\\index";
    static private String OUTPUT_DIR = "C:\\CS953\\ranklibTest\\output\\";
    static final private int Max_Results = 100;
    static private String queryLocation;
    static private String qrelLocation = "C:\\CS953\\ranklibTest\\queries\\benchmarkY1-test.v2.0.tar\\benchmarkY1\\benchmarkY1-test\\test.pages.cbor-article.qrels";
    static IndexData indexer;

    public static void main(String[] args) throws Exception,IOException, ParseException {
        System.setProperty("file.encoding", "UTF-8");

        String queryPath = "C:\\CS953\\ranklibTest\\queries\\benchmarkY1-test.v2.0.tar\\benchmarkY1\\benchmarkY1-test\\test.pages.cbor-outlines.cbor";


        String dataPath = "/Users/xin/Documents/19Spring/DS/test200/test200-train/train.pages.cbor-paragraphs.cbor";

        String lm_mercer = "lm_mercer";
        String lm_dirichlet = "lm_dirichlet";
        String avg_query = "average_query";
        String split_section = "split_sections";
        String combined = "combined";
        INDEX_DIRECTORY = args[0];
        queryPath = args[1];
        //dataPath = args[1];
        OUTPUT_DIR = args[2];
        qrelLocation = args[3];


//        indexer = new IndexData(INDEX_DIRECTORY, dataPath);
//        indexer.reIndex();

  //      QueryData queryData = new QueryData(queryPath);
//
//        Map<String,String> pageMap = queryData.getAllPageQueries();
//        Map<String,String> sectionMap = queryData.getAllSectionQueries();
//        ArrayList<Data.Page> pageList = queryData.getPageList();
//        ArrayList<Data.Section> sectionList = queryData.getSectionList();
//
//       //  Store all query strings temporarily.
//
//
//        System.out.println("Got " + pageMap.size() + " pages and " + sectionMap.size() + " sections.");
//
//        // Lucene Search
//
//
//        //SearchData searcher = new SearchData(INDEX_DIRECTORY, pageMap, sectionMap, Max_Results);
//
//
//
//        System.out.println("================");
//        System.out.println("length is: " + pageList.size());
//
//
//        // UL
//        UL page_ul = new UL(pageMap, Max_Results, INDEX_DIRECTORY, OUTPUT_DIR,"UnigramLanguageModel-Laplace-Page.run");
//        UL section_ul = new UL(sectionMap, Max_Results, INDEX_DIRECTORY, OUTPUT_DIR, "UnigramLanguageModel-Laplace-Section.run");
//
//        // UDS
//        UDS page_uds = new UDS(pageMap, Max_Results, INDEX_DIRECTORY, OUTPUT_DIR, "UnigramLanguageModel-UDS-Page.run");
//        UDS section_uds = new UDS(sectionMap,Max_Results, INDEX_DIRECTORY, OUTPUT_DIR, "UnigramLanguageModel-UDS-Section.run");
//
//        // UJM
//        UJM page_ujm = new UJM(pageMap, Max_Results, INDEX_DIRECTORY, OUTPUT_DIR, "UnigramLanguageModel-UJM-Page.run");
//        UJM section_ujm = new UJM(sectionMap, Max_Results, INDEX_DIRECTORY, OUTPUT_DIR, "UnigramLanguageModel-UJM-Section.run");
//
//
//
//        // BL
//        System.out.println("Running Biagram Language Model with Laplace Smoothing...");
//        BL page_bl = new BL(INDEX_DIRECTORY, Max_Results, OUTPUT_DIR);
//        page_bl.RankDocWithBigram_Laplace(pageMap, OUTPUT_DIR+"/"+"BigramLanguageModel-Laplace-Page.run");
//        BL section_bl = new BL(INDEX_DIRECTORY, Max_Results, OUTPUT_DIR);
//        section_bl.RankDocWithBigram_Laplace(sectionMap, OUTPUT_DIR+"/"+"BigramLanguageModel-Laplace-Section.run");

 //       System.out.println("QueryExpansion Begin");
//        QueryExpansion qe = new QueryExpansion(pageMap,sectionMap,INDEX_DIRECTORY,OUTPUT_DIR);
//
//        qe.runPage();
//        qe.runSection();


        // Run NLP entities variation methods
//        ArrayList<String> page_run = NLP_variation.getResults(pageMap, INDEX_DIRECTORY);
//        writeFile("NLP-variation-Page.run", page_run);
//        ArrayList<String> section_run = NLP_variation.getResults(sectionMap, INDEX_DIRECTORY);
//        writeFile("NLP-variation-Section.run", section_run);

//
//        QueryExpansionQueryEntity qeqe = new QueryExpansionQueryEntity(pageMap,sectionMap,INDEX_DIRECTORY,OUTPUT_DIR);
//        qeqe.runPage();
//        qeqe.runSection();
//
//        QueryExpansionLDA LDA = new QueryExpansionLDA(pageMap,sectionMap,INDEX_DIRECTORY,OUTPUT_DIR);
//        LDA.runPage();
//        LDA.runSection();

//        ArrayList<String> freqBigram_Page_run = Bigram_variation.getSearchResult(pageMap, INDEX_DIRECTORY);
//        writeFile("Bigram-Variation-Page.run", freqBigram_Page_run);
//
//        ArrayList<String> freqBigram_Section_run = Bigram_variation.getSearchResult(sectionMap, INDEX_DIRECTORY);
//        writeFile("Bigram-Variation-Section.run", freqBigram_Section_run);
        // Ranklib Query
            // lm-mercer
//           runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + lm_mercer + "_pages_query_results.run", lm_mercer, "pages");
//           runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + lm_mercer + "_sections_query_results.run", lm_mercer, "sections");

           // lm_dirichlet
//
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + lm_dirichlet + "_pages_query_results.run", lm_dirichlet, "pages");
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + lm_dirichlet + "_sections_query_results.run", lm_dirichlet, "sections");

        // average_query
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + avg_query + "_pages_query_results.run", avg_query, "pages");
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + avg_query + "_sections_query_results.run", avg_query, "sections");

        // split sections
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + split_section + "_pages_query_results.run", split_section, "pages");
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + split_section + "_sections_query_results.run", split_section, "sections");

        // combined
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + combined + "_pages_query_results.run", combined, "pages");
//        runRanklibQuery(INDEX_DIRECTORY, queryPath, OUTPUT_DIR  + combined + "_sections_query_results.run", combined, "sections");

        // Ranklib Trainer

        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + lm_mercer +"_ranklib_features.txt", lm_mercer, "pages");
        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + lm_dirichlet +"_ranklib_features.txt", lm_dirichlet, "pages");
        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + avg_query +"_ranklib_features.txt", avg_query, "pages");
        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + split_section +"_ranklib_features.txt", split_section, "pages");
        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + combined +"_ranklib_features.txt", combined, "pages");
//        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + lm_mercer +"_ranklib_features.txt", lm_mercer, "sections");
//        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + lm_dirichlet +"_ranklib_features.txt", lm_dirichlet, "sections");
//        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + avg_query +"_ranklib_features.txt", avg_query, "sections");
//        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + split_section +"_ranklib_features.txt", split_section, "sections");
//        runRanklibTrainer(INDEX_DIRECTORY, queryPath, qrelLocation, OUTPUT_DIR  + combined +"_ranklib_features.txt", combined, "sections");


        System.out.println("Finished");
    }

    public static void writeFile(String name, List<String> content){
        String fullpath = OUTPUT_DIR + "/" + name;
        System.out.println(fullpath);
        try (FileWriter runfile = new FileWriter(new File(fullpath))) {
            for (String line : content) {
                runfile.write(line + "\n");
            }

            runfile.close();
        } catch (IOException e) {
            System.out.println("Could not open " + fullpath);
        }
    }

    public static void runRanklibTrainer(String indexPath, String queryLocation, String qrelLocation, String output, String method, String flag) throws IOException {
        ranklibTrainer trainer = new ranklibTrainer(indexPath, queryLocation, qrelLocation, flag);
        trainer.train(method, output);
    }

    public static void runRanklibQuery(String indexPath, String queryLocation, String output, String method, String flag) throws IOException {

        ranklibTrainer trainer = new ranklibTrainer(indexPath, queryLocation, "", flag);
        trainer.runRanklibQuery(method, output);
    }


}
