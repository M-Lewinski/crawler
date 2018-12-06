import ir.utilities.MoreMath;
import ir.utilities.MoreString;
import ir.webutils.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class PageRankSpider extends Spider {

    public PageRankFile pageRankFile;

    public Graph graph;

    public static String htmlGraphFileName = "htmlGraph.txt";
    public static String graphFileName = "graph.txt";

    public static String logFileName = "log.txt";

    public Path htmlGraphPath;
    public Path graphPath;
    public Path logPath;

    public HashMap<String,String> htmlToFile;

    public PageRankSpider() {
        this.graph = new Graph();
        this.pageRankFile = new PageRankFile(graph);
        this.htmlToFile = new HashMap<>();
    }

    @Override
    protected List getNewLinks(HTMLPage page) {
        List list = super.getNewLinks(page);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(page.getLink().toString());
        for (Link outlink: (List<Link>) list) {
            stringBuilder.append(" ");
            stringBuilder.append(outlink.toString());
        }
        stringBuilder.append("\n");
        String graphNode = stringBuilder.toString();
        try {
            Files.write(this.htmlGraphPath, graphNode.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
        this.htmlToFile.put(page.getLink().toString(), "P" +  MoreString.padWithZeros(count,(int)Math.floor(MoreMath.log(maxCount, 10)) + 1));
        return list;
    }

    @Override
    protected void handleDCommandLineOption(String value) {
        super.handleDCommandLineOption(value);
        htmlGraphFileName = new StringBuilder().append(htmlGraphFileName).toString();
        logFileName = new StringBuilder().append(logFileName).toString();
        htmlGraphPath = Paths.get(htmlGraphFileName);
        graphPath = Paths.get(graphFileName);
        logPath = Paths.get(logFileName);
        try {
            Files.write(htmlGraphPath,"".getBytes());
            Files.write(graphPath,"".getBytes());
            Files.write(logPath,"".getBytes());
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
        this.pageRankFile.generatePathTofile(value);
    }


    @Override
    public void doCrawl() {
        PrintStream stdout = System.out;
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(this.logFileName)), true));
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        super.doCrawl();
        System.out.flush();
        System.setOut(stdout);
    }

    @Override
    public void go(String[] args) {
        super.go(args);
        generateGraph();
        calculatePageRank();
    }

    private void generateGraph(){
        try (Stream<String> stream = Files.lines(this.htmlGraphPath)) {
            stream.forEachOrdered(this::convertFromHtmlToFile);

            this.graph.readFromFile(graphFileName);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void convertFromHtmlToFile(String str){
        String[] split = str.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String htmlLink = split[i];
            String index = this.htmlToFile.get(htmlLink);
            if (index == null){
                if (i == 0) return;
                else continue;
            }
            if (i != 0) stringBuilder.append(" ");
            stringBuilder.append(index);
        }
        stringBuilder.append("\n");
        try {
            Files.write(this.graphPath,stringBuilder.toString().getBytes(),StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
    }

    private void calculatePageRank(){
        HashMap<Node,Double> result = this.pageRankFile.calculate();
        for (Node node : result.keySet()){
            StringBuilder outEdgesStringBuilder = new StringBuilder().append(node).append(" -> [");

            for (int i = 0; i < node.getEdgesOut().size(); i++) {
                Node out = ((List<Node>) node.getEdgesOut()).get(i);
                outEdgesStringBuilder.append(out);
                if (i < node.getEdgesOut().size()-1){
                    outEdgesStringBuilder.append(", ");
                }
            }
            outEdgesStringBuilder.append("]\n");
            try {
                Files.write(logPath,outEdgesStringBuilder.toString().getBytes(),StandardOpenOption.APPEND);
            } catch (IOException e){
                e.printStackTrace();
                System.exit(2);
            }
        }
        for (Map.Entry<Node, Double> entry : result.entrySet()) {
            try {
                Node node = entry.getKey();
                Files.write(logPath,new StringBuilder().append("PR(").append(node).append(") ").append(entry.getValue()).append("\n").toString().getBytes(),StandardOpenOption.APPEND);
            } catch (IOException e){
                e.printStackTrace();
                System.exit(2);
            }
        }

    }

    public static void main(String[] args) {
        PageRankSpider pageRankSpider = new PageRankSpider();
        pageRankSpider.go(args);

    }
}
