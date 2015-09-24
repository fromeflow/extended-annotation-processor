package com.github.t1.exap.reflection;

import static javax.lang.model.element.Modifier.*;
import static javax.tools.Diagnostic.Kind.*;

import java.lang.annotation.Annotation;
import java.util.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.*;

import com.github.t1.exap.JavaDoc;

class Elemental {
    private final ProcessingEnvironment processingEnv;
    private final Element element;

    public Elemental(ProcessingEnvironment processingEnv, Element element) {
        this.processingEnv = processingEnv;
        this.element = element;
    }

    public ProcessingEnvironment env() {
        return processingEnv;
    }

    public Element getElement() {
        return element;
    }

    protected Elements elements() {
        return processingEnv.getElementUtils();
    }

    protected Types types() {
        return processingEnv.getTypeUtils();
    }

    public Messager messager() {
        return processingEnv.getMessager();
    }

    public void error(CharSequence message) {
        messager().printMessage(ERROR, message, getElement());
    }

    public void warning(CharSequence message) {
        messager().printMessage(WARNING, message, getElement());
    }

    public void note(CharSequence message) {
        messager().printMessage(NOTE, message, getElement());
    }

    public List<AnnotationType> getAnnotationTypes() {
        List<AnnotationType> result = new ArrayList<>();
        for (AnnotationMirror mirror : getElement().getAnnotationMirrors()) {
            TypeElement annotation = (TypeElement) mirror.getAnnotationType().asElement();
            result.add(new AnnotationType(annotation));
        }
        return result;
    }

    public boolean isPublic() {
        return is(PUBLIC);
    }

    public boolean isStatic() {
        return is(STATIC);
    }

    public boolean isTransient() {
        return is(TRANSIENT);
    }

    protected boolean is(Modifier modifier) {
        return getElement().getModifiers().contains(modifier);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean isAnnotated(Class<?> type) {
        return getAnnotation((Class) type) != null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> type) {
        T annotation = this.getElement().getAnnotation(type);
        if (annotation == null && JavaDoc.class.equals(type) && docComment() != null)
            return type.cast(javaDoc());
        return annotation;
    }

    private String docComment() {
        return elements().getDocComment(this.getElement());
    }

    private JavaDoc javaDoc() {
        return new JavaDoc() {
            // TODO provide JavaDoc-tags
            // TODO convert JavaDoc-HTML to Markdown
            private final String docComment = docComment();
            private final int firstSentence = docComment.indexOf('.');

            @Override
            public Class<? extends Annotation> annotationType() {
                return JavaDoc.class;
            }

            @Override
            public String summary() {
                return (firstSentence < 0) ? docComment : docComment.substring(0, firstSentence);
            }

            @Override
            public String value() {
                return docComment;
            }
        };
    }

    /**
     * We can't extract annotation values of type class in an annotation processor, as the class object generally is not
     * loaded, only the meta data as represented in the TypeMirrors. You'd get a
     * {@link javax.lang.model.type.MirroredTypeException} with the message: Attempt to access Class object for
     * TypeMirror.
     * <p>
     * This method returns the <b>fully qualified class name</b> of the annotation 'method' instead; or
     * <code>null</code>, if there is no such 'method' on the annotation.
     * 
     * @see <a href="http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor">
     *      this blog </a>
     */
    public <T extends Annotation> String getAnnotationClassAttribute(Class<T> annotationType, String name) {
        for (AnnotationMirror annotationMirror : elements().getAllAnnotationMirrors(element))
            if (annotationType.getName().contentEquals(annotationMirror.getAnnotationType().toString()))
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationProperty //
                : elements().getElementValuesWithDefaults(annotationMirror).entrySet())
                    if (annotationProperty.getKey().getSimpleName().contentEquals(name)) {
                        String className = annotationProperty.getValue().toString();
                        if (className.endsWith(".class"))
                            className = className.substring(0, className.length() - 6);
                        return className;
                    }
        return null;
    }
}
