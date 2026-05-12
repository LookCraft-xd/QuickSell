package me.mrCookieSlime.QuickSell.commands.extra;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.Suggestion;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import me.mrCookieSlime.QuickSell.boosters.BoosterType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BoosterPlayerArgument extends ArgumentResolver<CommandSender, String> {

    public static final String KEY = "booster-player";

    @Override
    protected ParseResult<String> parse(Invocation<CommandSender> invocation, Argument<String> context, String argument) {
        try {
            Player player = Bukkit.getPlayer(argument);
            return ParseResult.success(player.getName());
        } catch (Exception e) {
            return ParseResult.successNull();
        }
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<String> argument, SuggestionContext context) {
        SuggestionResult collect = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(SuggestionResult.collector());
        collect.add(Suggestion.of("ALL"));
        return collect;
    }
}
