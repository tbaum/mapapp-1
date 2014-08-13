package mapapp;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author tbaum
 * @since 04.08.2014
 */
@BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
public @interface CacheDir {
}