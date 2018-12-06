import ir.webutils.Graph;
import ir.webutils.Node;
import ir.webutils.PageRank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageRankFile extends PageRank {


    public Graph pageGraph;

    public String pageRankFileName = "pageRanks.txt";
    public Path pageRankPath;

    public float alpha = 0.15f;
    public int iteration = 50;


    public PageRankFile(Graph pageGraph) {
        this.pageGraph = pageGraph;
    }

    public void generatePathTofile(String dir){
        this.pageRankPath = Paths.get(new StringBuilder().append(dir).append("/").append(this.pageRankFileName).toString());
        try {
            Files.write(this.pageRankPath,"".getBytes());
        } catch (IOException e){
            e.printStackTrace();
            System.exit(2);
        }
    }

    public HashMap<Node,Double> calculate(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        PrintStream stdout = System.out;
        System.setOut(ps);

        run(pageGraph,alpha,iteration);

        System.out.flush();
        System.setOut(stdout);
        List<Double> normR = getResultFromOutput(outputStream.toString());

        HashMap<Node,Double> results = new HashMap<>();
        Node[] nodes = pageGraph.nodeArray();
        for (int i = 0; i < nodes.length; i++) {
            results.put(nodes[i], normR.get(i));
            try {
                Files.write(pageRankPath,new StringBuilder().append(nodes[i].toString()).append(" ").append(normR.get(i)).append("\n").toString().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e){
                e.printStackTrace();
                System.exit(2);
            }
        }
        return results;
    }

    private List<Double> getResultFromOutput(String output){
        String[] split = output.split("\n");

        String normR = split[split.length-1];
        normR = normR.replaceAll("[A-Za-z=]","").replaceAll("\\[","").replaceAll("]","").replaceAll(",","");
        String[] doubles = normR.split(" ");
        List<Double> result = new ArrayList<>();
        for (String elem: doubles) {
            result.add(Double.parseDouble(elem));
        }
        return result;
    }

}
