package com.soliddowant.enderioconduitreplacer.integration.jei;

import javax.annotation.Nonnull;

import com.soliddowant.enderioconduitreplacer.init.ModItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

    @Override
    public void register(@Nonnull IModRegistry registry) {
        // 为导管替换器添加信息页面
        addConduitReplacerInfo(registry);
    }

    private void addConduitReplacerInfo(@Nonnull IModRegistry registry) {
        ItemStack conduitReplacer = new ItemStack(ModItems.CONDUIT_REPLACER);

        String[] description = new String[] {
            TextFormatting.WHITE.toString() + TextFormatting.BOLD + "导管替换器" + TextFormatting.RESET,
            "",
            "用于批量替换特定类型导管的工具。",
            "",
            "基础用法：",
            "1. " + TextFormatting.AQUA + "右键" + TextFormatting.RESET + "打开界面进行配置，或" + TextFormatting.AQUA + "[Shift]+ 中键" + TextFormatting.RESET + "点击导管以设定源/目标类型",
            "2. " + TextFormatting.AQUA + "右键" + TextFormatting.RESET + "点击导管束以替换匹配的导管",
            "",
            "可直接从JEI将物品拖入界面槽位。",
            "",
            "使用要求：",
            "• 源导管与目标导管须属同一类别",
            "• 目标替换用的导管必须存在于你的背包中",
        };

        registry.addIngredientInfo(conduitReplacer, VanillaTypes.ITEM, description);
    }
}
