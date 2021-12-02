import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.events.v1.Event;
import org.openjdk.jol.info.ClassLayout;

/**
 * @author flowscolors
 * @date 2021-10-05 15:12
 */
public class JolObjectSizeDemo {
    public static void main(String[] args) {
        ClassLayout classLayout = ClassLayout.parseInstance(new Event());
        System.out.println(classLayout.toPrintable());

        ClassLayout classLayout2 = ClassLayout.parseInstance(new Deployment());
        System.out.println(classLayout2.toPrintable());
    }
}
