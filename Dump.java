import java.lang.reflect.Method;
public class Dump {
    public static void main(String[] args) throws Exception {
        for (String c : args) {
            System.out.println("Methods in " + c + ":");
            for (Method m : Class.forName(c, false, Dump.class.getClassLoader()).getDeclaredMethods()) {
                System.out.println("  " + m.toString());
            }
        }
    }
}
