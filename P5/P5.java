import java.util.Map;

import syntaxtree.*;
import visitor.*;
import visitor.Pass1.funcAttr;

public class P5 {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         liveanalysis df1 = new liveanalysis<>();
         root.accept(df1, null); 
         registerallocation ra = new registerallocation<>();
         ra.function_map= df1.function_map;
         root.accept(ra, null);
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }

   }
}
