/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.vertx.ext.web.annotations.impl;

import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.annotations.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class ContentNegotiationProcessorHandler extends AbstractAnnotationHandler<Router> {

  private static final Comparator<String> ACCEPT_X_COMPARATOR = new Comparator<String>() {
    float getQuality(String s) {
      if (s == null) {
        return 0;
      }

      String[] params = s.split(" *; *");
      for (int i = 1; i < params.length; i++) {
        String[] q = params[1].split(" *= *");
        if ("q".equals(q[0])) {
          return Float.parseFloat(q[1]);
        }
      }
      return 1;
    }
    @Override
    public int compare(String o1, String o2) {
      float f1 = getQuality(o1);
      float f2 = getQuality(o2);
      if (f1 < f2) {
        return 1;
      }
      if (f1 > f2) {
        return -1;
      }
      return 0;
    }
  };

  public ContentNegotiationProcessorHandler() {
    super(Router.class);
  }

  @Override
  public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {

    Produces clazzProducesAnn = Processor.getAnnotation(clazz, Produces.class);
    Consumes clazzConsumesAnn = Processor.getAnnotation(clazz, Consumes.class);

    String[] clazzProduces = clazzProducesAnn != null ? clazzProducesAnn.value() : null;
    String[] clazzConsumes = clazzConsumesAnn != null ? clazzConsumesAnn.value() : null;

    Produces producesAnn = Processor.getAnnotation(method, Produces.class);
    Consumes consumesAnn = Processor.getAnnotation(method, Consumes.class);

    String[] produces = producesAnn != null ? producesAnn.value() : null;
    String[] consumes = consumesAnn != null ? consumesAnn.value() : null;

    if (produces == null) {
      produces = clazzProduces;
    }
    if (consumes == null) {
      consumes = clazzConsumes;
    }

    if (produces == null && consumes == null) {
      return;
    }

    // process the methods that have both RoutingContext and Handler

    if (Processor.isCompatible(method, ALL.class, RoutingContext.class)) {
      router.route(Processor.getAnnotation(method, ALL.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, CONNECT.class, RoutingContext.class)) {
      router.connect(Processor.getAnnotation(method, CONNECT.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, OPTIONS.class, RoutingContext.class)) {
      router.options(Processor.getAnnotation(method, OPTIONS.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, HEAD.class, RoutingContext.class)) {
      router.head(Processor.getAnnotation(method, HEAD.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, GET.class, RoutingContext.class)) {
      router.get(Processor.getAnnotation(method, GET.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, POST.class, RoutingContext.class)) {
      router.post(Processor.getAnnotation(method, POST.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, PUT.class, RoutingContext.class)) {
      router.put(Processor.getAnnotation(method, PUT.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, PATCH.class, RoutingContext.class)) {
      router.patch(Processor.getAnnotation(method, PATCH.class).value()).handler(wrap(consumes, produces));
    }
    if (Processor.isCompatible(method, DELETE.class, RoutingContext.class)) {
      router.delete(Processor.getAnnotation(method, DELETE.class).value()).handler(wrap(consumes, produces));
    }
  }

  private static Handler<RoutingContext> wrap(final String[] consumes, final String[] produces) {
    return ctx -> {
      // we only know how to process certain media types
      if (consumes != null) {
        boolean canConsume = false;
        for (String c : consumes) {
          if (is(ctx.request().getHeader("Content-Type"), c)) {
            canConsume = true;
            break;
          }
        }

        if (!canConsume) {
          // 415 Unsupported Media Type (we don't know how to handle this media)
          ctx.fail(415);
          return;
        }
      }

      // the object was marked with a specific content type
      if (produces != null) {
        String bestContentType = accepts(ctx.request().getHeader("Accept"), produces);

        // the client does not know how to handle our content type, return 406
        if (bestContentType == null) {
          ctx.fail(406);
          return;
        }

        // mark the response with the correct content type (which allows middleware to know it later on)
        ctx.response().putHeader("Content-Type", bestContentType);
      }

      // the request can be handled, it does respect the content negotiation
      ctx.next();
    };
  }

  /** Check if the incoming request contains the "Content-Type"
   * header field, and it contains the give mime `type`.
   *
   * Examples:
   *
   * // With Content-Type: text/html; charset=utf-8
   * is('html');
   * is('text/html');
   * is('text/*');
   * // returns true
   *
   * // When Content-Type is application/json
   * is('json');
   * is('application/json');
   * is('application/*');
   * // returns true
   *
   * req.is('html');
   * // returns false
   *
   * @param type content type
   * @param contentTypeHeader the raw header
   * @return true if content type is of type
   */
  private static boolean is(String contentTypeHeader, String type) {
    if (contentTypeHeader == null) {
      return false;
    }
    // get the content type only (exclude charset)
    contentTypeHeader = contentTypeHeader.split(";")[0];

    // if we received an incomplete CT
    if (type.indexOf('/') == -1) {
      // when the content is incomplete we assume */type, e.g.:
      // json -> */json
      type = "*/" + type;
    }

    // process wildcards
    if (type.contains("*")) {
      String[] parts = type.split("/");
      String[] ctParts = contentTypeHeader.split("/");
      return "*".equals(parts[0]) && parts[1].equals(ctParts[1]) || "*".equals(parts[1]) && parts[0].equals(ctParts[0]);

    }

    return contentTypeHeader.contains(type);
  }

  /** Check if the given type(s) is acceptable, returning the best match when true, otherwise null, in which
   * case you should respond with 406 "Not Acceptable".
   *
   * The type value must be a single mime type string such as "application/json" and is validated by checking
   * if the request string starts with it.
   */
  private static String accepts(final String acceptHeader, final String... types) {
    // accept anything when accept is not present
    if (acceptHeader == null) {
      return types[0];
    }

    // parse
    String[] acceptTypes = acceptHeader.split(" *, *");
    // sort on quality
    Arrays.sort(acceptTypes, ACCEPT_X_COMPARATOR);

    for (String senderAccept : acceptTypes) {
      String[] sAccept = splitMime(senderAccept);

      for (String appAccept : types) {
        String[] aAccept = splitMime(appAccept);

        if (
          (sAccept[0].equals(aAccept[0]) || "*".equals(sAccept[0]) || "*".equals(aAccept[0])) &&
            (sAccept[1].equals(aAccept[1]) || "*".equals(sAccept[1]) || "*".equals(aAccept[1]))) {
          return senderAccept;
        }
      }
    }

    return null;
  }

  private static String[] splitMime(String mime) {
    // find any ; e.g.: "application/json;q=0.8"
    int space = mime.indexOf(';');

    if (space != -1) {
      mime = mime.substring(0, space);
    }

    String[] parts = mime.split("/");

    if (parts.length < 2) {
      return new String[] {
        parts[0],
        "*"
      };
    }

    return parts;
  }
}
