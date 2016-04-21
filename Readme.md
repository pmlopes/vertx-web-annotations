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

