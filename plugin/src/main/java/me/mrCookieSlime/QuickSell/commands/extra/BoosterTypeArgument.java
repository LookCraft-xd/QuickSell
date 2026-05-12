package me.mrCookieSlime.QuickSell.commands.extra;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class BoosterTypeArgument extends ArgumentResolver<CommandSender, String> {

    public static final String KEY = "booster-type";

    @Override
    protected ParseResult<String> parse(Invocation<CommandSender> invocation, Argument<String> context, String argument) {
        try {
            BoosterType boosterType = BoosterType.valueOf(argument.toUpperCase());
            return ParseResult.success(boosterType.name());
        } catch (IllegalArgumentException ignored) {
            return ParseResult.successNull();
        }
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<String> argument, SuggestionContext context) {
        SuggestionResult collect = Arrays.stream(BoosterType.values()).map(Enum::name).collect(SuggestionResult.collector());
        collect.add(Suggestion.of("ALL"));
        return collect;
    }
}
