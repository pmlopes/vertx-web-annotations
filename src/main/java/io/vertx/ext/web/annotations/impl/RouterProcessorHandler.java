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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class RouterProcessorHandler extends AbstractAnnotationHandler<Router> {

  public RouterProcessorHandler() {
    super(Router.class);
  }

  @Override
  public void process(final Router router, final Object instance, final Class<?> clazz, final Method method) {

    // process the methods that have both RoutingContext and Handler

    if (Processor.isCompatible(method, ALL.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.route(Processor.getAnnotation(method, CONNECT.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, CONNECT.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.connect(Processor.getAnnotation(method, CONNECT.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, OPTIONS.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.options(Processor.getAnnotation(method, OPTIONS.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, HEAD.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.head(Processor.getAnnotation(method, HEAD.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, GET.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.get(Processor.getAnnotation(method, GET.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, POST.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.post(Processor.getAnnotation(method, POST.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, PUT.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.put(Processor.getAnnotation(method, PUT.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, PATCH.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.patch(Processor.getAnnotation(method, PATCH.class).value()).handler(wrap(instance, methodHandle));
    }
    if (Processor.isCompatible(method, DELETE.class, RoutingContext.class)) {
      MethodHandle methodHandle = Processor.getMethodHandle(method, RoutingContext.class);
      router.delete(Processor.getAnnotation(method, DELETE.class).value()).handler(wrap(instance, methodHandle));
    }
  }

  private static Handler<RoutingContext> wrap(final Object instance, final MethodHandle m) {
    return ctx -> {
      try {
        m.invoke(instance, ctx);
      } catch (Throwable e) {
        ctx.fail(e);
      }
    };
  }
}
