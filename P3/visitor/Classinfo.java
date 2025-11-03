package visitor ;
import java.util.HashMap;
public class Classinfo{
   public String className;
   public String parent;
   public HashMap <String,String> varMap;
   public HashMap <String,Methodinfo> methodMap;
   public HashMap <String,Integer> methodsource;

   public Classinfo(String _name,String _par)
   {
      className=_name;
      parent=_par;
      varMap=new HashMap<String,String>();
      methodMap=new HashMap<String,Methodinfo>();
      methodsource= new HashMap<String,Integer>();
   }
   public void pushvar(String id,String type)
   {
      varMap.put(id,type);
   }
   public void pushmethod(Methodinfo m)
   {
      //offset.put(m.name, methodMap.size());
      methodMap.put(m.name,m );
   }

   public void addparent(Classinfo c){
      for(String s:c.varMap.keySet()){
         varMap.put(s,c.varMap.get(s));
      }
      for(String s:c.methodMap.keySet()){
         if(methodMap.get(s)==null)
         methodMap.put(s,c.methodMap.get(s));
         methodsource.put(s,1);
      }
   }

   public Classinfo (Classinfo c){
      className=c.className;
      parent=c.parent;
      varMap=new HashMap<String,String>();
      methodMap=new HashMap<String,Methodinfo>();
      methodsource= new HashMap<String,Integer>();
      for(String s:c.varMap.keySet()){
         varMap.put(s,c.varMap.get(s));
      }
      for(String s:c.methodMap.keySet()){
         methodMap.put(s,c.methodMap.get(s));
         methodsource.put(s,1);
      }
   }
   
}