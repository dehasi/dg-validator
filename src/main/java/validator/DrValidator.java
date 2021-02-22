package validator;

import rules.RulesLoader;
import rules.ValudatioResult;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DrValidator {

    public static void main(String[] args) throws Exception {
        new DrValidator().run();
    }

    private void run() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Callable<ValudatioResult>> rules = new RulesLoader().rules();

        List<Future<ValudatioResult>> result = executor.invokeAll(rules);

        // process result
    }
}
