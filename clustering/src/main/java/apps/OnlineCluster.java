package apps;

import ClusterUtility.Cluster;
import ClusterUtility.DocumentVector;
import ClusterUtility.LinkageMethod.Linkage;
import ClusterUtility.LinkageMethod.LinkageFactory;
import ClusterUtility.LinkageMethod.LinkageFunction;
import ClusterUtility.SimilarityMethod.SimilaritFactory;
import ClusterUtility.SimilarityMethod.Similarity;
import ClusterUtility.SimilarityMethod.SimilarityFunction;
import index.Index;
import index.IndexFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class OnlineCluster {

    public static void main(String[] args) {
        boolean compressed;
        double threshold;
        LinkageFunction linkageFunction;

        Index index = IndexFactory.getIndex();
        Similarity sim = SimilaritFactory.getSimilarity(SimilarityFunction.COSINE);
        String outputFileName;


        if (args.length < 1) {
            compressed = true;
            linkageFunction = LinkageFunction.AVERAGEGROUP;

            index.load(compressed);
            Linkage linkage = LinkageFactory.getLinkage(linkageFunction, index, sim);
            OnlineCluster onlineCluster = new OnlineCluster();

            for (threshold = 0.05; threshold < 1; threshold += 0.05) {
                outputFileName = String.format("cluster-%.2f.out", threshold);
                Map<Integer, Integer> sizeOfEachCluster = onlineCluster.runOnlineCluster(index, threshold, linkage, outputFileName);

                System.out.println(String.format("threshold: %.2f, number of cluster: %d", threshold, sizeOfEachCluster.size()));
                for (int cId : sizeOfEachCluster.keySet()) {
                    System.out.print(sizeOfEachCluster.get(cId) + " ");
                }
                System.out.println();
            }
        }
        else {
            threshold = Double.parseDouble(args[0]);
            compressed = Boolean.parseBoolean(args[2]);

            switch (args[1]) {
                case "single":
                    linkageFunction = LinkageFunction.SINGLE;
                    break;
                case "complete":
                    linkageFunction = LinkageFunction.COMPLETE;
                    break;
                case "average":
                    linkageFunction = LinkageFunction.AVERAGE;
                    break;
                case "averagegroup":
                    linkageFunction = LinkageFunction.AVERAGEGROUP;
                    break;
                default:
                    throw new RuntimeException("Linkage type: single, complete, average, averagegroup");
            }

            index.load(compressed);
            Linkage linkage = LinkageFactory.getLinkage(linkageFunction, index, sim);
            OnlineCluster onlineCluster = new OnlineCluster();
            outputFileName = String.format("cluster-%.2f-%s.out", threshold, args[1]);
            onlineCluster.runOnlineCluster(index, threshold, linkage, outputFileName);
        }
    }

    private Map<Integer, Integer> runOnlineCluster(Index index, double threshold, Linkage linkage, String outputFileName) {
        Map<Integer, Cluster> clusters = new HashMap<>();
        int clusterId = 0;
        int limit = index.getDocCount();

        for (int docId = 1; docId <= limit; docId++) {
            DocumentVector curDocVec = index.getDocumentVector(docId);
            double score = 0.0;
            int best = -1;
            for (Cluster c: clusters.values()) {
                int cId = c.getClusterId();
                double s = c.score(curDocVec);
                if (s > score) {
                    score = s;
                    best = cId;
                }
            }

            if (score > threshold) {
                clusters.get(best).addDocumentVector(docId, curDocVec);
            }
            else {
                clusterId++;
                Cluster cluster = new Cluster(clusterId, index, linkage);
                cluster.addDocumentVector(docId, curDocVec);
                clusters.put(clusterId, cluster);
            }
        }

        // cId, size
        Map<Integer, Integer> sizeOfEachCluster = new HashMap<>();
        try {
            PrintWriter writer = new PrintWriter(outputFileName);
            clusters.keySet().stream().sorted().forEach((cId) -> {
                Cluster c = clusters.get(cId);
                c.getDocumentIds().forEach((dId) -> writer.write(cId + " " + dId + "\n"));

                int size = c.getDocumentIds().size();
                sizeOfEachCluster.put(cId, size);
            });
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return sizeOfEachCluster;
    }
}
