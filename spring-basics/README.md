## What is Spring

Spring is a lot of things, it's pretty ubiquitous in Java. It provides lots of nice mechanisms that have a genuine use. This repo concerns Spring itself rather than Spring Boot, Spring Boot is broadly speaker a layer that removes the large amount of boilerplate we will see Spring to entail. This configuration genuinely is quite tedious, and Spring Boot really is what people use, nonetheless it makes sense to learn Spring independently to have a full-fledged understanding of what it does as boot conceals a lot of it from you. Spring tries to do a great many things and its documentation is so so as a consequence of it having so much to say.


## What's a (Spring) Bean anyway

A Bean in Spring is really just any object managed in some form by Spring 'IoC container'. IoC being 'inversion of control' and container just meaning a place where things live. Informally put it's a place where objects go and get managed by Spring. When you want to get reference to one of these objects you ask Spring (nicely)

## Why would I want one of those

It is genuinely a way of supporting inversion of control, you don't have some client make its own database connection pool say, you make one somewhere else and it gets summoned via Spring.

## Basic example of a Bean
See BeanBasics.java plus GettingABean.java, we declare some class with @Configuration annotation, define a method in it marked @Bean and we can summon the result of that method. One of the genuinely nice things about beans is how they can get injected into other beans, the BeanyBabyParent Bean waits until the BeanyBaby instance is constructed and the constructor of BeanyBabyParent gets called using this instance, nice! In the provided example A bit of subtlety the example demonstrates is that the @Configuration marked class is itself a Spring bean, and the instance we can summon of that is one modified by Spring's proxy behaviour.

That example concerned annotation defined beans, which is the pleasasnt way of doing things, there is also a (very tedious looking) way of doing things by defining xml. We will speak no more of it as that is a truly legacy thing, noone seems to do it anymore. In spite of this the spring docs frequently phrase their bean definitions in the xml style.

## What else should I know about Beans
Not much, really... there's a few basic annotations which are used everywhere, @Autowired, @Bean, @Component, @Configuration, @DependsOn, @Inject etc.

Beans themselves aren't discovered in the way of our example where we explicitly said where some beans were defined, instead they're dynamically detected by looking at classpath using something like @ComponentScan, getThatBeanPackageScan method of GettingABean.java together with Scanner.java shows how that looks. In practical scenarios, things get really complicated and beans can be found from a great many places.

The debug logs for Spring tell you a huge number of details about what Spring is doing. This repo is setup such that you can see those logs, they say interesting stuff and are useful for finding out why some startup thing is failing.