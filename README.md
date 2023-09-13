# BValid

__BValid__ is an open-source Java library to provide easy business rules and model validation.

This project is under [Apache License, Version 2.0](#license).

__README Index__

<!-- TOC -->
* [BValid](#bvalid)
  * [Project set-up](#project-set-up)
  * [Usage](#usage)
    * [Usage with Annotations](#usage-with-annotations)
      * [Business Object](#business-object)
      * [Validation](#validation)
      * [Business Rules](#business-rules)
      * [Business member composition](#business-member-composition)
      * [Business object inheritance](#business-object-inheritance)
    * [Usage with Manual Builder (No annotations)](#usage-with-manual-builder-no-annotations)
      * [Programmatic business rules](#programmatic-business-rules)
      * [Programmatic business members](#programmatic-business-members)
      * [Programmatic members with inheritance](#programmatic-members-with-inheritance)
      * [Reusing builder](#reusing-builder)
      * [Complex use cases](#complex-use-cases)
    * [Default rules](#default-rules)
  * [Ideas behind BValid](#ideas-behind-bvalid)
  * [Sources and build](#sources-and-build)
    * [Requirements](#requirements)
    * [Build, test and package](#build-test-and-package)
    * [Mutation testing](#mutation-testing)
    * [Release a new version of BValid](#release-a-new-version-of-bvalid)
  * [Contribute](#contribute)
  * [License](#license)
<!-- TOC -->

## Project set-up

__BValid__ is available on Maven Central Repository

```xml

<dependency>
   <groupId>io.github.ceoche</groupId>
   <artifactId>bvalid</artifactId>
   <version>0.1.0</version>
</dependency>
```

## Usage

__BValid__ is a validation tool running a collection of assertions or rules on a business object to assess whether this
object is valid or not.

### Usage with Annotations

#### Business Object

When using __BValid__, a business object is a Java class annotated with `@BusinessObject` that has at least one business
rule or one business member.

```java
import io.github.ceoche.bvalid.BasicRules;
import io.github.ceoche.bvalid.BusinessObject;
import io.github.ceoche.bvalid.BusinessRule;

@BusinessObject
public class Author {

   private String name;

   public String getName() {
      return name;
   }

   public Author setName(String name) {
      this.name = name;
      return this;
   }

   @BusinessRule("Author's name must be defined.")
   public boolean isNameValid() {
      return BasicRules.isDefined(name);
   }
}
```

Any sub-classes of a class annotated with `@BusinessObject` are also considered business objects and can be validated.

#### Validation

To verify a business object that is annotated, create a `BValidator` using `BValidatorAnnotationBuilder` :

```java
import io.github.ceoche.bvalid.BValidator;
import io.github.ceoche.bvalid.BValidatorAnnotationBuilder;
import io.github.ceoche.bvalid.ObjectResult;
import io.github.ceoche.bvalid.RuleResult;

public class Example {

   public static void main(String[] args) {
       
      Author author = new Author();
      author.setName("Jules Verne");

      BValidator<Author> authorValidator = new BValidatorAnnotationBuilder<>(Author.class).build();
      ObjectResult result = authorValidator.validate(author);
      
      if (result.isValid()) {
         // do something
      } else {
          // print report as plain text.
         System.out.println(result);
      }
   }

}
```

Validation results can be explored using the `ObjectResult` class. ObjectResult comes with a method `assertValidOrThrow`
that can be used if the model is invalid to raise an exception using a `Supplier` (the exception must have at least a
constructor with a String as parameter):

```java
objectResult.assertValidOrThrow(IllegalArgumentException::new);
```

#### Business Rules

A business rule is a __public__ method that takes no arguments, returns a `boolean` and is annotated
with `@BusinessRule` (the method must return `true` if the rule is validated, `false` otherwise).

```java
import io.github.ceoche.bvalid.BasicRules;
import io.github.ceoche.bvalid.BusinessObject;
import io.github.ceoche.bvalid.BusinessRule;

@BusinessObject
public class Author {

   // [...]

   @BusinessRule("Author's name must be defined.")
   public boolean isNameValid() {
      return BasicRules.isDefined(name);
   }
}
```

`BusinessRule` should be enriched with a meaningful description. It will help other developers to understand the rule,
but it will also be used in the validation report.

If requirement engineering is required, `id` attribute can be used to declare the requirement id.

```java
public class Author {

   //[...]

   @BusinessRule(id = "req-01", value = "Author's name must be defined.")
   public boolean isNameValid() {
      return BasicRules.isDefined(name);
   }
}
```

#### Business member composition

Business objects can have other business objects as attributes to compose an aggregate (see DDD) or a business model.
__BValid__ is able to validate business members if they are defined via a __public__ accessor that takes no argument,
return a business object (instance of a class annotated with `@BusinessObject`) and is annotated with `@BusinessMember`:

```java
import io.github.ceoche.bvalid.BasicRules;
import io.github.ceoche.bvalid.BusinessMember;
import io.github.ceoche.bvalid.BusinessObject;
import io.github.ceoche.bvalid.BusinessRule;

@BusinessObject
public class Book {

   private Author author;

   @BusinessMember
   public Author getAuthor() {
      return author;
   }

   public Book setAuthor(Author author) {
      this.author = author;
      return this;
   }

   @BusinessRule("Author must be defined.")
   public boolean isAuthorValid() {
      return BasicRules.isDefined(author);
   }
}
```

When calling `BValidator::validate`, all business rules of declared business members will also be tested to assess the
validity of the business object. If the business member's accessor return a `null` value, the member will be ignored. It
allows optional associations in models.

Composition support also multiple cardinality:

```java
import io.github.ceoche.bvalid.BusinessMember;
import io.github.ceoche.bvalid.BusinessObject;

import java.util.ArrayList;

@BusinessObject(name = "library")
public class Library {

   public List<Book> books = new ArrayList<>();

   @BusinessMember(name = "books")
   public List<Book> getBooks() {
      return books;
   }
   
    public Library setBooks(List<Book> books) {
        this.books = books;
        return this;
    }
}
```

In this situation, the `BValidator` will go through all elements of the collection and validate them all.

Currently, only `java.util.Collection` and __arrays__ are supported. `java.util.Map` may be added in a future soon.

#### Business object inheritance

Business objects can inherit from others business object. They will get all business rules and members from the parent
business object.

```java
import io.github.ceoche.bvalid.BasicRules;
import io.github.ceoche.bvalid.BusinessObject;
import io.github.ceoche.bvalid.BusinessRule;

@BusinessObject
public class Comic extends Book {

   private String artist;

   public String getArtist() {
      return artist;
   }

   public Comic setArtist(String artist) {
      this.artist = artist;
      return this;
   }
   @Override
   public Comic setAuthor(Author author) { // override parent setter for java 8 style
      super.setAuthor(author);
      return this;
   }

   @BusinessRule("Artist must be defined if present.")
   public boolean isArtistValid() {
      BasicRules.isDefinedIfPresent(artist);
   }
}
```

In the example above, `Comic` will also inherit from the business rule `Author::isAuthorValid`.

### Usage with Manual Builder (No annotations)

It is also possible to use __BValid__ without any annotations. That is a good thing if you do not want to create coupling
between you business models and __bValid__.

To do so, use `BValidatorManualBuilder` to create a `BValidator` instance programmatically.

#### Programmatic business rules

The following example shows how to create a `BValidator` for the `Author` class that would not have any annotations.

```java

import io.github.ceoche.bvalid.BValidator;
import io.github.ceoche.bvalid.BValidatorManualBuilder;
import io.github.ceoche.bvalid.ObjectResult;

class Example {

   public static void main(String[] args) {
       
      BValidator<Author> authorValidator = new BValidatorManualBuilder<>(Author.class)
              .setBusinessObjectName("author") // optional, but we recommend to set it for better error messages.
              .addRule(Author::isNameValid, "Author's name must be defined.")
              .build();

      Author author = new Author();
      author.setName("John Doe");

      ObjectResult result = authorValidator.validate(author);
      
      result.assertValidOrThrow(IllegalArgumentException::new);
   }
}
```

Just as business rule annotated with `@BusinessRule`, it must be a __public__ method that takes no arguments and
returns a `boolean` (the method must return `true` if the rule is validated, `false` otherwise).

The business object name is optional, but we recommend to set it at least for the root business object to get better error messages.

#### Programmatic business members

Aggregates and associations can be validated by adding business members to the builder.

The add member method takes: 
- The name of the member 
- The getter method of the member
- The validators builders for the member and/or its subtypes.

In this example will suppose having a `Book` class with an `Author` member, with no subtypes.

```java

import io.github.ceoche.bvalid.BValidatorManualBuilder;

class Example {

   public static void main(String[] args) {
       BValidator<Book> bValidator = new BValidatorManualBuilder<>(Book.class)
               .setBusinessObjectName("Book") // optional, but we recommend to set it for better error messages.
               .addRule(Book::isAuthorValid, "Author must not be null.")
               .addMember("author", Book::getAuthor, 
                       new BValidatorManualBuilder<>(Author.class)
                               .addRule(Author::isNameValid, "Author's name must be defined.")
               )
               .build();
       
       // use the validator
   }
}

```

The manual builder supports multiple cardinality for collections and arrays the same way as single members.

```java
import io.github.ceoche.bvalid.BValidator;
import io.github.ceoche.bvalid.BValidatorManualBuilder;
import io.github.ceoche.bvalid.ObjectResult;

class Example {

   public static void main(String[] args) {
       BValidator<Library> bValidator = new BValidatorManualBuilder<>(Library.class)
               .setBusinessObjectName("library") // optional, but we recommend to set it for better error messages.
               .addMember("books", Library::getBooks, 
                       new BValidatorManualBuilder<>(Book.class)
                               .addRule(Book::isAuthorValid, "Author must be defined.")
                               .addMember("author", Book::getAuthor, 
                                       new BValidatorManualBuilder<>(Author.class)
                                               .addRule(Author::isNameValid, "Author's name must be defined.")
                               )
               )
               .build();
       
       // Instantiate a Library with several books
      Library library = new Library().setBooks(Arrays.asList(
                 new Book().setAuthor(new Author().setName("John Doe")),
                 new Book().setAuthor(new Author().setName("Jane Doe"))
      ));
         
      // use the validator
      ObjectResult result = bValidator.validate(library);
      System.out.println(result);
      
   }
}

```

The above validation would produce the output :
```
library.books[0] Author must be defined. => valid
library.books[0].author Author's name must be defined. => valid
library.books[1] Author must be defined. => valid
library.books[1].author Author's name must be defined. => valid
```

#### Programmatic members with inheritance

Polymorphism context is supported by the manual builder. although, the possible implementations should be provided 
at build time. This will be enhanced in future versions to support more extensibility.

Taking the example of the `Book` class, we can add a `Comic` class that extends `Book` and add a validator builder 
for the `Comic` implementation to the Library validator builder.

```java
import io.github.ceoche.bvalid.BValidator;
import io.github.ceoche.bvalid.BValidatorManualBuilder;
import io.github.ceoche.bvalid.ObjectResult;

class Example {

   public static void main(String[] args) {
       BValidator<Library> bValidator = new BValidatorManualBuilder<>(Library.class)
               .setBusinessObjectName("library") // optional, but we recommend to set it for better error messages.
               .addMember("books", Library::getBooks, 
                       new BValidatorManualBuilder<>(Book.class)
                               .addRule(Book::isAuthorValid, "Author must be defined.")
                               .addMember("author", Book::getAuthor, 
                                       new BValidatorManualBuilder<>(Author.class)
                                               .addRule(Author::isNameValid, "Author's name must be defined.")
                               ),
                       new BValidatorManualBuilder<>(Comic.class)  // Add builder for the new Comic subtype
                               .addRule(Comic::isArtistValid, "Artist must be defined if present.")
                               .addRule(Comic::isAuthorValid, "Author must be defined.")
                               .addMember("author", Comic::getAuthor, 
                                       new BValidatorManualBuilder<>(Author.class)
                                               .addRule(Author::isNameValid, "Author's name must be defined.")
                               )
               )
               .build();

      // Instantiate a Library with different type of books
      Library library = new Library().setBooks(Arrays.asList(
                      new Book().setAuthor(new Author().setName("John Doe")),
                      new Comic().setAuthor(new Author().setName("Jane Doe"))
                                .setArtist("Jack Doe")
      ));

      // use the validator
      ObjectResult result = bValidator.validate(library);
      System.out.println(result);
   }
}
```

The above validation would produce the output :
```
library.books[0] Author must be defined. => valid
library.books[0].author Author's name must be defined. => valid
library.books[1] Artist must be defined. => valid
library.books[1] Author must be defined. => valid
library.books[1].author Author's name must be defined. => valid
```

#### Reusing builder

As you can see in the previous example, the builder for `Author` is used in multiple places. It is possible to reuse
the builder by extracting it to a variable and reusing it.

```java
import io.github.ceoche.bvalid.BValidator;
import io.github.ceoche.bvalid.BValidatorManualBuilder;
import io.github.ceoche.bvalid.ObjectResult;

class Example {

   public static void main(String[] args) {
       BValidatorManualBuilder<Author> authorValidatorBuilder = new BValidatorManualBuilder<>(Author.class)
               .addRule(Author::isNameValid, "Author's name must be defined.");
       BValidator<Library> bValidator = new BValidatorManualBuilder<>(Library.class)
               .setBusinessObjectName("library") // optional, but we recommend to set it for better error messages.
               .addMember("books", Library::getBooks, 
                       new BValidatorManualBuilder<>(Book.class)
                               .addRule(Book::isAuthorValid, "Author must be defined.")
                               .addMember("author", Book::getAuthor, authorValidatorBuilder),
                       new BValidatorManualBuilder<>(Comic.class)  // Add builder for the new Comic subtype
                               .addRule(Comic::isArtistValid, "Artist must be defined if present.")
                               .addRule(Comic::isAuthorValid, "Author must be defined.")
                               .addMember("author", Comic::getAuthor, authorValidatorBuilder)
               )
               .build();
      // use the validator
   }
}
```

#### Complex use cases

The validator support other uses cases such: 
* Validating a recursive structure
* Use same validator builder reference for multiple members
* Cross recursive validation
* ...

### Default rules

__BValid__ provides the `BasicRules` utility to implement quickly default business rules to assert mandatory attributes
or cardinality.

```java
import io.github.ceoche.bvalid.BasicRules;

import java.util.Collections;

public class DefaultRulesDemo {

   public boolean mandatoryAttribute() {
      // check is not null, and also not blank for Strings.
      return BasicRules.isDefined(attribute);
   }

   public boolean optionalStringAttribute() {
      // check is not blank if string is not null (Only for strings).
      return BasicRules.isDefinedIfPresent(stringAttribute);
   }

   public boolean oneToManyAssociation() {
      // check a collection or an array has at least one element.
      return BasicRules.hasOneOrMoreElements(collectionAttribute);
   }

   public boolean zeroToManyDefinedAssociation() {
      // check there are no null elements in collections or arrays.
      return BasicRules.hasDefinedElements(collectionAttribute);
   }

   public boolean oneToManyDefinedAssociation() {
      // check a collection or an array has at least one element AND no null elements.
      return BasicRules.hasOneOrMoreDefinedElements(collectionAttribute);
   }


}
```

To avoid too much coupling with __BValid__, you should encapsulate `BasicRules` behind an interface. See adapter
pattern.

## Ideas behind BValid

A business object or model, is usually seen as a stateful data structure with enforced business rules. However, business
rules are not always required to be verified, for example when data are pulled from an already verified database, there
should be no need to verify business rules again. As such, we usually prefer writing the business rules in methods
separated from constructors and accessors, use cases can invoke them whenever necessary.

Then instead of manually aggregating validation methods to call and sequence collections of business rules, we can use
__BValid__ to list all business rules using annotation or the manual validator-builder.

Ideally, we would have expected such library to be the least intrusive in the code, because we do not want our business
code to depend on an obscure framework ! Unlike the famous Jakarta-EE-Validation that is declaring validation rules as
annotations, we have decided that all business rules should be methods and should be written in plain Java. Annotations
are used only to reference them. So even if you decide to no longer use __BValid__ in your project, your code will still
have all its valuable rules.

## Sources and build

### Requirements

* Java 17 or youngest
* Maven 3

### Build, test and package

To build, test and package the project as JAR :

```shell
mvn clean package
```

JAR file and javadoc will be available in `target/` directory. To make it available to any other Maven project on your
machine :

```shell
mvn install
```

### Mutation testing

To run mutation-tests and assess quality of unit-tests:

```shell
mvn pitest:mutationCoverage
```

A report will be generated in `target/pit-reports/yyyyMMddhhmm/index.html`

### Release a new version of BValid

Release process can only be performed by project members.

```shell
mvn release:prepare
mvn release:perform
# Verify the staging repo created, then run
mvn nexus-staging:release -Prelease -DstagingRepositoryId=${STAGING_REPO}
```

## Contribute

Feel free to open an issue, fork the project and/or propose a merge request.

Thanks to @achrafxx and [Kereval](https://www.kereval.com) for their contribution to the Programmatic builder.

## License

__Copyright 2022-2023 Cédric Eoche-Duval.__

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with
the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
