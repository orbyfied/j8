package net.orbyfied.j8.command;

import net.orbyfied.j8.command.Context;
import net.orbyfied.j8.command.SuggestionAccumulator;
import net.orbyfied.j8.command.argument.ArgumentType;
import net.orbyfied.j8.command.argument.TypeIdentifier;
import net.orbyfied.j8.command.argument.TypeResolver;
import net.orbyfied.j8.command.exception.ErrorLocation;
import net.orbyfied.j8.command.exception.NodeParseException;
import net.orbyfied.j8.util.StringReader;
import net.orbyfied.j8.util.functional.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class BukkitArgumentTypes {

    /** UTILITY CLASS */
    private BukkitArgumentTypes() { }

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

    //////////////////////////////////////////////

    public static final ArgumentType<Player> ONLINE_PLAYER_DIRECT = of(Player.class, "minecraft:online_player_direct",
            (context, reader) -> true,
            ((context, reader) -> {
                int i = reader.index();
                String s = reader.collect(c -> c != ' ');
                int i2 = reader.index() - 1;
                Player player;
                if ((player = Bukkit.getPlayer(s)) != null)
                    return player;
                try {
                    return Bukkit.getPlayer(UUID.fromString(s));
                } catch (Exception e) {
                    throw new NodeParseException(context.rootCommand(), context.currentNode(),
                            new ErrorLocation(reader, i, i2), "Unknown player");
                }
            }),
            (context, builder, s) -> builder.append(s.getUniqueId()),
            ((context, suggestions) -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.suggest(player.getName());
//                    suggestions.suggest(player.getUniqueId());
                }
            })
    );

}
