package net.krsmes.bukkit.groovy;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * PublicPlot ...
 *
 * @author Kevin R. Smith - ksmith@pillartechnology.com
 * @since 2011-04-18
 */
public class PublicPlot extends Plot {
    private static final long serialVersionUID = 1L;

    public static String PUBLIC_PLOT_NAME = "PUBLIC";
    public static int PUBLIC_PLOT_START_DEPTH = 48;

    public static int[] BREAKABLE = new int[] {17,18,37,38,39,40,59,81,83,86};
    public static int[] PLACEABLE = new int[] {6,18,37,38,39,40,59,81,83,86,295,325,328,333,342,343,338,354};
    public static int[] INTERACTABLE = new int[] {26,54,58,61,64,69,71,77,93,94,95};


    public PublicPlot() {
        super(PUBLIC_PLOT_NAME);
        setStartDepth(PUBLIC_PLOT_START_DEPTH);
    }


    @Override
    public void setName(String name) {
    }

    @Override
    public void setOwner(String owner) {
    }

    @Override
    public void setOwner(Player owner) {
    }

    @Override
    public void setAreas(List<Area> areas) {
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public void addArea(Area area) {
    }

    @Override
    public boolean allowDamage(Player player, Block block) {
        return super.allowDamage(player, block) || Arrays.binarySearch(BREAKABLE, block.getTypeId()) >= 0;
    }

    @Override
    public boolean allowInteract(Player player, Block block, ItemStack item) {
        return super.allowInteract(player, block, item) ||
            (item != null && Arrays.binarySearch(PLACEABLE, item.getTypeId()) >= 0) ||
            (Arrays.binarySearch(INTERACTABLE, block.getTypeId()) >= 0);
    }
}
