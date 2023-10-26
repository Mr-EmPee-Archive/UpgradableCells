package ml.empee.upgradableCells.controllers.views.utils;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class GTheme {

  public ItemStack background() {
    return ItemBuilder.from(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem())
        .setName(" ")
        .build();
  }

}
