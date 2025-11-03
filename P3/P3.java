import syntaxtree.*;
import visitor.*;

public class P3 {
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         FirstParse v = new FirstParse();
         root.accept(v);
         //v.printvtable();
         // v.printClasses();
         GJDepthFirst <Ret,String>gj = new GJDepthFirst<>();
         gj.vtable= v.vtable;
         gj.classMap= v.classMap2;
         root.accept(gj, "");
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }

   }
}
