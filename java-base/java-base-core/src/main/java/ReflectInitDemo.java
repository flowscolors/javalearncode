import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author flowscolors
 * @date 2021-10-05 15:10
 */
class ReflectInitTest {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {

        //通过 Class.forName()传入类的路径获取该类,再使用类的newInstance方法创建实例
        // Class.forName()是四种方式之一，如果直接知道类的名字，可以使用.class
        Class<?> targetClass = Class.forName("TargetObject");
        TargetObject targetObject = (TargetObject) targetClass.newInstance();

        //获得类里面所有方法
        System.out.println("TargetObject类中所有方法：");
        Method[] methods = targetClass.getDeclaredMethods();
        for(Method method : methods){
            System.out.println(method.getName());
        }

        System.out.println("  ");
        //实际上方法也是一种对象,实例化后使用invoke进行调用,传入object和object args
        //注意getDeclaredMethod获取到的是所有的成员方法，和修饰符无关。getMethod只能拿public的方法
        Method publicMethod = targetClass.getDeclaredMethod("publicMethod", String.class);
        publicMethod.invoke(targetObject,"hello world");

        //可以看到这时候object对象已经有了，于是我们现在使用反射，获取指定参数，并对其进行修改
        System.out.println("the value is "+targetObject.getValue());
        Field field = targetClass.getDeclaredField("value");
        field.setAccessible(true);
        field.set(targetObject,"new vlaue");
        System.out.println("the value is "+targetObject.getValue());

        //使用反射，也可以调用private方法，同样也是使用invoke进行调用
        //有很多文章讨论禁止通过反射访问一个对象的私有变量，但是到目前为止所有的jdk还是允许通过反射访问私有变量，需要setAccessible(true)。
        Method privateMethod = targetClass.getDeclaredMethod("privateMethod");
        privateMethod.setAccessible(true);
        privateMethod.invoke(targetObject);
    }
}

class TargetObject {
    private String value;

    public TargetObject(){
        value = "init value";
    }

    public void publicMethod(String  s) {
        System.out.println("This is publicMethod: "+s);
    }

    private void privateMethod() {
        System.out.println("This is privateMethod");
    }

    public String getValue() {
        return this.value;
    }
}