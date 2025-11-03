package visitor ;
import java.util.HashMap;
import java.util.Vector;
public class Methodinfo
{
   public String name;
   public String rettype;
   public HashMap<String,String> varMap = new HashMap<>();
    public Vector<String> arguements = new Vector<>();
    public Vector<String> vars = new Vector<>();
    public HashMap<String,String> arguementsMap = new HashMap<>();
    public HashMap<String,String> typeVar= new HashMap<>();
    public HashMap<String,String> typeArg= new HashMap<>();

   public Methodinfo(String _name,String rt)
   {
      name=_name;
      rettype=rt;
      varMap=new HashMap<String,String>();
      vars= new Vector<>();
      arguementsMap=new HashMap<String,String>();
      arguements= new Vector<String>();
      typeArg= new HashMap<String,String>();
      typeVar= new HashMap<String,String>();
   }
   public Methodinfo()
   {
     name="";
     rettype="";
     varMap=new HashMap<String,String>();
     vars= new Vector<>();

     arguementsMap=new HashMap<String,String>();
     arguements= new Vector<String>();
     typeArg= new HashMap<String,String>();
      typeVar= new HashMap<String,String>();
   }
   public void pushvar(String id, String type)
   {
      typeVar.put(id, type);
      varMap.put(id,"TEMP "+String.valueOf(arguements.size()+vars.size()+1));
      vars.addElement(id);
   }

   public void pusharg(String id, String type)
   {
      typeArg.put(id, type);
      arguementsMap.put(id,"TEMP "+String.valueOf(arguements.size()+1));
      arguements.addElement(id);
   }

}