/*
   Top-level for the QDL language. This should contain only imports, no actual grammar
*/
grammar QDLParser;

import QDLLexer;


elements : element* EOF;

element : statement ';' ;

statement :
            defineStatement
          | conditionalStatement
          | loopStatement
          | switchStatement
          | expression
          | tryCatchStatement
          | lambdaStatement
          | assertStatement
          | assertStatement2
          | blockStatement
          | localStatement
          | moduleStatement
          ;

 conditionalStatement : ifStatement | ifElseStatement;
                   
ifStatement  :
     IF conditionalBlock THEN? statementBlock;

ifElseStatement :
	  IF conditionalBlock THEN? statementBlock ELSE statementBlock;

loopStatement:
     WHILE conditionalBlock DO? statementBlock ;

switchStatement:
    SWITCH '['  (ifStatement ';')* ']';

defineStatement:
     DEFINE '[' function ']' BODY? docStatementBlock;

lambdaStatement:
    //  function LambdaConnector fdoc* (statement) | statementBlock;
      function LambdaConnector (BLOCK | LOCAL)? docStatementBlock;

moduleStatement:
   MODULE LeftBracket STRING (',' STRING)? RightBracket BODY? docStatementBlock;
  //   MODULE LeftBracket (URL|STRING) (',' STRING)? RightBracket BODY? docStatementBlock;

tryCatchStatement:
     TRY statementBlock CATCH statementBlock;

  assertStatement :
       ASSERT LeftBracket expression RightBracket LeftBracket expression RightBracket;

   blockStatement:
       BLOCK statementBlock;

   localStatement:
      LOCAL statementBlock;

assertStatement2:
  ASSERT2 expression (':' expression)?;
                  
    statementBlock : LeftBracket (statement ';')* RightBracket;
 docStatementBlock : LeftBracket fdoc* (statement ';')+ RightBracket;
   expressionBlock : LeftBracket expression ';' ( expression ';')+ RightBracket;
  conditionalBlock : LeftBracket expression RightBracket;
              fdoc : FDOC;

   iInterval : LeftBracket expression? ';' expression (';' | (';' expression))? RightBracket;
   rInterval : LDoubleBracket expression? ';' expression (';' | (';' expression))? RDoubleBracket;
//   typeList : Type_List;

          set : '{' expression (',' expression)* '}'  | '{' '}';
 stemVariable : '{' stemEntry (',' stemEntry)* '}';
    stemEntry : (Times | expression) ':' stemValue;
     stemList : '[' stemValue (',' stemValue)* ']'
              | '[' ']';
       
    stemValue : expression
              | stemVariable
              | stemList;

     function : FuncStart f_args? ')';
       op_ref : OP_REF;
 //       f_arg : (stemValue | f_ref);
       f_args : stemValue (',' stemValue)* ;
    //    f_ref : F_REF;
//         url : (Identifier Colon)  ((Identifier) Divide)* (Identifier)?;
   //      url : URL;
        // The next few lines are kept for reference to see what not to do.


        // The next were to promote @ to be an operator in its own right. Also not much payoff
        // for a big rewrite.
        // f_ref : FunctionMarker (AllOps | FUNCTION_NAME  | (FuncStart ')'));
        // f_ref : FunctionMarker (AllOps | FUNCTION_NAME | (FuncStart ')'));  // This allows for @f and @f() as equivalent.

// Again, the order here has been tweaked and any changes to this list will require running all the tests
// and checking for regression. Also Antlr 4 interprets the # tag in the right hand column and
// will use these for generating method names in Java. Be careful of altering these, they are
// parser directives, not comments!

my_integer : Integer;
expression
 :
   function                                                                    #functions
 | expression op=FunctionMarker expression                                     #dyadicFunctionRefernce
 //| expression '::' expression                                                  #arg_concat
 | variable? Hash expression                                                   #moduleExpression
 | expression StemDot+ expression                                              #dotOp
 | expression StemDot Times                                                    #stemDefaultValue
 | StemDot my_integer                                                          #decimalNumber2
 | FunctionMarker expression                                                   #functionReference // REUSED
 | expression postfix=StemDot                                                  #dotOp2
 | expression Backslash  expression                                            #extract
 | expression postfix=Backslash2                                               #extract2
 | expression Backslash3  expression                                           #extract3
 | expression postfix=Backslash4                                               #extract4
 | expression op=Apply expression                                              #appliesOperator
 | Apply expression                                                            #unaryApplyExpression
 | stemVariable                                                                #stemVar
 | stemList                                                                    #stemLi
 | set                                                                         #setThing
 | rInterval                                                                   #realInterval
 | iInterval                                                                   #intInterval
 | To_Set expression                                                           #toSet
 | expression (Tilde | TildeRight ) expression                                 #tildeExpression
 | expression postfix=(PlusPlus | MinusMinus)                                  #postfix
 | prefix=(PlusPlus | MinusMinus) expression                                   #prefix
 | prefix= Nroot  expression                                                   #squartExpression
 | expression (Exponentiation | Nroot) expression                              #powerExpression
