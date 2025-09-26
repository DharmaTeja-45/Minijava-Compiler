%{
    #include <bits/stdc++.h>
    #include "P1.tab.h"
    void yyerror(char *);
    extern int line_counter;
    using namespace std;
    extern int scope_counter;
%}

%%


"main"          { return MAIN ; }
"void"          { return VOID ; }
"if"            { return IF ; }
"else"          { return ELSE ; }
"while"         { return WHILE ; }
"import"        { return IMPORT ; }
"java"          { return JAVA ; }
"util"         { return UTIL ; }
"function"     { return f ; }
"Function"      { return FUNCTION ; }
"return"        { return RETURN ; }
"class"         { return CLASS;}
"public"        {return PUBLIC;}
"static"        {return STATIC;}
"System.out.println" {return SYSTEM;}
"extends"       {return EXTENDS;}
"boolean"       {return BOOLEAN;}
"do"            {return DO;}
"length"        {return LENGTH;}
"this"          {return THIS;}
"true"          {return TRUE;}
"false"         {return FALSE;}
"new"           {return NEW;}
"#define"       {return DEFINE;}
"String"        {return STRING;}

"int"           {return INT;}



"!"             { return '!';}
"<"             { return '<' ;}
">"             { return '>' ;}
"("             { return '('; }
")"             { return ')'; }
"*"             {return  '*'; }
"/"             { return '/'; }
"+"             { return '+'; }
"-"             { return '-'; }

"="             { return '='; }

"!="            { return NE; }

"<="            { return LE; }

"."             { return '.';}
"||"            { return  OR;}
"&&"            { return AND;}
"?"             { return '?';}
"{"             { scope_counter++;return '{' ; }
"}"             { scope_counter--;return '}' ; }
"["             { return '[' ; }
"]"             { return ']' ; }
";"             { return ';' ; }
","             { return ',' ; }
"->"            { return ARROW;}

0|[1-9][0-9]*   { yylval.val = strdup(yytext);return NUMBER ; }

[a-zA-Z_][a-zA-Z0-9_]* {yylval.val = strdup(yytext); return IDENTIFIER ; }

"/*"(.|\n)*?"*/" { /* ignore multi-line comments */ }
"//".*           { /* ignore single-line comments */ }


[ \t\r]           { /* ignore whitespace */ }
\n              { line_counter++; }

.               { }

%%

int yywrap(void) {
    return 1;
}
