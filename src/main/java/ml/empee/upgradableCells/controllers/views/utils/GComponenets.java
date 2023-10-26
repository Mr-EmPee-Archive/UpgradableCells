package ml.empee.upgradableCells.controllers.views.utils;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import ml.empee.itembuilder.ItemBuilder;
import ml.empee.simplemenu.model.GItem;
import ml.empee.simplemenu.model.menus.InventoryMenu;
import ml.empee.simplemenu.model.panes.ScrollPane;
import ml.empee.upgradableCells.config.LangConfig;
import mr.empee.lightwire.annotations.Singleton;

/**
 * Utility for common used item inside guis
 */

@Singleton
@RequiredArgsConstructor
public class GComponenets {

  private final LangConfig langConfig;

  public GItem nextButton(ScrollPane pane, InventoryMenu menu) {
    var item = ItemBuilder.from(XMaterial.ARROW.parseItem())
        .setName(langConfig.translate("navigation.next"))
        .build();

    return GItem.builder()
        .itemStack(item)
        .visibilityHandler(() -> pane.getColOffset() < pane.getTotalCols())
        .clickHandler(e -> {
          pane.setColOffset(pane.getColOffset() + 1);
          menu.update();
        }).build();
  }

  public GItem previousButton(ScrollPane pane, InventoryMenu menu) {
    var item = ItemBuilder.from(XMaterial.ARROW.parseItem())
        .setName(langConfig.translate("navigation.previous"))
        .build();

    return GItem.builder()
        .itemStack(item)
        .visibilityHandler(() -> pane.getColOffset() > 0)
        .clickHandler(e -> {
          pane.setColOffset(pane.getColOffset() - 1);
          menu.update();
        }).build();
  }

}
