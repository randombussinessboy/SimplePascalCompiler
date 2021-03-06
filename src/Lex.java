import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;


public class Lex {
    //目标语法关键字集合，下一步应该去掉硬编码
    static private Map reserveWordMap=new HashMap();

    //该语法的有效字符集合
    static private Map validCharacter=new HashMap();

    //将文件读入该缓存数组
    static private char[] BUFFERSTRING=new char[10000];
    static private String rawFileName;

    //文件大小
    static private int pointer=0;

    //错误计数 错误大于0词法分析没有通过
    static private int errorCount=0;


    /*常量，各个单词对应的助记符
     需要类别号的时候可以填这个，程序更加明了
     ,重构中，有一些调用直接打的类别号，有一些使用助记符
     非法的字符和单词也可以考虑弄助记符
     */
    static  final int BEGIN =1;
    static  final int END =2 ;
    static  final int IF =3;
    static  final int THEN =4 ;
    static  final int ELSE =5;
    static  final int WHILE =6;
    static  final int DO =7;
    static  final int VAR =8;
    static  final int ID=9;
    static  final int INT=10;
    static  final int  LT=11;
    static  final int  LE=12;
    static  final int  EQ=13;
    static  final int  NE=14;
    static  final int  GT=15;
    static  final int  GE=16;
    static  final int  ADD=17;
    static  final int  SUB=18;
    static  final int  MUL=19;
    static  final int  DIV=20;
    static  final int  COL_EQ=21;
    static  final int  SEMI=22;
    static  final int  COMMA=23;
    static  final int  LP=24;
    static  final int RP=25;

      /*
        -1 invalid character
        -2 identifier is to long
        -3 integer overflow
        -4 invalid word
         */
    static final private int INVALIDCHARACTER=-1;
    static final private int INDENTIFERTOLONG=-2;
    static final private int INTEGEROVERFLOW=-3;
    static final private int INVALIDWORD=-4;


    /*
        分析结果,结构化的数据结构放入此数据结构
     */
    static private List analyzeResult=new ArrayList<Word>();

    /*该类被加载进JVM后，马上执行的代码，功能为初始化上述的集合
    关键字映射集合value字段是类别码，字符映射集合value字段无意义，
    选则该数据结构是因为它查找速度快*/
    static {
        reserveWordMap.put("BEGIN",1);
        reserveWordMap.put("END",2);
        reserveWordMap.put("IF",3);
        reserveWordMap.put("THEN",4);
        reserveWordMap.put("ELSE",5);
        reserveWordMap.put("WHILE",6);
        reserveWordMap.put("DO",7);
        reserveWordMap.put("VAR",8);
        validCharacter.put("<",1);
        validCharacter.put("=",1);
        validCharacter.put(">",1);
        validCharacter.put("+",1);
        validCharacter.put("-",1);
        validCharacter.put("*",1);
        validCharacter.put("/",1);
        validCharacter.put(":",1);
        validCharacter.put(";",1);
        validCharacter.put(",",1);
        validCharacter.put("(",1);
        validCharacter.put(")",1);
        validCharacter.put(" ",1);
        validCharacter.put("\t",1);
        validCharacter.put("\n",1);
        validCharacter.put("\r",1);
        validCharacter.put("\0",1);
    }



    /*为词法分析器添加源代码文件，
      它会重置error count变量
       并把新添加将来的文件加载到缓存区，
       原来的文件将被覆盖，pointer变量也将刷新
     */
    static void setResource(String fileName){
        rawFileName=fileName;
        flushBuffer();
        copyToBuffer(fileName);
    }

    /*获取词法分析结果,使得词法分析器开始工作
    返回含有每个单词 以及这个单词坐标的集合
     */
    static List getResult(){
        //开始分析
        analyze();
        /*
        如果词法分析失败，进行提示，然后返回结果集合
        如果分析成功，直接返回结果集合
         */
        if (!isSuccessful()){
            System.out.println("Prompt： 词法分析失败，无法进行语法分析");
        }
        /* 为了方便观察，不管成功与否我们都把分析结果打印出来*/
        for (int i=0;i<analyzeResult.size();i++){
            Word word=(Word)analyzeResult.get(i);
            String content=word.getContent();
            int type =word.getType();
            int wordRow=word.getWordRow();
            int wordColumn=word.getWordColumn();
            System.out.println("("+type+","+content+")  行:"+wordRow+"列"+wordColumn);
        }
        return analyzeResult;
    }

    /*
     根据错误计数判断词法分析子程序有没有执行成功
     应在判定成功后再调用getResult进行语法分析
     */
    static  boolean isSuccessful(){
        if (errorCount==0)return true;
        return false;
    }


    /* 刷新错误计数变量,和结果List集合 从而开始分析全新的源文件*/
    static private void flushBuffer(){
        errorCount=0;
        analyzeResult.clear();
    }

