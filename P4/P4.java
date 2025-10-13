import syntaxtree.*;
import visitor.*;

public class P4 {
   public static void main(String [] args) {
      try {
         Node root = new MiniIRParser(System.in).Goal();
         GJDepthFirst <Ret,String> gj = new GJDepthFirst<>();
         root.accept(gj, "");
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }

   }
}
