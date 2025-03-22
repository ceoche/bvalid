package io.github.ceoche.lang;

import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Collectors {


      private Collectors() {
      }

      public static <T> Collector<T, ?, T> toSingletonOrThrow(Supplier<RuntimeException> tooManyItemExceptionSupplier,
                                                              Supplier<RuntimeException> noItemExceptionSupplier) {
         return java.util.stream.Collectors.collectingAndThen(
               java.util.stream.Collectors.toList(),
               list -> {
                  int size = list.size();
                  if (size > 1) {
                     throw tooManyItemExceptionSupplier.get();
                  } else if (size < 1) {
                     throw noItemExceptionSupplier.get();
                  } else {
                     return list.get(0);
                  }
               }
         );
      }

      public static <T> Collector<T, ?, T> toSingleton() {
         return toSingletonOrThrow(
               () -> new IllegalStateException("Too many elements in stream, only one expected."),
               () -> new NoSuchElementException()
         );
      }

}
