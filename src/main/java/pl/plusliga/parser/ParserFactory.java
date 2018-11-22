package pl.plusliga.parser;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.plusliga.model.Game;
import pl.plusliga.model.League;
import pl.plusliga.model.Player;
import pl.plusliga.model.PlayerGame;
import pl.plusliga.model.PlayerTeam;
import pl.plusliga.model.Team;
import pl.plusliga.parser.pls.DataprojectPlayerGameParser;
import pl.plusliga.parser.pls.PlpsGameParser;
import pl.plusliga.parser.pls.PlpsPlayerParser;
import pl.plusliga.parser.pls.PlpsPlayerTeamParser;
import pl.plusliga.parser.pls.PlpsTeamParser;

public class ParserFactory {

  private static final Map<Class<?>, Class<? extends JsoupParser<?>>> PLS_MAP = 
      Stream
          .of(new SimpleEntry<>(Game.class, PlpsGameParser.class),
              new SimpleEntry<>(Player.class, PlpsPlayerParser.class),
              new SimpleEntry<>(PlayerGame.class, DataprojectPlayerGameParser.class),
              new SimpleEntry<>(PlayerTeam.class, PlpsPlayerTeamParser.class),
              new SimpleEntry<>(Team.class, PlpsTeamParser.class))
          .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

  private static final Map<League, Map<Class<?>, Class<? extends JsoupParser<?>>>> PARSER_MAP =
      Stream
          .of(new SimpleEntry<>(League.ORLENLIGA, PLS_MAP),
              new SimpleEntry<>(League.PLUSLIGA, PLS_MAP))
          .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

  private ParserFactory() {}

  @SuppressWarnings("unchecked")
  public static <T> JsoupParser<T> getParser(League league, Class<T> clazz, Object... args) {
    Class<? extends JsoupParser<T>> parserClass =
        (Class<? extends JsoupParser<T>>) Optional.ofNullable(PARSER_MAP.get(league))
            .map(classMap -> classMap.get(clazz)).orElseThrow(() -> new IllegalArgumentException(
                "No parser for league/class: " + league + " / " + clazz.getName()));
    try {
      return parserClass.getConstructor(Stream.of(args)
          .map(arg -> (arg instanceof Collection) ? Collection.class : arg.getClass())
          .toArray(Class[]::new)).newInstance(args);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

}
