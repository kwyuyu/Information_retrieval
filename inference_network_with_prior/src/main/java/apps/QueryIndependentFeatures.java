package apps;

import InferenceNetworkUtility.Belief.AndNode;
import InferenceNetworkUtility.InferenceNetwork;
import InferenceNetworkUtility.Proximity.PriorNode;
import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.QueryNode;
import RetrievalModels.DirichletSmoothing;
import RetrievalModels.RetrievalModel;
import index.Index;
import index.IndexFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QueryIndependentFeatures {

    public static void main(String[] args) throws IOException {

        int k = Integer.parseInt(args[0]);
        boolean compressed = Boolean.parseBoolean(args[1]);

        Index index = IndexFactory.getIndex();
        index.load(compressed);
        makePrior(index);
        RetrievalModel model = new DirichletSmoothing(index.getCollectionSize(), 1500);

        String query = "the king queen royalty";

        String outputFile;
        String runTag;
        PrintWriter writer;
        String[] queryTerms = query.split("\\s+");
        List<ProximityNode> children;
        String priorType;
        QueryNode qNode;
        List<Map.Entry<Integer, Double>> result;

        // uniform.trecrun
        outputFile = "uniform.trecrun";
        runTag = "kungwenyu-and-dir-1500";
        priorType = "uniform";
        writer = new PrintWriter(outputFile);
        children = InferenceNetwork.getTermNodes(queryTerms, index, model);
        children.add(new PriorNode(index, model, priorType));
        qNode = new AndNode(children);
        result = InferenceNetwork.runQuery(qNode, k);
        outputResult(result, index, 0, runTag, writer);
        writer.close();

        // random.trecrun
        outputFile = "random.trecrun";
        runTag = "kungwenyu-and-dir-1500";
        priorType = "random";
        writer = new PrintWriter(outputFile);
        children = InferenceNetwork.getTermNodes(queryTerms, index, model);
        children.add(new PriorNode(index, model, priorType));
        qNode = new AndNode(children);
        result = InferenceNetwork.runQuery(qNode, k);
        outputResult(result, index, 0, runTag, writer);
        writer.close();
    }

    private static void outputResult(List<Map.Entry<Integer, Double>> result, Index index, int queryIndex, String runTag, PrintWriter writer) {
        for (int j = 0; j < result.size(); j++) {
            Map.Entry<Integer, Double> entry = result.get(j);
            writer.write(String.format("Q%-2d skip %-40s %-3d %.3f %s\n", queryIndex+1, index.getScene(entry.getKey()), j+1, entry.getValue(), runTag));
        }
    }

    public static void makePrior(Index index) {
        try {
            String name = "uniform";
            RandomAccessFile writer = new RandomAccessFile(name + ".prior", "rw");
            double uniform = Math.log(1.0 / index.getDocCount());
            for (int i = 1; i <= index.getDocCount(); i++) {
                writer.writeDouble(uniform);
//                System.out.println(index.getDocName(i) + "\t" + uniform);
            }
            writer.close();

            name = "random";
            Random rand = new Random(1024);
            writer = new RandomAccessFile(name + ".prior", "rw");
            for (int i = 1; i <= index.getDocCount(); i++) {
                double prior = Math.log(rand.nextDouble());
                writer.writeDouble(prior);
//                System.out.println(index.getDocName(i) + "\t" + prior);
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
