package rules;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class RulesLoader {

    public List<Callable<ValudatioResult>> rules() throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        String rulesPackage = "rules";
        Enumeration<URL> resources = classLoader.getResources(rulesPackage);

        return stream(spliteratorUnknownSize(resources.asIterator(), ORDERED), false)
                .map(unchecked(URL::toURI))
                .map(File::new)
                .filter(file -> file.listFiles() != null)
                .flatMap(unchecked(file -> Arrays.stream(file.listFiles())))
                .filter(File::exists)
                .map(File::getName)
                .filter(name -> name.endsWith(".class"))
                .map(name -> name.substring(0, name.lastIndexOf('.')))
                .map(name -> createClass(rulesPackage, name))
                .filter(this::isRule)
                .map(this::createRule)
                .collect(toList());
    }

    private Class<?> createClass(String basePackage, String className) {
        try {
            return Class.forName(basePackage + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isRule(Class<?> clazz) {
        return clazz.isAnnotationPresent(Rule.class);
    }

    @SuppressWarnings("unchecked")
    private Callable<ValudatioResult> createRule(Class<?> clazz) {
        try {
            return (Callable<ValudatioResult>) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static <IN, OUT> Function<IN, OUT> unchecked(CheckedFunction<IN, OUT> f) {
        return x -> {
            try {
                return f.apply(x);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @FunctionalInterface
    private interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
