package section02.lambda.dambda1;

import section02.lambda.Procedure;

public class ProcedureMain1 {

    public static void main(String[] args) {
        Procedure procedure = new Procedure() {

            @Override
            public void run() {
                System.out.println("hello! lambda");
            }
        };

        procedure.run();
    }
}
