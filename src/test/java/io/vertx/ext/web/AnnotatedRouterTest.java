package io.vertx.ext.web;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.annotations.GET;
import io.vertx.ext.web.annotations.Produces;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class AnnotatedRouterTest {

  private static Vertx vertx = Vertx.vertx();

  public static class TestRouter {
    @GET("/ws")
    public void get(RoutingContext ctx) {
      ctx.response().end("Hello ws!");
    }
  }

  @Test
  public void testAnnotatedRouter1(TestContext ctx) {

    final Async async = ctx.async();

    final Router app = AnnotatedRouter.create(vertx, new TestRouter());

    vertx.createHttpServer().requestHandler(app::accept).listen(8080, server -> {
      if (server.failed()) {
        ctx.fail(server.cause());
        return;
      }

      vertx.createHttpClient().get(8080, "localhost", "/ws").exceptionHandler(ctx::fail).handler(res -> {
        ctx.assertEquals(200, res.statusCode());
        res.bodyHandler(buff -> {
          ctx.assertEquals("Hello ws!", buff.toString());

          server.result().close(v -> {
            if (v.failed()) {
              ctx.fail(v.cause());
              return;
            }
            async.complete();
          });
        });
      }).end();
    });

    async.await();
  }

  public static class TestRouter2 {
    @GET("/ws")
    @Produces({"text/plain"})
    public void get(RoutingContext ctx) {
      ctx.response().end("Hello ws!");
    }
  }

  @Test
  public void testAnnotatedRouter2(TestContext ctx) {

    final Async async = ctx.async();

    final Router app = AnnotatedRouter.create(vertx, new TestRouter2());

    vertx.createHttpServer().requestHandler(app::accept).listen(8080, server -> {
      if (server.failed()) {
        ctx.fail(server.cause());
        return;
      }

      vertx.createHttpClient().get(8080, "localhost", "/ws").exceptionHandler(ctx::fail).handler(res -> {
        ctx.assertEquals(200, res.statusCode());
        ctx.assertEquals("text/plain", res.getHeader("Content-Type"));
        res.bodyHandler(buff -> {
          ctx.assertEquals("Hello ws!", buff.toString());

          server.result().close(v -> {
            if (v.failed()) {
              ctx.fail(v.cause());
              return;
            }
            async.complete();
          });
        });
      }).end();
    });

    async.await();
  }
}
