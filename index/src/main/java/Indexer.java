import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class Indexer {
    /*

     ctf = term freq (collection term frequency)
     tdf = num doc per term
     dtf = term freq per doc

    */



    // internal document id
    private int curDocId = 0;

    // statistic variable
    private int totalSceneLength = 0;
    private float avgSceneLength = 0f;

    private String shortestSceneId;
    private int shortestSceneSize = Integer.MAX_VALUE;

    private String longestSceneId;
    private int longestSceneSize = Integer.MIN_VALUE;

    private Map<String, Integer> playIdTextLength = new HashMap<>();

    // inverted list related
    private Map<String, Lookup> lookUpMap = new HashMap<>(); // term: Lookup
    private Map<String, PostingList> invertedList = new HashMap<>(); // term: PostingList
    private Map<String, Integer> ctf = new HashMap<>();
    private Map<Integer, String> docIdToDocMeta = new HashMap<>();




    /* Constructor */
    public Indexer() {

    }




    /* Methods: inverted list creation related */
    public void createInvList(String dataFileName) throws IOException, ParseException {
        /*
        * @Func: create inverted list from json file.
        * */

        // reader json file
        FileReader reader = new FileReader(dataFileName);
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = (JSONObject) jsonParser.parse(reader);
        JSONArray scenes = (JSONArray) obj.get("corpus");


        // iterate all scenes
        for (Object scene_obj: scenes) {
            this.curDocId += 1;
            JSONObject scene = (JSONObject) scene_obj;

            // extract components
            String playId = (String) scene.get("playId");
            String sceneId = (String) scene.get("sceneId");
            String docMeta = playId + "|" + sceneId;
            long sceneNum = (long) scene.get("sceneNum");
            String[] terms = ((String)scene.get("text")).split("\\s+");


            // statistic
            this.calcStatistic(terms.length, sceneId, playId);


            // iterate all terms
            for (int i = 0; i < terms.length; i++) {
                String term = terms[i];

                // if the current term is the first occurrence
                if (!this.invertedList.containsKey(term)) {
                    this.invertedList.put(term, new PostingList());
                }

                // add scene
                this.invertedList.get(term).addDocIDPos(this.curDocId, i);

                // update ctf
                this.ctf.put(term, this.ctf.getOrDefault(term, 0) + 1);

                // update the lookup map of converting doc id to doc meta
                this.docIdToDocMeta.put(this.curDocId, docMeta);
            }
        }

        // get average scene_length
        this.avgSceneLength = this.totalSceneLength / scenes.size();
    }


    private void calcStatistic(int textLength, String sceneId, String playId) {
        /*
        * @Func: calculate the statistic of the inverted list.
        * */

        this.totalSceneLength += textLength;

        if (this.shortestSceneSize > textLength) {
            this.shortestSceneSize = textLength;
            this.shortestSceneId = sceneId;
        }

        if (this.longestSceneSize < textLength) {
            this.longestSceneSize = textLength;
            this.longestSceneId = sceneId;
        }

        this.playIdTextLength.put(playId, this.playIdTextLength.getOrDefault(playId, 0) + textLength);
    }






    /* Methods: write operations related */
    public void writeToDisk(boolean compression) throws FileNotFoundException, IOException {
        /*
        * @Func: write the complete inverted list to the disk.
        * */

        File diskDir = new File("disk");
        File lookupDir = new File("lookup");

        if (!diskDir.exists()) {
            diskDir.mkdir();
        }

        if (!lookupDir.exists()) {
            lookupDir.mkdir();
        }

        String binaryFileName = compression ? "disk/binaryFileCompress" : "disk/binaryFileUncompress";
        String lookupFileName = compression ? "lookup/lookupCompress.json" : "lookup/lookupUncompress.json";

        // write the inverted list to binary file
        RandomAccessFile writer = new RandomAccessFile(binaryFileName, "rw");

        long offset = writer.getFilePointer();

        for (String term: this.invertedList.keySet()) {
            PostingList postingList = this.invertedList.get(term);

            if (compression) {
                this.writeCompressPostings(writer, postingList);
            }
            else {
                this.writeUncompressPostings(writer, postingList);
            }

            // assign offset of the current term and other information
            Lookup lookup = new Lookup(offset, (int)(writer.getFilePointer() - offset), this.ctf.get(term), postingList.getTdf());
            this.lookUpMap.put(term, lookup);

            // update the offset
            offset = writer.getFilePointer();
        }

        writer.close();

        // write lookup table to json file
        this.writeLookupTable(lookupFileName);

        // write ctf to json file
        this.writeCtf();

        // write docIdToDocMeta to json file
        this.writeDocIdToDocMeta();

        // write statistic information to txt file
        this.writeStatistic();
    }

    private void writeUncompressPostings(RandomAccessFile writer, PostingList postingList) throws IOException {
        /*
        * @Func: uncompressed version, writing a list of postings to the disk.
        * @Params: writer: writer
        *          postingList: the postingList that will be written into disk.
        * */

        Map<Integer, Posting> postings = postingList.getPostings();

        for (int docId: postings.keySet()) {
            List<Integer> positions = postings.get(docId).getPositions();
            int[] data = new int[positions.size() + 2];

            data[0] = docId;
            data[1] = postings.get(docId).getDtf();

            for (int i = 0; i < positions.size(); i++) {
                data[i + 2] = positions.get(i);
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);
            byte[] array = byteBuffer.array();
            writer.write(array);
        }
    }

    private void writeCompressPostings(RandomAccessFile writer, PostingList postingList) throws IOException {
        /*
        * @Func: compressed version, write a list of postings to the disk.
        * @Params: writer: writer
        *          postingList: the postingList that will be written into disk.
        * */

        Map<Integer, Posting> postings = postingList.getPostings();

        for (int docId: postings.keySet()) {
            List<Integer> positions = postings.get(docId).getPositions();
            int[] data = new int[positions.size() + 2];

            data[0] = docId;
            data[1] = postings.get(docId).getDtf();

            // delta encoding
            int[] positionDelta = Encoder.deltaEncoding(positions);

            for (int i = 0; i < positionDelta.length; i++) {
                data[i + 2] = positionDelta[i];
            }

            // vbyte encoding
            byte[] array = Encoder.vByteEncoding(data);

            writer.write(array);
        }
    }

    private void writeLookupTable(String fileName) throws IOException {
        /*
        * @Func: write the lookup table to "lookup.json" file
        * @Params: fileName: output file name
        * */

        JSONObject lookUpTable = new JSONObject();

        for (String term: this.lookUpMap.keySet()) {
            JSONObject look = new JSONObject();
            look.put("offset", this.lookUpMap.get(term).getOffset());
            look.put("buffLength", this.lookUpMap.get(term).getBuffLength());
            look.put("ctf", this.lookUpMap.get(term).getCtf());
            look.put("tdf", this.lookUpMap.get(term).getTdf());

            lookUpTable.put(term, look);
        }

        FileWriter writer = new FileWriter(fileName);

        writer.write(lookUpTable.toJSONString());
        writer.close();
    }

    private void writeCtf() throws IOException {
        /*
        * @Func: write ctf to "ctd.json" file.
        * */

        JSONObject ctfMap = new JSONObject();

        for (String term: this.ctf.keySet()) {
            ctfMap.put(term, this.ctf.get(term));
        }

        FileWriter writer = new FileWriter("lookup/ctf.json");

        writer.write(ctfMap.toJSONString());
        writer.close();
    }

    private void writeDocIdToDocMeta() throws IOException {
        /*
         * @Func: write docIdToDocMeta to "idToMeta.json".
         * */

        JSONObject docIdToDocMetaMap = new JSONObject();

        for (int docId: this.docIdToDocMeta.keySet()) {
            docIdToDocMetaMap.put(docId, this.docIdToDocMeta.get(docId));
        }

        FileWriter writer = new FileWriter("lookup/idToMeta.json");

        writer.write(docIdToDocMetaMap.toJSONString());
        writer.close();
    }

    private void writeStatistic() throws IOException {
        /*
        * @Func: write statistic information to "stat.json" file.
        * */

        FileWriter writer = new FileWriter("stat.txt");

        writer.write("Average length of a scene: " + this.avgSceneLength + "\n");
        writer.write("Shortest scene: " + this.shortestSceneId + " with size " + this.shortestSceneSize + "\n");


        int shortestPlayIdSize = Integer.MAX_VALUE;
        String shortestPlayId = "";
        int longestPlayIdSize = Integer.MIN_VALUE;
        String longestPlayId = "";

        for (String playId: this.playIdTextLength.keySet()) {
            if (this.playIdTextLength.get(playId) <  shortestPlayIdSize) {
                shortestPlayIdSize = this.playIdTextLength.get(playId);
                shortestPlayId = playId;
            }

            if (longestPlayIdSize < this.playIdTextLength.get(playId)) {
                longestPlayIdSize = this.playIdTextLength.get(playId);
                longestPlayId = playId;
            }
        }

        writer.write("Longest play: " + longestPlayId + " with size " + longestPlayIdSize + "\n");
        writer.write("Shortest play: " + shortestPlayId + " with size " + shortestPlayIdSize + "\n");

        writer.close();
    }








    /* Methods: read operations related */
    public PostingList readATermFromDisk(String term, Lookup lookup, boolean compression) throws IOException, ParseException {
        /*
        * @Func: get a list of posting for the specific term from disk.
        * @Params: term: the term we want to read from the disk
        *          lookup: information of the term.
        *          compression: query by using compress or uncompress version binary file.
        * @Return: a postingList of the term
        * */

        String binaryFileName = compression ? "disk/binaryFileCompress" : "disk/binaryFileUncompress";

        RandomAccessFile reader = new RandomAccessFile(binaryFileName, "rw");

        // get the beginning disk pointer of the query term and other information about this term
        long offset = lookup.getOffset();
        int buffLength = lookup.getBuffLength();
        int ctf = lookup.getCtf();
        int tdf = lookup.getTdf();

        byte[] buffer = new byte[buffLength];

        reader.seek(offset);
        reader.read(buffer, 0, buffLength);

        PostingList postingList;
        if (compression) {
            postingList = this.readCompressPostings(buffer);
        }
        else {
            postingList = this.readUncompressPostings(buffer, buffLength);
        }

        reader.close();

        return postingList;
    }

    private PostingList readUncompressPostings(byte[] buffer, int buffLength) {
        /*
        * @Func: uncompressed version, get a list of postings.
        * @Params: buffer: the byte array of a specific postingList
        *          buffLength: the length of the buffer
        * @Return: a postingList of a term
        * */
        PostingList postingList = new PostingList();
        int off = 0;

        while (off < buffLength) {
            int docId = this.fromByteArray(Arrays.copyOfRange(buffer, off, off + 4));
            off += 4;

            int dtf = this.fromByteArray(Arrays.copyOfRange(buffer, off, off + 4));
            off += 4;

            Posting post = new Posting();
            for (int i = 0; i < dtf; i++) {
                post.addPosition(this.fromByteArray(Arrays.copyOfRange(buffer, off, off + 4)));
                off += 4;
            }

            postingList.addPost(docId, post);
        }

        return postingList;
    }

    private PostingList readCompressPostings(byte[] compressedBuffer) {
        /*
        * @Func: compressed version, get a list of postings.
        * @Params: compressedBuffer: the compressed byte array.
        * @Return: a PostingList of a term.
        * */

        // vbyte decoding
        byte[] deltaBuffer = Encoder.vByteDecoding(compressedBuffer);

        PostingList postingList = new PostingList();
        int buffLength = deltaBuffer.length;
        int off = 0;

        while (off < buffLength) {
            int docId = this.fromByteArray(Arrays.copyOfRange(deltaBuffer, off, off + 4));
            off += 4;

            int dtf = this.fromByteArray(Arrays.copyOfRange(deltaBuffer, off, off + 4));
            off += 4;

            int[] deltaPositions = new int[dtf];
            for (int i = 0; i < dtf; i++) {
                deltaPositions[i] = this.fromByteArray(Arrays.copyOfRange(deltaBuffer, off, off + 4));
                off += 4;
            }

            // delta decoding
            postingList.addPost(docId, new Posting(Encoder.deltaDecoding(deltaPositions)));
        }

        return postingList;
    }

    public Map<String, Lookup> readLookUpTable(boolean compression) throws IOException, ParseException {
        /*
        * @Func: get the look up table from "lookup.json" file.
        * @Params: compression: read the compress of uncompress version of lookupMap
        * @Return: a lookup Map.
        * */

        // read the lookup table from "lookup.json" file and return it

        String lookupFileName = compression ? "lookup/lookupCompress.json" : "lookup/lookupUncompress.json";

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(lookupFileName);
        JSONObject lookup_json = (JSONObject) jsonParser.parse(reader);

        Map<String, Lookup> lookupMap = new HashMap<>();
        for (Object term_obj: lookup_json.keySet()) {
            JSONObject lookup_obj = (JSONObject) lookup_json.get(term_obj);

            String term = (String) term_obj;
            long offset = (long) lookup_obj.get("offset");
            long buffLength = (long) lookup_obj.get("buffLength");
            long ctf = (long) lookup_obj.get("ctf");
            long tdf = (long) lookup_obj.get("tdf");

            Lookup lookup = new Lookup(offset, (int) buffLength, (int) ctf, (int) tdf);
            lookupMap.put(term, lookup);
        }

        return lookupMap;
    }

    public Map<String, Integer> readCtf() throws IOException, ParseException {
        /*
        * @Func: read ctf information from "ctf.json" file.
        * @Return: a ctf map.
        * */
        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader("lookup/ctf.json");
        JSONObject ctf_json = (JSONObject) jsonParser.parse(reader);

        Map<String, Integer> new_ctf = new HashMap<>();
        for (Object term_obj: ctf_json.keySet()) {
            String term = (String) term_obj;
            long val = (long) ctf_json.get(term_obj);
            new_ctf.put(term, (int) val);
        }

        return new_ctf;
    }






    /* Sanity check */
    public void sanityCheck(String dataFileName, boolean compression) throws IOException, ParseException {
        /*
        * @Func: get the complete inverted list and compare all the information with the original inverted list.
        * */

        // create the original inverted list
        this.createInvList(dataFileName);

        // write the original inverted list to the disk
        this.writeToDisk(compression);

        // read lookup map
        Map<String, Lookup> lookupMap = this.readLookUpTable(compression);

        for (String term: lookupMap.keySet()) {
            PostingList postingList = this.readATermFromDisk(term, lookupMap.get(term), compression);
            if (!this.invertedList.get(term).equals(postingList)) {
                System.out.println("Term " + term + " is inconsistent!");
            }
        }

        System.out.println("Consistent!");
    }

    public void sanityCheckTwoIndexes() throws IOException, ParseException {
        /*
        * @Func: sanity check between compress and uncompress file.
        * */

        Map<String, Lookup> lookupMapCompress = this.readLookUpTable(true);
        Map<String, Lookup> lookupMapUncompress = this.readLookUpTable(false);

        if (lookupMapCompress.size() != lookupMapUncompress.size()) {
            System.out.println("Term size inconsistent!");
            return;
        }

        for (String term: lookupMapCompress.keySet()) {
            if (!lookupMapUncompress.containsKey(term)) {
                System.out.println("Term \"" + term + "\" does not exists in uncompress file!");
                continue;
            }

            PostingList compress = this.readATermFromDisk(term, lookupMapCompress.get(term), true);
            PostingList uncompress = this.readATermFromDisk(term, lookupMapUncompress.get(term), false);

            if (!compress.equals(uncompress)) {
                System.out.println("Term " + term + " is inconsistent!");
            }
        }

        System.out.println("Consistent!");
    }





    /* Utils */
    private int fromByteArray(byte[] bytes) {
        /*
        * @Func: Convert byte array to int.
        * */
        return ByteBuffer.wrap(bytes).getInt();
    }
}
















