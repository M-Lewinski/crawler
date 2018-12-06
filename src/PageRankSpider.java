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

public class PageRankSpider extends Spider {

    public PageRankFile pageRankFile;

    public Graph graph;

    public String graphFileName = "graph.txt";
    public String logFileName = "log.txt";

    public Path graphPath;
    public Path logPath;

    private List<List<Link>> listOutEdgesOfVisitedNodes;

    public PageRankSpider() {
        this.graph = new Graph();
        this.pageRankFile = new PageRankFile(graph);
        this.listOutEdgesOfVisitedNodes = new ArrayList<>();
    }

    @Override
    protected List getNewLinks(HTMLPage page) {
        List list = super.getNewLinks(page);
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(page.toString());
//        for (Link outlink: (List<Link>) list) {
//            stringBuilder.append(" ");
//            graph.addEdge(page.toString(),outlink.toString());
//            stringBuilder.append(outlink.toString());
//        }
//        stringBuilder.append("\n");
//        String graphNode = stringBuilder.toString();
//        try {
//            Files.write(this.graphPath, graphNode.getBytes(), StandardOpenOption.APPEND);
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            System.exit(2);
//        }

        this.listOutEdgesOfVisitedNodes.add((List<Link>) list);

        return list;
    }

    @Override
    protected void handleDCommandLineOption(String value) {
        super.handleDCommandLineOption(value);
        graphFileName = new StringBuilder().append(value).append("/").append(graphFileName).toString();
        logFileName = new StringBuilder().append(value).append("/").append(logFileName).toString();
        graphPath = Paths.get(graphFileName);
        logPath = Paths.get(logFileName);
        try {
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
        int i = 0;
        for (Link nodeLink : (HashSet<Link>) this.visited) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(nodeLink.toString());
            for (Link outlink: (List<Link>) this.listOutEdgesOfVisitedNodes.get(i)) {
                stringBuilder.append(" ");
                stringBuilder.append(outlink.toString());
            }
            stringBuilder.append("\n");
            String graphNode = stringBuilder.toString();
            try {
                Files.write(this.graphPath, graphNode.getBytes(), StandardOpenOption.APPEND);
            }
            catch (IOException e){
                e.printStackTrace();
                System.exit(2);
            }
            i++;
        }
        try {
            this.graph.readFromFile(this.graphFileName);
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
