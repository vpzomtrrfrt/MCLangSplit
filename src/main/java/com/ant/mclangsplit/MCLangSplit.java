package com.ant.mclangsplit;

import com.ant.mclangsplit.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCLangSplit implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "mclangsplit";

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        ModConfig.COMMON = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
