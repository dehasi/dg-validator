package rules;

import java.util.concurrent.Callable;

@Rule
public class Rule1 implements Callable<ValudatioResult> {

    @Override
    public ValudatioResult call() throws Exception {
        return new ValudatioResult();
    }
}
