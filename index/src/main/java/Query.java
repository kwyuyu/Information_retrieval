import com.jcraft.jsch.IO;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.*;

public class Query {

    private Indexer index;

    /* Constructor */
    public Query(Indexer index) {
        this.index = index;

        File queryDir = new File("query");
        if (!queryDir.exists()) {
            queryDir.mkdir();
        }
    }



    /* Methods */
    public void compressionHypothesisRandom() throws IOException, ParseException {
        /*
        * @Func: produce the processing result of query for compress and uncompress.
        *        the time consumed will store in to "queryTimeUncompress.txt" and "queryTimeCompress.txt" file.
        *        Each line is with the format "number_query time_consumed".
        *        The result can be visualized by running python file "plotChart.py"
        * */
        PrintWriter writerUncompress = new PrintWriter("query/queryTimeUncompress.txt", "UTF-8");
        PrintWriter writerCompress = new PrintWriter("query/queryTimeCompress.txt", "UTF-8");

        int[] nums = {10, 100, 1000, 10000, 20000};
        for (int num: nums) {
            long start = System.currentTimeMillis();
            this.querySentenceRandomQuery(5, 7, num, false);
            writerUncompress.write(String.format("%d %f\n", num, (System.currentTimeMillis() - start) / 1000.0));

            start = System.currentTimeMillis();
            this.querySentenceRandomQuery(5, 7, num, true);
            writerCompress.write(String.format("%d %f\n", num, (System.currentTimeMillis() - start) / 1000.0));
        }

        writerCompress.close();
        writerUncompress.close();
    }

    public void compressionHypothesisFromFile(String fileNameOutput, String fileNameCompress, String fileNameUncompress) throws IOException, ParseException {
        /*
         * @Func: produce the processing result of query for two input files.
         *        the time consumed will store in to fileNameOutput.
         * */
        PrintWriter writer = new PrintWriter(fileNameOutput);

        long start = System.currentTimeMillis();
        this.querySentenceWithFile(5, fileNameUncompress, false);
        writer.write(String.format("Query from uncompress: %f s\n", (System.currentTimeMillis() - start) / 1000.0));

        start = System.currentTimeMillis();
        this.querySentenceWithFile(5, fileNameCompress, true);
        writer.write(String.format("Query from compress: %f s\n", (System.currentTimeMillis() - start) / 1000.0));

        writer.close();
    }

    public void querySentenceWithFile(int k, String queryFileName, boolean compression) throws IOException, ParseException {
        /*
        * @Func: query sentence from existing file.
        * @Params: k: return top k doc id.
        *          queryFileName: the file contains query sentence
        *          compression: query by using compress or uncompress version binary file.
        * */

        Map<String, Lookup> lookupMap = this.index.readLookUpTable(compression);

        FileReader reader = new FileReader(queryFileName);
        Scanner scanner = new Scanner(reader);

        while (scanner.hasNextLine()) {
            String[] query = scanner.nextLine().split(" ");
            List<PostingList> postingLists = new ArrayList<>();
            for (String word: query) {
                postingLists.add(this.index.readATermFromDisk(word, lookupMap.get(word), compression));
            }

            List<Integer> topKDocs = this.documentAtATime(k, postingLists);
        }
    }

    public void querySentenceRandomQuery(int k, int sentenceLength, int numQuery, boolean compression) throws IOException, ParseException {
        /*
         * @Func: query random generated sentences.
         * @Params: k: return top k doc id.
         *          sentenceLength: the length of random sentence.
         *          numQuery: number of query will be generated.
         *          compression: query by using compress or uncompress version binary file.
         * */

        Map<String, Lookup> lookupMap = this.index.readLookUpTable(compression);
        List<String> termList = new ArrayList<>(lookupMap.keySet());
        int totalTerms = termList.size();

        for (int i = 0; i < numQuery; i++) {
            // get numVocab random number without duplicate
            Set<Integer> randNums = this.generateRandomNum(sentenceLength, totalTerms);

            List<PostingList> postingLists = new ArrayList<>();
            for (int randTermId: randNums) {
                String word = termList.get(randTermId);
                postingLists.add(this.index.readATermFromDisk(word, lookupMap.get(word), compression));
            }

            List<Integer> topkDocs = this.documentAtATime(k, postingLists);
        }
    }

    private List<Integer> documentAtATime(int k, List<PostingList> postingLists) {
        /*
        * @Func: implementation of document at a time.
        * @Params: k: return top k doc id.
        *          postingLists: a list of term related postingList.
        * @Return: a list of doc id.
        * */

        PriorityQueue<QueryScore> priorityQueue = new PriorityQueue<>();

        // find all documents id
        Set<Integer> docIds = new HashSet<>();
        for (PostingList postingList: postingLists) {
            docIds.addAll(postingList.getAllDocIds());
        }

        // document at a time
        for (int docId: docIds) {
            int score = 0;

            for (PostingList postingList: postingLists) {
                if (postingList.containsId(docId)) {
                    score += postingList.getOnePosting(docId).getDtf();
                }
            }

            priorityQueue.add(new QueryScore(docId, score));
        }

        // return top k result
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            if (!priorityQueue.isEmpty()) {
                result.add(priorityQueue.poll().getDocId());
            }
        }

