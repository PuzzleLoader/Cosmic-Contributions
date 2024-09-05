//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package finalforeach.cosmicreach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.puzzle.core.ResourceLocation;
import com.github.puzzle.util.AnsiColours;
import de.pottgames.tuningfork.SoundBuffer;
import finalforeach.cosmicreach.io.SaveLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameAssetLoader {
    public static final HashMap<String, FileHandle> ALL_ASSETS = new HashMap();
    public static final HashMap<String, SoundBuffer> ALL_SOUNDS = new HashMap();
    public static final HashMap<String, Texture> ALL_TEXTURES = new HashMap();
    public static AssetManager assetManager = new AssetManager();
    private static final String[] defaultAssetList;
    static JsonReader jsonReader;

    public GameAssetLoader() {
    }

    public static FileHandle loadAsset(String fileName) {
        return loadAsset(fileName, false);
    }

    public static SoundBuffer getSound(String fileName) {
        return get(fileName, ALL_SOUNDS, (f) -> {
            return GameSingletons.soundManager.loadSound(f);
        });
    }

    public static Texture getTexture(String fileName) {
        return get(fileName, ALL_TEXTURES, Texture::new);
    }

    public static <T> T get(String fileName, HashMap<String, T> map, Function<FileHandle, T> instantiator) {
        if (map.containsKey(fileName)) {
            return map.get(fileName);
        } else {
            T asset = instantiator.apply(loadAsset(fileName));
            map.put(fileName, asset);
            return asset;
        }
    }

    public static String[] getDefaultAssetList() {
        return defaultAssetList;
    }

    public static JsonValue loadJson(String path) {
        return jsonReader.parse(loadAsset(path));
    }

    public static JsonValue loadJson(FileHandle fileHandle) {
        return jsonReader.parse(fileHandle);
    }

    public static void forEachDefaultAsset(String prefix, String extension, Consumer<String> assetPathConsumer) {
        for (String assetPath : defaultAssetList) {
            if (assetPath.startsWith(prefix) && assetPath.endsWith(extension)) {
                assetPathConsumer.accept(assetPath);
            }
        }

    }

    public static FileHandle loadAsset(String fileName, boolean forceReload) {
        if (!forceReload && ALL_ASSETS.containsKey(fileName)) {
            return ALL_ASSETS.get(fileName);
        } else {
            ResourceLocation location = ResourceLocation.fromString(fileName);

            FileHandle modLocationFile = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/" + location.name);
            if (modLocationFile.exists()) {
                System.out.println("Loading " + AnsiColours.CYAN+"\""+location.name+"\"" + AnsiColours.WHITE + " from Mods Folder");
                return modLocationFile;
            }

            FileHandle classpathLocationFile = Gdx.files.classpath("assets/%s/%s".formatted(location.namespace, location.name));
            if (classpathLocationFile.exists()) {
                System.out.println("Loading " + AnsiColours.PURPLE + "\""+location.name+"\"" + AnsiColours.WHITE + " from Java Mod " + AnsiColours.GREEN + "\"" + location.namespace + "\"" + AnsiColours.WHITE);
                return classpathLocationFile;
            }

            FileHandle vanillaLocationFile = Gdx.files.internal(location.name);
            if (vanillaLocationFile.exists()) {
                System.out.println("Loading " + AnsiColours.YELLOW + "\""+location.name+"\""+AnsiColours.WHITE+" from Cosmic Reach");
                return vanillaLocationFile;
            }

            System.out.println("Cannot find the resource " + location);
            return null;
        }
    }

    public static void forEachAsset(String prefix, String extension, BiConsumer<String, FileHandle> assetConsumer) {
        HashSet<String> allPaths = new HashSet<>();

        for (String assetPath : defaultAssetList) {
            if (assetPath.startsWith(prefix) && assetPath.endsWith(extension)) {
                allPaths.add(assetPath);
            }
        }

        String modAssetRoot = Gdx.files.absolute(SaveLocation.getSaveFolderLocation() + "/mods/assets/").path().replace("\\", "/");
        String modPrefix = SaveLocation.getSaveFolderLocation() + "/mods/assets/" + prefix;
        FileHandle[] moddedAssetDir = Gdx.files.absolute(modPrefix).list();

        for (FileHandle asset : moddedAssetDir) {
            String assetPath = asset.path().replace("\\", "/").replace(modAssetRoot, "");
            if (assetPath.startsWith("/")) {
                assetPath = assetPath.substring(1);
            }

            if (assetPath.startsWith(prefix) && assetPath.endsWith(extension)) {
                allPaths.add(assetPath.replaceFirst(modPrefix, prefix));
            }
        }

        for (String path : allPaths) {
            assetConsumer.accept(path, loadAsset(path));
        }

    }

    static {
        defaultAssetList = Gdx.files.internal("assets.txt").readString().split("\n");
        jsonReader = new JsonReader();
    }
}
