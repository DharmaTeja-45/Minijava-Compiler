import syntaxtree.*;
import visitor.*;

public class P2 {
   public static void main(String [] args) {
      try {
         Node root = new MiniJavaParser(System.in).Goal();
         DepthFirstVisitor v = new DepthFirstVisitor();
         root.accept(v);
         //v.printClasses();
         //System.out.println(v.classMap.get("Tree").methodMap.get("fun").varMap.get("f"));
         GJDepthFirst <String, String>gj= new GJDepthFirst<String, String>();
         gj.classMap= v.classMap;
         gj.lambdaPresent=v.lambdaPresent;
         root.accept(gj,"");
         System.out.println("Program type checked successfully");
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
      catch(TypeException e){
         System.out.println(e.getMessage());
      }
      catch(SymbolTableException e){
         System.out.println(e.getMessage());
      }

   }
}
