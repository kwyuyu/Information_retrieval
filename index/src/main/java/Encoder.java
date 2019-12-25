import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Encoder {

    /* Constructor */
    public Encoder() {

    }


    /* Methods: delta encoding */
    public static int[] deltaEncoding(List<Integer> intList) {
        int[] output = new int[intList.size()];
        output[0] = intList.get(0);

        for (int i = 1; i < intList.size(); i++) {
            output[i] = intList.get(i) - intList.get(i-1);
        }

        return output;
    }

    public static List<Integer> deltaDecoding(int[] encoded) {
        List<Integer> output = new ArrayList<>();
        output.add(encoded[0]);

        for (int i = 1; i < encoded.length; i++) {
            output.add(output.get(i-1) + encoded[i]);
        }

        return output;
    }




    /* Methods: v-byte encoding */
    public static byte[] vByteEncoding(int[] input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int i: input) {
            while (i >= 128) { // 10000000
                output.write((byte) (i & 0x7F)); // 01111111
                i >>>= 7;  // logical shift, no sign bit extension
            }

            output.write((byte) (i | 0x80));
        }

        return output.toByteArray();
    }

    public static byte[] vByteDecoding(byte[] input) {
        List<Integer> output = new ArrayList<>();

        for (int i = 0; i < input.length; i++) {
            int position = 0;
            int result = (int)input[i] & 0x7f;

            while ((input[i] & 0x80) == 0) {
                i += 1;
                position += 1;
                int unsignedByte = (int)input[i] & 0x7f;
                result |= (unsignedByte << (7 * position));
            }

            output.add(result);
        }

        int[] data = new int[output.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = output.get(i);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(data);
        return byteBuffer.array();
    }
}












