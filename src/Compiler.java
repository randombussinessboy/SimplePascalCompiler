import java.io.File;
import java.util.List;

public class Compiler {

    public static void main(String[] args){
//        //为词法分析器设置需要分析的源码
//        Lex.setResource("src/simple_pascal_program_2.txt");
//
//        //请求词法分析器返回结果
//        List<Word> lexAnalysisResult=Lex.getResult();
//
//        if (Lex.isSuccessful()){
//            //这里开始进行语法分析
//        }

        //分析另外一个源码文件,这里可以用循环进行实现
        Lex.setResource("src/simple_pascal_program_2.txt");

        //请求词法分析器返回结果
       List<Word> lexAnalysisResult=Lex.getResult();
        if (Lex.isSuccessful()){
            //这里开始进行语法分析
            System.out.println("Prompt： 词法分析成功，准备进行语法分析");
        }
    }
}
