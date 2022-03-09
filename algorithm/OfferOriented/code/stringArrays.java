package code;

import java.util.*;

/**
 * @author flowscolors
 * @date 2022-03-01 20:45
 */
public class stringArrays {
    //评测题目1: 将一个字符串数组进行分组输出，每组中的字符串都由相同字符组成。举个例子，
    // 输入:["eat","tea","tan","ate","nat","bat"]，
// 输出:[["ate","eat","tea"],["nat","tan"],["bat"]]
    public static String[][] solution(String[] input){
        String[][] result = new String[input.length][input.length];
        for(int i = 0;i<input.length-1;i++){
            int local = 0;
            for(int j = i+1 ;j<input.length;j++){
                if(islike(input[i],input[j])){
                    result[i][local++] = input[i];
                    result[i][local++] = input[j];
                }
            }
        }
        return result;
    }


    public List<List<String>> stringGroup(String[] strs){
        if(strs == null || strs.length == 0){
            return new ArrayList<>();
        }
        Arrays.sort(strs);
        Map<String,List<String>> map = new HashMap<>();
        for(String str : strs){
            char[] c = str.toCharArray();
            Arrays.sort(c);
            String sortedStr = String.valueOf(c);
            if(!map.containsKey(sortedStr)) {
                map.put(sortedStr,new ArrayList<>());
            }
            map.get(sortedStr).add(str);
        }
        return new ArrayList<>(map.values());
    }

    //判断一个字符串和另一个字符串是否由相同字母组成，相同字符组成 aatt 与at？
    public static  Boolean islike(String a,String b){
        if(a.length()>b.length()){
            return islike(b,a);
        }
        int[] counts = new int[26];
        for(int i=0;i<a.length();i++){
            counts[a.charAt(i)-'a']++;
        }
        for(int i=0;i<b.length();i++){
            counts[b.charAt(i)-'a']--;
        }
        for(int i =0;i<counts.length;i++){
            if(counts[i]>0){
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) {
        String[] input = new String[]{"eat","tea","tan","ate","nat","bat"};
        //System.out.println(islike("aatt","at"));
        for(int i = 0;i <solution(input).length;i++ ){
            System.out.println(Arrays.toString(solution(input)[i]));
        }

    }
}
