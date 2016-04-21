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
package io.vertx.ext.web;

import io.vertx.core.Vertx;
import io.vertx.ext.web.annotations.impl.Processor;

/**
 * AnnotatedRouter builder that transforms POJOs into Router objects
 *
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public interface AnnotatedRouter {

  /**
   * Transform a list of POJOs annotated with the provided annotations into a new Router object
   *
   * @param vertx the vertx instance (required to build the router object)
   * @param objs the pojos to process
   * @return router object
   */
  static Router create(final Vertx vertx, final Object... objs) {
    return create(Router.router(vertx), objs);
  }

  /**
   * Merges a list of POJOs annotated with the provided annotations into a provided Router object
   *
   * @param router router object initialized outside this builder
   * @param objs the pojos to process
   * @return router object
   */
  static Router create(final Router router, Object... objs) {
    for (Object o : objs) {
      Processor.process(router, o);
    }

    return router;
  }
}
