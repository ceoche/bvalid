/*
 * Copyright 2022 Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * ou may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ceoche.bvalid;

/**
 * Runtime exception that wrap an exception raised while invoking a method through reflexion
 * during business validation.
 * The original exception can be retrieved with {@link #getCause()}. This is exception is similar
 * than {@link java.lang.reflect.InvocationTargetException}, but unchecked.
 *
 * @author ceoche
 * @see java.lang.reflect.InvocationTargetException
 */
public class InvocationException extends RuntimeException {
   private static final long serialVersionUID = 2812729875699036718L;

   public InvocationException(Throwable throwable) {
      super(throwable);
   }
}
