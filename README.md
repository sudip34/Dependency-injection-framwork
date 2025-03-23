Dependency-Injection-Framework
---
These frameworks provide 2 concept. 
* The concept of singleton, and 
* The dependency injection itself.

## What is a dependency Injection

Dependency Injection is used in application to make sure all business objects are correctly initialized, and all their 
`Fields` receive a correct value.

Instead of creating the objects your application needs yourself, you delegate this task to a factory. This factory can then reflectively explore your class, check if some fields need to be initialized, and if this is the case, do it for you. 

## Tasks

* Creating `@Singleton` annotation
* Creating BeanFactory
* BeanFactory needs to find `@Singleton` to get the correct instance and also register the instance in a **Registry**
* This Registry should be **Thread-Safe**
* Injecting dependency


## Creating a Bean Factory (for Singleton creation)

```

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

        // return the bean
        return newCreatedBean;
    }
}
```


### Creating Singletons

A very common pattern used in enterprise application is to have our factory to create singletons. 
This makes sense if your bean are used to access services like a database or some REST server. 
We can implement such a pattern in the BeanFactory class.

> We need to/ can define a `Annotation` for crating a **Singleton**  e.g. `@Singleton`


## Injecting Dependencies

* Create the annotation `@Inject`
* Factory need to put a correct **instance** to **the Field annotated with @Inject** 
* Refactor the Method `instantiateBeanClass()` in the Factory to scan the **field** annotated with `@Inject` and get **the correct instance**

> Inside the `instantiateBeanClass()` after creating a `bean` we need to **add** the following code

```
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
```



### use the annotation to create instances

```
@Singleton
public class MyApp {
    @Inject
    private DBService dbService;
    public boolean isDBServiceSet(){
        return dbService != null;
    }
}
```