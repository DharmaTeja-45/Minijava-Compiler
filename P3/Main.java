import syntaxtree.*;
import visitor.*;


public class Main {
   public static void main(String [] args) {
      try {
        Node root = new MiniJavaParser(System.in).Goal();


	      LambdaEliminate v = new LambdaEliminate();

         root.accept(v, "");
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
}

