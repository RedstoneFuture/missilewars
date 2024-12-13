package de.butzlabben.missilewars.configuration;

import de.butzlabben.missilewars.Logger;
import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.game.Game;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionSet {
    
    @Getter
    @RequiredArgsConstructor
    public class Action {
        
        final ActionType actionType;
        final String data;
        
    }
    
    public final List<Action> actionMap = new ArrayList<>();
    
    public ActionSet(List<String> actions) {
        
        actions.forEach(s -> {
            String[] actionDef = s.split(" ", 2);
            Action action = new Action(getActionType(actionDef[0]), actionDef[1]);
            this.actionMap.add(action);
        });
        
    }
    
    public enum ActionType {
        PLAYER_CMD,
        CONSOLE_CMD,
        PLAYER_MSG,
        GAME_MSG,
        TEAM_MSG,
        SERVER_MSG
    }
    
    private ActionType getActionType(String actionDef) {
        String prefix = actionDef.split(" ", 2)[0];
        
        // Action-Type specification inspired by DeluxeMenus https://wiki.helpch.at/helpchat-plugins/deluxemenus/options-and-configurations#actions-types
        
        switch (prefix) {
            case "[player-cmd]": return ActionType.PLAYER_CMD;
            case "[console-cmd]": return ActionType.CONSOLE_CMD;
            case "[player-msg]": return ActionType.PLAYER_MSG;
            case "[game-msg]": return ActionType.GAME_MSG;
            case "[team-msg]": return ActionType.TEAM_MSG;
            case "[server-msg]": return ActionType.SERVER_MSG;
            default: return null;
        }
    }
    
    public void runActions(Player player, Game game) {
        Server server = MissileWars.getInstance().getServer();
        
        actionMap.forEach(a -> {
            
            Logger.DEBUG.log("Run Action: " + a.getActionType() + " -> '" + a.data + "'");
            
            String data = PluginMessages.getPapiMessage(a.getData(), player).replace("%prefix%", PluginMessages.getPrefix());
            switch (a.getActionType()) {
                case PLAYER_CMD:
                    player.performCommand(data);
                    break;
                case CONSOLE_CMD:
                    server.dispatchCommand(server.getConsoleSender(), data);
                    break;
                case PLAYER_MSG:
                    player.sendMessage(data);
                    break;
                case GAME_MSG:
                    game.getPlayers().values().forEach(mwPlayer -> {
                        mwPlayer.getPlayer().sendMessage(data);
                    });
                    break;
                case TEAM_MSG:
                    game.getPlayer(player).getTeam().getMembers().forEach(mwPlayer -> {
                        mwPlayer.getPlayer().sendMessage(data);
                    });
                    break;
                case SERVER_MSG:
                    server.broadcastMessage(data);
                    break;
            }
            
        });
        
    }
}
