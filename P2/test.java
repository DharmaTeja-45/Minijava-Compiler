import java.util.function.Function;

class test {
    public static void main(String[] args) {
        System.out.println((new A2()).foo((x)->(x+1)));
    }
}



class A1{
    public int foo(Function<Integer,Integer> f){
        return 1;
    }
}
class A2 extends A1{
    public int foo(Function<Integer,Integer> f){
        return 2;
    }
}