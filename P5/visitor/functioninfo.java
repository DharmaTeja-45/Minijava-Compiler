package visitor;

import java.lang.reflect.Array;
import java.util.* ;

public class functioninfo {
    String function_name;
    Integer num_args;
    Integer max_callee_args;
    Integer total_stack_size;
    Integer stack_ptr;
    Map <String, intervalinfo> interval_map;
    Map<String, Integer> label_map;
    ArrayList<blockinfo> blocks;
    Boolean call_check;
    Boolean spilled_check;
    ArrayList<intervalinfo> intervallist;
    ArrayList<intervalinfo> activelist;
    ArrayList<intervalinfo> stack_space;
    
    functioninfo(String name) {
        this.function_name = name;
        this.num_args = 0;
        this.max_callee_args = 0;
        this.total_stack_size = 0;
        this.stack_ptr = 0;
        this.label_map = new HashMap<String, Integer>();
        this.blocks = new ArrayList<blockinfo>();
        this.interval_map = new HashMap <String, intervalinfo>();
        this.call_check = false;
        this.spilled_check = false;
        this.intervallist = new ArrayList<intervalinfo>();
        this.activelist = new ArrayList<intervalinfo>();
        this.stack_space = new ArrayList<intervalinfo>();
    }

    void print(){
        System.out.println("Function Name: "+ function_name);
        System.out.println("Number of Arguments: "+ num_args);
        System.out.println("Max Callee Args: "+ max_callee_args);
        System.out.println("Total Stack Size: "+ total_stack_size);
        System.out.println("Stack Pointer: "+ stack_ptr);
        System.out.println("Label Map: "+ label_map.toString());
        System.out.println("Blocks: ");
        for(blockinfo b: blocks){
            b.print();
        }
    }
}