// Comment -- do set ops here since doing it in the lexer causes issues with / and /\ not being distinct.
// Keep lexical tokens separate and just glom them together here
 | expression op=('\\/' | '∩' | '/\\' | '∪') expression                        #intersectionOrUnion
 | expression op=(Times | Divide | Percent ) expression                        #multiplyExpression
 | (Floor | Ceiling) expression                                                #floorOrCeilingExpression
 | (Plus | UnaryPlus | Minus | UnaryMinus) expression                          #unaryMinusExpression
 | expression op=(Plus | Minus ) expression                                    #addExpression
 | expression op=(LessThan | GreaterThan | LessEquals | MoreEquals) expression #compExpression
 | expression op=(Equals | NotEquals) expression                               #eqExpression
 | expression op=RegexMatches expression                                       #regexMatches
 | expression op=Excise expression                                             #excise
 | expression '<<' expression                                                  #is_a
 | '(' expression ')'                                                          #association
//| expression '&'+ expression                                                  #typeCheck
// | expression '`'+ expression                                                  #index
 | expression '`' (Times | expression )                                                  #axis
// | prefix=',' expression                                                       #unravel
// | expression ((Stile + expression ) | (Stile '*'))                            #restriction
// Fix https://github.com/ncsa/qdl/issues/97
 | expression op=Membership expression                                         #epsilon  // unicode 2208, 2209
 | expression op=ContainsKey expression                                        #containsKey  // unicode 220b, 220c
 | IsDefined expression                                                        #isDefinedExpression
 | expression op=IsDefined expression                                          #isDefinedDyadicExpression
 | ('!'  | '¬') expression                                                     #notExpression
 | expression And expression                                                   #andExpression
 | expression Or expression                                                    #orExpression
 | expression AltIfMarker expression (':' expression)?                         #altIFExpression
 | expression SwitchMarker expression (':' expression)?                        #switchExpression
 // Note that we cannot have something like a lambda on the lhs of ∀
 // because the parser won't quite flag it right.
 | expression op=ForAll expression                                             #forAll  // unicode 2200
 | expression op=Transpose expression                                          #transposeOperator
 | expression op=ExprDyadicOps expression                                      #expressionDyadicOps
 | expression op=FRefDyadicOps expression                                      #frefDyadicOps
 | (Tilde | TildeRight) expression                                             #unaryTildeExpression
 | (Excise) expression                                                         #notTildeExpression
 | Transpose expression                                                        #unaryTransposeExpression
 | STRING                                                                      #strings
 |  op_ref                                                                     #operatorReference
  | integer                                                                    #integers
 | number                                                                      #numbers
 | variable                                                                    #variables
 | keyword                                                                     #keywords
 | Bool                                                                        #logical
 | Null                                                                        #null
 //| url                                                                         #url2
 | expression  op=ASSIGN  expression                                           #assignment
 | (function | '(' f_args* ')') LambdaConnector expression                     #lambdaDef
 // removed the next expression (and keeping it for reference wit this comment) because empty expressions were
 // being misinterpreted inside of slices in certain edge cases. It is better to just get errors if a
 // user values in something like ;;;; rather than have wrong slices.
 //| ';'                                                                         #semi_for_empty_expressions
 ;


       variable : Identifier ;
  //complex_number: (Decimal |  SCIENTIFIC_NUMBER) 'J' (Decimal |  SCIENTIFIC_NUMBER);
         number : Decimal |  SCIENTIFIC_NUMBER;
//         number : Decimal |  SCIENTIFIC_NUMBER | COMPLEX_NUMBER;
        integer : Integer;

   keyword : ConstantKeywords;


