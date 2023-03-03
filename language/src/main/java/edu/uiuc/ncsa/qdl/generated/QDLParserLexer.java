// Generated from QDLParser.g4 by ANTLR 4.9.1
package edu.uiuc.ncsa.qdl.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QDLParserLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, ConstantKeywords=12, ASSERT=13, ASSERT2=14, BOOL_FALSE=15, 
		BOOL_TRUE=16, BLOCK=17, LOCAL=18, BODY=19, CATCH=20, DEFINE=21, DO=22, 
		ELSE=23, IF=24, MODULE=25, Null=26, Null_Set=27, SWITCH=28, THEN=29, TRY=30, 
		WHILE=31, Integer=32, Decimal=33, SCIENTIFIC_NUMBER=34, Bool=35, STRING=36, 
		LeftBracket=37, RightBracket=38, Comma=39, Colon=40, SemiColon=41, LDoubleBracket=42, 
		RDoubleBracket=43, LambdaConnector=44, Times=45, Divide=46, PlusPlus=47, 
		Plus=48, MinusMinus=49, Minus=50, LessThan=51, GreaterThan=52, SingleEqual=53, 
		To_Set=54, LessEquals=55, MoreEquals=56, IsA=57, Equals=58, NotEquals=59, 
		RegexMatches=60, LogicalNot=61, Membership=62, IsDefined=63, ContainsKey=64, 
		Exponentiation=65, And=66, Or=67, Backtick=68, Percent=69, Tilde=70, Backslash=71, 
		Backslash2=72, Backslash3=73, Backslash4=74, Hash=75, Stile=76, TildeRight=77, 
		StemDot=78, UnaryMinus=79, UnaryPlus=80, Floor=81, Ceiling=82, FunctionMarker=83, 
		ASSIGN=84, Identifier=85, FuncStart=86, F_REF=87, FDOC=88, WS=89, COMMENT=90, 
		LINE_COMMENT=91;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "ConstantKeywords", "ASSERT", "ASSERT2", "BOOL_FALSE", 
			"BOOL_TRUE", "BLOCK", "LOCAL", "BODY", "CATCH", "DEFINE", "DO", "ELSE", 
			"IF", "MODULE", "Null", "Null_Set", "SWITCH", "THEN", "TRY", "WHILE", 
			"Integer", "Decimal", "SCIENTIFIC_NUMBER", "E", "SIGN", "Bool", "STRING", 
			"ESC", "UnicodeEscape", "HexDigit", "StringCharacters", "StringCharacter", 
			"LeftBracket", "RightBracket", "Comma", "Colon", "SemiColon", "LDoubleBracket", 
			"RDoubleBracket", "LambdaConnector", "Times", "Divide", "PlusPlus", "Plus", 
			"MinusMinus", "Minus", "LessThan", "GreaterThan", "SingleEqual", "To_Set", 
			"LessEquals", "MoreEquals", "IsA", "Equals", "NotEquals", "RegexMatches", 
			"LogicalNot", "Membership", "IsDefined", "ContainsKey", "Exponentiation", 
			"And", "Or", "Backtick", "Percent", "Tilde", "Backslash", "Backslash2", 
			"Backslash3", "Backslash4", "Hash", "Stile", "TildeRight", "StemDot", 
			"UnaryMinus", "UnaryPlus", "Floor", "Ceiling", "FunctionMarker", "ASSIGN", 
			"Identifier", "FuncStart", "F_REF", "AllOps", "FUNCTION_NAME", "FDOC", 
			"WS", "COMMENT", "LINE_COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "')'", "'('", "'\\/'", "'\u2229'", "'/\\'", "'\u222A'", 
			"'!'", "'\u00AC'", "'?'", null, "'assert'", "'\u22A8'", "'false'", "'true'", 
			"'block'", "'local'", "'body'", "'catch'", "'define'", "'do'", "'else'", 
			"'if'", "'module'", "'null'", "'\u2205'", "'switch'", "'then'", "'try'", 
			"'while'", null, null, null, null, null, "'['", "']'", "','", "':'", 
			"';'", null, null, null, null, null, "'++'", "'+'", "'--'", "'-'", "'<'", 
			"'>'", "'='", null, null, null, "'<<'", null, null, null, null, null, 
			null, null, "'^'", null, null, "'`'", null, "'~'", null, null, null, 
			null, "'#'", "'|'", null, "'.'", "'\u00AF'", "'\u207A'", "'\u230A'", 
			"'\u2308'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"ConstantKeywords", "ASSERT", "ASSERT2", "BOOL_FALSE", "BOOL_TRUE", "BLOCK", 
			"LOCAL", "BODY", "CATCH", "DEFINE", "DO", "ELSE", "IF", "MODULE", "Null", 
			"Null_Set", "SWITCH", "THEN", "TRY", "WHILE", "Integer", "Decimal", "SCIENTIFIC_NUMBER", 
			"Bool", "STRING", "LeftBracket", "RightBracket", "Comma", "Colon", "SemiColon", 
			"LDoubleBracket", "RDoubleBracket", "LambdaConnector", "Times", "Divide", 
			"PlusPlus", "Plus", "MinusMinus", "Minus", "LessThan", "GreaterThan", 
			"SingleEqual", "To_Set", "LessEquals", "MoreEquals", "IsA", "Equals", 
			"NotEquals", "RegexMatches", "LogicalNot", "Membership", "IsDefined", 
			"ContainsKey", "Exponentiation", "And", "Or", "Backtick", "Percent", 
			"Tilde", "Backslash", "Backslash2", "Backslash3", "Backslash4", "Hash", 
			"Stile", "TildeRight", "StemDot", "UnaryMinus", "UnaryPlus", "Floor", 
			"Ceiling", "FunctionMarker", "ASSIGN", "Identifier", "FuncStart", "F_REF", 
			"FDOC", "WS", "COMMENT", "LINE_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public QDLParserLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "QDLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2]\u029f\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6"+
		"\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r"+
		"\3\r\3\r\5\r\u00e8\n\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3"+
		"\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\27\3"+
		"\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3"+
		"\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\35\3\35\3\35\3\35\3"+
		"\35\3\35\3\35\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3 \3 \3 \3"+
		" \3 \3 \3!\6!\u014c\n!\r!\16!\u014d\3\"\3\"\3\"\3\"\3#\3#\3#\5#\u0157"+
		"\n#\3#\3#\5#\u015b\n#\3$\3$\3%\3%\3%\3%\5%\u0163\n%\3&\3&\5&\u0167\n&"+
		"\3\'\3\'\5\'\u016b\n\'\3\'\3\'\3(\3(\3(\5(\u0172\n(\3)\3)\6)\u0176\n)"+
		"\r)\16)\u0177\3)\3)\3)\3)\3)\3*\3*\3+\6+\u0182\n+\r+\16+\u0183\3,\3,\5"+
		",\u0188\n,\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\62\5\62\u0197"+
		"\n\62\3\63\3\63\3\63\5\63\u019c\n\63\3\64\3\64\3\64\5\64\u01a1\n\64\3"+
		"\65\3\65\3\66\3\66\3\67\3\67\3\67\38\38\39\39\39\3:\3:\3;\3;\3<\3<\3="+
		"\3=\3>\3>\3>\5>\u01ba\n>\3?\3?\3?\5?\u01bf\n?\3@\3@\3@\5@\u01c4\n@\3A"+
		"\3A\3A\3B\3B\3B\5B\u01cc\nB\3C\3C\3C\5C\u01d1\nC\3D\3D\3D\5D\u01d6\nD"+
		"\3E\3E\3F\3F\3G\3G\3H\3H\3I\3I\3J\3J\3J\5J\u01e5\nJ\3K\3K\3K\5K\u01ea"+
		"\nK\3L\3L\3M\3M\3N\3N\3O\3O\3O\5O\u01f5\nO\3P\3P\3P\3P\3P\5P\u01fc\nP"+
		"\3Q\3Q\3Q\3Q\3Q\5Q\u0203\nQ\3R\3R\3R\3R\3R\3R\3R\5R\u020c\nR\3S\3S\3T"+
		"\3T\3U\3U\3U\5U\u0215\nU\3V\3V\3W\3W\3X\3X\3Y\3Y\3Z\3Z\3[\3[\3\\\3\\\3"+
		"\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\\3\\"+
		"\5\\\u0237\n\\\3]\5]\u023a\n]\3]\3]\7]\u023e\n]\f]\16]\u0241\13]\3^\3"+
		"^\3^\3_\3_\3_\3_\3_\7_\u024b\n_\f_\16_\u024e\13_\3_\3_\3_\3_\5_\u0254"+
		"\n_\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`\3`"+
		"\3`\3`\3`\3`\5`\u0270\n`\3a\3a\7a\u0274\na\fa\16a\u0277\13a\3b\3b\7b\u027b"+
		"\nb\fb\16b\u027e\13b\3c\6c\u0281\nc\rc\16c\u0282\3c\3c\3d\3d\3d\3d\7d"+
		"\u028b\nd\fd\16d\u028e\13d\3d\3d\3d\3d\3d\3e\3e\3e\3e\7e\u0299\ne\fe\16"+
		"e\u029c\13e\3e\3e\3\u028c\2f\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13"+
		"\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61"+
		"\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G\2I\2K%M&O\2Q\2S\2U\2W\2Y\'["+
		"(])_*a+c,e-g.i/k\60m\61o\62q\63s\64u\65w\66y\67{8}9\177:\u0081;\u0083"+
		"<\u0085=\u0087>\u0089?\u008b@\u008dA\u008fB\u0091C\u0093D\u0095E\u0097"+
		"F\u0099G\u009bH\u009dI\u009fJ\u00a1K\u00a3L\u00a5M\u00a7N\u00a9O\u00ab"+
		"P\u00adQ\u00afR\u00b1S\u00b3T\u00b5U\u00b7V\u00b9W\u00bbX\u00bdY\u00bf"+
		"\2\u00c1\2\u00c3Z\u00c5[\u00c7\\\u00c9]\3\2\21\3\2\62;\4\2GGgg\t\2))^"+
		"^ddhhppttvv\5\2\62;CHch\6\2\f\f\17\17))^^\4\2,,\u00d9\u00d9\4\2\61\61"+
		"\u00f9\u00f9\4\2##\u00ae\u00ae\4\2\u2213\u2213\u2a0d\u2a0d\4\2\'\'\u2208"+
		"\u2208\4\2BB\u2299\u2299\13\2&&C\\aac|\u0393\u03ab\u03b3\u03cb\u03d3\u03d3"+
		"\u03d8\u03d8\u03f2\u03f3\n\2&&\62;C\\aac|\u0393\u03ab\u03b3\u03cb\u03d3"+
		"\u03d3\4\2\f\f\17\17\5\2\13\f\16\17\"\"\2\u02e0\2\3\3\2\2\2\2\5\3\2\2"+
		"\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2"+
		"\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3"+
		"\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3"+
		"\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3"+
		"\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2Y\3\2\2"+
		"\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2"+
		"g\3\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3"+
		"\2\2\2\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3"+
		"\2\2\2\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2\2"+
		"\2\u0089\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091"+
		"\3\2\2\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2"+
		"\2\2\u009b\3\2\2\2\2\u009d\3\2\2\2\2\u009f\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3"+
		"\3\2\2\2\2\u00a5\3\2\2\2\2\u00a7\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2"+
		"\2\2\u00ad\3\2\2\2\2\u00af\3\2\2\2\2\u00b1\3\2\2\2\2\u00b3\3\2\2\2\2\u00b5"+
		"\3\2\2\2\2\u00b7\3\2\2\2\2\u00b9\3\2\2\2\2\u00bb\3\2\2\2\2\u00bd\3\2\2"+
		"\2\2\u00c3\3\2\2\2\2\u00c5\3\2\2\2\2\u00c7\3\2\2\2\2\u00c9\3\2\2\2\3\u00cb"+
		"\3\2\2\2\5\u00cd\3\2\2\2\7\u00cf\3\2\2\2\t\u00d1\3\2\2\2\13\u00d3\3\2"+
		"\2\2\r\u00d6\3\2\2\2\17\u00d8\3\2\2\2\21\u00db\3\2\2\2\23\u00dd\3\2\2"+
		"\2\25\u00df\3\2\2\2\27\u00e1\3\2\2\2\31\u00e7\3\2\2\2\33\u00e9\3\2\2\2"+
		"\35\u00f0\3\2\2\2\37\u00f2\3\2\2\2!\u00f8\3\2\2\2#\u00fd\3\2\2\2%\u0103"+
		"\3\2\2\2\'\u0109\3\2\2\2)\u010e\3\2\2\2+\u0114\3\2\2\2-\u011b\3\2\2\2"+
		"/\u011e\3\2\2\2\61\u0123\3\2\2\2\63\u0126\3\2\2\2\65\u012d\3\2\2\2\67"+
		"\u0132\3\2\2\29\u0134\3\2\2\2;\u013b\3\2\2\2=\u0140\3\2\2\2?\u0144\3\2"+
		"\2\2A\u014b\3\2\2\2C\u014f\3\2\2\2E\u0153\3\2\2\2G\u015c\3\2\2\2I\u0162"+
		"\3\2\2\2K\u0166\3\2\2\2M\u0168\3\2\2\2O\u0171\3\2\2\2Q\u0173\3\2\2\2S"+
		"\u017e\3\2\2\2U\u0181\3\2\2\2W\u0187\3\2\2\2Y\u0189\3\2\2\2[\u018b\3\2"+
		"\2\2]\u018d\3\2\2\2_\u018f\3\2\2\2a\u0191\3\2\2\2c\u0196\3\2\2\2e\u019b"+
		"\3\2\2\2g\u01a0\3\2\2\2i\u01a2\3\2\2\2k\u01a4\3\2\2\2m\u01a6\3\2\2\2o"+
		"\u01a9\3\2\2\2q\u01ab\3\2\2\2s\u01ae\3\2\2\2u\u01b0\3\2\2\2w\u01b2\3\2"+
		"\2\2y\u01b4\3\2\2\2{\u01b9\3\2\2\2}\u01be\3\2\2\2\177\u01c3\3\2\2\2\u0081"+
		"\u01c5\3\2\2\2\u0083\u01cb\3\2\2\2\u0085\u01d0\3\2\2\2\u0087\u01d5\3\2"+
		"\2\2\u0089\u01d7\3\2\2\2\u008b\u01d9\3\2\2\2\u008d\u01db\3\2\2\2\u008f"+
		"\u01dd\3\2\2\2\u0091\u01df\3\2\2\2\u0093\u01e4\3\2\2\2\u0095\u01e9\3\2"+
		"\2\2\u0097\u01eb\3\2\2\2\u0099\u01ed\3\2\2\2\u009b\u01ef\3\2\2\2\u009d"+
		"\u01f4\3\2\2\2\u009f\u01fb\3\2\2\2\u00a1\u0202\3\2\2\2\u00a3\u020b\3\2"+
		"\2\2\u00a5\u020d\3\2\2\2\u00a7\u020f\3\2\2\2\u00a9\u0214\3\2\2\2\u00ab"+
		"\u0216\3\2\2\2\u00ad\u0218\3\2\2\2\u00af\u021a\3\2\2\2\u00b1\u021c\3\2"+
		"\2\2\u00b3\u021e\3\2\2\2\u00b5\u0220\3\2\2\2\u00b7\u0236\3\2\2\2\u00b9"+
		"\u0239\3\2\2\2\u00bb\u0242\3\2\2\2\u00bd\u0245\3\2\2\2\u00bf\u026f\3\2"+
		"\2\2\u00c1\u0271\3\2\2\2\u00c3\u0278\3\2\2\2\u00c5\u0280\3\2\2\2\u00c7"+
		"\u0286\3\2\2\2\u00c9\u0294\3\2\2\2\u00cb\u00cc\7}\2\2\u00cc\4\3\2\2\2"+
		"\u00cd\u00ce\7\177\2\2\u00ce\6\3\2\2\2\u00cf\u00d0\7+\2\2\u00d0\b\3\2"+
		"\2\2\u00d1\u00d2\7*\2\2\u00d2\n\3\2\2\2\u00d3\u00d4\7^\2\2\u00d4\u00d5"+
		"\7\61\2\2\u00d5\f\3\2\2\2\u00d6\u00d7\7\u222b\2\2\u00d7\16\3\2\2\2\u00d8"+
		"\u00d9\7\61\2\2\u00d9\u00da\7^\2\2\u00da\20\3\2\2\2\u00db\u00dc\7\u222c"+
		"\2\2\u00dc\22\3\2\2\2\u00dd\u00de\7#\2\2\u00de\24\3\2\2\2\u00df\u00e0"+
		"\7\u00ae\2\2\u00e0\26\3\2\2\2\u00e1\u00e2\7A\2\2\u00e2\30\3\2\2\2\u00e3"+
		"\u00e8\5!\21\2\u00e4\u00e8\5\37\20\2\u00e5\u00e8\5\65\33\2\u00e6\u00e8"+
		"\5\67\34\2\u00e7\u00e3\3\2\2\2\u00e7\u00e4\3\2\2\2\u00e7\u00e5\3\2\2\2"+
		"\u00e7\u00e6\3\2\2\2\u00e8\32\3\2\2\2\u00e9\u00ea\7c\2\2\u00ea\u00eb\7"+
		"u\2\2\u00eb\u00ec\7u\2\2\u00ec\u00ed\7g\2\2\u00ed\u00ee\7t\2\2\u00ee\u00ef"+
		"\7v\2\2\u00ef\34\3\2\2\2\u00f0\u00f1\7\u22aa\2\2\u00f1\36\3\2\2\2\u00f2"+
		"\u00f3\7h\2\2\u00f3\u00f4\7c\2\2\u00f4\u00f5\7n\2\2\u00f5\u00f6\7u\2\2"+
		"\u00f6\u00f7\7g\2\2\u00f7 \3\2\2\2\u00f8\u00f9\7v\2\2\u00f9\u00fa\7t\2"+
		"\2\u00fa\u00fb\7w\2\2\u00fb\u00fc\7g\2\2\u00fc\"\3\2\2\2\u00fd\u00fe\7"+
		"d\2\2\u00fe\u00ff\7n\2\2\u00ff\u0100\7q\2\2\u0100\u0101\7e\2\2\u0101\u0102"+
		"\7m\2\2\u0102$\3\2\2\2\u0103\u0104\7n\2\2\u0104\u0105\7q\2\2\u0105\u0106"+
		"\7e\2\2\u0106\u0107\7c\2\2\u0107\u0108\7n\2\2\u0108&\3\2\2\2\u0109\u010a"+
		"\7d\2\2\u010a\u010b\7q\2\2\u010b\u010c\7f\2\2\u010c\u010d\7{\2\2\u010d"+
		"(\3\2\2\2\u010e\u010f\7e\2\2\u010f\u0110\7c\2\2\u0110\u0111\7v\2\2\u0111"+
		"\u0112\7e\2\2\u0112\u0113\7j\2\2\u0113*\3\2\2\2\u0114\u0115\7f\2\2\u0115"+
		"\u0116\7g\2\2\u0116\u0117\7h\2\2\u0117\u0118\7k\2\2\u0118\u0119\7p\2\2"+
		"\u0119\u011a\7g\2\2\u011a,\3\2\2\2\u011b\u011c\7f\2\2\u011c\u011d\7q\2"+
		"\2\u011d.\3\2\2\2\u011e\u011f\7g\2\2\u011f\u0120\7n\2\2\u0120\u0121\7"+
		"u\2\2\u0121\u0122\7g\2\2\u0122\60\3\2\2\2\u0123\u0124\7k\2\2\u0124\u0125"+
		"\7h\2\2\u0125\62\3\2\2\2\u0126\u0127\7o\2\2\u0127\u0128\7q\2\2\u0128\u0129"+
		"\7f\2\2\u0129\u012a\7w\2\2\u012a\u012b\7n\2\2\u012b\u012c\7g\2\2\u012c"+
		"\64\3\2\2\2\u012d\u012e\7p\2\2\u012e\u012f\7w\2\2\u012f\u0130\7n\2\2\u0130"+
		"\u0131\7n\2\2\u0131\66\3\2\2\2\u0132\u0133\7\u2207\2\2\u01338\3\2\2\2"+
		"\u0134\u0135\7u\2\2\u0135\u0136\7y\2\2\u0136\u0137\7k\2\2\u0137\u0138"+
		"\7v\2\2\u0138\u0139\7e\2\2\u0139\u013a\7j\2\2\u013a:\3\2\2\2\u013b\u013c"+
		"\7v\2\2\u013c\u013d\7j\2\2\u013d\u013e\7g\2\2\u013e\u013f\7p\2\2\u013f"+
		"<\3\2\2\2\u0140\u0141\7v\2\2\u0141\u0142\7t\2\2\u0142\u0143\7{\2\2\u0143"+
		">\3\2\2\2\u0144\u0145\7y\2\2\u0145\u0146\7j\2\2\u0146\u0147\7k\2\2\u0147"+
		"\u0148\7n\2\2\u0148\u0149\7g\2\2\u0149@\3\2\2\2\u014a\u014c\t\2\2\2\u014b"+
		"\u014a\3\2\2\2\u014c\u014d\3\2\2\2\u014d\u014b\3\2\2\2\u014d\u014e\3\2"+
		"\2\2\u014eB\3\2\2\2\u014f\u0150\5A!\2\u0150\u0151\7\60\2\2\u0151\u0152"+
		"\5A!\2\u0152D\3\2\2\2\u0153\u015a\5C\"\2\u0154\u0156\5G$\2\u0155\u0157"+
		"\5I%\2\u0156\u0155\3\2\2\2\u0156\u0157\3\2\2\2\u0157\u0158\3\2\2\2\u0158"+
		"\u0159\5A!\2\u0159\u015b\3\2\2\2\u015a\u0154\3\2\2\2\u015a\u015b\3\2\2"+
		"\2\u015bF\3\2\2\2\u015c\u015d\t\3\2\2\u015dH\3\2\2\2\u015e\u0163\5o8\2"+
		"\u015f\u0163\5\u00afX\2\u0160\u0163\5s:\2\u0161\u0163\5\u00adW\2\u0162"+
		"\u015e\3\2\2\2\u0162\u015f\3\2\2\2\u0162\u0160\3\2\2\2\u0162\u0161\3\2"+
		"\2\2\u0163J\3\2\2\2\u0164\u0167\5!\21\2\u0165\u0167\5\37\20\2\u0166\u0164"+
		"\3\2\2\2\u0166\u0165\3\2\2\2\u0167L\3\2\2\2\u0168\u016a\7)\2\2\u0169\u016b"+
		"\5U+\2\u016a\u0169\3\2\2\2\u016a\u016b\3\2\2\2\u016b\u016c\3\2\2\2\u016c"+
		"\u016d\7)\2\2\u016dN\3\2\2\2\u016e\u016f\7^\2\2\u016f\u0172\t\4\2\2\u0170"+
		"\u0172\5Q)\2\u0171\u016e\3\2\2\2\u0171\u0170\3\2\2\2\u0172P\3\2\2\2\u0173"+
		"\u0175\7^\2\2\u0174\u0176\7w\2\2\u0175\u0174\3\2\2\2\u0176\u0177\3\2\2"+
		"\2\u0177\u0175\3\2\2\2\u0177\u0178\3\2\2\2\u0178\u0179\3\2\2\2\u0179\u017a"+
		"\5S*\2\u017a\u017b\5S*\2\u017b\u017c\5S*\2\u017c\u017d\5S*\2\u017dR\3"+
		"\2\2\2\u017e\u017f\t\5\2\2\u017fT\3\2\2\2\u0180\u0182\5W,\2\u0181\u0180"+
		"\3\2\2\2\u0182\u0183\3\2\2\2\u0183\u0181\3\2\2\2\u0183\u0184\3\2\2\2\u0184"+
		"V\3\2\2\2\u0185\u0188\n\6\2\2\u0186\u0188\5O(\2\u0187\u0185\3\2\2\2\u0187"+
		"\u0186\3\2\2\2\u0188X\3\2\2\2\u0189\u018a\7]\2\2\u018aZ\3\2\2\2\u018b"+
		"\u018c\7_\2\2\u018c\\\3\2\2\2\u018d\u018e\7.\2\2\u018e^\3\2\2\2\u018f"+
		"\u0190\7<\2\2\u0190`\3\2\2\2\u0191\u0192\7=\2\2\u0192b\3\2\2\2\u0193\u0194"+
		"\7]\2\2\u0194\u0197\7~\2\2\u0195\u0197\7\u27e8\2\2\u0196\u0193\3\2\2\2"+
		"\u0196\u0195\3\2\2\2\u0197d\3\2\2\2\u0198\u0199\7~\2\2\u0199\u019c\7_"+
		"\2\2\u019a\u019c\7\u27e9\2\2\u019b\u0198\3\2\2\2\u019b\u019a\3\2\2\2\u019c"+
		"f\3\2\2\2\u019d\u019e\7/\2\2\u019e\u01a1\7@\2\2\u019f\u01a1\7\u2194\2"+
		"\2\u01a0\u019d\3\2\2\2\u01a0\u019f\3\2\2\2\u01a1h\3\2\2\2\u01a2\u01a3"+
		"\t\7\2\2\u01a3j\3\2\2\2\u01a4\u01a5\t\b\2\2\u01a5l\3\2\2\2\u01a6\u01a7"+
		"\7-\2\2\u01a7\u01a8\7-\2\2\u01a8n\3\2\2\2\u01a9\u01aa\7-\2\2\u01aap\3"+
		"\2\2\2\u01ab\u01ac\7/\2\2\u01ac\u01ad\7/\2\2\u01adr\3\2\2\2\u01ae\u01af"+
		"\7/\2\2\u01aft\3\2\2\2\u01b0\u01b1\7>\2\2\u01b1v\3\2\2\2\u01b2\u01b3\7"+
		"@\2\2\u01b3x\3\2\2\2\u01b4\u01b5\7?\2\2\u01b5z\3\2\2\2\u01b6\u01b7\7~"+
		"\2\2\u01b7\u01ba\7`\2\2\u01b8\u01ba\7\u22a4\2\2\u01b9\u01b6\3\2\2\2\u01b9"+
		"\u01b8\3\2\2\2\u01ba|\3\2\2\2\u01bb\u01bc\7>\2\2\u01bc\u01bf\7?\2\2\u01bd"+
		"\u01bf\7\u2266\2\2\u01be\u01bb\3\2\2\2\u01be\u01bd\3\2\2\2\u01bf~\3\2"+
		"\2\2\u01c0\u01c1\7@\2\2\u01c1\u01c4\7?\2\2\u01c2\u01c4\7\u2267\2\2\u01c3"+
		"\u01c0\3\2\2\2\u01c3\u01c2\3\2\2\2\u01c4\u0080\3\2\2\2\u01c5\u01c6\7>"+
		"\2\2\u01c6\u01c7\7>\2\2\u01c7\u0082\3\2\2\2\u01c8\u01c9\7?\2\2\u01c9\u01cc"+
		"\7?\2\2\u01ca\u01cc\7\u2263\2\2\u01cb\u01c8\3\2\2\2\u01cb\u01ca\3\2\2"+
		"\2\u01cc\u0084\3\2\2\2\u01cd\u01ce\7#\2\2\u01ce\u01d1\7?\2\2\u01cf\u01d1"+
		"\7\u2262\2\2\u01d0\u01cd\3\2\2\2\u01d0\u01cf\3\2\2\2\u01d1\u0086\3\2\2"+
		"\2\u01d2\u01d3\7?\2\2\u01d3\u01d6\7\u0080\2\2\u01d4\u01d6\7\u224a\2\2"+
		"\u01d5\u01d2\3\2\2\2\u01d5\u01d4\3\2\2\2\u01d6\u0088\3\2\2\2\u01d7\u01d8"+
		"\t\t\2\2\u01d8\u008a\3\2\2\2\u01d9\u01da\4\u220a\u220b\2\u01da\u008c\3"+
		"\2\2\2\u01db\u01dc\4\u2205\u2206\2\u01dc\u008e\3\2\2\2\u01dd\u01de\t\n"+
		"\2\2\u01de\u0090\3\2\2\2\u01df\u01e0\7`\2\2\u01e0\u0092\3\2\2\2\u01e1"+
		"\u01e2\7(\2\2\u01e2\u01e5\7(\2\2\u01e3\u01e5\7\u2229\2\2\u01e4\u01e1\3"+
		"\2\2\2\u01e4\u01e3\3\2\2\2\u01e5\u0094\3\2\2\2\u01e6\u01e7\7~\2\2\u01e7"+
		"\u01ea\7~\2\2\u01e8\u01ea\7\u222a\2\2\u01e9\u01e6\3\2\2\2\u01e9\u01e8"+
		"\3\2\2\2\u01ea\u0096\3\2\2\2\u01eb\u01ec\7b\2\2\u01ec\u0098\3\2\2\2\u01ed"+
		"\u01ee\t\13\2\2\u01ee\u009a\3\2\2\2\u01ef\u01f0\7\u0080\2\2\u01f0\u009c"+
		"\3\2\2\2\u01f1\u01f2\7^\2\2\u01f2\u01f5\7#\2\2\u01f3\u01f5\7^\2\2\u01f4"+
		"\u01f1\3\2\2\2\u01f4\u01f3\3\2\2\2\u01f5\u009e\3\2\2\2\u01f6\u01f7\7^"+
		"\2\2\u01f7\u01f8\7#\2\2\u01f8\u01fc\7,\2\2\u01f9\u01fa\7^\2\2\u01fa\u01fc"+
		"\7,\2\2\u01fb\u01f6\3\2\2\2\u01fb\u01f9\3\2\2\2\u01fc\u00a0\3\2\2\2\u01fd"+
		"\u01fe\7^\2\2\u01fe\u01ff\7#\2\2\u01ff\u0203\7@\2\2\u0200\u0201\7^\2\2"+
		"\u0201\u0203\7@\2\2\u0202\u01fd\3\2\2\2\u0202\u0200\3\2\2\2\u0203\u00a2"+
		"\3\2\2\2\u0204\u0205\7^\2\2\u0205\u0206\7#\2\2\u0206\u0207\7@\2\2\u0207"+
		"\u020c\7,\2\2\u0208\u0209\7^\2\2\u0209\u020a\7@\2\2\u020a\u020c\7,\2\2"+
		"\u020b\u0204\3\2\2\2\u020b\u0208\3\2\2\2\u020c\u00a4\3\2\2\2\u020d\u020e"+
		"\7%\2\2\u020e\u00a6\3\2\2\2\u020f\u0210\7~\2\2\u0210\u00a8\3\2\2\2\u0211"+
		"\u0212\7\u0080\2\2\u0212\u0215\7~\2\2\u0213\u0215\7\u2243\2\2\u0214\u0211"+
		"\3\2\2\2\u0214\u0213\3\2\2\2\u0215\u00aa\3\2\2\2\u0216\u0217\7\60\2\2"+
		"\u0217\u00ac\3\2\2\2\u0218\u0219\7\u00b1\2\2\u0219\u00ae\3\2\2\2\u021a"+
		"\u021b\7\u207c\2\2\u021b\u00b0\3\2\2\2\u021c\u021d\7\u230c\2\2\u021d\u00b2"+
		"\3\2\2\2\u021e\u021f\7\u230a\2\2\u021f\u00b4\3\2\2\2\u0220\u0221\t\f\2"+
		"\2\u0221\u00b6\3\2\2\2\u0222\u0237\7\u2256\2\2\u0223\u0224\7<\2\2\u0224"+
		"\u0237\7?\2\2\u0225\u0237\7\u2257\2\2\u0226\u0227\7?\2\2\u0227\u0237\7"+
		"<\2\2\u0228\u0229\7-\2\2\u0229\u0237\7?\2\2\u022a\u022b\7/\2\2\u022b\u0237"+
		"\7?\2\2\u022c\u022d\5i\65\2\u022d\u022e\7?\2\2\u022e\u0237\3\2\2\2\u022f"+
		"\u0230\5k\66\2\u0230\u0231\7?\2\2\u0231\u0237\3\2\2\2\u0232\u0233\7\'"+
		"\2\2\u0233\u0237\7?\2\2\u0234\u0235\7`\2\2\u0235\u0237\7?\2\2\u0236\u0222"+
		"\3\2\2\2\u0236\u0223\3\2\2\2\u0236\u0225\3\2\2\2\u0236\u0226\3\2\2\2\u0236"+
		"\u0228\3\2\2\2\u0236\u022a\3\2\2\2\u0236\u022c\3\2\2\2\u0236\u022f\3\2"+
		"\2\2\u0236\u0232\3\2\2\2\u0236\u0234\3\2\2\2\u0237\u00b8\3\2\2\2\u0238"+
		"\u023a\7(\2\2\u0239\u0238\3\2\2\2\u0239\u023a\3\2\2\2\u023a\u023b\3\2"+
		"\2\2\u023b\u023f\t\r\2\2\u023c\u023e\t\16\2\2\u023d\u023c\3\2\2\2\u023e"+
		"\u0241\3\2\2\2\u023f\u023d\3\2\2\2\u023f\u0240\3\2\2\2\u0240\u00ba\3\2"+
		"\2\2\u0241\u023f\3\2\2\2\u0242\u0243\5\u00c1a\2\u0243\u0244\7*\2\2\u0244"+
		"\u00bc\3\2\2\2\u0245\u0253\5\u00b5[\2\u0246\u0254\5\u00bf`\2\u0247\u0248"+
		"\5\u00b9]\2\u0248\u0249\5\u00a5S\2\u0249\u024b\3\2\2\2\u024a\u0247\3\2"+
		"\2\2\u024b\u024e\3\2\2\2\u024c\u024a\3\2\2\2\u024c\u024d\3\2\2\2\u024d"+
		"\u024f\3\2\2\2\u024e\u024c\3\2\2\2\u024f\u0254\5\u00c1a\2\u0250\u0251"+
		"\5\u00bb^\2\u0251\u0252\7+\2\2\u0252\u0254\3\2\2\2\u0253\u0246\3\2\2\2"+
		"\u0253\u024c\3\2\2\2\u0253\u0250\3\2\2\2\u0254\u00be\3\2\2\2\u0255\u0270"+
		"\5i\65\2\u0256\u0270\5k\66\2\u0257\u0270\5o8\2\u0258\u0270\5s:\2\u0259"+
		"\u0270\5u;\2\u025a\u0270\5}?\2\u025b\u0270\5w<\2\u025c\u0270\5\u0091I"+
		"\2\u025d\u0270\5}?\2\u025e\u0270\5\177@\2\u025f\u0270\5\u0083B\2\u0260"+
		"\u0270\5\u0085C\2\u0261\u0270\5\u0093J\2\u0262\u0270\5\u0095K\2\u0263"+
		"\u0270\5\u0099M\2\u0264\u0270\5\u009bN\2\u0265\u0270\5\u00a9U\2\u0266"+
		"\u0270\5\u0089E\2\u0267\u0270\5\u0087D\2\u0268\u0270\5\u00b1Y\2\u0269"+
		"\u0270\5\u00b3Z\2\u026a\u0270\5\u008bF\2\u026b\u0270\5{>\2\u026c\u0270"+
		"\5\u0081A\2\u026d\u0270\5\u008dG\2\u026e\u0270\5\u008fH\2\u026f\u0255"+
		"\3\2\2\2\u026f\u0256\3\2\2\2\u026f\u0257\3\2\2\2\u026f\u0258\3\2\2\2\u026f"+
		"\u0259\3\2\2\2\u026f\u025a\3\2\2\2\u026f\u025b\3\2\2\2\u026f\u025c\3\2"+
		"\2\2\u026f\u025d\3\2\2\2\u026f\u025e\3\2\2\2\u026f\u025f\3\2\2\2\u026f"+
		"\u0260\3\2\2\2\u026f\u0261\3\2\2\2\u026f\u0262\3\2\2\2\u026f\u0263\3\2"+
		"\2\2\u026f\u0264\3\2\2\2\u026f\u0265\3\2\2\2\u026f\u0266\3\2\2\2\u026f"+
		"\u0267\3\2\2\2\u026f\u0268\3\2\2\2\u026f\u0269\3\2\2\2\u026f\u026a\3\2"+
		"\2\2\u026f\u026b\3\2\2\2\u026f\u026c\3\2\2\2\u026f\u026d\3\2\2\2\u026f"+
		"\u026e\3\2\2\2\u0270\u00c0\3\2\2\2\u0271\u0275\t\r\2\2\u0272\u0274\t\16"+
		"\2\2\u0273\u0272\3\2\2\2\u0274\u0277\3\2\2\2\u0275\u0273\3\2\2\2\u0275"+
		"\u0276\3\2\2\2\u0276\u00c2\3\2\2\2\u0277\u0275\3\2\2\2\u0278\u027c\7\u00bd"+
		"\2\2\u0279\u027b\n\17\2\2\u027a\u0279\3\2\2\2\u027b\u027e\3\2\2\2\u027c"+
		"\u027a\3\2\2\2\u027c\u027d\3\2\2\2\u027d\u00c4\3\2\2\2\u027e\u027c\3\2"+
		"\2\2\u027f\u0281\t\20\2\2\u0280\u027f\3\2\2\2\u0281\u0282\3\2\2\2\u0282"+
		"\u0280\3\2\2\2\u0282\u0283\3\2\2\2\u0283\u0284\3\2\2\2\u0284\u0285\bc"+
		"\2\2\u0285\u00c6\3\2\2\2\u0286\u0287\7\61\2\2\u0287\u0288\7,\2\2\u0288"+
		"\u028c\3\2\2\2\u0289\u028b\13\2\2\2\u028a\u0289\3\2\2\2\u028b\u028e\3"+
		"\2\2\2\u028c\u028d\3\2\2\2\u028c\u028a\3\2\2\2\u028d\u028f\3\2\2\2\u028e"+
		"\u028c\3\2\2\2\u028f\u0290\7,\2\2\u0290\u0291\7\61\2\2\u0291\u0292\3\2"+
		"\2\2\u0292\u0293\bd\2\2\u0293\u00c8\3\2\2\2\u0294\u0295\7\61\2\2\u0295"+
		"\u0296\7\61\2\2\u0296\u029a\3\2\2\2\u0297\u0299\n\17\2\2\u0298\u0297\3"+
		"\2\2\2\u0299\u029c\3\2\2\2\u029a\u0298\3\2\2\2\u029a\u029b\3\2\2\2\u029b"+
		"\u029d\3\2\2\2\u029c\u029a\3\2\2\2\u029d\u029e\be\2\2\u029e\u00ca\3\2"+
		"\2\2)\2\u00e7\u014d\u0156\u015a\u0162\u0166\u016a\u0171\u0177\u0183\u0187"+
		"\u0196\u019b\u01a0\u01b9\u01be\u01c3\u01cb\u01d0\u01d5\u01e4\u01e9\u01f4"+
		"\u01fb\u0202\u020b\u0214\u0236\u0239\u023f\u024c\u0253\u026f\u0275\u027c"+
		"\u0282\u028c\u029a\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}