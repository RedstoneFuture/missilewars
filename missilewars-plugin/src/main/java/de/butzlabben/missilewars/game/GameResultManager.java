package de.butzlabben.missilewars.game;

import de.butzlabben.missilewars.MissileWars;
import de.butzlabben.missilewars.configuration.Messages;
import de.butzlabben.missilewars.game.enums.GameResult;
import de.butzlabben.missilewars.game.enums.TeamType;
import de.butzlabben.missilewars.player.MWPlayer;
import de.butzlabben.missilewars.util.ColorUtil;
import de.butzlabben.missilewars.util.MoneyUtil;
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
            Team team = mwPlayer.getTeam();
            
            if (team.getTeamType() == TeamType.PLAYER) {
                sendMoney(mwPlayer);
                sendGameResultTitle(mwPlayer);
                sendGameResultSound(mwPlayer);
            } else {
                sendNeutralGameResultTitle(player);
            }
        }
        
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(20), 10);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(20), 40);
        Bukkit.getScheduler().runTaskLater(MissileWars.getInstance(), () -> spawnSetOfFireworks(20), 70);
        
    }
    
    /**
     * This method sends all team members the money for playing the game
     * with a specific amount for win and lose.
     */
    public void sendMoney(MWPlayer mwPlayer) {
        int money;

        switch (mwPlayer.getTeam().getGameResult()) {
            case WIN:
                money = game.getArena().getMoney().getWin();
                break;
            case LOSE:
                money = game.getArena().getMoney().getLoss();
                break;
            case DRAW:
                money = game.getArena().getMoney().getDraw();
                break;
            default:
                money = 0;
                break;
        }

        MoneyUtil.giveMoney(mwPlayer.getUuid(), money);
    }

    /**
     * This method sends all team members the title / subtitle of the
     * game result.
     */
    public void sendGameResultTitle(MWPlayer mwPlayer) {
        String title;
        String subTitle;

        switch (mwPlayer.getTeam().getGameResult()) {
            case WIN:
                title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_WINNER);
                subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_WINNER);
                break;
            case LOSE:
                title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_LOSER);
                subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_LOSER);
                break;
            case DRAW:
                title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_DRAW);
                subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_DRAW);
                break;
            default:
                title = null;
                subTitle = null;
                break;
        }

        mwPlayer.getPlayer().sendTitle(title, subTitle, 10, 140, 20);
    }

    /**
     * This method sends all team members the end-sound of the
     * game result.
     */
    public void sendGameResultSound(MWPlayer mwPlayer) {

        Player player = mwPlayer.getPlayer();

        switch (mwPlayer.getTeam().getGameResult()) {
            case WIN:
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100, 3);
                break;
            case LOSE:
            case DRAW:
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 100, 0);
                break;
            default:
                break;
        }
    }
    
    /**
     * This method sends the players the title / subtitle of the
     * game result there are not in a team (= spectator).
     */
    private void sendNeutralGameResultTitle(Player player) {
        String title;
        String subTitle;

        if (teamManager.getTeam1().getGameResult() == GameResult.WIN) {
            title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_WON)
                    .replace("%team%", teamManager.getTeam1().getName());
            subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_WON);

        } else if (teamManager.getTeam2().getGameResult() == GameResult.WIN) {
            title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_WON)
                    .replace("%team%", teamManager.getTeam2().getName());
            subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_WON);

        } else {
            title = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_TITLE_DRAW);
            subTitle = Messages.getMessage(false, Messages.MessageEnum.GAME_RESULT_SUBTITLE_DRAW);

        }

        player.sendTitle(title, subTitle, 10, 140, 20);
    }
    
    private void spawnSetOfFireworks(int amount) {
        for (int i = 1; i <= amount; i++) {
            spawnFirework();
        }
    }
    
    private void spawnFirework() {
        Color winnerTeamColor = null;
        
        if (teamManager.getTeam1().getGameResult() == GameResult.WIN) {
            winnerTeamColor = ColorConverter.getColorFromCode(teamManager.getTeam1().getColorCode());
        
        } else if (teamManager.getTeam2().getGameResult() == GameResult.WIN) {
            winnerTeamColor = ColorConverter.getColorFromCode(teamManager.getTeam2().getColorCode());
        
        }
        
        Firework firework = game.getGameWorld().getWorld().spawn(game.getArena().getSpectatorSpawn(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        
        fireworkMeta.clearEffects();

        Random random = new Random();
        
        // Create the effect:
        FireworkEffect.Builder effectBuilder = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.BALL);
        if (winnerTeamColor != null) {
            effectBuilder.withColor(ColorUtil.darkenColor(winnerTeamColor, 0.1));
            effectBuilder.withFade(ColorUtil.darkenColor(winnerTeamColor, 0.5));
        }
        
        // Add the effect. (Multiple effects can be added.)
        fireworkMeta.addEffect(effectBuilder.build());
        fireworkMeta.setPower(random.nextInt(1, 10));
        firework.setFireworkMeta(fireworkMeta);
        
        // Flight behavior:
        firework.setShotAtAngle(false);
        
    }
    
}
