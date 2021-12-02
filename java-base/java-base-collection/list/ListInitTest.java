import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author flowscolors
 * @date 2021-10-04 16:17
 */
public class ListInitTest {
    public static void main(String[] args) {
        //List初始化大小
        LinkedList linkedList = new LinkedList();
        System.out.println("LinkedList初始化大小： "+linkedList.size());
        linkedList.add("a");
        System.out.println("LinkedList插入1大小： "+linkedList.size());
        linkedList.add("b");
        System.out.println("LinkedList插入2大小： "+linkedList.size());

        //wait check 为什么ArrayList初始化时候不是10？
        ArrayList arrayList = new ArrayList();
        System.out.println("ArrayList初始化大小： "+arrayList.size());
        arrayList.add("a");
        System.out.println("ArrayList插入1大小： "+arrayList.size());
        arrayList.add("b");
        System.out.println("ArrayList插入2大小： "+arrayList.size());
    }
}
