package net.orbyfied.j8.command;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.argument.ArgumentType;
import net.orbyfied.j8.command.argument.TypeIdentifier;
import net.orbyfied.j8.command.argument.TypeResolver;
import net.orbyfied.j8.util.StringReader;
import net.orbyfied.j8.util.functional.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class BungeeArgumentTypes {

    /**
     * The singleton type resolver.
     */
    public static final TypeResolver typeResolver = TypeResolver.memoryBacked();

    /**
     * Function to quickly create simple
     * parameter types with lambdas.
     * @see ArgumentType
     */
    static <T> ArgumentType<T> of(final Class<T> klass,
                                  final String baseId,
                                  final BiPredicate<Context, StringReader> acceptor,
                                  final BiFunction<Context, StringReader, T> parser,
                                  final TriConsumer<Context, StringBuilder, T> writer,
                                  final BiConsumer<Context, SuggestionAccumulator> suggester) {
        // parse identifier
        final TypeIdentifier bid = TypeIdentifier.of(baseId);

        // create type
        ArgumentType<T> type = new ArgumentType<>() {
            @Override
            public TypeIdentifier getBaseIdentifier() {
                return bid;
            }

            @Override
            public Class<T> getType() {
                return klass;
            }

            @Override
            public boolean accepts(Context context, StringReader reader) {
                return acceptor.test(context, reader);
            }

            @Override
            public T parse(Context context, StringReader reader) {
                return parser.apply(context, reader);
            }

            @Override
            public void write(Context context, StringBuilder builder, T v) {
                writer.accept(context, builder, v);
            }

            @Override
            public void suggest(Context context, SuggestionAccumulator suggestions) {
                suggester.accept(context, suggestions);
            }

            @Override
            public String toString() {
                return bid.toString();
            }
        };

        // register type
        typeResolver.register(type);

        // return
        return type;
    }

    /////////////////////////////////////////////////////

    public static ArgumentType<ProxiedPlayer> ONLINE_PLAYER = of(ProxiedPlayer.class, "bungee:online_player",
            (context, reader) -> ProxyServer.getInstance().getPlayer(reader.collect(c -> c != ' ')) != null,
            (context, reader) -> ProxyServer.getInstance().getPlayer(reader.collect(c -> c != ' ')),
            (context, builder, player) -> builder.append(player.getName()),
            (context, accumulator) -> ProxyServer.getInstance().getPlayers()
                    .stream().map(ProxiedPlayer::getName).forEach(accumulator::suggest)
    );

}
