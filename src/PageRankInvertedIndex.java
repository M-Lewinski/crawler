import ir.vsr.DocumentIterator;
import ir.vsr.HashMapVector;
import ir.vsr.InvertedIndex;
import ir.vsr.Retrieval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

public class PageRankInvertedIndex extends InvertedIndex {

    public double weight = 1.0;
    public static String queryResultFileName = "queryResult.txt";
    private Path queryResultPath;

    public HashMap<String,Double> pageRanksList;

    private void readPageRanks(){
        try (Stream<String> stream = Files.lines(Paths.get(PageRankFile.pageRankFileName))) {
            stream.forEachOrdered(this::parsePageRankFile);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void parsePageRankFile(String str){
        String[] split = str.split(" ");
        if (split.length != 2) System.exit(5);
        this.pageRanksList.put(split[0],Double.parseDouble(split[1]));
    }

    public PageRankInvertedIndex(File dirFile, short docType, boolean stem, boolean feedback, double weight) {
        super(dirFile, docType, stem, feedback);
        this.weight = weight;
        this.pageRanksList = new HashMap<>();
        queryResultPath = Paths.get(queryResultFileName);
        this.readPageRanks();
        try {
            Files.write(queryResultPath,"".getBytes());
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public void presentRetrievals(HashMapVector queryVector, Retrieval[] retrievals) {
        for (Retrieval ret: retrievals) {
            double value = this.pageRanksList.get(ret.docRef.file.getName().toString());
            ret.score = ret.score + this.weight*value;
        }
        saveResultToFile(queryVector,retrievals);
        super.presentRetrievals(queryVector, retrievals);

    }

    private void saveResultToFile(HashMapVector queryVector, Retrieval[] retrievals){
        StringBuilder queryTerms = new StringBuilder().append("querry terms:");
        for (String term : (Set<String>) queryVector.hashMap.keySet()) {
            queryTerms.append(" ").append(term);
        }
        queryTerms.append("\n");
        for (int i = 0; i < retrievals.length; i++) {
            Retrieval ret = retrievals[i];
            queryTerms.append(i+1).append(". ").append(ret.docRef.file.getName().toString()).append(" Score: ").append(ret.score).append("\n");
        }
        queryTerms.append("\n");
        try{
            Files.write(queryResultPath,queryTerms.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }

    }

    public static void main(String[] args) {
        double weight = 1.0;
        String dirFileName = "";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]){
                case "-weight":
                    if (i+1 > args.length-1) {
                        System.out.println("Provide value for weight");
                        System.exit(3);
                    }
                    weight = Double.parseDouble(args[i+1]);
                    break;
                default: dirFileName = args[i];
            }
        }
        if (dirFileName.isEmpty()){
            System.out.println("Provide directory name with documents");
        }
        File dirFile = new File(dirFileName);
        PageRankInvertedIndex pageRankInvertedIndex = new PageRankInvertedIndex(dirFile, DocumentIterator.TYPE_HTML,true,false,weight);
        pageRankInvertedIndex.processQueries();
    }

}
