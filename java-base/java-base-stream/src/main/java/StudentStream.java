import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author flowscolors
 * @date 2022-03-02 14:26
 */
public class StudentStream {

    public static void main(String[] args) {
        Student student1 = new Student(10000,"joker","Math",98);
        Student student2 = new Student(10000,"joker","English",85);
        Student student3 = new Student(10000,"joker","Compute",80);
        Student student4 = new Student(10001,"Quinn","Math",20);
        Student student5 = new Student(10001,"Quinn","English",70);
        Student student6 = new Student(10002,"BatMan","Compute",100);
        Student student7 = new Student(10003,"SuperMan","Compute",60);

        ArrayList<Student> students = new ArrayList<Student>();
        students.add(student1);
        students.add(student2);
        students.add(student3);
        students.add(student4);
        students.add(student5);
        students.add(student6);
        students.add(student7);

        System.out.println(Arrays.toString(students.toArray()));

        Map<String, List<Student>> nameclass  = students.stream().collect(Collectors.groupingBy(Student::getName));
        System.out.println(nameclass);

        Map<String,Long> scoreSum = students.stream().collect(Collectors.groupingBy(Student::getName,Collectors.summingLong(Student::getScore)));
        System.out.println(scoreSum);

        List<String> finalSum = students.stream().collect(Collectors.groupingBy(Student::getName,Collectors.summingLong(Student::getScore)))
                .entrySet().stream().sorted((v1,v2) -> {
                    if (v1.getValue().equals(v2.getValue())) {
                        return v1.getKey().compareTo(v2.getKey());
                    } else {
                        return v2.getValue().compareTo(v1.getValue());
                    }
                }).map(Map.Entry::getKey)
                .limit(3)
                .collect(Collectors.toList());
        System.out.println(finalSum);

        System.out.println("============");

        for (String result : finalSum){
            System.out.println(nameclass.get(result).get(0).getNum()+" "+result+" "+ scoreSum.get(result));
        }


        List<Student> students2 =  students.stream()
                .sorted(Comparator.comparing(Student::getScore).reversed())
                .limit(3).collect(Collectors.toList());


        //System.out.println(Arrays.toString(students2.toArray()));


    }



}

class Student {
    Integer num;
    String name;
    String subject;
    Integer score;

    public Student(Integer num,String name,String subject,Integer score){
        this.num = num;
        this.name = name;
        this.subject = subject;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{" +
                "num=" + num +
                ", name='" + name + '\'' +
                ", subject='" + subject + '\'' +
                ", score=" + score +
                '}'+'\n';
    }


    public Integer getNum() {
        return num;
    }

    public Integer getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }
}

