import java.util.HashMap;
import java.util.Map;

/**
 * @author flowscolors
 * @date 2021-10-05 14:52
 */
public class HashMapTest {
    public static void main(String[] args) {
        Map<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("Aa", "Aa");
        hashMap.put("BB", "BB");
        hashMap.put("AaAa", "AaAa");
        hashMap.put("AaBB", "AaBB");
        hashMap.put("BBAa", "BBAa");
        hashMap.put("BBBB", "BBBB");
        hashMap.put("AaAaAa", "AaAaAa");
        hashMap.put("AaAaBB", "AaAaBB");
        hashMap.put("AaBBAa", "AaBBAa");
        hashMap.put("AaBBBB", "AaBBBB");
        hashMap.put("BBAaAa", "BBAaAa");
        hashMap.put("BBAaBB", "BBAaBB");
        hashMap.put("BBBBAa", "BBBBAa");
        hashMap.put("BBBBBB", "BBBBBB");
    }
}
