package somepackage;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.*;

@Repeatable(AA.class)
@Retention(RUNTIME)
public @interface A {
    String value();
}
