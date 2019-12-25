package ClusterUtility.SimilarityMethod;

public class SimilaritFactory {

    public static Similarity getSimilarity(SimilarityFunction s) {
        switch (s) {
            case COSINE:
                return new CosineSimilarity();
        }
        return null;
    }
}
