import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author flowscolors
 * @date 2021-12-01 18:14
 */
public class Stream02Demo {
    public static void main(String[] args) {
        List<Integer> aList = Arrays.asList(1,2,13,4);
        List<Integer> bList = Arrays.asList(1,9,18,4);
        List<Integer> aLambdaList = new ArrayList<Integer>(Arrays.asList(1,2,13,4));
        List<Integer> bLambdaList = new ArrayList<Integer>(Arrays.asList(1,9,18,4));

        System.out.println("ListA 数组为 ： " + aList.toString());
        System.out.println("ListB 数组为 ： " + bList.toString());

        System.out.println("[Java 7]: 求交集: " + listMixUse7(new ArrayList<Integer>(aList),new ArrayList<Integer>(bList)).toString());
        System.out.println("[Java 8]: 求交集: " + aLambdaList.stream().filter(a -> bLambdaList.contains(a)).collect(Collectors.toList()).toString());

        System.out.println("[Java 7]: 求并集，有重复值: " + listUnionUse7(new ArrayList<Integer>(aList),new ArrayList<Integer>(bList)).toString());
        System.out.println("[Java 8]: 求并集，有重复值: " + Stream.concat(aLambdaList.stream(),bLambdaList.stream()).collect(Collectors.toList()).toString());

        System.out.println("[Java 7]: 求并集，无重复值: " + listUnionNoDoubleUse7(new ArrayList<Integer>(aList),new ArrayList<Integer>(bList)).toString());
        System.out.println("[Java 8]: 求并集，无重复值: " + Stream.concat(aLambdaList.stream(),bLambdaList.stream()).distinct().collect(Collectors.toList()).toString());

        System.out.println("[Java 7]: 求差集: " + listSubUse7(new ArrayList<Integer>(aList),new ArrayList<Integer>(bList)).toString());
        System.out.println("[Java 8]: 求差集: " + aLambdaList.stream().filter(a -> !bLambdaList.contains(a)).collect(Collectors.toList()).toString());
    }

    private static List<Integer> listMixUse7 (List<Integer> a,List<Integer> b){
        a.retainAll(b);
        return a;
    }

    private static List<Integer> listUnionUse7 (List<Integer> a,List<Integer> b){
        a.addAll(b);
        return a;
    }

    private static List<Integer> listUnionNoDoubleUse7 (List<Integer> a,List<Integer> b){
        b.removeAll(a);
        a.addAll(b);
        return a;
    }

    private static List<Integer> listSubUse7 (List<Integer> a,List<Integer> b){
        a.removeAll(b);
        return a;
    }
}
