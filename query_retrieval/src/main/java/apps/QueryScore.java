package apps;

import RetrievalModels.DirichletSmoothing;
import index.Index;
import index.IndexFactory;
import RetrievalModels.BM25Model;
import RetrievalModels.JMSmoothing;
import RetrievalModels.RetrievalModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class QueryScore {

    public static void main(String[] args) throws IOException {
        // BM25:  args = [k, compressed, model, k1, k2]
        // QLJM:  args = [k, compressed, model, lambda]
        // QLDIR: args = [k, compressed, model, mu]

        Index index = IndexFactory.getIndex();
        boolean compressed = Boolean.parseBoolean(args[1]);
        index.load(compressed);
        int k = Integer.parseInt(args[0]);


        String[] queries = {"the king queen royalty", "servant guard soldier", "hope dream sleep",
                "ghost spirit", "fool jester player", "to be or not to be",
                "alas", "alas poor", "alas poor yorick", "antony strumpet"};

        RetrievalModel rModel;
        BM25Model bm25Model;

        List<Map.Entry<String, Double>> results;
        String outputFileName;
        String runTag;
        PrintWriter writer;

        switch (args[2]) {
            case "BM25":
                double k1 = Double.parseDouble(args[3]);
                double k2 = Double.parseDouble(args[4]);
                double b = 0.75;
                bm25Model = new BM25Model(index, k1, k2, b);
                outputFileName = "bm25.trecrun";
                runTag = String.format("kungwenyu-bm25-%s-%s",
                                        Double.toString(k1).replaceAll("\\.?0*$", ""),
                                        Double.toString(k2).replaceAll("\\.?0*$", ""));

                writer = new PrintWriter(outputFileName);

                for (int i = 0; i < queries.length; i++) {
                    String query = queries[i];
                    results = bm25Model.retrieveQuery(query, k);

                    // loop through all doc
                    for (int j = 0; j < results.size(); j++) {
                        Map.Entry<String, Double> doc = results.get(j);

                        // query_number, skip, scene_identifier, rank, score, run_tag
                        writer.write(String.format("Q%-2d skip %-40s %-3d %.3f %s\n", i+1, doc.getKey(), j+1, doc.getValue(), runTag));
                    }
                }

                writer.close();
                break;
            case "QLJM":
                double lambda = 0.2;
                rModel = new JMSmoothing(index, lambda);
                outputFileName = "ql-jm.trecrun";
                runTag = String.format("kungwenyu-ql-jm-%s",
                        Double.toString(lambda).replaceAll("\\.?0*$", ""));

                writer = new PrintWriter(outputFileName);

                for (int i = 0; i < queries.length; i++) {
                    String query = queries[i];
                    results = rModel.retrieveQuery(query, k);

                    // loop through all doc
                    for (int j = 0; j < results.size(); j++) {
                        Map.Entry<String, Double> doc = results.get(j);

                        // query_number, skip, scene_identifier, rank, score, run_tag
                        writer.write(String.format("Q%-2d skip %-40s %-3d %.3f %s\n", i+1, doc.getKey(), j+1, doc.getValue(), runTag));
                    }
                }

                writer.close();
                break;
            case "QLDIR":
                Double mu = Double.parseDouble(args[3]);
                rModel = new DirichletSmoothing(index, mu);
                outputFileName = "ql-dir.trecrun";
                runTag = String.format("kungwenyu-ql-dir-%d", mu.intValue());

                writer = new PrintWriter(outputFileName);

                for (int i = 0; i < queries.length; i++) {
                    String query = queries[i];
                    results = rModel.retrieveQuery(query, k);

                    // loop through all doc
                    for (int j = 0; j < results.size(); j++) {
                        Map.Entry<String, Double> doc = results.get(j);

                        // query_number, skip, scene_identifier, rank, score, run_tag
                        writer.write(String.format("Q%-2d skip %-40s %-3d %.3f %s\n", i+1, doc.getKey(), j+1, doc.getValue(), runTag));
                    }
                }

                writer.close();
                break;
            default:
                throw new RuntimeException("No such model, Valid models: BM25, QLJM, QLDIR");

        }

    }
}
