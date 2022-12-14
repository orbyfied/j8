package net.orbyfied.j8.command.annotation;

import net.orbyfied.j8.command.CommandManager;
import net.orbyfied.j8.command.component.Properties;
import net.orbyfied.j8.command.component.Executable;
import net.orbyfied.j8.command.Node;
import net.orbyfied.j8.command.exception.NodeExecutionException;
import net.orbyfied.j8.registry.Identifier;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

public class BaseAnnotationProcessor {

    /**
     * The command engine.
     */
    protected final CommandManager engine;

    /**
     * The descriptor object.
     */
    protected final Object obj;

    /**
     * The descriptor class.
     */
    protected final Class<?> klass;

    /**
     * The root node to parse from/onto.
     */
    protected Node root;

    public BaseAnnotationProcessor(CommandManager engine, Object obj) {
        this.engine = engine;
        this.obj    = obj;
        this.klass  = obj.getClass();
    }

    public Object getObject() {
        return obj;
    }

    public CommandManager getEngine() {
        return engine;
    }

    public Node getBase() {
        return root;
    }

    public BaseAnnotationProcessor compile() {

        // get base descriptor
        BaseCommand baseCommandDesc = klass.getAnnotation(BaseCommand.class);

        // create base node
        root = new Node(baseCommandDesc.name(), null, null)
                .addAliases(baseCommandDesc.aliases());

        // parse base node properties
        parseExecutableNodeProperties(root, klass);

        // creates the executables
        // and the parameters for them
        for (Method m : klass.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Subcommand.class)) continue;
            m.setAccessible(true);
            Subcommand desc = m.getAnnotation(Subcommand.class);

            // parse parameters
            final ArrayList<String> paramNames = new ArrayList<>(m.getParameterCount());
            Parameter[] parameters = m.getParameters();
            int l = parameters.length;
            for (int i = 2; i < l; i++) {
                // get parameter
                Parameter param = parameters[i];
                // check if it is a parameter
                if (!param.isAnnotationPresent(CommandParameter.class)) continue;
                // resolve the name and add
                String name = param.getAnnotation(CommandParameter.class).value();
                if (name.equals(""))
                    name = param.getName();
                paramNames.add(name);
            }

            Method initializerSub = null;
            try {
                // get the initializer method
                initializerSub = klass.getDeclaredMethod(m.getName(), Node.class);
                if (initializerSub.isAnnotationPresent(SubInitializer.class)) {
                    initializerSub.setAccessible(true);
                }
            } catch (NoSuchMethodException e) {
                // ignore
            } catch (Exception e) {
                e.printStackTrace();
            }

            // register all subcommands
            for (String subcommandStr : desc.value()) {
                SubcommandParser parser = new SubcommandParser(this, engine, root, subcommandStr);
                Node sub = parser.parse();

                // parse node properties
                parseExecutableNodeProperties(sub, m);

                // invoke initializer
                try {
                    if (initializerSub != null)
                        initializerSub.invoke(obj, sub);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // set the actual method executor
                sub.getComponent(Executable.class).setExecutor((ctx, cmd) -> {
                    try {
                        ArrayList<Object> args = new ArrayList<>();
                        args.add(ctx);
                        args.add(cmd);
                        for (String paramn : paramNames) {
                            Identifier pid = new Identifier(null, paramn);
                            args.add(ctx.getArgument(pid));
                        }

                        // invoke
                        m.invoke(obj, args.toArray());
                    } catch (Throwable e) {
                        // throw node execution exception
                        throw new NodeExecutionException(cmd.root(), cmd, e);
                    }
                });
            }
        }

        // return
        return this;

    }

    protected BaseAnnotationProcessor parseExecutableNodeProperties(Node node,
                                                                    AnnotatedElement element) {
        // parse properties (Properties)
        CommandUsage commandUsage;
        if ((commandUsage = element.getAnnotation(CommandUsage.class)) != null)
            node.component(Properties.class, Properties::new, (n1, cp) -> cp.usage(commandUsage.value()));
        CommandDescription commandDescription;
        if ((commandDescription = element.getAnnotation(CommandDescription.class)) != null)
            node.component(Properties.class, Properties::new, (n1, cp) -> cp.description(commandDescription.value()));
        CommandLabel commandLabel;
        if ((commandLabel = element.getAnnotation(CommandLabel.class)) != null)
            node.component(Properties.class, Properties::new, (n1, cp) -> cp.label(commandLabel.value()));

        // return this
        return this;
    }

    public BaseAnnotationProcessor register() {
        if (root != null)
            engine.register(root);
        return this;
    }

}
