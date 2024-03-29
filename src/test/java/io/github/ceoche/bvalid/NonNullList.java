/*
 * Copyright 2022-2023 Cédric Eoche-Duval
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

import java.util.ArrayList;
import java.util.Collection;

public class NonNullList<T> extends ArrayList<T> {

   public NonNullList(Collection<? extends T> c) {
      super(c);
      if (super.contains(null)) {
         throw new NullPointerException();
      }
   }

   @Override
   public boolean contains(Object o) {
      if (o != null) {
         return super.contains(o);
      } else {
         throw new NullPointerException();
      }
   }

   @Override
   public boolean add(T t) {
      if (t != null) {
         return super.add(t);
      } else {
         throw new NullPointerException();
      }
   }
}
