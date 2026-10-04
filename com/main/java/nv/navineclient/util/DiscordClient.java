package nv.navineclient.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.client.Minecraft;
import nv.navineclient.NavineClient;


public class DiscordClient {
    private static JDA jda = null;
    private static final String CHANNEL_ID = "1466507536428699921";
    private static boolean connected = false;

    private static String resolveBotToken() {
        String token = System.getenv("NAVINE_DISCORD_TOKEN");
        if (token == null || token.isBlank()) {
            token = System.getProperty("navine.discordToken");
        }
        return token == null ? null : token.trim();
    }
    
    public static void initDiscord() {
        String botToken = resolveBotToken();
        if (botToken == null || botToken.isEmpty()) {
            NavineClient.LOGGER.warn("Discord bot disabled: set NAVINE_DISCORD_TOKEN or -Dnavine.discordToken to enable it.");
            return;
        }
        // Initialize Discord in a separate thread to avoid blocking
        new Thread(() -> {
            try {
                NavineClient.LOGGER.info("Initializing Discord bot...");
                
                // Create listener first
                net.dv8tion.jda.api.hooks.ListenerAdapter listener = new net.dv8tion.jda.api.hooks.ListenerAdapter() {
                    @Override
                    public void onReady(net.dv8tion.jda.api.events.session.ReadyEvent event) {
                        connected = true;
                        NavineClient.LOGGER.info("Discord bot connected successfully! Bot: " + event.getJDA().getSelfUser().getName());
                    }
                };
                
                // Build JDA with listener
                jda = JDABuilder.createDefault(botToken)
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(listener)
                        .build();
                
                NavineClient.LOGGER.info("Waiting for Discord bot to connect...");
                jda.awaitReady();
                NavineClient.LOGGER.info("Discord bot is ready!");
            } catch (Exception e) {
                connected = false;
                NavineClient.LOGGER.error("Failed to initialize Discord bot: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                if (e.getCause() != null) {
                    NavineClient.LOGGER.error("Caused by: " + e.getCause().getMessage());
                }
                e.printStackTrace();
            }
        }, "Discord-Init").start();
    }
    
    public static boolean isConnected() {
        if (jda == null) {
            return false;
        }
        JDA.Status status = jda.getStatus();
        boolean isConnected = connected && status == JDA.Status.CONNECTED;
        if (!isConnected && status != JDA.Status.SHUTDOWN && status != JDA.Status.INITIALIZING) {
            // Log status for debugging
            NavineClient.LOGGER.debug("Discord bot status: " + status + ", connected flag: " + connected);
        }
        return isConnected;
    }
    
    public static String getStatus() {
        if (jda == null) {
            return "Not initialized";
        }
        return jda.getStatus().toString();
    }
    
    public static void sendMessage(String message) {
        if (!isConnected()) {
            return;
        }
        
        try {
            TextChannel channel = jda.getTextChannelById(CHANNEL_ID);
            if (channel != null) {
                channel.sendMessage(message).queue();
            } else {
                NavineClient.LOGGER.error("Discord channel not found: " + CHANNEL_ID);
            }
        } catch (Exception e) {
            NavineClient.LOGGER.error("Failed to send Discord message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static String getMinecraftUsername() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getUser() != null) {
            return mc.getUser().getName();
        }
        return "Unknown";
    }
    
    public static void shutdown() {
        if (jda != null) {
            jda.shutdown();
            connected = false;
        }
    }
}
