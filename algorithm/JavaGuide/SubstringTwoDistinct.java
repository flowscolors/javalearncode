import java.util.HashSet;

/**
 * @author flowscolors
 * @date 2021-10-03 19:47
 */

//Leetcode 159 找至多两个最长字串的长度
//获取每2个字符组成的字符串的长度的最大值，每次和max比较，最大则替换
public class SubstringTwoDistinct {
    public static int solution (String s){
        HashSet<Character> hashSet = new HashSet<>(2);
        int left=0,right=0,max=0;
        StringBuffer sb = new StringBuffer();
        for(int i = 0;i<s.length()-1;i++){
            if(hashSet.size()<2 ){
                hashSet.add(s.charAt(i));
                right++;
                sb.append(s.charAt(i));
            }else if(hashSet.size()==2  &&  hashSet.contains(s.charAt(i))){
                right++;
                sb.append(s.charAt(i));
            }else{
                right++;
                left = s.split(sb.toString(),1)[0].length();

            }
            max = right-left > max ? right-left : max;
        }
        return  max;
    }

    public static void main(String[] args) {
        System.out.println(solution("ccaabbb"));
    }
}
