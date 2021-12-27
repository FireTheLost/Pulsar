package org.codepulsar.pulsar;

public enum TokenType {
    // Operators
    TK_NOT, // Unary
    TK_MINUS, TK_PLUS, TK_MULTIPLICATION, TK_DIVISION, TK_MODULUS, // Binary
    TK_NOT_EQUAL, TK_EQUAL_EQUAL, TK_GT, TK_GT_EQUAL, TK_LT, TK_LT_EQUAL, // Comparison
    TK_EQUAL, TK_PLUS_EQUAL, TK_MINUS_EQUAL, TK_MUL_EQUAL, TK_DIV_EQUAL, TK_MOD_EQUAL, // Assignment
    TK_LOGICAL_OR, TK_LOGICAL_AND, // Logical

    // Other Character Tokens
    TK_DOT, TK_COMMA, TK_COLON, TK_SEMICOLON, TK_DOUBLE_COLON, TK_QMARK,
    TK_LPAR, TK_RPAR, TK_LBRACE, TK_RBRACE,

    // Keywords
    TK_PACKAGE, TK_IMPORT, TK_MOD, TK_CLASS,
    TK_VAR, TK_CONST,
    TK_FUN, TK_RETURN,
    TK_IF, TK_ELSE, TK_WHILE,

    // Data Types and Variables
    TK_STRING, TK_INTEGER, TK_DOUBLE, TK_IDENTIFIER, TK_TRUE, TK_FALSE, TK_NULL,

    // Special Tokens
    TK_PRINT, TK_ERROR, TK_EOF
}
