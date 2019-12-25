package ClusterUtility.SimilarityMethod;

import ClusterUtility.DocumentVector;

import java.util.Map;

public class CosineSimilarity implements Similarity {

    public CosineSimilarity() {
    }

    @Override
    public double score(DocumentVector dv1, DocumentVector dv2) {
        double numerator = dv1.dot(dv2);
        double denominator = Math.sqrt(dv1.sumSquare() * dv2.sumSquare());
        return numerator / denominator;
    }
}
