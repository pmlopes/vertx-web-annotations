# Vert.x-Web Annotations

This is a basic annotation addon to Vert.x-Web in order to de-clutter your code.

There are annotations for all HTTP methods plus content negotiation.

## Usage:

Initially create a POJO and annotate the public methods with a RoutingContext as argument
with one of the annotations, e.g.: `GET`

```java
public class TestRouter {
  @GET("/ws")
  public void get(RoutingContext ctx) {
    ctx.response().end("Hello ws!");
  }
}
```

Now in your main Verticle you can transform this POJO to a router as:

```java
public class MyVerticle extends AbstactVerticle {

  @Override
  public void start() {
    final Router app = AnnotatedRouter.create(vertx, new TestRouter());

    vertx.createHttpServer()
      .requestHandler(app::accept)
      .listen(8080);
  }
}
```

If you want to compose your app from various POJOs, you can do it like:

```java
public class MyVerticle extends AbstactVerticle {

  @Override
  public void start() {
    Router app;

    app = AnnotatedRouter.create(vertx, new SomePOJO());
    // note that the first argument is now the previous Router
    app = AnnotatedRouter.append(app, new OtherPOJO());

    vertx.createHttpServer()
      .requestHandler(app::accept)
      .listen(8080);
  }
}
```

Alternatively you can specify all POJOs at once using a vararg param:

```java
public class MyVerticle extends AbstactVerticle {

  @Override
  public void start() {
    final Router app = AnnotatedRouter.create(vertx, new SomePOJO(), new OtherPOJO());

    vertx.createHttpServer()
      .requestHandler(app::accept)
      .listen(8080);
  }
}
```

## Design decisions

The POJO to be passed to the builder is a Object instance not a class, the reason for this is that
in some cases it is handy to have a constructor where you can pass arguments such as configuration,
vertx instance, etc....


## Extending this thing!

So you want more annotations, perhaps you want swagger generation ;) well in that case all you need
is to:

1. Create your custom annotation
2. Register a new processor for your annotation
3. Profit

### Example

#### Create a custom annotation

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Say {
  String value() default "Hello World";
}
```

#### Create a processor

```java
public class SayProcessorHandler extends AbstractAnnotationHandler<Router> {

  public SayProcessorHandler() {
    super(Router.class);
  }

  @Override
  public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {

    if (Processor.isCompatible(method, Say.class, RoutingContext.class)) {
      System.out.println("The annotation says: " + Processor.getAnnotation(method, Say.class).value());
    }
  }
}
```

As you see from the process method, you have the router object instance and your annotation so now
you can add your custom handlers if you wish so, e.g.:

```java
public class SayProcessorHandler extends AbstractAnnotationHandler<Router> {

  public SayProcessorHandler() {
    super(Router.class);
  }

  @Override
  public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {

    if (Processor.isCompatible(method, Say.class, RoutingContext.class)) {
      final String whatToSay = Processor.getAnnotation(method, Say.class).value();
      router.route().handler(ctx -> {
        System.out.println(whatToSay);
        ctx.next();
      });
    }
  }
}
```
