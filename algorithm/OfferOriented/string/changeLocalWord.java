package string;

/**
 * @author flowscolors
 * @date 2022-02-16 14:21
 */
public class changeLocalWord {
    public static boolean solution(String s1,String s2){
        if(s1.length() > s2.length()){
            return solution(s2,s1);
        }
        int[] counts = new int[26];
        for(int i = 0;i<s1.length();i++){
            counts[s1.charAt(i)-'a']++;
        }
        for(int i = 0;i<s2.length();i++){
            counts[s2.charAt(i)-'a']--;
        }
        for(int i = 0;i<counts.length;i++){
            if(counts[i] > 0){
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) {
        String s1 = "ac";
        String s2 = "dcgaf";
        System.out.println(solution(s1,s2));

    }
}
