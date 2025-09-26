%{
    #include <bits/stdc++.h>
    using namespace std;
    void yyerror(char *);
    int yylex(void);
    int scope_counter=0;
    int line_counter=1;
    class Macros{
        public:
        string args;
        string body;
        bool expr;
    };
    string tabs(int n);
    map <string, Macros> macro_map;
%}

%union {
    char* val;
    int valid;
}
%token <val> IDENTIFIER NUMBER
%type <val> Goal MacroDefinitions MacroDefinition TypeDeclarations Identifier
%type <val> MainClass TypeDeclaration classtemp Attributes Methods MethodDeclaration Arguements Temp
%type <val> Type array ifstatement FuncArguements Temp2 Statements Statement Expression PrimaryExpression
%type <val> Expressiontemp Identifiers temp3 ImportFunction
%token INT VOID IF WHILE RETURN MAIN DEFINE TRUE STRING AND OR ARROW
%token FALSE NEW THIS LENGTH DO BOOLEAN EXTENDS STATIC CLASS PUBLIC SYSTEM INTEGER
%token NE LE IMPORT JAVA UTIL FUNCTION f

%left '+' '-' 
%left '*' '/'  
%left NE LE
%nonassoc LOWER_THAN_ELSE
%nonassoc ELSE

%%

Goal:
    ImportFunction MacroDefinitions MainClass TypeDeclarations{
        printf("%s\n", (string($1) + string($3)+ (string)$4).c_str());
    }
    ;

MacroDefinitions:
      MacroDefinition MacroDefinitions
    |{$$="";}
    ;
ImportFunction	:	IMPORT JAVA '.' UTIL '.' f '.' FUNCTION ';' {
    $$= strdup(("import java.util.function.Function;\n" ));
    }
    | {$$="";}
    ;
TypeDeclarations:
    TypeDeclaration TypeDeclarations{$$= strdup(((string)$1 + (string)$2).c_str());}
    |{$$="";}
    ;
MainClass	:	
    CLASS Identifier '{'   PUBLIC STATIC VOID MAIN '('  STRING '['  ']'  Identifier ')'  '{'  SYSTEM '('   Expression ')' ';' '}' '}'
    {
        $$= strdup(("class " + (string)$2 + " { \n\tpublic static void main( String[] " + (string)$12 + ") {\n\t\tSystem.out.println ( " + (string)$17 + ");\n\t}\n}\n").c_str());
    }
    ;
TypeDeclaration	:	
     CLASS Identifier classtemp{$$= strdup(("class " + (string)$2 + (string)$3).c_str());}
    ;
classtemp:
       '{' Attributes Methods '}'{$$= strdup(("{\n" + (string)$2 + (string)$3 + tabs(scope_counter) + "}\n").c_str());}
    |	EXTENDS Identifier '{' Attributes Methods '}'{
        $$= strdup(("extends " + (string)$2 + "{\n" + (string)$4+ (string)$5 + tabs(scope_counter) + "}\n").c_str());
    }
    ;
Attributes:
       Attributes Type Identifier ';' {$$= strdup(((string)$1 + tabs(scope_counter)+ (string)$2 + (string)$3+ ";\n").c_str());}
    |{$$= "";}
    ;
Methods:
     MethodDeclaration Methods{$$= strdup(((string)$1 + (string)$2).c_str());}
    |{$$="";}
    ;

MethodDeclaration	:	
    PUBLIC Type Identifier '('  Arguements ')'  '{'  Attributes Statements RETURN Expression ';' '}'
    {$$= strdup(("\tpublic " + (string)$2 + (string)$3 + "( " + (string)$5 + ") {\n" + (string)$8 +(string)$9+ tabs(scope_counter+1) + "return " + (string)$11 + ";\n"+ tabs(scope_counter) + "}\n").c_str());}
    ;

Arguements:
     Type Identifier Temp{$$= strdup(((string)$1 + (string)$2 + (string)$3).c_str());}
    |{$$="";}
    ;
Temp:
       ',' Type Identifier Temp{$$= strdup((", " + (string)$2 + (string)$3+(string)$4).c_str());}
    | {$$= "";}
    ;

