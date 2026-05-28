```java
package com.badbones69.crazyauctions.controllers;

import com.badbones69.crazyauctions.CrazyAuctions;
import com.badbones69.crazyauctions.api.Managers;
import com.badbones69.crazyauctions.api.enums.Messages;
import com.badbones69.crazyauctions.api.objects.AuctionItem;
import com.badbones69.crazyauctions.methods.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.UUID;

import static com.badbones69.crazyauctions.controllers.GUI.List;

public class GUIListener implements Listener {

    @EventHandler
    public void onInvClick(InventoryClickEvent clickEvent) {

        if (!(clickEvent.getWhoClicked() instanceof Player)) return;

        Player player = (Player) clickEvent.getWhoClicked();

        if (!(clickEvent.getInventory().getHolder() instanceof AuctionMenu)) return;

        clickEvent.setCancelled(true);

        int slot = clickEvent.getSlot();

        if (!List.containsKey(player.getUniqueId())) return;

        // Bloqueia slots decorativos/botões
        if (slot >= 45) return;

        List<Integer> playerItems = List.get(player.getUniqueId());

        // Proteção contra slot inválido
        if (slot < 0 || slot >= playerItems.size()) return;

        int id = playerItems.get(slot);

        AuctionItem auctionItem = null;

        for (AuctionItem item : Managers.getAuctionManager().getItems()) {
            if (item.getItemID() == id) {
                auctionItem = item;
                break;
            }
        }

        // Item não encontrado
        if (auctionItem == null) {
            player.sendMessage(CrazyAuctions.getPrefix() + "§cEste item não existe mais.");
            player.closeInventory();
            return;
        }

        // Não pode comprar o próprio item
        UUID ownerUUID = auctionItem.getOwner();

        if (ownerUUID != null && ownerUUID.equals(player.getUniqueId())) {
            player.sendMessage(CrazyAuctions.getPrefix() + "§cVocê não pode comprar seu próprio item.");
            return;
        }

        // Verifica espaço no inventário
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(CrazyAuctions.getPrefix() + "§cSeu inventário está cheio.");
            return;
        }

        // Verifica dinheiro
        if (!Methods.hasMoney(player, auctionItem.getPrice())) {
            player.sendMessage(CrazyAuctions.getPrefix() + "§cVocê não possui dinheiro suficiente.");
            return;
        }

        // Remove dinheiro do comprador
        Methods.takeMoney(player, auctionItem.getPrice());

        // Dá dinheiro ao vendedor
        OfflinePlayer seller = Bukkit.getOfflinePlayer(ownerUUID);

        if (seller != null) {
            Methods.giveMoney(seller, auctionItem.getPrice());
        }

        // Entrega item ao comprador
        player.getInventory().addItem(auctionItem.getItem());

        // Remove item do leilão
        Managers.getAuctionManager().removeItem(auctionItem);

        // Atualiza inventário
        player.updateInventory();

        // Mensagem de sucesso
        player.sendMessage(CrazyAuctions.getPrefix() + "§aVocê comprou o item com sucesso!");

        player.closeInventory();
    }
}
```
