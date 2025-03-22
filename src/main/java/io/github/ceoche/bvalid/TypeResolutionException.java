/*
 * Copyright 2025. Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.github.ceoche.bvalid;

import java.io.Serial;

/**
 * Exception thrown when a generic type cannot be resolved. Typically, if one of the member getter returns a List with a
 * wildcard type such as {@code List<? extends T>}.
 */
public class TypeResolutionException extends RuntimeException {

   @Serial
   private static final long serialVersionUID = -3765746936406947537L;

   /**
    * Constructor of TypeResolutionException
    * @param message the message of the exception
    */
   public TypeResolutionException(String message) {
      super(message);
   }

   /**
    * Constructor of TypeResolutionException
    * @param cause the cause of the exception
    */
   public TypeResolutionException(Throwable cause) {
      super(cause);
   }

}
