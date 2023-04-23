import org.apache.hadoop.io.Text;

import java.util.ArrayList;

public class Utils {

    static ArrayList<String> convert(Iterable<Text> input) {
        ArrayList<String> result = new ArrayList<>();
        for (Text t : input) {
            result.add(t.toString());
        }
        return result;
    }
}
