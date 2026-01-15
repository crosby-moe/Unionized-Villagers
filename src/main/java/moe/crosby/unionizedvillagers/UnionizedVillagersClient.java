package moe.crosby.unionizedvillagers;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class UnionizedVillagersClient {
    public static final Identifier WARNING_TEXTURE = new Identifier(UnionizedVillagers.MODID, "textures/gui/warning.png");
}
