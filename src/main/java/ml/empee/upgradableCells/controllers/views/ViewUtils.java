package ml.empee.upgradableCells.controllers.views;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.upgradableCells.config.LangConfig;
import org.bukkit.inventory.ItemStack;

/**
 * Utility for common used item inside guis
 */

@RequiredArgsConstructor
public class ViewUtils implements Bean {

  private final LangConfig langConfig;

  public ItemStack nextButton() {
    return ItemBuilder.from(XMaterial.ARROW.parseItem())
        .setName(langConfig.translate("navigation.next"))
        .build();
  }

  public ItemStack previousButton() {
    return ItemBuilder.from(XMaterial.ARROW.parseItem())
        .setName(langConfig.translate("navigation.previous"))
        .build();
  }

}
