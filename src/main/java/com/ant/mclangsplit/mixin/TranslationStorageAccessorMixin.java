package com.ant.mclangsplit.mixin;

import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TranslationStorage.class)
public interface TranslationStorageAccessorMixin {
    @Invoker("<init>")
    static TranslationStorage createTranslationStorage(Map<String, String> translations, boolean rightToLeft) {
        throw new AssertionError();
    }

    @Accessor
    Map<String, String> getTranslations();
}
