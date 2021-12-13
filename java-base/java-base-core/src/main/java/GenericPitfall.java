import java.util.ArrayList;
import java.util.List;

/**
 * @author flowscolors
 * @date 2021-12-12 13:36
 */
public class GenericPitfall {
    public static void main(String[] args) {
        List list = new ArrayList();
        list.add("123");
        List<Integer> list2 = list;
        System.out.println(list2.get(0).intValue());
    }
}