Type	:	
        INT  array{$$= strdup(("int " + (string)$2).c_str());}
    |	BOOLEAN{$$= strdup("boolean "); }
    |	Identifier{$$= strdup($1);}
    |	FUNCTION '<' Identifier ',' Identifier '>'{$$= strdup(("Function < " + (string)$3 + "," + (string)$5 + "> ").c_str());}
    ;

array:
    '['  ']' {$$= strdup("[] ");}
    | {$$="";}
    ;
Statement	:
       '{'  Statements '}'
       {$$= strdup(("{\n" + (string)$2 + tabs(scope_counter) + "}\n").c_str());}
    |	SYSTEM '('  Expression ')' ';' 
    {$$= strdup((tabs(scope_counter)+"System.out.println( " + (string)$3 + ");\n").c_str());}
    |	Identifier '=' Expression ';' 
    {$$= strdup((tabs(scope_counter)+(string)$1 + "= " + (string)$3 + ";\n").c_str());}
    |	Identifier '['  Expression ']'  '=' Expression ';' 
    {$$= strdup((tabs(scope_counter)+(string)$1 + "[ " + (string)$3 + "] = " + (string)$6 + ";\n").c_str());}
    |   IF '('  Expression ')'  Statement ifstatement
    {$$= strdup((tabs(scope_counter)+"if ( " + (string)$3 + ") " + (string)$5+ (string)$6).c_str());}
    |	DO Statement WHILE '(' Expression ')' ';'
    {$$= strdup((tabs(scope_counter)+"do " + (string)$2 + " while ( " + (string)$5 + ") ;\n").c_str());  }
    |	WHILE '('  Expression ')'  Statement
    {$$= strdup((tabs(scope_counter)+"while ( " + (string)$3 + ") " + (string)$5).c_str());}
    |	Identifier '('  FuncArguements ')' ';' {
        string id= $1; 
        if(macro_map.find(id)!=macro_map.end() && !macro_map[id].expr){
            Macros m= macro_map[id];
            vector<string> args;
            stringstream ss1(m.args);
            string token;
            while(getline(ss1, token, ',')){
                token.pop_back();
                args.push_back(token);
            }
            vector<string> values;
            stringstream ss2(string($3));
            while(getline(ss2, token, ',')){
                token.pop_back();
                values.push_back(token);
            }
            if(args.size()!=values.size()){
                yyerror("");
            }
            unordered_map<string, string> argmap;
            for(int i=0;i<(int)args.size();i++){
                argmap[args[i]]= values[i];
            }
            string input= m.body;
            stringstream ss(input);
            string result;

            while (ss >> token) { 
                if (argmap.find(token) != argmap.end()) {
                    result += argmap[token];
                } else {
                    result += token;
                }
                result += " ";
            }
            $$= strdup(((tabs(scope_counter)+ result+"\n").c_str()));
        }
        else{
            yyerror("");
        }
    }
    ;

ifstatement:
    ELSE Statement{$$= strdup((tabs(scope_counter)+"else " + (string)$2).c_str());}
    | %prec LOWER_THAN_ELSE {$$="";}
    ;
FuncArguements:
       Expression Temp2{$$= strdup(((string)$1 + (string)$2).c_str());}
    |   { $$= ""; }
    ;
Temp2:
       ',' Expression Temp2{$$= strdup((", " + (string)$2 + (string)$3).c_str());}
    |   { $$= ""; }
    ;
Statements:
    Statement Statements{$$= strdup(((string)$1 + (string)$2).c_str());}
    | { $$= ""; }
    ;
