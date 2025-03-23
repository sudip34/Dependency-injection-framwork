package com.ssaha;

import static java.io.IO.println;

public class Main {
    public static void main(String[] args) {
        BeanFactory beanFactory = BeanFactory.INSTANCE;

        Message message = beanFactory.getInstanceOf(Message.class, "Hello");
        println("Message  = " + message);

        DBService dbService1 = beanFactory.getInstanceOf(DBService.class);
        DBService dbService2 = beanFactory.getInstanceOf(DBService.class);
        println("Instances of DBService are the same? " + (dbService1 == dbService2));


        MyApp app = BeanFactory.INSTANCE.getInstanceOf(MyApp.class);

        println("App has been created with a DBService : " + app.isDBServiceSet());
    }
}