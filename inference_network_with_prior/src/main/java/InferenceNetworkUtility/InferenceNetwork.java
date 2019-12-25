package InferenceNetworkUtility;

import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.Proximity.TermNode;
import RetrievalModels.RetrievalModel;
import index.Index;

import java.util.*;

public class InferenceNetwork {

    public static List<ProximityNode> getTermNodes(String[] queryTerms, Index index, RetrievalModel model) {
        List<ProximityNode> children = new ArrayList<>();
        for (String term: queryTerms) {
            ProximityNode node = new TermNode(term, index, model);
            children.add(node);
        }
        return children;
    }

    public static List<Map.Entry<Integer, Double>> runQuery(QueryNode qNode, int k) {
        PriorityQueue<Map.Entry<Integer, Double>> result = new PriorityQueue<>(Map.Entry.<Integer, Double>comparingByValue());

        while (qNode.hasMore()) {
            int docId = qNode.nextCandidate();
            qNode.skipToNext(docId);
            Double score = qNode.score(docId);

            if (score != null) {
                result.add(new AbstractMap.SimpleEntry<Integer, Double>(docId, score));

                if (result.size() > k) {
                    result.poll();
                }
            }

            qNode.skipToNext(docId + 1);
        }

        ArrayList<Map.Entry<Integer, Double>> scores = new ArrayList<>();
        scores.addAll(result);
        scores.sort(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder()));
        return scores;
    }
}
