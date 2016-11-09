package com.aminelaadhari.squidb.nullable;

import com.yahoo.aptutils.model.DeclaredTypeName;

public class AnnotatedDeclaredTypeName extends DeclaredTypeName {
    private final String annotation;

    public AnnotatedDeclaredTypeName(DeclaredTypeName declaredTypeName, String annotation) {
        super(declaredTypeName.getPackageName(), declaredTypeName.getSimpleName());
        this.annotation = annotation;
    }

    public AnnotatedDeclaredTypeName(String packageName, String simpleName, String annotation) {
        super(packageName, simpleName);
        this.annotation = annotation;
    }

    public AnnotatedDeclaredTypeName(String fullyQualifiedName, String annotation) {
        super(fullyQualifiedName);
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public String toString() {
        if (annotation == null || annotation.isEmpty()) {
            return super.toString();
        } else {
            return annotation + " " + super.toString();
        }
    }

    @Override
    public String getSimpleName() {
        if (annotation == null || annotation.isEmpty()) {
            return super.getSimpleName();
        } else {
            return annotation + " " + super.getSimpleName();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPackageName() == null) ? 0 : getPackageName().hashCode());
        result = prime * result + ((getSimpleName() == null) ? 0 : getSimpleName().hashCode());
        result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AnnotatedDeclaredTypeName other = (AnnotatedDeclaredTypeName) obj;
        if (getPackageName() == null) {
            if (other.getPackageName() != null) {
                return false;
            }
        } else if (!getPackageName().equals(other.getPackageName())) {
            return false;
        }
        if (getSimpleName() == null) {
            if (other.getSimpleName() != null) {
                return false;
            }
        } else if (!getSimpleName().equals(other.getSimpleName())) {
            return false;
        }
        if (annotation == null) {
            if (other.getAnnotation() != null) {
                return false;
            }
        } else if (!annotation.equals(other.getAnnotation())) {
            return false;
        }
        return true;
    }
}
