import org.apache.hadoop.io.Text;

import java.util.ArrayList;

public class Utils2 {

    static ArrayList<String> convert(Iterable<Text> input) {
        ArrayList<String> result = new ArrayList<>();
        for (Text t : input) {
            result.add(t.toString());
        }
        return result;
    }

    static <T> ArrayList<T> convertGeneric(Iterable<T> input) {
        ArrayList<T> result = new ArrayList<>();
        for (T t : input) {
            result.add(t);
        }
        return result;
    }
}
