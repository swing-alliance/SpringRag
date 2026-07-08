package JUnit;

public class jisuan {
    public int cal(int a,int b){
        return a+b;

    }
    public int multiple(int a,int b){
        return a*b;
    }
    public static float division(int a,int b){

        return (float)a/b;
    }

    public static void main(String[] args){
        int a=1;
        int b=2;
        float c=division(a,b);
        System.out.print(c);
    }

}
