package visitor;

public class intervalinfo {
    String temp_name;
    Integer start_line;
    Integer end_line;
    String allocated_register;
    Integer stack_location;

    public intervalinfo(String temp_name, Integer start_line, Integer end_line) {
        this.temp_name = temp_name;
        this.start_line = start_line;
        this.end_line = end_line;
        this.allocated_register = "-1";
        this.stack_location = -1;
    }

}
