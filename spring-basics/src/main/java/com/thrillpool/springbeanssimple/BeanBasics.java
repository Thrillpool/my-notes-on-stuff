package com.thrillpool.springbeanssimple;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanBasics {
    @Bean
    BeanyBaby getBeanyBaby() {
        return new BeanyBaby(57);
    }

    @Bean
    BeanyBabyParent getBeanyBabyParent(BeanyBaby beanyBaby) {
        return new BeanyBabyParent(beanyBaby);
    }

}