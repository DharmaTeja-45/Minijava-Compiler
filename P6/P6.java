import java.util.Map;

import syntaxtree.*;
import visitor.*;

public class P6 {
   public static void main(String [] args) {
      try {
         Node root = new MiniRAParser(System.in).Goal();
         GJDepthFirst df1 = new GJDepthFirst<>();
         root.accept(df1, null); 
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }

   }
}
