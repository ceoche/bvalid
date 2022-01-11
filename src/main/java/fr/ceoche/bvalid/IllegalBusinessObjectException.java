/*
 * Copyright 2021 Cédric Eoche-Duval
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
package fr.ceoche.bvalid;

public class IllegalBusinessObjectException extends RuntimeException {
   private static final long serialVersionUID = 2793010909620457756L;

   public IllegalBusinessObjectException(String s) {
      super(s);
   }

   public IllegalBusinessObjectException(String s, Throwable throwable) {
      super(s, throwable);
   }
}
