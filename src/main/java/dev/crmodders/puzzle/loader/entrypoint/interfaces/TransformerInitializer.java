package dev.crmodders.puzzle.loader.entrypoint.interfaces;

import dev.crmodders.puzzle.loader.launch.Piece;
import dev.crmodders.puzzle.loader.launch.PuzzleClassLoader;
import dev.crmodders.puzzle.util.PuzzleEntrypointUtil;

public interface TransformerInitializer {
    String ENTRYPOINT_KEY = "transformers";

    void onTransformerInit(PuzzleClassLoader classLoader);

    static void invokeTransformers(PuzzleClassLoader classLoader) {
        Piece.MOD_LAUNCH_STATE = Piece.LAUNCH_STATE.TRANSFORMER_INJECT;

        PuzzleEntrypointUtil.invoke(ENTRYPOINT_KEY,
                TransformerInitializer.class,
                transformerInitializer -> {
                    transformerInitializer.onTransformerInit(classLoader);
                });
    }
}
