package io.github.nattocb.treasure_seas.common.tag;

import com.google.gson.*;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.core.config.ConfigManager;
import io.github.nattocb.treasure_seas.core.utility.FishUtils;
import io.github.nattocb.treasure_seas.core.utility.JsonHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DynamicResourcePack implements PackResources {

    private final Map<String, FishWrapper> fishWrapperMap;

    public DynamicResourcePack(Map<String, FishWrapper> fishWrapperMap) {
        this.fishWrapperMap = fishWrapperMap;
    }

    @Nullable
    @Override
    public InputStream getRootResource(@NotNull String fileName) {
        return null;
    }

    @Override
    public @NotNull InputStream getResource(@NotNull PackType type, @NotNull ResourceLocation location) throws IOException {
        if (type == PackType.SERVER_DATA && location.equals(new ResourceLocation("minecraft", "tags/items/raw_fishes.json"))) {
            JsonObject tagJson = new JsonObject();
            tagJson.addProperty("replace", false);

            JsonArray valuesArray = new JsonArray();
            for (String itemName : fishWrapperMap.keySet()) {
                if (FishUtils.isFish(fishWrapperMap.get(itemName))) {
                    valuesArray.add(new JsonPrimitive(itemName));
                }
            }

            tagJson.add("values", valuesArray);

            String jsonString = JsonHelper.toStableString(tagJson);
            return new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        }
        throw new FileNotFoundException(location.getPath());
    }

    @Override
    public @NotNull Collection<ResourceLocation> getResources(@NotNull PackType type, @NotNull String namespace, @NotNull String path, int maxDepth, @NotNull Predicate<String> filter) {
        if (type == PackType.SERVER_DATA && namespace.equals("minecraft") && path.equals("tags/items")) {
            if (filter.test("raw_fishes.json")) {
                return Collections.singletonList(
                        new ResourceLocation("minecraft", "tags/items/raw_fishes.json"));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasResource(@NotNull PackType type, @NotNull ResourceLocation location) {
        return type == PackType.SERVER_DATA && location.equals(
                new ResourceLocation("minecraft", "tags/items/raw_fishes.json"));
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        if (type == PackType.SERVER_DATA) {
            return Set.of("minecraft");
        }
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> serializer) {
        // todo other versions enum
        if (serializer == PackMetadataSection.SERIALIZER) {
            return (T) new PackMetadataSection(
                    new TextComponent("Adds dynamic raw fishes"),
                    8 // 1.18 数据包的 pack_format 版本是 8
            );
        }
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "treasure_seas_dynamic_resource_pack";
    }

    @Override
    public void close() {
    }

}