package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.MCLangSplit;
import com.ant.mclangsplit.config.ConfigHandler;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sun.media.jfxmedia.logging.Logger;
import net.minecraft.client.resources.ClientLanguageMap;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ClientLanguageMap.class)
public abstract class MixinClientLanguageMap {
    @Inject(at = @At("HEAD"), method="loadFrom", cancellable = true)
    private static void loadFrom(IResourceManager p_239497_0_, List<Language> p_239497_1_, CallbackInfoReturnable cir) {
        Map<String, String> map = Maps.newHashMap();
        Map<String, String> map1 = Maps.newHashMap();
        Map<String, String> map2 = Maps.newHashMap();
        boolean flag = false;

        for(Language language : p_239497_1_) {
            flag |= language.isBidirectional();
            String s = String.format("lang/%s.json", language.getCode());

            for(String s1 : p_239497_0_.getNamespaces()) {
                try {
                    ResourceLocation resourcelocation = new ResourceLocation(s1, s);
                    appendFrom(p_239497_0_.getResources(resourcelocation), map1);
                } catch (FileNotFoundException filenotfoundexception) {
                } catch (Exception exception) {
                    MCLangSplit.LOGGER.warn("Skipped language file: {}:{} ({})", s1, s, exception.toString());
                }
            }
        }

        boolean found = false;
        for (Language l : p_239497_1_) {
            if (l.getCode().equals(ConfigHandler.COMMON.languageSetting.get())) {
                found = true;
            }
        }
        if (!found) {
            Map<String, Language> langMap = extractLanguages(p_239497_0_.listPacks());
            if (langMap.containsKey(ConfigHandler.COMMON.languageSetting.get())) {
                Language language = langMap.get(ConfigHandler.COMMON.languageSetting.get());
                flag |= language.isBidirectional();
                String s = String.format("lang/%s.json", language.getCode());

                for(String s1 : p_239497_0_.getNamespaces()) {
                    try {
                        ResourceLocation resourcelocation = new ResourceLocation(s1, s);
                        appendFrom(p_239497_0_.getResources(resourcelocation), map2);
                    } catch (FileNotFoundException filenotfoundexception) {
                    } catch (Exception exception) {
                        MCLangSplit.LOGGER.warn("Skipped language file: {}:{} ({})", s1, s, exception.toString());
                    }
                }
            }
        }

        for (String s : map1.keySet()) {
            map.put(s, map1.get(s) + (map2.containsKey(s) && !map1.get(s).equals(map2.get(s)) ? " " + map2.get(s) : ""));
        }

        Constructor<?> constructor = ObfuscationReflectionHelper.findConstructor(ClientLanguageMap.class, Map.class, Boolean.TYPE);
        if (Modifier.isPrivate(constructor.getModifiers())) {
            constructor.setAccessible(true);
        }
        try {
            ClientLanguageMap clm = (ClientLanguageMap)constructor.newInstance(ImmutableMap.copyOf(map), flag);
            cir.setReturnValue(clm);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            MCLangSplit.LOGGER.error("Could not access constructor in ClientLanguageMap injection", ex);
        }
    }

    private static void appendFrom(List<IResource> resources, Map<String, String> map) {
        for(IResource iresource : resources) {
            try (InputStream inputstream = iresource.getInputStream()) {
                LanguageMap.loadFromJson(inputstream, map::put);
            } catch (IOException ioexception) {
                MCLangSplit.LOGGER.warn("Failed to load translations from {}", iresource, ioexception);
            }
        }

    }

    private static Map<String, Language> extractLanguages(Stream<IResourcePack> resourcePackStream) {
        Map<String, Language> map = Maps.newHashMap();
        resourcePackStream.forEach((e) -> {
            try {
                LanguageMetadataSection languagemetadatasection = e.getMetadataSection(LanguageMetadataSection.SERIALIZER);
                if (languagemetadatasection != null) {
                    for(Language language : languagemetadatasection.getLanguages()) {
                        map.putIfAbsent(language.getCode(), language);
                    }
                }
            } catch (IOException | RuntimeException runtimeexception) {
                MCLangSplit.LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", e.getName(), runtimeexception);
            }

        });
        return ImmutableMap.copyOf(map);
    }
}
