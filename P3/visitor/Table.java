package visitor;

import java.util.HashMap;

public class Table{
    String name="";
    public HashMap<String,Integer> fields= new HashMap<>();
    public HashMap<String,Integer> methods= new HashMap<>(); 
    public HashMap<String,Integer> argsize= new HashMap<>();
    public HashMap<String,String> source= new HashMap<>();
    Integer field_offset= 1;
    Integer method_offset= 0;
    Table(){
        
    }

    Table(Table t){
        for(String s: t.fields.keySet()){
            fields.put(s,t.fields.get(s));
        }
        for(String s: t.methods.keySet()){
            methods.put(s,t.methods.get(s));
        }
        for(String s: t.argsize.keySet()){
            argsize.put(s,t.argsize.get(s));
        }
        for(String s: t.source.keySet()){
            source.put(s,t.source.get(s));
            //System.out.println("Source of method "+s+" is "+t.source.get(s));
        }
        field_offset= t.field_offset;
        method_offset= t.method_offset;
    }

    public void addfield(String fieldname){
        if(fields.get(fieldname)==null){
            fields.put(fieldname, field_offset);
            field_offset+= 1;
        }
    }

    public void addmethod(Methodinfo m){
        String methodname= m.name;
        if(methods.get(methodname)==null){
            methods.put(methodname, method_offset);
            argsize.put(methodname, m.arguements.size());
            source.put(methodname,name);
            method_offset+= 1;
        }
        else{
            source.put(methodname,name);
        }
        //System.out.println("Source of method "+methodname+" is "+source.get(methodname));
    }

    public void printTable() {
    System.out.println("Class/Table Name: " + name);
    System.out.println("Fields:");
    for (String field : fields.keySet()) {
        System.out.println("  " + field + " -> offset " + fields.get(field));
    }

    System.out.println("Methods:");
    for (String method : methods.keySet()) {
        System.out.println("  " + method 
                           + " -> offset " + methods.get(method) 
                           + ", args: " + argsize.get(method) 
                           + ", source: " + source.get(method));
    }

    System.out.println("Field Offset Counter: " + field_offset);
    System.out.println("Method Offset Counter: " + method_offset);
}


}
