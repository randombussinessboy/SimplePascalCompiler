# SimplePascalCompiler
一个简单的Pascal编译器，目前已实现词法分析功能

_This Simple pascal Compiler develop to enhance my 
theory course of compile  , and I have implemented
the function of WordGrammar analysis;_

~~wrote preceding paragraph only to practice my poor english
  and skill of using markdown , hah~~


# Grammar
<语句表>→<语句> | <语句>;<语句表></br>
<语句>→<赋值语句>|<条件语句>|<WHILE语句>|<复合语句></br>
<赋值语句>→<变量>:=<算术表达式></br>
<条件语句>→IF<关系表达式>THEN<语句>ELSE<语句></br>
<WHILE语句>→WHILE<关系表达式>DO<语句></br>
<复合语句>→BEGIN<语句表>END</br>
<算术表达式>→<项>|<算术表达式>+<项>|<算术表达式>-<项></br>
<项>→<因式>|<项>*<因式>|<项>/<因式></br>
<因式>→<变量>|<常数>|(<算术表达式>)</br>
<关系表达式>→<算术表达式><关系符><算术表达式></br>
<变量>→<标识符></br>
<标识符>→<标识符><字母>|<标识符><数字>|<字母></br>
<常数>→<整数></br>
<整数>→0|<非零数字><泛整数></br>
<泛整数>→<数字>|<数字><泛整数>|ε</br>
<关系符>→<|<=|==|>|>=|<></br>
<字母></br>
→A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z</br>
<非零数字>→1|2|3|4|5|6|7|8|9</br>
<数字>→<非零数字>|0</br>
