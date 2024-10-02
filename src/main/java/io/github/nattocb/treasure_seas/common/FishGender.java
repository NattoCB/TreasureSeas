package io.github.nattocb.treasure_seas.common;

import net.minecraft.network.chat.TranslatableComponent;

public enum FishGender {

    MALE(new TranslatableComponent("fish.treasure_seas.gender.male")),
    FEMALE(new TranslatableComponent("fish.treasure_seas.gender.female"));

    private final TranslatableComponent translatableComponent;

    FishGender(TranslatableComponent translatableComponent) {
        this.translatableComponent = translatableComponent;
    }

    public String getGenderAsString() {
        return this.name();
    }

    public static FishGender fromString(String genderString) {
        return FishGender.valueOf(genderString.toUpperCase());
    }

    public TranslatableComponent getTranslatableComponent() {
        return translatableComponent;
    }

}