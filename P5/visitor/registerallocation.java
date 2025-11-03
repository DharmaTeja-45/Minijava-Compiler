package visitor;

import syntaxtree.*;
import java.util.*;

public class registerallocation<R, A> implements GJVisitor<R, A> {

   Integer line_counter = 1;
   Integer arg = 0;
   blockinfo current_block = new blockinfo(1);
   functioninfo current_function = new functioninfo("main");
   public Map<String, functioninfo> function_map = new HashMap<String, functioninfo>();

   ArrayList<String> registers = new ArrayList<String>(Arrays.asList(
         "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
         "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9",
         "v1", "v0", "a0", "a1", "a2", "a3"
   ));

   Integer free_reg = 18;
   Map<String, Integer> label_linemap = new HashMap<String, Integer>();
   boolean label_status = false;
   Integer argcounter = 0;
   Integer stack_ptr = 0;
   Boolean v0_status = false;
   Boolean v1_status = false;
   Boolean arg_check = false;
   String morethan80= "";
   void emitStoreForTemp(String temp, String srcReg) {
      String targetReg = "-1";

      if (current_function.interval_map.containsKey(temp)) {
         intervalinfo interval = current_function.interval_map.get(temp);

         if (interval.start_line > line_counter || interval.end_line < line_counter) {
            targetReg = "v1";
            System.out.println("\tMOVE " + targetReg + " " + srcReg);
         } else if (!interval.allocated_register.equals("-1")) {
            targetReg = interval.allocated_register;
            System.out.println("\tMOVE " + targetReg + " " + srcReg);
         } else {
            targetReg = "v0";
            System.out.println("\tMOVE " + targetReg + " " + srcReg);
            Integer stackIndex = Math.max(0, current_function.num_args - 4) + interval.stack_location;
            System.out.println("\tASTORE SPILLEDARG " + stackIndex + " " + targetReg);
         }
      } else {
         targetReg = "v1";
         System.out.println("\tMOVE " + targetReg + " " + srcReg);
      }
   }

