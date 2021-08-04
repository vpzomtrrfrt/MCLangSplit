package com.ant.mclangsplit.config;

import com.ant.mclangsplit.MCLangSplit;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.Collections;
import java.util.List;

@Config(name = MCLangSplit.MOD_ID)
public class ModConfig implements ConfigData {
    public static ModConfig COMMON;

    @ConfigEntry.Gui.Tooltip()
    public String languageSetting = "";

    @ConfigEntry.Gui.Tooltip()
    public List<String> ignoreKeys = Collections.emptyList();
}