        return result;
    }

    public void randomSelectVocabFreq(int numVocab, int rep, boolean compression) throws IOException, ParseException {
        /*
        * @Func: Randomly select 7 terms from the vocabulary. Record the selected terms, their term frequency and document frequency. Do this 100 times.
        * @Params: numVocab: number of vocabulary that we want to choose.
        *          rep: number of repetition.
        *          compression: query by using compress or uncompress version binary file.
        * */

        Map<String, Lookup> lookupMap = this.index.readLookUpTable(compression);

        List<String> termList = new ArrayList<>(lookupMap.keySet());
        Map<String, Integer> ctfMap = this.index.readCtf();
        int totalTerms = termList.size();

        PrintWriter writer = new PrintWriter("query/randomQueryFreq.txt", "UTF-8");
        writer.write(String.format("%30s %5s %5s\n", "Term", "Ctf", "Tdf"));

        for (int r = 0; r < rep; r++) {
            writer.write("Query " + (r+1) + "\n");

            // get numVocab random number without duplicate
            Set<Integer> randNums = this.generateRandomNum(numVocab, totalTerms);

            // 7 random terms
            for (int randTermId: randNums) {
                String term = termList.get(randTermId);
                PostingList postingList = this.index.readATermFromDisk(term, lookupMap.get(term), compression);
                int ctf = ctfMap.get(term);
                int tdf = postingList.getTdf();

                writer.write(String.format("%30s %5d %5d\n", term, ctf, tdf));
            }
        }

        writer.close();
    }

    public void randomSelectVocabCoef(int numVocab, int rep, boolean compression) throws IOException, ParseException {
        /*
        * @Func: Using Dice's coefficient (see section 6.2.1 and page 201), identify the highest scoring two word phrase for each of the 7 terms in your set of 100.
        * @Params: numVocab: number of vocabulary that we want to choose.
        *          rep: number of repetition.
        *          compression: query by using compress or uncompress version binary file.
        * */

        Map<String, Lookup> lookupMap = this.index.readLookUpTable(compression);

        List<String> termList = new ArrayList<>(lookupMap.keySet());
        int totalTerms = termList.size();

        String fileOneName = String.format("query/randomQuery1%s.txt", (compression ? "Compress" : "Uncompress"));
        String fileTwoName = String.format("query/randomQuery2%s.txt", (compression ? "Compress" : "Uncompress"));

        PrintWriter writerSevenTerms = new PrintWriter(fileOneName, "UTF-8");
        PrintWriter writerFourteenTerms = new PrintWriter(fileTwoName, "UTF-8");

        for (int r = 0; r < rep; r++) {

            System.out.println(r+1);

            // get numVocab random number without duplicate
            Set<Integer> randNums = this.generateRandomNum(numVocab, totalTerms);

            // 7 random terms
            for (int randTermId: randNums) {
                String term1 = termList.get(randTermId);
                String term2 = "";
                float highestScore = 0F;

                PostingList postingList1 = this.index.readATermFromDisk(term1, lookupMap.get(term1), compression);

                // find the highest score among all terms
                for (String term2Candidate: lookupMap.keySet()) {
                    if (!term1.equals(term2Candidate)) {
                        PostingList postingList2 = this.index.readATermFromDisk(term2Candidate, lookupMap.get(term2Candidate), compression);

                        float score = this.diceCoefficient(postingList1, postingList2);
                        if (highestScore < score) {
                            term2 = term2Candidate;
                            highestScore = score;
                        }
                    }
                }

                writerSevenTerms.write(term1 + " ");
                writerFourteenTerms.write(term1 + " " + term2 + " ");
            }

            writerSevenTerms.write("\n");
            writerFourteenTerms.write("\n");
        }

        writerSevenTerms.close();
        writerFourteenTerms.close();
    }

    private float diceCoefficient(PostingList postingList1, PostingList postingList2) {
        /*
         * @Func: calculate the dice coefficient between term1 and term2.
         *        (2 * n12) / (n1 + n2),
         *        n1: number of document contain word1
         *        n2: number of document contain word2
         *        n12: number of document contain both word1, and word1 is followed by word2
         * @Params: postingList1 and postingList2: the postingList of the term we will compute the dice coefficient.
         * */
        float n1 = postingList1.getTdf();
        float n2 = postingList2.getTdf();

        Set<Integer> intersection = new HashSet<>(postingList1.getAllDocIds());
        intersection.retainAll(postingList2.getAllDocIds());

        float n12 = 0F;
        for (int docId: intersection) {
            for (int pos1: postingList1.getOnePosting(docId).getPositions()) {
                for (int pos2: postingList2.getOnePosting(docId).getPositions()) {
                    if (pos2 - pos1 == 1) {
                        n12 += 1;
                    }
                }
            }
        }

        return (2 * n12) / (n1 + n2);
    }




    /* Utils */
    private Set<Integer> generateRandomNum(int k, int range) {
        /*
        * @Func: generate the rangome k number without duplicate.
        * @Params: k: number of random number
        *          range: the range of random integer
        * @Return: a set of random number
        * */
        Random random = new Random();

        Set<Integer> randNums = new HashSet<>();
        while (randNums.size() < k) {
            int num = random.nextInt(range);
            if (!randNums.contains(num)) {
                randNums.add(num);
            }
        }

        return randNums;
    }






    /* Inner class */
    class QueryScore implements Comparable<QueryScore> {

        private int docId;
        private int score;

        /* Constructor */
        public QueryScore(int docId, int score) {
            this.docId = docId;
            this.score = score;
        }

        /* Getter */
        public int getDocId() {
            return this.docId;
        }

        public int getScore() {
            return this.score;
        }


        /* Comparator */
        @Override
        public int compareTo(QueryScore other) {
            return this.score - other.score;
        }
    }

}