    /*调用IO stream 将源文件读入缓存数组*/
    static private void copyToBuffer(String fileName){
            File file=new File(fileName);
            try {
                FileReader reader=new FileReader(file);
                pointer=(int)file.length();
                reader.read(BUFFERSTRING);
                reader.close();

            }catch (Exception e){
                e.printStackTrace();
            }
    }
    /*查询字符串是否为保留字，返回类别码，如若表示则返回-1*/
    static private int searchReserve(String rawString){

        if (reserveWordMap.containsKey(rawString)){
            return (int)reserveWordMap.get(rawString);
        }else {
            return -1;
        }
    }

    /*
    判断是否为大写字母，因为在该词法中，小写字母是非法字符，
    所以没有小写的
     */
    static private boolean isUpperLetter(char letter){
        if (letter>='A'&&letter<='Z'
        ){
            return true;
        }
        return false;
    }

    /*判断是否为数字，这两个函数java.lang应该有实现，可以直接调用*/
    static private boolean isDigit(char digit){
        if (digit>='0'&&digit<='9'){
            return true;
        }
        return false;
    }


    /*
    根据有效字符集合，判断一个字符是不是有效的，是有效的返回0
    无效的返回1
     */
    static private boolean isInvalidCharacter(char raw){
        if (isDigit(raw)||raw>='A'&&raw<='Z'||
                validCharacter.containsKey(new String(new char[]{raw}))){
            return false;
        }
        return true;
    }

    /*

    根据有限自动机进行编码，首先应该跳过白空格
    以及删除两种注释，然后根据自动机使用while或者case语句进行
    组词

    */
    static private void analyze(){

        /*
        相关变量的初始化，行为1，列为0
        每递进一个字符column+1
        字符串arr用来存放识别出来的单词
        ch是从内存缓冲区读取出来的字符
         */
        int row=1;
        int column=0;
        String arr="";
        char ch;
        int category;
        for (int i=0;i<pointer;i++){

            /*
            循环开始，Pointer指针是源码文件总的字符个数

             */
            ch=BUFFERSTRING[i];
            column++;
            arr="";
            if (BUFFERSTRING[i]=='/'&&BUFFERSTRING[i+1]=='/'){
                /*若为单行注释“//”，则去除后面的东西，直到遇到回车换行
                限制i小于pointer 是为了防止源码文件的最后一行的单行注释检测不到
                对应的\n从而发送数组越界
                 */
                while (BUFFERSTRING[i]!='\n'&&i<=pointer){
                    /*
                    I+1 column则加1
                     */

                    i++;
                    column++;
                }
                /*
                注意到while循环结束时此处为换行，则行数加1，列数置为初值
                 */
                row++;
                column=0;
                //结束本轮循环
                continue;
            }


            /*
            下列循环是用来判断多行注释的
             */

            if (BUFFERSTRING[i]=='/'&&BUFFERSTRING[i+1]=='*'){
                i+=2;
                column+=2;
                while (BUFFERSTRING[i]!='*'||BUFFERSTRING[i+1]!='/'){
                    i++;
                    column++;
                    if (i==pointer){
                        System.out.println("注释出错，" +
                                "没有找到*/，编译终止");
                        exit(0);
                    }
                    if (BUFFERSTRING[i]=='\n'){
                        row++;
                        column=0;

                    }

                }
                i+=2;
                column+=2;
                ch=BUFFERSTRING[i];
            }

            /*
            跳跃这些无意义的空格
            换行则行数加1 列数置为处置
             */
            if (ch==' '||ch=='\t'
            ||ch=='\n'||ch=='\r'){
                if (ch=='\n'){
                    row++;
                    column=0;
                }

            }
            /*
            下面if的逻辑是
            如果当前字符为大写字符
            则向下试探进行组词，下一个字符
            只要是大写字母或者数字就是合法的
            如果不是无效字符则也可以加入arr
            是因为，这样形成的一个单词叫做
            非法单词 使用标志位flag来表示结果单词
            中有没有无效的字符,无效的字符就是除了大写字母
            ，数字以及我们用到的各种运算符分割符空白指标字符外的
            字符
            结果单词的字符大于8的时候应该进行截断，然后输出警告
             */
            else if (isUpperLetter(ch)){
                int flag=0;
                int wordColumn=column;
                while(isUpperLetter(ch)||isDigit(ch)||isInvalidCharacter(ch)){
                    arr+=ch;
                    if (isInvalidCharacter(ch))flag=1;
                    ch=BUFFERSTRING[++i];
                    column++;
                }
                if (flag==1){
                    saveWordAndPrinter(INVALIDWORD,arr,row,wordColumn);
                    i--;
                    column--;
                    continue;
                }
                if (arr.length()>=8){
                    saveWordAndPrinter(INDENTIFERTOLONG,arr,row,wordColumn);
                    arr=arr.substring(0,7);
                }
                i--;
                column--;
                //i-1 是因为当前字符不是这个单词的，所以我们回退，开始新的一轮
                //大循环 把结果单词查保留字（关键字）表，看是关键字还是普通的标识符
                category=searchReserve(arr);
                if (category!=-1){
                    saveWordAndPrinter(category,arr,row,wordColumn);
                }
                else {
                   saveWordAndPrinter(ID,arr,row,wordColumn);
                }
            }
            //数字串的识别思路跟上面一样，这里添加了对小数的支持
            else if (isDigit(ch)){
                int wordColumn=column;
               while (isDigit(ch)||(ch=='.'&&isDigit(BUFFERSTRING[++i]))){
                    if (ch=='.'){
                        i--;
                        column--;
                    }
                    arr=arr+ch;
                      ch=BUFFERSTRING[++i];
                      column++;
                }
                i--;
                column--;
                //下面这个if是为检测数字打头的标识符，例如 12AB1 是非法的
                if (isUpperLetter(BUFFERSTRING[i+1])){
                    i++;
                    column++;
                    while (isUpperLetter(BUFFERSTRING[i])||isDigit(BUFFERSTRING[i])){
                        arr=arr+BUFFERSTRING[i];
                        i++;
                        column++;
                    }
                    i--;
                    column--;
                    saveWordAndPrinter(INVALIDWORD,arr,row,wordColumn);
                    continue;
                }
                //如果这个数字串大于65535则应该报错，因为这样对这个数据结构来说越界了
                int num=Integer.parseInt(arr);
                if (num>65535){
                    saveWordAndPrinter(INTEGEROVERFLOW,String.valueOf(num),row,wordColumn);
                }else {
                    saveWordAndPrinter(INT,arr,row,wordColumn);
                }

            }//下面就是简单CASE语句进行判断是什么字符了
            else switch (ch){
                    case '+':saveWordAndPrinter(ADD,"+",row,column);break;
                    case '-':saveWordAndPrinter(SUB,"-",row,column);break;
                    case '*':saveWordAndPrinter(MUL,"*",row,column);break;
                    case '/':saveWordAndPrinter(DIV,"/",row,column);break;
                    case '=':saveWordAndPrinter(EQ,"=",row,column);break;
                    //分界符
                    case '(':saveWordAndPrinter(LP,"(",row,column);break;
                    case ')':saveWordAndPrinter(RP,")",row,column);break;
                    case ';':saveWordAndPrinter(SEMI,";",row,column);break;
                    case ',':saveWordAndPrinter(COMMA,",",row,column);break;
                    //运算符
                    case ':':{
                        int wordClumn=column;
                        ch = BUFFERSTRING[++i];
                        column++;
                        if(ch == '=')saveWordAndPrinter(COL_EQ,":=",row,wordClumn);
                        else {
                            saveWordAndPrinter(INVALIDCHARACTER,":",row,wordClumn);
                            i--;
                            column--;
                        }
                    }break;
                    case '>':{
                        int wordCount=column;
                        ch = BUFFERSTRING[++i];
                        column++;
                        if(ch == '=')saveWordAndPrinter(GE,">=",row,wordCount);
                        else {
                            saveWordAndPrinter(GT,">",row,wordCount);
                            i--;
                            column--;
                        }
                    }break;
                    case '<':{
                        int wordCount=column;
                        ch = BUFFERSTRING[++i];
                        column++;
                        if(ch == '='){
                            saveWordAndPrinter(LE,"<=",row,wordCount);

                        }
                        else if (ch=='>'){
                            saveWordAndPrinter(NE,"<>",row,wordCount);
                        }
                        else {
                            saveWordAndPrinter(LT,"<",row,wordCount);
                            i--;
                            column--;
                        }
                    }break;

                    //无识别
                    default:arr+=ch;
                        int wordColumn=column;
                        i++;
                        column++;
                        while (isDigit(BUFFERSTRING[i])||isUpperLetter(BUFFERSTRING[i])){
                            arr+=BUFFERSTRING[i];
                            i++;
                            column++;
                        }
                        i--;
                        column--;
                        saveWordAndPrinter(INVALIDCHARACTER,arr,row,wordColumn);

                }
        }

    }


