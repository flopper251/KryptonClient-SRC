package skid.krypton.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import skid.krypton.Krypton;
import skid.krypton.module.Category;
import skid.krypton.module.Module;
import skid.krypton.module.setting.*;
import skid.krypton.utils.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public final class ClickGUI extends Screen {
    private Category selectedCategory;
    private Module selectedModule;
    private String searchQuery;
    public Color currentColor;

    // Custom transparent constant (since java.awt.Color has no TRANSPARENT)
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // Color scheme
    private final Color BACKGROUND_COLOR = new Color(20, 20, 25, 200);
    private final Color PANEL_COLOR = new Color(30, 30, 35, 255);
    private final Color ACCENT_COLOR = new Color(138, 43, 226, 255);
    private final Color SELECTED_COLOR = new Color(138, 43, 226, 180);
    private final Color TEXT_COLOR = new Color(220, 220, 220, 255);
    private final Color SEARCH_BG = new Color(40, 40, 45, 255);
    private final Color HOVER_COLOR = new Color(60, 60, 70, 255);

    // Layout constants - made smaller and centered
    private static final int SETTINGS_PANEL_WIDTH = 250;
    private static final int CATEGORY_PANEL_WIDTH = 150;
    private static final int MODULE_PANEL_WIDTH = 280;
    private static final int HEADER_HEIGHT = 40;
    private static final int ITEM_HEIGHT = 28;
    private static final int PADDING = 12;
    private static final int PANEL_SPACING = 15;
    private static final int TOTAL_WIDTH = SETTINGS_PANEL_WIDTH + CATEGORY_PANEL_WIDTH + MODULE_PANEL_WIDTH + (PANEL_SPACING * 2);
    private static final int TOTAL_HEIGHT = 400;

    public ClickGUI() {
        super(Text.empty());
        this.selectedCategory = Category.COMBAT;
        this.searchQuery = "";
    }

    // Utility to convert java.awt.Color to MC color int
    private static int toMCColor(Color c) {
        return net.minecraft.util.math.ColorHelper.Argb.getArgb(c.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
    }

    public boolean isDraggingAlready() {
        return false;
    }

    public void setTooltip(final CharSequence tooltipText, final int tooltipX, final int tooltipY) {
        // Modern tooltip handling can be added here
    }

    public void setInitialFocus() {
        if (this.client == null) {
            return;
        }
        super.setInitialFocus();
    }

    public void render(final DrawContext drawContext, final int n, final int n2, final float n3) {
        if (Krypton.mc.currentScreen == this) {
            if (Krypton.INSTANCE.screen != null) {
                Krypton.INSTANCE.screen.render(drawContext, 0, 0, n3);
            }
            if (this.currentColor == null) {
                this.currentColor = new Color(0, 0, 0, 0);
            } else {
                this.currentColor = new Color(0, 0, 0, this.currentColor.getAlpha());
            }
            
            int n4;
            if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
                n4 = 200;
            } else {
                n4 = 0;
            }
            if (this.currentColor.getAlpha() != n4) {
                int n5;
                if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
                    n5 = 200;
                } else {
                    n5 = 0;
                }
                this.currentColor = ColorUtil.a(0.05f, n5, this.currentColor);
            }
            if (Krypton.mc.currentScreen instanceof ClickGUI) {
                drawContext.fill(0, 0, Krypton.mc.getWindow().getWidth(), Krypton.mc.getWindow().getHeight(), this.currentColor.getRGB());
            }
            RenderUtils.unscaledProjection();
            final int n6 = n * (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            final int n7 = n2 * (int) MinecraftClient.getInstance().getWindow().getScaleFactor();
            super.render(drawContext, n6, n7, n3);
            
            this.renderBackground(drawContext);
            this.renderSettingsPanel(drawContext, n6, n7);
            this.renderCategoryPanel(drawContext, n6, n7);
            this.renderModulePanel(drawContext, n6, n7);
            
            RenderUtils.scaledProjection();
        }
    }

    private void renderBackground(final DrawContext drawContext) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();

        if (skid.krypton.module.modules.client.Krypton.renderBackground.getValue()) {
            drawContext.fill(0, 0, screenWidth, screenHeight, toMCColor(new Color(0, 0, 0, 100)));
        }
    }

    private void renderSettingsPanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int endX = startX + SETTINGS_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, 8.0, 8.0, 8.0, 8.0, 50.0);

        if (this.selectedModule != null) {
            final String headerText = "SETTINGS: " + this.selectedModule.getName().toString().toUpperCase();
            TextRenderer.drawString(headerText, drawContext, startX + PADDING, startY + 12, toMCColor(ACCENT_COLOR));

            int yOffset = startY + HEADER_HEIGHT + PADDING;

            for (Object setting : this.selectedModule.getSettings()) {
                if (setting instanceof Setting) {
                    final Setting s = (Setting) setting;
                    final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, SETTINGS_PANEL_WIDTH, ITEM_HEIGHT);

                    if (isHovered) {
                        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), HOVER_COLOR,
                                startX + 5, yOffset, endX - 5, yOffset + ITEM_HEIGHT, 4.0, 4.0, 4.0, 4.0, 30.0);
                    }

                    TextRenderer.drawString(s.getName().toString().toUpperCase(), drawContext,
                            startX + PADDING, yOffset + 6, toMCColor(TEXT_COLOR));

                    String valueText = "";
                    Color valueColor = ACCENT_COLOR;

                    if (setting instanceof BooleanSetting) {
                        final BooleanSetting boolSetting = (BooleanSetting) setting;
                        valueText = boolSetting.getValue() ? "ON" : "OFF";
                        valueColor = boolSetting.getValue() ? ACCENT_COLOR : new Color(120, 120, 120, 255);
                    } else if (setting instanceof NumberSetting) {
                        final NumberSetting numSetting = (NumberSetting) setting;
                        valueText = String.format("%.2f", numSetting.getValue());
                    } else if (setting instanceof ModeSetting) {
                        final ModeSetting<?> modeSetting = (ModeSetting<?>) setting;
                        valueText = modeSetting.getValue().toString();
                    } else if (setting instanceof BindSetting) {
                        final BindSetting bindSetting = (BindSetting) setting;
                        valueText = bindSetting.getValue() == -1 ? "NONE" : String.valueOf(bindSetting.getValue());
                    } else if (setting instanceof StringSetting) {
                        final StringSetting stringSetting = (StringSetting) setting;
                        valueText = stringSetting.getValue().isEmpty() ? "EMPTY" : stringSetting.getValue();
                    }

                    TextRenderer.drawString(valueText, drawContext,
                            endX - PADDING - TextRenderer.getWidth(valueText), yOffset + 6, toMCColor(valueColor));

                    yOffset += ITEM_HEIGHT + 3;
                }
            }
        } else {
            TextRenderer.drawCenteredString("SELECT A MODULE", drawContext,
                    startX + SETTINGS_PANEL_WIDTH / 2, startY + 80, toMCColor(new Color(120, 120, 120, 255)));
        }
    }

    private void renderCategoryPanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int endX = startX + CATEGORY_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, 8.0, 8.0, 8.0, 8.0, 50.0);

        TextRenderer.drawString("KRYPTON+", drawContext, startX + PADDING, startY + 12, toMCColor(ACCENT_COLOR));

        int yOffset = startY + HEADER_HEIGHT + PADDING;
        for (Category category : Category.values()) {
            final boolean isSelected = category == this.selectedCategory;
            final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, CATEGORY_PANEL_WIDTH, ITEM_HEIGHT);

            Color bgColor = isSelected ? SELECTED_COLOR : (isHovered ? HOVER_COLOR : TRANSPARENT);
            Color textColor = isSelected ? Color.WHITE : TEXT_COLOR;

            if (bgColor != TRANSPARENT) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor,
                        startX + 5, yOffset, endX - 5, yOffset + ITEM_HEIGHT, 6.0, 6.0, 6.0, 6.0, 30.0);
            }

            TextRenderer.drawString(category.name.toString().toUpperCase(), drawContext,
                    startX + PADDING, yOffset + 6, toMCColor(textColor));

            yOffset += ITEM_HEIGHT + 3;
        }
    }

    private void renderModulePanel(final DrawContext drawContext, final int mouseX, final int mouseY) {
        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int startX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int startY = (screenHeight - TOTAL_HEIGHT) / 2;
        final int endX = startX + MODULE_PANEL_WIDTH;
        final int endY = startY + TOTAL_HEIGHT;

        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), PANEL_COLOR,
                startX, startY, endX, endY, 8.0, 8.0, 8.0, 8.0, 50.0);

        final String categoryTitle = "CATEGORY: " + this.selectedCategory.name.toString().toUpperCase();
        TextRenderer.drawString(categoryTitle, drawContext, startX + PADDING, startY + 12, toMCColor(TEXT_COLOR));

        final int searchY = startY + HEADER_HEIGHT - 12;
        RenderUtils.renderRoundedQuad(drawContext.getMatrices(), SEARCH_BG,
                startX + PADDING, searchY, endX - PADDING, searchY + 25, 6.0, 6.0, 6.0, 6.0, 30.0);

        final String searchText = this.searchQuery.isEmpty() ? "SEARCH..." : this.searchQuery;
        final Color searchTextColor = this.searchQuery.isEmpty() ? new Color(120, 120, 120, 255) : TEXT_COLOR;
        TextRenderer.drawString(searchText, drawContext, startX + PADDING + 8, searchY + 6, toMCColor(searchTextColor));

        final List<Module> modules = Krypton.INSTANCE.getModuleManager().a(this.selectedCategory);
        int yOffset = startY + HEADER_HEIGHT + 20;

        for (Module module : modules) {
            if (!this.searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())) {
                continue;
            }

            final boolean isSelected = module == this.selectedModule;
            final boolean isHovered = this.isHoveredInRect(mouseX, mouseY, startX, yOffset, MODULE_PANEL_WIDTH, ITEM_HEIGHT);
            final boolean isEnabled = module.isEnabled();

            Color bgColor = isSelected ? SELECTED_COLOR : (isHovered ? HOVER_COLOR : TRANSPARENT);
            Color textColor = isEnabled ? ACCENT_COLOR : TEXT_COLOR;

            if (bgColor != TRANSPARENT) {
                RenderUtils.renderRoundedQuad(drawContext.getMatrices(), bgColor,
                        startX + 5, yOffset, endX - 5, yOffset + ITEM_HEIGHT, 6.0, 6.0, 6.0, 6.0, 30.0);
            }

            TextRenderer.drawString(module.getName().toString().toUpperCase(), drawContext,
                    startX + PADDING, yOffset + 6, toMCColor(textColor));

            final Color indicatorColor = isEnabled ? ACCENT_COLOR : new Color(80, 80, 85, 255);
            RenderUtils.renderRoundedQuad(drawContext.getMatrices(), indicatorColor,
                    endX - 20, yOffset + 6, endX - 8, yOffset + 22, 3.0, 3.0, 3.0, 3.0, 20.0);

            yOffset += ITEM_HEIGHT + 2;
        }
    }

    private boolean isHoveredInRect(final int mouseX, final int mouseY, final int x, final int y, final int width, final int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (keyCode == 259 && !this.searchQuery.isEmpty()) {
            this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(final char chr, final int modifiers) {
        if (Character.isLetterOrDigit(chr) || chr == ' ') {
            this.searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final double scaledMouseX = mouseX * MinecraftClient.getInstance().getWindow().getScaleFactor();
        final double scaledMouseY = mouseY * MinecraftClient.getInstance().getWindow().getScaleFactor();

        final int screenWidth = Krypton.mc.getWindow().getWidth();
        final int screenHeight = Krypton.mc.getWindow().getHeight();
        final int categoryStartX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING;
        final int categoryStartY = (screenHeight - TOTAL_HEIGHT) / 2 + HEADER_HEIGHT + PADDING;
        int categoryY = categoryStartY;

        for (Category category : Category.values()) {
            if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, categoryStartX, categoryY, CATEGORY_PANEL_WIDTH, ITEM_HEIGHT)) {
                this.selectedCategory = category;
                this.selectedModule = null;
                return true;
            }
            categoryY += ITEM_HEIGHT + 3;
        }

        final int modulePanelStartX = (screenWidth - TOTAL_WIDTH) / 2 + SETTINGS_PANEL_WIDTH + PANEL_SPACING + CATEGORY_PANEL_WIDTH + PANEL_SPACING;
        final int modulePanelStartY = (screenHeight - TOTAL_HEIGHT) / 2 + HEADER_HEIGHT + 20;
        final List<Module> modules = Krypton.INSTANCE.getModuleManager().a(this.selectedCategory);
        int moduleY = modulePanelStartY;

        for (Module module : modules) {
            if (!this.searchQuery.isEmpty() && !module.getName().toString().toLowerCase().contains(this.searchQuery.toLowerCase())) {
                continue;
            }

            if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, modulePanelStartX, moduleY, MODULE_PANEL_WIDTH, ITEM_HEIGHT)) {
                if (button == 0) {
                    this.selectedModule = module;
                } else if (button == 1) {
                    module.toggle();
                }
                return true;
            }
            moduleY += ITEM_HEIGHT + 2;
        }

        if (this.selectedModule != null) {
            final int settingsPanelStartX = (screenWidth - TOTAL_WIDTH) / 2;
            final int settingsPanelStartY = (screenHeight - TOTAL_HEIGHT) / 2 + HEADER_HEIGHT + PADDING;
            int settingY = settingsPanelStartY;

            for (Object setting : this.selectedModule.getSettings()) {
                if (setting instanceof Setting) {
                    if (this.isHoveredInRect((int) scaledMouseX, (int) scaledMouseY, settingsPanelStartX, settingY, SETTINGS_PANEL_WIDTH, ITEM_HEIGHT)) {
                        this.handleSettingClick(setting, button);
                        return true;
                    }
                    settingY += ITEM_HEIGHT + 3;
                }
            }
        }

        return super.mouseClicked(scaledMouseX, scaledMouseY, button);
    }

    private void handleSettingClick(final Object setting, final int button) {
        if (setting instanceof BooleanSetting) {
            final BooleanSetting boolSetting = (BooleanSetting) setting;
            if (button == 0) {
                boolSetting.toggle();
            }
        } else if (setting instanceof ModeSetting) {
            final ModeSetting<?> modeSetting = (ModeSetting<?>) setting;
            if (button == 0) {
                modeSetting.cycleUp();
            } else if (button == 1) {
                modeSetting.cycleDown();
            }
        }
    }

    public boolean shouldPause() {
        return false;
    }

    public void close() {
        Krypton.INSTANCE.getModuleManager().getModuleByClass(skid.krypton.module.modules.client.Krypton.class).setEnabled(false);
        this.onGuiClose();
    }

    public void onGuiClose() {
        Krypton.mc.setScreenAndRender(Krypton.INSTANCE.screen);
        this.currentColor = null;
    }

    static {
    }
}
