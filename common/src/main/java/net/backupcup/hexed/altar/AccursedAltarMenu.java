package net.backupcup.hexed.altar;

import net.backupcup.hexed.Hexed;
import net.backupcup.hexed.hex.HexHelper;
import net.backupcup.hexed.network.AltarHexesPayload;
import net.backupcup.hexed.network.HexedNetwork;
import net.backupcup.hexed.register.RegisterItems;
import net.backupcup.hexed.register.RegisterMenus;
import net.backupcup.hexed.register.RegisterSounds;
import net.backupcup.hexed.register.RegisterStats;
import net.backupcup.hexed.util.TaintedItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AccursedAltarMenu extends AbstractContainerMenu {
    public static final int BUTTON_HEX = 0;
    public static final int BUTTON_SCROLL_UP = 1;
    public static final int BUTTON_SCROLL_DOWN = 2;

    private static final int MATERIAL_SLOT = 0;
    private static final int GEAR_SLOT = 1;

    private final Player player;
    @Nullable
    private final AccursedAltarBlockEntity altar;
    private final DataSlot active = DataSlot.standalone();
    private final Container inventory = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            AccursedAltarMenu.this.slotsChanged(this);
        }
    };

    private List<Holder.Reference<Enchantment>> availableHexes = List.of();
    private int currentHex = 0;

    /** Client side. */
    public AccursedAltarMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null);
    }

    public AccursedAltarMenu(int containerId, Inventory playerInventory, @Nullable AccursedAltarBlockEntity altar) {
        super(RegisterMenus.ACCURSED_ALTAR.get(), containerId);
        this.player = playerInventory.player;
        this.altar = altar;
        this.active.set(altar != null && altar.isActive() ? 1 : 0);

        addSlot(new Slot(inventory, MATERIAL_SLOT, 18, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(RegisterItems.BRIMSTONE_CRYSTAL.get());
            }
        });
        addSlot(new Slot(inventory, GEAR_SLOT, 18, 82) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return canBeHexed(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 18 + column * 18, 118 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 18 + column * 18, 176));
        }

        addDataSlot(active);
    }

    private boolean canBeHexed(ItemStack stack) {
        return !(stack.getItem() instanceof TaintedItem)
            && !Hexed.config().isListed(stack.getItem())
            && !HexHelper.availableHexes(player.level(), stack).isEmpty();
    }

    public ItemStack getMaterialSlot() {
        return inventory.getItem(MATERIAL_SLOT);
    }

    public ItemStack getGearSlot() {
        return inventory.getItem(GEAR_SLOT);
    }

    public boolean isAltarActive() {
        return active.get() == 1;
    }

    private void refreshActive() {
        active.set(altar != null && altar.isActive() ? 1 : 0);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (player.level().isClientSide()) return;

        refreshActive();
        availableHexes = HexHelper.availableHexes(player.level(), getGearSlot());
        currentHex = 0;
        sendHexes();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (availableHexes.isEmpty()) return false;

        switch (id) {
            case BUTTON_HEX -> {
                refreshActive();
                ItemStack gear = getGearSlot();
                if (!isAltarActive() || gear.isEmpty() || getMaterialSlot().isEmpty()) return false;

                Holder.Reference<Enchantment> hex = availableHexes.get(currentHex);
                HexHelper.removeHexes(gear);
                gear.enchant(hex, 1);
                if (!player.getAbilities().instabuild) {
                    getMaterialSlot().shrink(1);
                }

                player.awardStat(RegisterStats.ITEMS_HEXED.get());
                player.level().playSound(null, player.blockPosition(), RegisterSounds.ACCURSED_ALTAR_HEX.get(), SoundSource.BLOCKS, 1.25f, 1f);
                if (altar != null) altar.extinguishCandles();
                player.displayClientMessage(Component.translatable("message.hexed.altar_used")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD, ChatFormatting.ITALIC), true);

                inventory.setChanged();
            }
            case BUTTON_SCROLL_UP -> {
                refreshActive();
                currentHex = currentHex - 1 >= 0 ? currentHex - 1 : availableHexes.size() - 1;
                sendHexes();
            }
            case BUTTON_SCROLL_DOWN -> {
                refreshActive();
                currentHex = currentHex + 1 < availableHexes.size() ? currentHex + 1 : 0;
                sendHexes();
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void sendHexes() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        List<ResourceLocation> ids = new ArrayList<>(availableHexes.size());
        for (Holder.Reference<Enchantment> hex : availableHexes) {
            ids.add(hex.key().location());
        }
        HexedNetwork.sendToPlayer(serverPlayer, new AltarHexesPayload(ids, currentHex));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack original = slot.getItem();
            result = original.copy();
            if (index < inventory.getContainerSize()) {
                if (!moveItemStackTo(original, inventory.getContainerSize(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(original, 0, inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (original.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, inventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return altar == null || Container.stillValidBlockEntity(altar, player);
    }
}
