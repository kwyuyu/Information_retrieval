import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Assignment1 {

    public static void parser(Map<String, String> argparse) throws IOException, ParseException {
        Indexer index = null;

        if (argparse.containsKey("-build")) {
            if (!argparse.containsKey("-file")) {
                throw new RuntimeException("-file: file name is needed");
            }
            if (!argparse.containsKey("-com")) {
                throw new RuntimeException("-com: compression argument is needed");
            }

            boolean compression = argparse.get("-com").equals("true");

            index = new Indexer();
            index.createInvList(argparse.get("-file"));
            index.writeToDisk(compression);
        }

        if (index == null) {
            index = new Indexer();
        }

        if (argparse.containsKey("-sanity")) {
            if (argparse.get("-sanity").equals("two")) {
                // compare compress and uncompress
                index.sanityCheckTwoIndexes();
            }
            else {
                // compare original inverted list and the one in the disk
                if (!argparse.containsKey("-com")) {
                    throw new RuntimeException("-com: compression argument is needed");
                }
                if (!argparse.containsKey("-file")) {
                    throw new RuntimeException("-file: file name is needed");
                }

                boolean compression = argparse.get("-com").equals("true");

                index.sanityCheck(argparse.get("-file"), compression);
            }
        }



        Query query = new Query(index);

        if (argparse.containsKey("-query")) {
            int testNum = Integer.parseInt(argparse.get("-query"));
            if (testNum == 1) {
                // Randomly select 7 terms from the vocabulary. Record the selected terms, their term frequency and document frequency. Do this 100 times.
                if (!argparse.containsKey("-numV")) {
                    throw new RuntimeException("-numV: number of vocabulary per query is needed");
                }
                if (!argparse.containsKey("-numQ")) {
                    throw new RuntimeException("-numQ: number of query is needed");
                }

                int numV = Integer.parseInt(argparse.get("-numV"));
                int numQ = Integer.parseInt(argparse.get("-numQ"));
                boolean compression = argparse.containsKey("-com") && argparse.get("-com").equals("true");

                query.randomSelectVocabFreq(numV, numQ, compression);
            }
            else if (testNum == 2) {
                // Using Dice's coefficient (see section 6.2.1 and page 201), identify the highest scoring two word phrase for each of the 7 terms in your set of 100.
                if (!argparse.containsKey("-numV")) {
                    throw new RuntimeException("-numV: number of vocabulary per query is needed");
                }
                if (!argparse.containsKey("-numQ")) {
                    throw new RuntimeException("-numQ: number of query is needed");
                }

                int numV = Integer.parseInt(argparse.get("-numV"));
                int numQ = Integer.parseInt(argparse.get("-numQ"));
                boolean compression = argparse.containsKey("-com") && argparse.get("-com").equals("true");

                query.randomSelectVocabCoef(numV, numQ, compression);
            }
            else if (testNum == 3) {
                // query sentence from existing file.
                if (!argparse.containsKey("-k")) {
                    throw new RuntimeException("-k: need to define top k result of the query");
                }
                if (!argparse.containsKey("-file")) {
                    throw new RuntimeException("-file: query file name is needed");
                }

                int k = Integer.parseInt(argparse.get("-k"));
                String fileName = argparse.get("-file");
                boolean compression = argparse.containsKey("-com") && argparse.get("-com").equals("true");

                query.querySentenceWithFile(k, fileName, compression);
            }
            else if (testNum == 4) {
                // query random generated sentences.
                if (!argparse.containsKey("-k")) {
                    throw new RuntimeException("-k: need to define top k result of the query");
                }
                if (!argparse.containsKey("-numV")) {
                    throw new RuntimeException("-numV: number of vocabulary per query is needed");
                }
                if (!argparse.containsKey("-numQ")) {
                    throw new RuntimeException("-numQ: number of query is needed");
                }

                int k = Integer.parseInt(argparse.get("-k"));
                int numV = Integer.parseInt(argparse.get("-numV"));
                int numQ = Integer.parseInt(argparse.get("-numQ"));
                boolean compression = argparse.containsKey("-com") && argparse.get("-com").equals("true");

                query.querySentenceRandomQuery(k, numV, numQ, compression);
            }
            else if (testNum == 5) {
                query.compressionHypothesisFromFile("query/compHyp7Terms.txt", "query/randomQuery1Compress.txt", "query/randomQuery1Uncompress.txt");
                query.compressionHypothesisFromFile("query/compHyp14Terms.txt", "query/randomQuery2Compress.txt", "query/randomQuery2Uncompress.txt");
            }
            else if (testNum == 6) {
                // test compression hypothesis with multiple times of querySentenceRandomQuery
                query.compressionHypothesisRandom();

                // execute python plotting script
                Process p = Runtime.getRuntime().exec("python3 plotChart.py");
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        /*
        * @Args: -build <no_arg>: create inverted list and write it into disk
        *        -com <boolean>: compression option, true or false
        *        -sanity <one or two>: sanity check between compress and uncompress (two) or between original inverted list and the one in the disk (one)
        *        -query <int>: query number
        *        -file <String>: file name
        *        -numV <int>: number of vocabulary
        *        -numQ <int>: number of query
        *        -k <int>: top k result
        * */

        Map<String, String> argparse = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-build")) {
                argparse.put(args[i], "b");
            }
            else {
                argparse.put(args[i], args[i+1]);
                i += 1;
            }
        }

        parser(argparse);



//        Indexer index = new Indexer();
//        index.createInvList("shakespeare-scenes.json");
//        index.writeToDisk();
//
//        Query query = new Query(index);
//
//        query.randomSelectVocabCoef(7, 100, true);
//        query.randomSelectVocabFreq(7, 100, false);
//        query.querySentenceWithFile(5, "randomQuery2Uncompress.txt", false);
//        query.querySentenceRandomQuery(5, 7, 10000, false);
//        query.compressionHypothesisRandom();




    }
}