   public R visit(NodeList n, A argu) {
      R _ret = null;
      int _count = 0;
      for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this, argu);
         _count++;
      }
      return _ret;
   }

   public R visit(NodeListOptional n, A argu) {
      if (n.present()) {
         R _ret = null;
         int _count = 0;
         for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this, argu);
            _count++;
         }
         return _ret;
      } else
         return null;
   }

   public R visit(NodeOptional n, A argu) {
      if (n.present())
         return n.node.accept(this, argu);
      else
         return null;
   }

   public R visit(NodeSequence n, A argu) {
      R _ret = null;
      int _count = 0;
      for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
         e.nextElement().accept(this, argu);
         _count++;
      }
      return _ret;
   }

   public R visit(NodeToken n, A argu) {
      return null;
   }

   public R visit(Goal n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      String functionName = "MAIN";
      current_function = function_map.get(functionName);
      stack_ptr = current_function.stack_ptr;

      System.out.println(functionName + "[ " + current_function.num_args + " ] [ " +
            current_function.total_stack_size + " ] [" + current_function.max_callee_args + " ]");

      line_counter++;
      n.f1.accept(this, argu);

      System.out.println("END");
      line_counter++;

      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      return _ret;
   }

   public R visit(StmtList n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      return _ret;
   }

   public R visit(Procedure n, A argu) {
      R _ret = null;

      label_status = true;
      n.f0.accept(this, argu);
      label_status = false;

      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);

      String functionName = n.f0.f0.toString();
      current_function = function_map.get(functionName);
      stack_ptr = current_function.stack_ptr;

      System.out.println(functionName + "[ " + current_function.num_args + " ] [ " +
            current_function.total_stack_size + " ] [" + current_function.max_callee_args + " ]");
      line_counter++;

      for (int i = 0; i < 8; i++) {
         System.out.println("\tASTORE SPILLEDARG " + stack_ptr + " s" + i);
         stack_ptr++;
      }

      for (int i = 0; i < current_function.num_args; i++) {
         if (i < 4) {
            String varName = "TEMP " + i;
            String argReg = "a" + i;
            if (current_function.interval_map.containsKey(varName)) {
               intervalinfo interval = current_function.interval_map.get(varName);
               if (!interval.allocated_register.equals("-1")) {
                  String allocated = interval.allocated_register;
                  System.out.println("\tMOVE " + allocated + " " + argReg);
               } else {
                  System.out.println("\tMOVE v0 " + argReg);
                  Integer stackLoc = Math.max(0, current_function.num_args - 4) + interval.stack_location;
                  System.out.println("\tASTORE SPILLEDARG " + stackLoc + " v0");
               }
            } else {
               System.out.println("\tMOVE v1 " + argReg);
            }
         }
      }

      for (int i = 4; i < current_function.num_args; i++) {
         System.out.println("\tALOAD v0 SPILLEDARG " + (i - 4));
         String varName = "TEMP " + i;
         if (current_function.interval_map.containsKey(varName)) {
            intervalinfo interval = current_function.interval_map.get(varName);
            if (!interval.allocated_register.equals("-1")) {
               String allocated = interval.allocated_register;
               System.out.println("\tMOVE " + allocated + " v0");
            } else {
               Integer stackLoc = Math.max(0, current_function.num_args - 4) + interval.stack_location;
               System.out.println("\tASTORE SPILLEDARG " + stackLoc + " v0");
            }
         } else {
            System.out.println("\tMOVE v1 v0");
         }
      }

      n.f4.accept(this, argu);

      for (int i = 7; i >= 0; i--) {
         stack_ptr--;
         System.out.println("\tALOAD s" + i + " SPILLEDARG " + stack_ptr);
      }

      System.out.println("END");
      return _ret;
   }

   public R visit(Stmt n, A argu) {
      R _ret = null;
      line_counter++;
      n.f0.accept(this, argu);
      return _ret;
   }

   public R visit(NoOpStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      System.out.println("\tNOOP");
      return _ret;
   }

   public R visit(ErrorStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      System.out.println("\tERROR");
      return _ret;
   }

   public R visit(CJumpStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      v0_status = true;
      String condReg = (String) n.f1.accept(this, argu);
      v0_status = false;

      label_status = true;
      String label = current_function.function_name + "_" + (String) n.f2.accept(this, argu);
      label_status = false;

      System.out.println("\tCJUMP " + condReg + " " + label);
      return _ret;
   }

   public R visit(JumpStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      label_status = true;
      String label = current_function.function_name + "_" + (String) n.f1.accept(this, argu);
      label_status = false;

      System.out.println("\tJUMP " + label);
      return _ret;
   }

   public R visit(HStoreStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      v0_status = true;
      String baseReg = (String) n.f1.accept(this, argu);
      v0_status = false;

      v1_status = true;
      String offset = (String) n.f2.accept(this, argu);
      v1_status = false;

      String valueReg = (String) n.f3.accept(this, argu);
      System.out.println("\tHSTORE " + baseReg + " " + offset + " " + valueReg);
      return _ret;
   }

   public R visit(HLoadStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      String varName = "TEMP " + (String) n.f1.f1.f0.toString();

      v0_status = true;
      String baseReg = (String) n.f2.accept(this, argu);
      v0_status = false;

      String offset = (String) n.f3.accept(this, argu);
      System.out.println("\tHLOAD v0 " + baseReg + " " + offset);
      emitStoreForTemp(varName, "v0");
      return _ret;
   }

   public R visit(MoveStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      String varName = "TEMP " + (String) n.f1.f1.f0.toString();

      label_status = true;
      String src = (String) n.f2.accept(this, argu);
      label_status = false;
      System.out.println(morethan80);
      morethan80= "";
      emitStoreForTemp(varName, src);
      return _ret;
   }

   public R visit(PrintStmt n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      String r = (String) n.f1.accept(this, argu);
      System.out.println("\tPRINT " + r);
      return _ret;
   }

   public R visit(Exp n, A argu) {
      R _ret = null;
      String e = (String) n.f0.accept(this, argu);
      return (R) e;
   }

   public R visit(StmtExp n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      line_counter++;
      n.f1.accept(this, argu);
      line_counter++;
      n.f2.accept(this, argu);

      String resultReg = (String) n.f3.accept(this, argu);
      System.out.println("\tMOVE v0 " + resultReg);

      n.f4.accept(this, argu);
      line_counter++;
      return _ret;
   }

   public R visit(Call n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      String funcReg = (String) n.f1.accept(this, argu);

      for (int i = 0; i < 10; i++) {
         System.out.println("\tASTORE SPILLEDARG " + stack_ptr + " t" + i);
         stack_ptr++;
      }

      n.f2.accept(this, argu);
      arg_check = true;
      argcounter = 0;
      n.f3.accept(this, argu);
      arg_check = false;
      n.f4.accept(this, argu);

      System.out.println("\tCALL " + funcReg);

      for (int i = 9; i >= 0; i--) {
         stack_ptr--;
         System.out.println("\tALOAD t" + i + " SPILLEDARG " + stack_ptr);
      }
      return (R) "v0";
   }

   public R visit(HAllocate n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      String allocReg = (String) n.f1.accept(this, argu);
      String s;
      if(allocReg.startsWith("s")||allocReg.startsWith("t")){
      morethan80= "MOVE v0 PLUS " + allocReg+ " 4";
      s= "HALLOCATE v0" ;
      }
      else {
         s= "HALLOCATE " + allocReg;
      }
      return (R) s;
   }

   public R visit(BinOp n, A argu) {
      R _ret = null;
      String op = (String) n.f0.accept(this, argu);

      v1_status = true;
      String left = (String) n.f1.accept(this, argu);
      v1_status = false;

      String right = (String) n.f2.accept(this, argu);

      String binop = (op + " " + left + " " + right);
      return (R) binop;
   }

   public R visit(Operator n, A argu) {
      R _ret = null;
      String op = n.f0.choice.toString();
      return (R) op;
   }

   public R visit(SimpleExp n, A argu) {
      R _ret = null;
      v0_status = true;
      String r = (String) n.f0.accept(this, argu);
      v0_status = false;
      return (R) r;
   }

   public R visit(Temp n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);

      String tempName = "TEMP " + n.f1.f0.toString();
      String allocatedReg = "v0";
      if (v1_status) allocatedReg = "v1";

      if (current_function.interval_map.containsKey(tempName)) {
         intervalinfo interval = current_function.interval_map.get(tempName);
         if (!interval.allocated_register.equals("-1")) {
            allocatedReg = interval.allocated_register;
         } else {
            Integer idx = Math.max(0, current_function.num_args - 4) + interval.stack_location;
            System.out.println("\tALOAD " + allocatedReg + " SPILLEDARG " + idx);
         }
      }

      if (arg_check) {
         if (argcounter < 4) {
            System.out.println("\tMOVE a" + (argcounter) + " " + allocatedReg);
         } else {
            System.out.println("\tPASSARG " + (argcounter - 3) + " " + allocatedReg);
         }
         argcounter++;
      }

      return (R) allocatedReg;
   }

   public R visit(IntegerLiteral n, A argu) {
      R _ret = null;
      n.f0.accept(this, argu);
      String value = n.f0.toString();
      return (R) value;
   }

   public R visit(Label n, A argu) {
      R _ret = null;
      String label = n.f0.toString();
      if (!label_status) {
         line_counter++;
         System.out.println(current_function.function_name + "_" + label);
      }
      return (R) label;
   }

}
