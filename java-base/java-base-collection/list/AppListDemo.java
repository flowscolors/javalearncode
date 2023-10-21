/**
 * @author flowscolors
 * @date 2021-10-04 15:33
 */
public class AppListDemo {
    public static void main( String[] args )
    {

        ArrayListTest.addFromHeaderTest(100000);
        LinkedListTest.addFromHeaderTest(100000);
        System.out.println(" ");

        ArrayListTest.addFromMidTest(10000);
        LinkedListTest.addFromMidTest(10000);
        System.out.println(" ");

        ArrayListTest.addFromTailTest(1000000);
        LinkedListTest.addFromTailTest(1000000);
        System.out.println(" ");

        ArrayListTest.deleteFromHeaderTest(100000);
        LinkedListTest.deleteFromHeaderTest(100000);
        System.out.println(" ");

        ArrayListTest.deleteFromMidTest(100000);
        LinkedListTest.deleteFromMidTest(100000);
        System.out.println(" ");

        ArrayListTest.deleteFromTailTest(1000000);
        LinkedListTest.deleteFromTailTest(1000000);
        System.out.println(" ");

        ArrayListTest.getByForTest(10000);
        LinkedListTest.getByForTest(10000);
        System.out.println(" ");

        ArrayListTest.getByIteratorTest(100000);
        LinkedListTest.getByIteratorTest(100000);
        System.out.println(" ");
    }
}
