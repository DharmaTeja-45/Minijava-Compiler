package visitor;
import java.util.* ;
public class blockinfo {
    Integer line_number;
    Integer next_block;
    Integer jump_block;
    String jump_block_name;
    Set<String> def;
    Set<String> use;
    Set<String> in;
    Set<String> out;

    public blockinfo(Integer line_number) {
        this.line_number = line_number;
        this.next_block= line_number + 1;
        this.jump_block = -1;
        this.def = new HashSet<String>();
        this.use = new HashSet<String>();
        this.in = new HashSet<String>();
        this.out = new HashSet<String>();
        this.jump_block_name = "";
    }

    void print(){
        System.out.println("Block starting at line: "+ line_number);
        System.out.println("Next Block: "+ next_block);
        System.out.println("Jump Block: "+ jump_block + " ("+ jump_block_name +")");
        System.out.println("DEF set: "+ def.toString());
        System.out.println("USE set: "+ use.toString());
        System.out.println("IN set: "+ in.toString());
        System.out.println("OUT set: "+ out.toString());
    }

}
