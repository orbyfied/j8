public class Logger {

  static BuilderTemplate<Logger, Builder<Logger>> BUILDER_TEMPLATE = new BuilderTemplate<>(Logger.class)
    .parameter("name", Property.string().defaulted("Untitled").require())
    .parameter("important", Property.bool().defaulted(false).require())
    .constructors(Constructor.takeBuilder().onlyIf(builder -> !builder.get("important", Boolean.class)),
                  Constructor.function(builder -> new Logger(builder.get("name"), (boolean)builder.get("important"))));

  //////////////////////////////////////////////////

  /**
   * Builder Constructor.
   * @see Constructor#takeBuilder()
   */
  Logger(Builder<Logger> builder) {
    this.name      = builder.get("name");
    this.important = builder.get("important");
  }

  public Logger(String name, boolean important) {
    this.name      = name;
    this.important = important;
  }

  final String name;
  final boolean important;

  // ...

}

//////////////////////////////////////////////

/* example 1: simple builder */ {

  Logger logger = new Builder<Logger>(Logger.BUILDER_TEMPLATE)
    .set("name", "Hello!")
    .set("important", true)
    .build();

}

/* example 2: simple factory from builder */ {

  Factory<Logger> importantLoggerFactory = new Builder<Logger>(Logger.BUILDER_TEMPLATE)
    .set("important", true) // always important
    .ignore("name")

    // builds a factory, with unset or ignored required parameters
    // as required parameters to the factory. the required parameters are
    // positioned in the order they were added in the builder template
    .buildFactory(); 

  // ...

  void use() {
    //// Factory<Logger>#create(Object... parameters) -> Logger
    importantLoggerFactory.create("ImportantLogger");
  }

}

/* example 3: more complex factory from builder */ {

  Factory<Logger> importantLoggerFactory = new Builder<Logger>(Logger.BUILDER_TEMPLATE)
    .set("important", true) // always important
    .ignore("name")

    // creates a factory builder for customizing the factory
    .factory(/* defaults (see explanation of buildFactory() ) */ true)
    .forParameterProcessValue("name", value -> "Prefixed" + value)
    .build();

  void use() {
    //// Factory<Logger>#create(Object... parameters) -> Logger
    // name will become "PrefixedImportantLogger"
    importantLoggerFactory.create("ImportantLogger"); 
  }

}