package io.github.nattocb.treasure_seas.common.tag;

import com.google.gson.*;
import io.github.nattocb.treasure_seas.TreasureSeas;
import io.github.nattocb.treasure_seas.core.FishWrapper;
import io.github.nattocb.treasure_seas.core.utility.FishUtils;
import io.github.nattocb.treasure_seas.core.utility.JsonHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
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
    private static final String PACK_MC_META = "pack.mcmeta";
    private static final Gson GSON = new Gson();

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
        if (serializer == PackMetadataSection.SERIALIZER) {
            int packFormat = readPackFormat();
            TreasureSeas.getLogger().info("DynamicResourcePack: using data pack: " + packFormat);
            Component description = new TextComponent("Adds dynamic raw fishes");
            return (T) new PackMetadataSection(description, packFormat);
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

    // Method to read pack_format from pack.mcmeta
    private int readPackFormat() {
        InputStream inputStream = null;
        try {
            // Attempt to read the pack.mcmeta file from the mod's resources
            inputStream = getClass().getClassLoader().getResourceAsStream(PACK_MC_META);
            if (inputStream == null) {
                // Handle the case where pack.mcmeta is not found
                throw new FileNotFoundException("pack.mcmeta not found in resources");
            }
            // Parse the JSON content of pack.mcmeta
            JsonObject json = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
            JsonObject packObject = json.getAsJsonObject("pack");
            if (packObject == null) {
                throw new JsonParseException("Missing 'pack' object in pack.mcmeta");
            }
            // First try to get 'forge:data_pack_format', then 'pack_format'
            int packFormat = -1;
            if (packObject.has("forge:data_pack_format")) {
                packFormat = packObject.get("forge:data_pack_format").getAsInt();
            } else if (packObject.has("pack_format")) {
                packFormat = packObject.get("pack_format").getAsInt();
            }

            if (packFormat == -1) {
                throw new JsonParseException("pack_format not found in pack.mcmeta");
            }

            return packFormat;
        } catch (IOException | JsonParseException e) {
            TreasureSeas.getLogger().error("Error reading pack.mcmeta: " + e.getMessage());
            // Default to a safe pack_format (e.g., 6 for 1.16)
            return 6;
        } finally {
            // Close the InputStream if it was opened
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
