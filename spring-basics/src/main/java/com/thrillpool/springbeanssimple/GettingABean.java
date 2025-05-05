package com.thrillpool.springbeanssimple;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class GettingABean {
    public void getThatBean() {
        AnnotationConfigApplicationContext annotationContext = new AnnotationConfigApplicationContext(BeanBasics.class);
        BeanyBaby b = annotationContext.getBean(BeanyBaby.class);
        BeanyBabyParent bp = annotationContext.getBean(BeanyBabyParent.class);
        BeanBasics yesThisIsAlsoABean = annotationContext.getBean(BeanBasics.class);
        System.out.println(b.x);
        System.out.println(bp.y);
        System.out.println(yesThisIsAlsoABean);
    }

    public void getThatBeanPackageScan() {
        AnnotationConfigApplicationContext annotationContext = new AnnotationConfigApplicationContext(Scanner.class);
        BeanyBaby b = annotationContext.getBean(BeanyBaby.class);
        System.out.println(b.x);
    }
}
