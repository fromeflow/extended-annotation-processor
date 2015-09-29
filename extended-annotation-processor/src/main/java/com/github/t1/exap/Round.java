package com.github.t1.exap;

import java.lang.annotation.Annotation;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;

import com.github.t1.exap.reflection.Type;

public class Round {
    private final ProcessingEnvironment processingEnv;
    private final RoundEnvironment roundEnv;
    private final int roundNumber;

    public Round(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, int roundNumber) {
        this.processingEnv = processingEnv;
        this.roundEnv = roundEnv;
        this.roundNumber = roundNumber;
    }

    public List<Type> typesAnnotatedWith(Class<? extends Annotation> type) {
        List<Type> result = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(type))
            result.add(Type.of(element.asType(), processingEnv));
        return result;
    }

    public int number() {
        return roundNumber;
    }

    public boolean isLast() {
        return roundEnv.processingOver();
    }

    @Override
    public String toString() {
        return "Round#" + roundNumber + "-" + roundEnv.getRootElements();
    }
}
