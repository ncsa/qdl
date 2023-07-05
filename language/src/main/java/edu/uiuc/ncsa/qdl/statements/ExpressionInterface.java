package edu.uiuc.ncsa.qdl.statements;

/**
 * The actual top-level interface for an expression. This adds information about whether this is
 * executing in a module and has an alias.
 * <p>Created by Jeff Gaynor<br>
 * on 9/28/20 at  4:18 PM
 */
public interface ExpressionInterface extends Statement, HasResultInterface {
    public ExpressionInterface makeCopy(); // would prefer clone, but there is a conflict in packages because it has protected access

    boolean hasAlias();

    String getAlias();

    void setAlias(String alias);

    /**
     * QDL is not strongly typed but Java is, so each node type should have a unique
     * integer and conditionals should use that, not the instanceof operator. This allows
     * for efficient programming with switch statements rather than a bevy of conditionals
     * (potentially each of which
     * gets done even if the correct case has been handled).
     *
     * @return
     */
    // Fixes https://github.com/ncsa/qdl/issues/30
    int getNodeType();

    int UNKNOWN_NODE = 0;

    int ALL_INDICES_NODE = 1;
    int ALT_IF_NODE = 2;
    int ASSIGNMENT_NODE = 3;
    int CLOSED_SLICE_NODE = 4;
    int COMPARISON_DYAD_NODE = 5;
    int CONSTANT_NODE = 6;
    int DYAD_NODE = 7;
    int EXPRESSION_STEM2_NODE = 8;
    int EXPRESSION_STEM_NODE = 9;
    int FUNCTION_REFERENCE_NODE = 10;
    int LAMBDA_DEFINITION_NODE = 11;
    int LIST_NODE = 12;
    int MODULE_NODE = 13;
    int MONAD_NODE = 14;
    int NILAD_NODE = 15;
    int OPEN_SLICE_NODE = 16;
    int PARENTHESIZED_NODE = 17;
    int PARSE_EXPRESSION_BLOCK_NODE = 18;
    int PARSE_STATEMENT_BLOCK_NODE = 19;
    int POLYAD_NODE = 20;
    int QDL_NULL_NODE = 21;
    int SELECT_NODE = 22;
    int SET_NODE = 23;
    int STEM_ENTRY_NODE = 24;
    int STEM_EXTRACTION_NODE = 25;
    int STEM_NODE = 26;
    int VARIABLE_NODE = 27;
}
