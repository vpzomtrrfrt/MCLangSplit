package com.ant.mclangsplit.mixin;

import com.ant.mclangsplit.MCLangSplit;
import com.ant.mclangsplit.config.ModConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.resource.metadata.LanguageResourceMetadata;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

@Mixin(TranslationStorage.class)
public abstract class MixinClientLanguageMap {
    private static final List<String> IGNORE_DUAL_TRANSLATION_KEYS = new ArrayList<>();
    static {
        IGNORE_DUAL_TRANSLATION_KEYS.add("translation.test.invalid");
        IGNORE_DUAL_TRANSLATION_KEYS.add("translation.test.invalid2");
        IGNORE_DUAL_TRANSLATION_KEYS.add("options.on.composed");
        IGNORE_DUAL_TRANSLATION_KEYS.add("options.off.composed");
    }

    @Inject(at = @At("RETURN"), method="load", cancellable = true)
    private static void load(ResourceManager p_239497_0_, List<LanguageDefinition> p_239497_1_, CallbackInfoReturnable<TranslationStorage> cir) {
        Map<String, String> map = Maps.newHashMap();
        Map<String, String> map1 = ((TranslationStorageAccessorMixin) cir.getReturnValue()).getTranslations();
        Map<String, String> map2 = Maps.newHashMap();
        boolean flag = false;

        for(LanguageDefinition language : p_239497_1_) {
            flag |= language.isRightToLeft();
            String s = String.format("lang/%s.json", language.getCode());

            for(String s1 : p_239497_0_.getAllNamespaces()) {
                try {
                    Identifier resourcelocation = new Identifier(s1, s);
                    appendFrom(p_239497_0_.getAllResources(resourcelocation), map1);
                } catch (FileNotFoundException filenotfoundexception) {
                } catch (Exception exception) {
                    MCLangSplit.LOGGER.warn("Skipped language file: {}:{} ({})", s1, s, exception.toString());
                }
            }
        }

        boolean found = false;
        for (LanguageDefinition l : p_239497_1_) {
            if (l.getCode().equals(ModConfig.COMMON.languageSetting)) {
                found = true;
            }
        }
        if (!found) {
            Map<String, LanguageDefinition> langMap = extractLanguages(p_239497_0_.streamResourcePacks());
            if (langMap.containsKey(ModConfig.COMMON.languageSetting)) {
                LanguageDefinition language = langMap.get(ModConfig.COMMON.languageSetting);
                flag |= language.isRightToLeft();
                String s = String.format("lang/%s.json", language.getCode());

                for(String s1 : p_239497_0_.getAllNamespaces()) {
                    try {
                        Identifier resourcelocation = new Identifier(s1, s);
                        appendFrom(p_239497_0_.getAllResources(resourcelocation), map2);
                    } catch (FileNotFoundException filenotfoundexception) {
                    } catch (Exception exception) {
                        MCLangSplit.LOGGER.warn("Skipped language file: {}:{} ({})", s1, s, exception.toString());
                    }
                }
            }
        }

        for (String s : map1.keySet()) {
            String str = map1.get(s);
            if (!ModConfig.COMMON.ignoreKeys.contains(s) && !IGNORE_DUAL_TRANSLATION_KEYS.contains(s) && map2.containsKey(s) && !specialEquals(map1.get(s), map2.get(s))) {
                String s1 = map2.get(s);
                if (s1.contains("%s") || s1.contains("$s")) {
                    int i = 1;
                    String tmp = str;
                    while (tmp.contains("%s")) {
                        int tmpi = tmp.indexOf("%s");
                        tmp = tmp.substring(0, tmpi) + "%" + i++ + "$s" + tmp.substring(tmpi + 2);
                    }
                    List<String> mappingList = new ArrayList<>();
                    while (tmp.contains("$s")) {
                        int tmpi = tmp.indexOf("$s");
                        mappingList.add(tmp.substring(tmpi-2, tmpi+2));
                        tmp = tmp.substring(tmpi+2);
                    }
                    i = 0;
                    while (s1.contains("%s")) {
                        int index = s1.indexOf("%s");
                        try {
                            s1 = s1.substring(0, index) + mappingList.get(i++) + s1.substring(index + 2);
                        } catch (IndexOutOfBoundsException ex) {
                            MCLangSplit.LOGGER.error(ex.getMessage() + "; " + str + " " + s1);
                        }
                    }
                }
                str += " " + s1;
            }
            map.put(s, str);
        }

        TranslationStorage clm = TranslationStorageAccessorMixin.createTranslationStorage(ImmutableMap.copyOf(map), flag);
        cir.setReturnValue(clm);
    }

    private static void appendFrom(List<Resource> resources, Map<String, String> map) {
        for(Resource iresource : resources) {
            try (InputStream inputstream = iresource.getInputStream()) {
                Language.load(inputstream, map::put);
            } catch (IOException ioexception) {
                MCLangSplit.LOGGER.warn("Failed to load translations from {}", iresource, ioexception);
            }
        }

    }

    private static Map<String, LanguageDefinition> extractLanguages(Stream<ResourcePack> resourcePackStream) {
        Map<String, LanguageDefinition> map = Maps.newHashMap();
        resourcePackStream.forEach((e) -> {
            try {
                LanguageResourceMetadata languagemetadatasection = e.parseMetadata(LanguageResourceMetadata.READER);
                if (languagemetadatasection != null) {
                    for(LanguageDefinition language : languagemetadatasection.getLanguageDefinitions()) {
                        map.putIfAbsent(language.getCode(), language);
                    }
                }
            } catch (IOException | RuntimeException runtimeexception) {
                MCLangSplit.LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", e.getName(), runtimeexception);
            }

        });
        return ImmutableMap.copyOf(map);
    }

    private static final Map<String, String> SPECIAL_REPLACE = new HashMap<>();
    static {
        SPECIAL_REPLACE.put("\uFF1A", ": ");
    }

    private static boolean specialEquals(String s1, String s2) {
        for (String s : SPECIAL_REPLACE.keySet()) {
            String sr = SPECIAL_REPLACE.get(s);
            s1 = s1.replace(s, sr);
            s2 = s2.replace(s, sr);
        }
        return s1.equals(s2);
    }
}
