package section02.lambda.dambda1;

import section02.lambda.Procedure;

public class ProcedureMain2 {

    public static void main(String[] args) {
        Procedure procedure = () -> {
            System.out.println("hello! lambda");
        };

        procedure.run();
    }
}
