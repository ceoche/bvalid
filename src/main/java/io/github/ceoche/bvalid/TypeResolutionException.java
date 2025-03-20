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
