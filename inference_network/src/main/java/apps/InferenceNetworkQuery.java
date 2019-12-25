package apps;

import InferenceNetworkUtility.*;
import InferenceNetworkUtility.Belief.*;
import InferenceNetworkUtility.Proximity.OrderedWindow;
import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.Proximity.UnorderedWindow;
import RetrievalModels.DirichletSmoothing;
import RetrievalModels.RetrievalModel;
import index.Index;
import index.IndexFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class InferenceNetworkQuery {

    public static void main(String[] args) throws IOException {

        int k = Integer.parseInt(args[0]);
        boolean compressed = Boolean.parseBoolean(args[1]);


        Index index = IndexFactory.getIndex();
        index.load(compressed);
        RetrievalModel model = new DirichletSmoothing(index.getCollectionSize(), 1500);


        String[] queries = {"the king queen royalty", "servant guard soldier", "hope dream sleep",
                "ghost spirit", "fool jester player", "to be or not to be",
                "alas", "alas poor", "alas poor yorick", "antony strumpet"};

        String outputFile;
        String runTag;
        PrintWriter writer;

        // od1.trecrun
        outputFile = "od1.trecrun";
        runTag = "kungwenyu-od1-dir-1500";
        writer = new PrintWriter(outputFile);
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            String[] queryTerms = query.split("\\s+");

            List<ProximityNode> children = InferenceNetwork.getTermNodes(queryTerms, index, model);
            QueryNode qNode = new OrderedWindow(index, model, children, 1);

            List<Map.Entry<Integer, Double>> result = InferenceNetwork.runQuery(qNode, k);
            outputResult(result, index, i, runTag, writer);
        }
        writer.close();


        // uw.trecrun
        outputFile = "uw.trecrun";
        runTag = "kungwenyu-uw-dir-1500";
        writer = new PrintWriter(outputFile);
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            String[] queryTerms = query.split("\\s+");

            List<ProximityNode> children = InferenceNetwork.getTermNodes(queryTerms, index, model);
            QueryNode qNode = new UnorderedWindow(index, model, children, 3 * queryTerms.length);

            List<Map.Entry<Integer, Double>> result = InferenceNetwork.runQuery(qNode, k);
            outputResult(result, index, i, runTag, writer);
        }
        writer.close();


        // sum.trecrun
        outputFile = "sum.trecrun";
        runTag = "kungwenyu-sum-dir-1500";
        writer = new PrintWriter(outputFile);
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            String[] queryTerms = query.split("\\s+");

            List<ProximityNode> children = InferenceNetwork.getTermNodes(queryTerms, index, model);
            QueryNode qNode = new SumNode(children);

            List<Map.Entry<Integer, Double>> result = InferenceNetwork.runQuery(qNode, k);
            outputResult(result, index, i, runTag, writer);
        }
        writer.close();


        // and.trecrun
        outputFile = "and.trecrun";
        runTag = "kungwenyu-and-dir-1500";
        writer = new PrintWriter(outputFile);
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            String[] queryTerms = query.split("\\s+");

            List<ProximityNode> children = InferenceNetwork.getTermNodes(queryTerms, index, model);
            QueryNode qNode = new AndNode(children);

            List<Map.Entry<Integer, Double>> result = InferenceNetwork.runQuery(qNode, k);
            outputResult(result, index, i, runTag, writer);
        }
        writer.close();


        // or.trecrun
        outputFile = "or.trecrun";
        runTag = "kungwenyu-or-dir-1500";
        writer = new PrintWriter(outputFile);
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            String[] queryTerms = query.split("\\s+");

            List<ProximityNode> children = InferenceNetwork.getTermNodes(queryTerms, index, model);
            QueryNode qNode = new OrNode(children);

            List<Map.Entry<Integer, Double>> result = InferenceNetwork.runQuery(qNode, k);
            outputResult(result, index, i, runTag, writer);
        }
        writer.close();


        // max.trecrun
        outputFile = "max.trecrun";
        runTag = "kungwenyu-max-dir-1500";
        writer = new PrintWriter(outputFile);
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
            String[] queryTerms = query.split("\\s+");

            List<ProximityNode> children = InferenceNetwork.getTermNodes(queryTerms, index, model);
            QueryNode qNode = new MaxNode(children);

            List<Map.Entry<Integer, Double>> result = InferenceNetwork.runQuery(qNode, k);
            outputResult(result, index, i, runTag, writer);
        }
        writer.close();
    }

    private static void outputResult(List<Map.Entry<Integer, Double>> result, Index index, int queryIndex, String runTag, PrintWriter writer) {
        for (int j = 0; j < result.size(); j++) {
            Map.Entry<Integer, Double> entry = result.get(j);
            writer.write(String.format("Q%-2d skip %-40s %-3d %.3f %s\n", queryIndex+1, index.getScene(entry.getKey()), j+1, entry.getValue(), runTag));
        }
    }
}


