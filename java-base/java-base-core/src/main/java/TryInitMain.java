/**
 * @author flowscolors
 * @date 2021-12-12 13:33
 */
public class TryInitMain {
    public static void main(String[] args) {
        ChildClass cc = new ChildClass();
        cc.printX();
    }
}

class ParentClass {
    private int parentX;
    public ParentClass() {
        setX(100);
    }
    public void setX(int x) {
        parentX = x;
    }
}

class ChildClass extends ParentClass{
    private int childX = 1;
    public ChildClass() {}
    @Override
    public void setX(int x) {
        super.setX(x);
        childX = x;
        System.out.println("ChildX 被赋值为 " + x);
    }
    public void printX() {
        System.out.println("ChildX = " + childX);
    }

}
