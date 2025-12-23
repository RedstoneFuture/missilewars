package de.butzlabben.missilewars.configuration.arena.modules;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@RequiredArgsConstructor
public class EndGameActionsConfig {
    
    @SerializedName("execution_delay") private int executionDelay = 10;
    
    @SerializedName("player_win") private List<String> playerWin = new ArrayList<>() {{
        add("[console-cmd] eco give %player_name% 80");
        add("[player-msg] %prefix%&7You received &e80 &7coins.");
    }};
    
    @SerializedName("player_lose") private List<String> playerLose = new ArrayList<>() {{
        add("[console-cmd] eco give %player_name% 10");
        add("[player-msg] %prefix%&7You received &e10 &7coins.");
    }};
    
    @SerializedName("player_draw") private List<String> playerDraw = new ArrayList<>() {{
        add("[console-cmd] eco give %player_name% 40");
        add("[player-msg] %prefix%&7You received &e40 &7coins.");
    }};
    
    @SerializedName("spectator") private List<String> spectator = new ArrayList<>();

}