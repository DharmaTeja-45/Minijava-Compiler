import syntaxtree.*;
import visitor.*;
import java.util.*;
import visitor.Pass1.blockAttr;
import visitor.Pass1.funcAttr;
import visitor.Pass1.intervalAttr;

public class Main {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         Pass1 df1 = new Pass1();
         Object value1 = root.accept(df1, null); // Your assignment part is invoked here.
         Map<String, funcAttr> func_list = df1.func_list;
         Pass2 df2 = new Pass2(func_list);
         Object value2 = root.accept(df2, null); // Your assignment part is invoked here.
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 