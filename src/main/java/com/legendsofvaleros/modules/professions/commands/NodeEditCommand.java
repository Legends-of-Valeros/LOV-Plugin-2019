package com.legendsofvaleros.modules.professions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.legendsofvaleros.LegendsOfValeros;
import com.legendsofvaleros.modules.characters.api.PlayerCharacter;
import com.legendsofvaleros.modules.characters.core.Characters;
import com.legendsofvaleros.modules.professions.ProfessionsController;
import com.legendsofvaleros.modules.professions.gathering.mining.MiningNode;
import com.legendsofvaleros.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Crystall on 04/12/2019
 */
@CommandAlias("professions|prof")
public class NodeEditCommand extends BaseCommand {

    @Subcommand("node edit")
    @Description("Puts you into the node edit mode.")
    @CommandPermission("professions.edit")
    public void cmdNodeEdit(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            MessageUtil.sendError(sender, "You can't execute this command in the console");
            return;
        }
        if (!LegendsOfValeros.getMode().allowEditing()) {
            MessageUtil.sendError(sender, "The server mode doesn't allow editing data.");
            return;
        }
        if (!Characters.isPlayerCharacterLoaded((Player) sender)) {
            return;
        }
        PlayerCharacter playerCharacter = Characters.getPlayerCharacter((Player) sender);
        if (ProfessionsController.getInstance().editModePlayers.contains(playerCharacter)) {
            MessageUtil.sendError(sender, "You are no longer in edit mode.");
            ProfessionsController.getInstance().editModePlayers.remove(playerCharacter);
            for (MiningNode node : ProfessionsController.getInstance().zoneMiningNodes.get(playerCharacter.getCurrentZone().id)) {
                node.removeGlowing();
            }
            return;
        }
        ProfessionsController.getInstance().editModePlayers.add(playerCharacter);

        for (MiningNode node : ProfessionsController.getInstance().zoneMiningNodes.get(playerCharacter.getCurrentZone().id)) {
            node.setGlowing();
        }
        MessageUtil.sendInfo(sender, "You are now in professions editing mode.");
        MessageUtil.sendInfo(sender, "Placing nodes will cause them to be saved to the database.");
        MessageUtil.sendInfo(sender, "Destroying them will cause them to be deleted from the database.");
        MessageUtil.sendInfo(sender, "Existing nodes will be highlighted with a glow effect.");
    }

}
