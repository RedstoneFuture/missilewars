package de.butzlabben.missilewars.game;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Config;
import de.butzlabben.missilewars.configuration.PluginMessages;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.ColorUtil;
import de.butzlabben.missilewars.util.version.ColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

public class GameResultManager {
    
    private final Game game;
    private final TeamManager teamManager;
    
    public GameResultManager(Game game) {
        this.game = game;
        this.teamManager = game.getTeamManager();
    }
    
    public void executeResult() {
        for (Player player : game.getGameWorld().getWorld().getPlayers()) {
            MWPlayer mwPlayer = game.getPlayer(player);
            
            sendGameResultTitle(mwPlayer);
            sendGameResultSound(mwPlayer);
        
        }
        
        if (!Config.isGameResultFirework()) return;
        
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(15), 10);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(20), 40);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(20), 70);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(15), 100);
        
    }

    /**
     * This method sends the title and subtitle to the target team member or
     * spectator of the Game.
     *
     * @param mwPlayer (MWPlayer) the target player
     */
    public void sendGameResultTitle(MWPlayer mwPlayer) {
        Player player = mwPlayer.getPlayer();
        
        String title;
        String subTitle;
        
        if (mwPlayer.getTeam().getTeamType() == TeamType.PLAYER) {

            switch (mwPlayer.getTeam().getGameResult()) {
                case WIN:
                    title = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_TITLE_WINNER);
                    subTitle = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_SUBTITLE_WINNER);
                    break;
                case LOSE:
                    title = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_TITLE_LOSER);
                    subTitle = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_SUBTITLE_LOSER);
                    break;
                case DRAW:
                    title = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_TITLE_DRAW);
                    subTitle = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_SUBTITLE_DRAW);
                    break;
                default:
                    title = null;
                    subTitle = null;
                    break;
            }
        
        } else {
            
            if (teamManager.getTeam1().getGameResult() == GameResult.WIN) {
                title = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_TITLE_WON)
                        .replace("%team%", teamManager.getTeam1().getName());
                subTitle = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_SUBTITLE_WON);
    
            } else if (teamManager.getTeam2().getGameResult() == GameResult.WIN) {
                title = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_TITLE_WON)
                        .replace("%team%", teamManager.getTeam2().getName());
                subTitle = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_SUBTITLE_WON);
    
            } else {
                title = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_TITLE_DRAW);
                subTitle = PluginMessages.getMessage(false, PluginMessages.MessageEnum.GAME_RESULT_SUBTITLE_DRAW);
            }
            
        }
        
        player.sendTitle(title, subTitle, 10, 140, 20);
    }
    
    /**
     * This method sends the end-game sound to the target team member or
     * spectator of the Game.
     *
     * @param mwPlayer (MWPlayer) the target player
     */
    public void sendGameResultSound(MWPlayer mwPlayer) {
        Player player = mwPlayer.getPlayer();
        
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 100, 0);
        // player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 100, 1);
    }
    
    private void spawnSetOfFireworks(int amount) {
        for (int i = 1; i <= amount; i++) {
            spawnFirework();
        }
    }
    
    private void spawnFirework() {
        Color winnerTeamColor;
        
        if (teamManager.getTeam1().getGameResult() == GameResult.WIN) {
            winnerTeamColor = ColorConverter.getColorFromCode(teamManager.getTeam1().getColorCode());
        
        } else if (teamManager.getTeam2().getGameResult() == GameResult.WIN) {
            winnerTeamColor = ColorConverter.getColorFromCode(teamManager.getTeam2().getColorCode());
        
        } else {
            return;
            
        }
        
        Firework firework = game.getGameWorld().getWorld().spawn(game.getArenaConfig().getSpectatorSpawn(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        
        fireworkMeta.clearEffects();

        Random random = new Random();
        
        // Create the effect:
        // https://minecraft.tools/en/firework.php
        
        FireworkEffect.Builder effectBuilder1 = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.BALL)
                .withColor(ColorUtil.darkenColor(winnerTeamColor, 0.1))
                .withFade(ColorUtil.lightenColor(winnerTeamColor, 0.6));
        
        FireworkEffect.Builder effectBuilder2 = FireworkEffect.builder()
                .flicker(false)
                .trail(false)
                .with(FireworkEffect.Type.BURST)
                .withColor(ColorUtil.darkenColor(winnerTeamColor, 0.5))
                .withFade(ColorUtil.lightenColor(winnerTeamColor, 0.2));
        
        // Add the effect. (Multiple effects can be added.)
        fireworkMeta.addEffects(effectBuilder1.build(), effectBuilder2.build());
        fireworkMeta.setPower(random.nextInt(1, 7));
        firework.setFireworkMeta(fireworkMeta);
        
        // Flight behavior:
        firework.setShotAtAngle(false);
        
    }
    
}