Expression	:	
    PrimaryExpression Expressiontemp{$$= strdup(((string)$1 + (string)$2).c_str());}
    |	Identifier '('  FuncArguements ')' {
        string id= string($1);
        if(macro_map.find(id)!=macro_map.end() && macro_map[id].expr){
            Macros m= macro_map[id];
            vector<string> args;
            stringstream ss1(m.args);
            string token;
            while(getline(ss1, token, ',')){
                token.pop_back();
                args.push_back(token);
            }
            vector<string> values;
            stringstream ss2(string($3));
            while(getline(ss2, token, ',')){
                token.pop_back();
                values.push_back(token);
            }
            if(args.size()!=values.size()){
                yyerror("");
            }
            unordered_map<string, string> argmap;
            for(int i=0;i<(int)args.size();i++){
                argmap[args[i]]= values[i];
            }
            string input= m.body;
            stringstream ss(input);
            string result;

            while (ss >> token) {
                if (argmap.find(token) != argmap.end()) {

                    result += "("+argmap[token]+")";
                } else {
                    result += token;
                }
                result += " ";
            }

            $$= strdup((result).c_str());
        }
        else{
            yyerror("");
        }
    }
    |	'(' Identifier ')'  ARROW Expression{$$= strdup(("( " + (string)$2 + ") -> " + (string)$5).c_str());}
    ;
Expressiontemp:
    AND  PrimaryExpression {$$= strdup(("&& " + (string)$2).c_str());}
    |OR PrimaryExpression {$$= strdup(("|| " + (string)$2).c_str());}
    |NE PrimaryExpression {$$= strdup(("!= " + (string)$2).c_str());}
    |LE PrimaryExpression {$$= strdup(("<= " + (string)$2).c_str());}
    |'+' PrimaryExpression {$$= strdup(("+ " + (string)$2).c_str());}
    |'-' PrimaryExpression {$$= strdup(("- " + (string)$2).c_str());}
    |'*' PrimaryExpression {$$= strdup(("* " + (string)$2).c_str());}
    |'/' PrimaryExpression {$$= strdup(("/ " + (string)$2).c_str());}
    |'['  PrimaryExpression ']' {$$= strdup(("[ " + (string)$2 + "] ").c_str());}
    |'.' LENGTH {$$= strdup(". length ");}
    |'.' Identifier '('   FuncArguements ')' {
        $$= strdup((". " + (string)$2 + "( " + (string)$4 + ") ").c_str());
    }
    |{$$= "";}
    ;
PrimaryExpression	:	
    NUMBER{$$= strdup(((string)$1+" ").c_str());} 
    |	TRUE{$$= strdup("true ");} 
    |	FALSE{$$= strdup("false ");} 
    |	Identifier{$$= strdup($1);}
    |	THIS{$$= strdup("this ");} 
    |	NEW INT '['  Expression ']'{$$= strdup(("new int [ " + (string)$4 + "] ").c_str());}
    |	NEW Identifier '('  ')'{$$= strdup(("new " + (string)$2 + "() ").c_str());}
    |	'!' Expression{$$= strdup(("! " + (string)$2).c_str());}
    |    '('  Identifier ')'{$$= strdup(("( " + (string)$2 + ") ").c_str());}
    |	'('  Expression ')'{$$= strdup(("( " + (string)$2 + ") ").c_str());}
    ;
MacroDefinition	:	
    DEFINE Identifier '('  Identifiers ')' '{'  Statements '}'{
        Macros m;
        m.args= string($4);
        m.body= string($7);
        m.expr= false;
        macro_map[$2]= m;
    }
    |	DEFINE Identifier '('  Identifiers ')' '('  Expression ')' {
        Macros m;
        m.args= string($4);
        m.body= string($7);
        m.expr= true;
        macro_map[$2]= m;
    }
    ;

Identifiers:
    Identifier temp3{$$= strdup(((string)$1 + (string)$2).c_str());}
    |{$$= "";}
    ;
temp3:
    ','  Identifier temp3{$$= strdup(("," + (string)$2 + (string)$3).c_str());}
    |{$$= "";}
    ;
Identifier	:	
    IDENTIFIER {
        $$= strdup(((string)$1 + " ").c_str());
    }
    ;

%%

inline string tabs(int n){
    string s= "";
    for(int i=0;i<n;i++)s+= "\t";
    return s;
}
void yyerror(char *s) {
    printf("// Failed to parse macrojava code.\n");
    exit(1);
}

int main(void) {
    return yyparse();
}