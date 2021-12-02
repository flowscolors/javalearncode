/**
 * @author flowscolors
 * @date 2021-10-25 10:32
 */
public class StringDemo {
    public static void main(String[] args) {
        String str1 = "kube-system";
        String str2 = "kube-system";
        System.out.println("str1 hashcode： "+str1.hashCode());
        System.out.println("str2 hashcode： "+str2.hashCode());

        String str3 = new String("kube-system");
        String str4 = new String("kube-system");
        System.out.println("str3 hashcode： "+str3.hashCode());
        System.out.println("str4 hashcode： "+str4.hashCode());
    }

}