    /*
    1.根据错误代码，位置坐标，非法的单词和字符 进行格式化输出
    2.根据类别码，位置坐标，保存进分析结果集合，并进行输出
    */
    static private void  saveWordAndPrinter(int category,String raw
    ,int wordRow ,int wordColumn){
        /*
        -1 invalid character
        -2 identifier is to long
        -3 integer overflow
        -4 invalid word
         */
       if (category==-1){
           System.out.println("Error: type"+"[非法字符]"+"  at line"+"["+wordRow+"]"+": ‘"+raw+"’含有非法字符");
           errorCount++;

       }else if ((category==-2)){
           System.out.println("Warning: type[标识符超长]"+"  at line"+"["+wordRow+"]:"+"'"+raw+"'"+"将截取前八位");
       }else if ((category==-3)){
           System.out.println("Error: type"+"[整数越界]"+"  at line"+"["+wordRow+"]"+": ‘"+raw+"’超过可表示范围");
           //Error type [错误类型] at line[行号]：说明文字
           errorCount++;
       }else if (category==-4){
           System.out.println("Error: type"+"[非法单词]"+"  at line"+"["+wordRow+"]"+": ‘"+raw+"’非法单词");
           //Error type [错误类型] at line[行号]：说明文字
           errorCount++;
       }
       else
       {
           Word newWord=new Word(raw,category,wordRow,wordColumn);
           analyzeResult.add(newWord);
       }
    }





}
