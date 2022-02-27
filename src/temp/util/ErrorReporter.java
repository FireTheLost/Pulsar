package temp.util;

import temp.lang.CompilerError;

public class ErrorReporter {
    public static void report(CompilerError errors, String code) {
        if (!errors.hasError()) {
            return;
        }

        reportErrors(errors, code);
    }

    private static void reportErrors(CompilerError errors, String code) {
        System.out.println("\n-- Errors --\n");

        String[] lines = code.split(System.getProperty("line.separator"));

        for (CompilerError.Error error: errors.getErrors()) {
            System.out.print(error.getErrorType() + " | ");
            System.out.println(error.getMessage());
            System.out.println("Line " + error.getToken().getLine() + ": " + lines[error.getToken().getLine() - 1] + "\n");
        }
    }
}