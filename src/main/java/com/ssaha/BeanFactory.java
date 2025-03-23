package com.ssaha;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum BeanFactory {
    INSTANCE;

    // create a Thread safe Registry/Map
    private final ConcurrentMap<Class<?>, Object> registry = new ConcurrentHashMap<>();

  //method for instantiating a class with this factory
    public <T> T getInstanceOf(Class<T> beanClass, Object... arguments) {
        try{
            // Checking if the @Singleton annotation is on the beanClass or not
            if (beanClass.isAnnotationPresent(Singleton.class)){
                //checking if the registry has an instance of beanClass
                if (registry.containsKey(beanClass)) {
                    T exitingBean = (T) registry.get(beanClass);
                    return exitingBean;
                }

                //if not create a new instance of beanClass
                // add the new instance into the registry
                //putIfAbsent() is atomic in the case of a ConcurrentMap
                T newCreatedBean = instantiateBeanClass(beanClass, arguments);
                registry.putIfAbsent(beanClass, newCreatedBean);


                // returning the value that landed in the map/registry
                // it could be another one than ours
                return (T) registry.get(beanClass);

            } else {
                // if there is no @Singleton annotation is there
                T newCreatedBean = instantiateBeanClass(beanClass, arguments);
                return newCreatedBean;
            }


        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // creating the method to return the single Instance with reflection
    private <T> T instantiateBeanClass(Class<T> beanClass, Object[] arguments)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // crating the array of the parameter types
        Class<?>[] argumentClasses = Arrays.stream(arguments) //crating the stream of arguments/parameters
                .map(Object::getClass) // map to its class with Object.getClass()
                .toArray(Class<?>[]::new); // creating a Array to collect all the Classes

        // Now we will Locate the corresponding constructor for the classes
        Constructor<T> beanConstructor = beanClass.getConstructor(argumentClasses);

        // create new instance using the beanConstructor
        T newCreatedBean = beanConstructor.newInstance(arguments);

        // Getting the Fields of this bean

        // Filtering the fields that declare the @Inject annotation
        // From that Field get the Class
        // and creating an instance of that class using BeanFactory
        //set the Field with this instance created
        Field[] fields = newCreatedBean.getClass().getDeclaredFields();

        Field[] injectableFields = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .toArray(Field[]::new);

        for (Field injectabelField : injectableFields) {
            Class<?> fieldClass = injectabelField.getType();
            Object fieldValue = BeanFactory.INSTANCE.getInstanceOf(fieldClass);

            injectabelField.setAccessible(true);
            injectabelField.set(newCreatedBean,fieldValue);
        }

        // return the bean
        return newCreatedBean;
    }



    // creating the method to return the single Instance with reflection
    private <T> T instantiateBeanClassWithFieldInstantiation(Class<T> beanClass, Object[] arguments)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // crating the array of the parameter types
        Class<?>[] argumentClasses = Arrays.stream(arguments) //crating the stream of arguments/parameters
                .map(Object::getClass) // map to its class with Object.getClass()
                .toArray(Class<?>[]::new); // creating a Array to collect all the Classes

        // Now we will Locate the corresponding constructor for the classes
        Constructor<T> beanConstructor = beanClass.getConstructor(argumentClasses);

        // create new instance using the beanConstructor
        T newCreatedBean = beanConstructor.newInstance(arguments);

        // return the bean
        return newCreatedBean;
    }




}
