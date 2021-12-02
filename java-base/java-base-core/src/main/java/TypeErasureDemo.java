import java.util.ArrayList;
import java.util.List;

/**
 * @author flowscolors
 * @date 2021-10-05 15:08
 */
public class TypeErasureDemo {
    public static void main(String[] args) {
        List list = new ArrayList();
        List listString = new ArrayList<String>();
        List listInteger = new ArrayList<Integer>();

        try {
            list.getClass().getMethod("add", Object.class).invoke(list, 1);
            listString.getClass().getMethod("add", Object.class).invoke(listString, 1);
            listInteger.getClass().getMethod("add", Object.class).invoke(listInteger, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("list size:" + list.size());
        System.out.println("listString size:" + listString.size());
        System.out.println("listInteger size:" + listInteger.size());
    }
}
