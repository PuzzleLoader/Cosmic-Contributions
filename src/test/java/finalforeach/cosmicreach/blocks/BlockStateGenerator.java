package finalforeach.cosmicreach.blocks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.github.puzzle.core.ResourceLocation;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.io.SaveLocation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

public class BlockStateGenerator {
    private static HashMap<String, BlockStateGenerator> generators = new HashMap<>();
    public String stringId;
    public String[] includes = new String[0];
    public String modelName;
    public OrderedMap<String, String> params;
    public OrderedMap<String, ?> overrides;

    private static void loadGeneratorsFromFile(String fileName) {
        String jsonStr = GameAssetLoader.loadAsset(fileName).readString();
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(jsonStr);
        JsonValue allGens = value.get("generators");
        JsonValue currentGenVal = allGens.child;

        for(Json json = new Json(); currentGenVal != null; currentGenVal = currentGenVal.next) {
            BlockStateGenerator generator = json.fromJson(BlockStateGenerator.class, currentGenVal.toJson(OutputType.json));
            if ((generator.params == null || generator.params.size == 0) && generator.includes.length == 0) {
                throw new RuntimeException("Generator " + generator.stringId + " must declare params or reference other generators");
            }

            generators.put(generator.stringId, generator);
        }

    }

    public static BlockStateGenerator getInstance(String genKey) {
        if (generators.containsKey(genKey))
            return generators.get(genKey);
        else {
            ResourceLocation location = ResourceLocation.fromString(genKey);
            location.name = "block_state_generators/" + location.name + ".json";
            System.out.println(location);
            loadGeneratorsFromFile(location.toString());
        }
        return generators.get(genKey);
    }

    public void generate(BlockState oldState) {
        for (String included : includes) {
            BlockStateGenerator subGenerator = getInstance(included);
            subGenerator.generate(oldState);
        }

        if (this.params != null && this.params.size != 0) {
            BlockState blockState = oldState.copy(false);
            blockState.stateGenerators = null;
            String newSaveKey = oldState.getSaveKey();

            ObjectMap.Entry<String, String> param;
            for(ObjectMap.Entries<String, String> var12 = this.params.entries().iterator(); var12.hasNext(); newSaveKey = BlockState.getModifiedSaveKey(newSaveKey, (String)param.key, ((String)param.value).toString())) {
                param = var12.next();
            }

            blockState.stringId = newSaveKey.replaceFirst(oldState.getBlockId(), "");
            blockState.stringId = blockState.stringId.substring(1, blockState.stringId.length() - 1);

            try {
                Object overrideVal;
                Field f;
                if (this.overrides != null) {
                    for(ObjectMap.Keys<String> overrides = this.overrides.keys().iterator(); overrides.hasNext(); f.set(blockState, overrideVal)) {
                        String override = overrides.next();
                        f = BlockState.class.getField(override);
                        overrideVal = this.overrides.get(override);
                        if (f.getType() == Integer.TYPE) {
                            if (overrideVal instanceof Float value) {
                                if (value == (float)value.intValue()) {
                                    overrideVal = value.intValue();
                                }
                            }
                        } else if (f.getType() == OrderedMap.class) {
                            overrideVal = (new Json()).readValue(f.getType(), (JsonValue)overrideVal);
                        }
                    }
                }
            } catch (Exception var9) {
                throw new RuntimeException(var9);
            }

            blockState.initialize(oldState.getBlock());
            if (this.modelName != null) {
                String genModelName = "gen_model::" + oldState.getBlockId() + "[" + blockState.stringId + "]";
                GameSingletons.blockModelInstantiator.createGeneratedModelInstance(blockState, oldState.getModel(), this.modelName, genModelName, blockState.rotXZ);
                blockState.setBlockModel(genModelName);
            }

            Block.allBlockStates.put(blockState.stringId, blockState);
            blockState.getBlock().blockStates.put(blockState.stringId, blockState);
        }
    }

    static {
        String folderName = "block_state_generators";
        String[] defaultAssetList = Gdx.files.internal("assets.txt").readString().split("\n");
        HashSet<String> assetsToLoad = new HashSet<>();

        for (String asset : defaultAssetList) {
            try {
                if (asset.startsWith(folderName) && asset.endsWith(".json") && Gdx.files.internal(asset).exists()) {
                    assetsToLoad.add(asset);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        FileHandle[] moddedBlockDir = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/" + folderName).list();

        for (FileHandle f : moddedBlockDir) {
            if (f.name().endsWith(".json")) {
                assetsToLoad.add(folderName + "/" + f.name());
            }
        }

        for (String asset : assetsToLoad) {
            loadGeneratorsFromFile(asset);

        }

    }
}
