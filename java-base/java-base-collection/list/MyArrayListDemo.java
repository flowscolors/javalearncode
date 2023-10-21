import java.util.List;


/**
 * @author flowscolors
 * @date 2021-12-10 13:29
 */
//模拟扩容时的数组冲突，该扩容时没扩容，导致最后的一个加到数组中越界了
public class MyArrayListDemo {
    public static void main(String[] args) {
        List<Integer> arrayList = new MyArrayList<>();
        for (int i = 0; i < 9; i++) {
            arrayList.add(i);
        }
        new Thread(() -> arrayList.add(9), "My1").start();
        new Thread(() -> arrayList.add(10), "My2").start();
        System.out.println("arrayList = " + arrayList + ",size=" + arrayList.size());
    }
}
