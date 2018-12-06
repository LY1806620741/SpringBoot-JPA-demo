package com.example.demo.tools;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.compile.RenderingContext;
import org.hibernate.query.criteria.internal.expression.function.BasicFunctionExpression;

public class StaticExpression extends BasicFunctionExpression {

    @SuppressWarnings("unchecked")
    public StaticExpression(CriteriaBuilderImpl criteriaBuilder, Class javaType, String functionName) {
        super(criteriaBuilder, javaType, functionName);
    }

    @Override
    public String render(RenderingContext renderingContext) {
        return getFunctionName();
    }

}
