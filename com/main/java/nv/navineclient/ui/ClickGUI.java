package nv.navineclient.ui;

import nv.navineclient.util.ClientAccess;

import com.mojang.blaze3d.platform.InputConstants;

import nv.navineclient.NavineClient;
import nv.navineclient.config.ConfigManager;
import nv.navineclient.module.CategoryKey;
import nv.navineclient.module.CategoryRegistry;
import nv.navineclient.module.Module;
import nv.navineclient.module.ModuleManager;
import nv.navineclient.module.settings.*;
import nv.navineclient.module.world.DevMod;
import nv.navineclient.util.NavineButtonRenderer;
import nv.navineclient.util.NavineTheme;
import nv.navineclient.util.NavineSoundManager;
import nv.navineclient.util.RenderUtil;
import nv.navineclient.util.UiAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClickGUI extends Screen {
    private static final Minecraft mc = Minecraft.getInstance();
    public static int getToggleKey() {
        return NavineClient.getClickGuiKey();
    }

    private static final int SIDEBAR_W = 78;
    private static final int HEADER_H = 20;
    private static final int SEARCH_H = 16;
    private static final int MODULE_LIST_WIDTH = 148;
    private static final int MODULE_HEIGHT = 14;
    private static final int SCROLLBAR_WIDTH = 3;
    private static final int FRAME_PAD = 6;
    private static final int CAT_ROW_H = 16;

    private static final int PANEL_BG = NavineTheme.CLICK_MODULE_BG;
    private static final int HEADER_BG = NavineTheme.HEADER_BG;
    private static final int ACCENT = NavineTheme.ACCENT;
    private static final int ACCENT2 = NavineTheme.ACCENT2;
    private static final int BORDER = NavineTheme.CLICK_MODULE_BORDER;
    private static final int HOVER = NavineTheme.HOVER;
    private static final int ENABLED_BG = NavineTheme.ENABLED_BG;
    private static final int TEXT_PRIMARY = NavineTheme.TEXT_PRIMARY;
    private static final int ENABLED_COLOR = NavineTheme.TEXT_PRIMARY;
    private static final int DISABLED_COLOR = NavineTheme.TEXT_DISABLED;
    private static final int SCROLLBAR_BG = NavineTheme.SCROLLBAR_BG;
    private static final int SCROLLBAR_COLOR = NavineTheme.SCROLLBAR_COLOR;
    private static final int SIDEBAR_BG = 0xFF141414;
    private static final int FRAME_BG = 0xF0121212;
    private static final int ROW_DIVIDER = 0xFF1E1E1E;
    private static final int TEXT_SECONDARY = NavineTheme.TEXT_SECONDARY;
    private static final int SEARCH_HIGHLIGHT = 0x405C9FD4;

    private int frameX;
    private int frameY;
    private int frameW;
    private int frameH;
    private int contentX;
    private int contentY;
    private int contentW;
    private int contentH;
    private int searchX;
    private int searchY;
    private int searchW;
    private int searchH;
    private int searchClearX;
    private int searchClearY;
    private int searchClearW;
    private int searchClearH;
    private int moduleListX;
    private int moduleListY;
    private int moduleListW;
    private int moduleListH;

    private static final Module.Category[] TAB_CATEGORY_ORDER = {
            Module.Category.COMBAT,
            Module.Category.PLAYER,
            Module.Category.MOVEMENT,
            Module.Category.RENDER,
            Module.Category.WORLD,
            Module.Category.MISC
    };

    private static final String SEARCH_TAB_ID = "__search__";
    private static final Map<String, Boolean> savedExp = new HashMap<>();

    private final List<TabEntry> tabs = new ArrayList<>();
    private String activeTabId = null;
    private int lastLayoutWidth = -1;
    private int lastCategoryCount = -1;

    private String searchQuery = "";
    private String searchText = "";
    private boolean searchFocused = false;
    private final Map<String, Integer> moduleScrollByTab = new HashMap<>();
    private final Map<String, Float> smoothScrollByTab = new HashMap<>();
    private int maxModuleScroll = 0;

    private Module selectedModule = null;
    private Module settingsModule = null;
    private NumberSetting draggingSlider = null;
    private ColorSetting draggingColorSetting = null;
    private int draggingColorChannel = -1;
    private StringSetting editingStringSetting = null;
    private String stringEditBuffer = "";
    private boolean listeningKey = false;
    private int settingsScroll = 0;
    private float settingsAnim = 0f;
    private float tabFadeAnim = 1f;
    private float openAnim = 0f;
    private float closeHoverAnim = 0f;
    private float uiDelta = 0f;
    private String lastContentTabId = null;
    private final Map<Module, Float> moduleHoverAnim = new HashMap<>();
    private final Map<Module, Float> moduleEnableAnim = new HashMap<>();
    private final Map<Setting<?>, Float> settingToggleAnim = new HashMap<>();
    private final Map<String, Float> tabHoverAnim = new HashMap<>();

    private boolean leftMouseDown = false;
    private boolean rightMouseDown = false;
    private boolean toggleKeyHeld = false;

    private int settingsPanelX = -1;
    private int settingsPanelY = -1;
    private boolean draggingSettingsPanel = false;
    private int settingsDragOffsetX = 0;
    private int settingsDragOffsetY = 0;
    private boolean pendingSettingsDrag = false;
    private double pendingSettingsDragX = 0;
    private double pendingSettingsDragY = 0;

    private String draggingTabId = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private String pendingTabClickId = null;
    private double pendingTabClickX = 0;
    private double pendingTabClickY = 0;
    private static final int TAB_DRAG_THRESHOLD = 4;

    public ClickGUI() {
        super(Component.literal("Navine Client"));
    }

    public void prepareForOpen() {
        savedExp.clear();
        activeTabId = null;
        searchQuery = "";
        searchText = "";
        searchFocused = false;
        moduleScrollByTab.clear();
        selectedModule = null;
        settingsModule = null;
        listeningKey = false;
        editingStringSetting = null;
        stringEditBuffer = "";
        draggingColorSetting = null;
        draggingColorChannel = -1;
        settingsScroll = 0;
        settingsAnim = 0f;
        tabFadeAnim = 1f;
        openAnim = 0f;
        closeHoverAnim = 0f;
        tabHoverAnim.clear();
        lastContentTabId = null;
        settingsPanelX = -1;
        settingsPanelY = -1;
        draggingSettingsPanel = false;
        pendingSettingsDrag = false;
        draggingTabId = null;
        pendingTabClickId = null;
        lastLayoutWidth = -1;
        for (TabEntry tab : tabs) {
            tab.expanded = false;
        }
        ensureLayout();
        for (TabEntry tab : tabs) {
            tab.expanded = tab.id.equals(activeTabId);
            savedExp.put(tab.id, tab.expanded);
        }
        toggleKeyHeld = ClientAccess.isKeyDown(NavineClient.getClickGuiKey());
    }

    private void ensureLayout() {
        int sw = width > 0 ? width : mc.getWindow().getGuiScaledWidth();
        int catCount = CategoryRegistry.orderedKeys().size();
        if (sw == lastLayoutWidth && catCount == lastCategoryCount && !tabs.isEmpty()) {
            return;
        }
        lastLayoutWidth = sw;
        lastCategoryCount = catCount;
        buildTabs();
        layoutTabs(sw);
    }

    private void buildTabs() {
        tabs.clear();
        for (Module.Category category : TAB_CATEGORY_ORDER) {
            tabs.add(TabEntry.category(CategoryKey.builtin(category)));
        }
        for (CategoryKey key : CategoryRegistry.orderedKeys()) {
            if (key.isCustom()) {
                tabs.add(TabEntry.category(key));
            }
        }
        tabs.add(TabEntry.search());
        if (activeTabId == null && !tabs.isEmpty()) {
            activeTabId = tabs.get(0).id;
        }
    }

    private void layoutTabs(int sw) {
        int listMax = ConfigManager.getModuleListMaxHeight();
        int catH = Math.max(CAT_ROW_H * tabs.size(), listMax);
        contentH = Math.max(listMax, catH);
        frameW = SIDEBAR_W + MODULE_LIST_WIDTH + FRAME_PAD * 2 + 4;
        frameH = HEADER_H + SEARCH_H + contentH + FRAME_PAD * 2 + 4;
        frameX = Math.max(8, (sw - frameW) / 2);
        frameY = Math.max(8, (height > 0 ? height : mc.getWindow().getGuiScaledHeight()) / 2 - frameH / 2);

        contentX = frameX + SIDEBAR_W + FRAME_PAD;
        contentY = frameY + HEADER_H + SEARCH_H + FRAME_PAD;
        contentW = MODULE_LIST_WIDTH;

        searchX = contentX;
        searchY = frameY + HEADER_H + 3;
        searchW = contentW;
        searchH = SEARCH_H - 2;

        moduleListX = contentX;
        moduleListY = contentY;
        moduleListW = contentW;
        moduleListH = contentH;

        int catY = frameY + HEADER_H + FRAME_PAD;
        for (TabEntry tab : tabs) {
            tab.x = frameX + 1;
            tab.y = catY;
            tab.width = SIDEBAR_W - 2;
            tab.height = CAT_ROW_H;
            tab.expanded = tab.id.equals(activeTabId);
            tab.moduleListX = moduleListX;
            tab.moduleListY = moduleListY;
            tab.moduleListW = moduleListW;
            tab.moduleListH = moduleListH;
            tab.searchInputX = searchX;
            tab.searchInputY = searchY;
            tab.searchInputW = searchW;
            tab.searchInputH = searchH;
            catY += CAT_ROW_H;
        }
    }

    private int getModuleScroll() {
        if (activeTabId == null) {
            return 0;
        }
        return moduleScrollByTab.getOrDefault(activeTabId, 0);
    }

    private void setModuleScroll(int scroll) {
        if (activeTabId != null) {
            moduleScrollByTab.put(activeTabId, scroll);
        }
    }

    private void updateTabDrag(int mx, int my) {
    }

    private TabEntry findTab(String id) {
        for (TabEntry tab : tabs) {
            if (tab.id.equals(id)) {
                return tab;
            }
        }
        return null;
    }

    private TabEntry getActiveTab() {
        return activeTabId != null ? findTab(activeTabId) : null;
    }

    private boolean isTabExpanded(TabEntry tab) {
        return tab != null && tab.id.equals(activeTabId);
    }

    @Override
    protected void init() {
        ensureLayout();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        lastLayoutWidth = -1;
        ensureLayout();
    }

    @Override
    public void onClose() {
        if (activeTabId != null) {
            TabEntry tab = findTab(activeTabId);
            if (tab != null) {
                savedExp.put(tab.id, tab.expanded);
            }
        }
        ConfigManager.save();
        selectedModule = null;
        settingsModule = null;
        listeningKey = false;
        settingsScroll = 0;
        settingsAnim = 0f;
        settingsPanelX = -1;
        settingsPanelY = -1;
        searchFocused = false;
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        ensureLayout();
        long window = mc.getWindow().handle();
        int toggleKey = NavineClient.getClickGuiKey();
        boolean togglePressed = ClientAccess.isKeyDown(toggleKey);
        if (!togglePressed) {
            toggleKeyHeld = false;
        }

        boolean leftNow = ClientAccess.isMouseButtonDown(InputConstants.MOUSE_BUTTON_LEFT);
        boolean rightNow = ClientAccess.isMouseButtonDown(InputConstants.MOUSE_BUTTON_RIGHT);

        if (leftNow && !leftMouseDown) {
            handleMouseClicked(mx, my, 0);
        }
        if (rightNow && !rightMouseDown) {
            handleMouseClicked(mx, my, 1);
        }
        if (!leftNow && leftMouseDown) {
            handleMouseReleased(mx, my, 0);
        }
        if (!rightNow && rightMouseDown) {
            handleMouseReleased(mx, my, 1);
        }

        leftMouseDown = leftNow;
        rightMouseDown = rightNow;
        uiDelta = delta;
        openAnim = UiAnim.approach(openAnim, 1f, 10f, delta);

        if (leftNow && pendingSettingsDrag && !draggingSettingsPanel) {
            double sdx = mx - pendingSettingsDragX;
            double sdy = my - pendingSettingsDragY;
            if (Math.abs(sdx) > TAB_DRAG_THRESHOLD || Math.abs(sdy) > TAB_DRAG_THRESHOLD) {
                draggingSettingsPanel = true;
                pendingSettingsDrag = false;
            }
        }

        if (leftNow && draggingSettingsPanel && settingsModule != null) {
            SettingsLayout layout = computeSettingsLayout(settingsAnim);
            int newX = mx - settingsDragOffsetX;
            int newY = my - settingsDragOffsetY;
            settingsPanelX = Math.max(0, Math.min(newX, width - layout.scaledW));
            settingsPanelY = Math.max(0, Math.min(newY, height - layout.scaledH));
        }

        int alpha = Math.min(255, Math.max(0, (int) (openAnim * 255)));
        ctx.fill(0, 0, width, height, UiAnim.scaleAlpha(NavineTheme.CLICK_SCREEN_DIM, openAnim));
        renderFrame(ctx, mx, my, alpha);

        TabEntry active = getActiveTab();
        if (active != null) {
            if (!active.id.equals(lastContentTabId)) {
                tabFadeAnim = 0f;
                lastContentTabId = active.id;
            }
            tabFadeAnim = UiAnim.approach(tabFadeAnim, 1f, 12f, delta);
            int contentAlpha = Math.min(255, Math.max(0, (int) (tabFadeAnim * openAnim * 255)));
            renderSearchBar(ctx, mx, my, contentAlpha);
            renderModuleList(ctx, active, mx, my, contentAlpha);
        } else {
            lastContentTabId = null;
            tabFadeAnim = 1f;
        }

        float targetAnim = selectedModule != null ? 1f : 0f;
        settingsAnim = UiAnim.approach(settingsAnim, targetAnim, 10f, delta);
        if (selectedModule != null) {
            settingsModule = selectedModule;
        } else if (settingsAnim < 0.02f) {
            settingsModule = null;
            settingsScroll = 0;
        }

        if (settingsModule != null && settingsAnim > 0.01f) {
            renderSettingsPanel(ctx, mx, my, settingsAnim);
        }

        if (leftNow && draggingSlider != null && settingsModule != null && leftMouseDown) {
            updateSlider(mx, my);
        }
        if (leftNow && draggingColorSetting != null && settingsModule != null && leftMouseDown) {
            updateColorSlider(mx, my);
        }

        super.extractRenderState(ctx, mx, my, delta);
    }

    private void renderFrame(GuiGraphicsExtractor ctx, int mx, int my, int alpha) {
        RenderUtil.drawPanel(ctx, frameX, frameY, frameW, frameH, applyAlpha(FRAME_BG, alpha), applyAlpha(BORDER, alpha));
        ctx.fill(frameX + 1, frameY + 1, frameX + SIDEBAR_W, frameY + frameH - 1, applyAlpha(SIDEBAR_BG, alpha));
        RenderUtil.drawVLine(ctx, frameX + SIDEBAR_W, frameY + 1, frameH - 2, applyAlpha(BORDER, alpha));
        ctx.fill(frameX + 1, frameY + 1, frameX + frameW - 1, frameY + HEADER_H, applyAlpha(HEADER_BG, alpha));
        RenderUtil.drawHLine(ctx, frameX + 1, frameY + HEADER_H, frameW - 2, applyAlpha(BORDER, alpha));
        String title = "Navine";
        ctx.text(font, title, frameX + 8, frameY + 6, applyAlpha(TEXT_PRIMARY, alpha), false);
        TabEntry active = getActiveTab();
        if (active != null) {
            String cat = active.label;
            int catW = mc.font.width(cat);
            ctx.text(font, cat, frameX + frameW - catW - 8, frameY + 6, applyAlpha(TEXT_SECONDARY, alpha), false);
        }
        for (TabEntry tab : tabs) {
            renderCategory(ctx, tab, mx, my, alpha);
        }
    }

    private void renderCategory(GuiGraphicsExtractor ctx, TabEntry tab, int mx, int my, int alpha) {
        boolean active = tab.id.equals(activeTabId);
        boolean hover = mx >= tab.x && mx <= tab.x + tab.width && my >= tab.y && my <= tab.y + tab.height;
        float hoverAnim = tabHoverAnim.getOrDefault(tab.id, 0f);
        hoverAnim = UiAnim.approach(hoverAnim, hover ? 1f : 0f, 14f, uiDelta);
        tabHoverAnim.put(tab.id, hoverAnim);
        if (active) {
            ctx.fill(tab.x, tab.y, tab.x + tab.width, tab.y + tab.height, applyAlpha(ENABLED_BG, alpha));
            RenderUtil.drawAccentBar(ctx, tab.x, tab.y + 2, tab.height - 4, applyAlpha(ACCENT, alpha));
        } else if (hoverAnim > 0.01f) {
            ctx.fill(tab.x, tab.y, tab.x + tab.width, tab.y + tab.height,
                    applyAlpha(UiAnim.lerpColor(0x00000000, HOVER, hoverAnim), alpha));
        }
        int textColor = active ? TEXT_PRIMARY : UiAnim.lerpColor(TEXT_SECONDARY, TEXT_PRIMARY, hoverAnim);
        ctx.text(font, tab.label, tab.x + 8, tab.y + 4, applyAlpha(textColor, alpha), false);
    }

    private void renderSearchBar(GuiGraphicsExtractor ctx, int mx, int my, int alpha) {
        int border = searchFocused ? applyAlpha(ACCENT, alpha) : applyAlpha(BORDER, alpha);
        ctx.fill(searchX, searchY, searchX + searchW, searchY + searchH, applyAlpha(PANEL_BG, alpha));
        RenderUtil.drawOutline(ctx, searchX, searchY, searchW, searchH, 1, border);
        String display = searchText.isEmpty() ? "Search..." : searchText;
        int textColor = searchText.isEmpty() ? applyAlpha(DISABLED_COLOR, alpha) : applyAlpha(TEXT_PRIMARY, alpha);
        if (searchText.isEmpty()) {
            ctx.text(font, display, searchX + 4, searchY + 4, textColor, false);
        } else {
            navine_renderSearchText(ctx, searchX + 4, searchY + 4, searchText, searchQuery, alpha);
        }
        if (!searchText.isEmpty()) {
            searchClearX = searchX + searchW - 12;
            searchClearY = searchY;
            searchClearW = 10;
            searchClearH = searchH;
            boolean clearHover = mx >= searchClearX && mx <= searchClearX + searchClearW
                    && my >= searchClearY && my <= searchClearY + searchClearH;
            ctx.text(font, "x", searchClearX + 1, searchY + 4,
                    clearHover ? applyAlpha(TEXT_PRIMARY, alpha) : applyAlpha(DISABLED_COLOR, alpha), false);
        } else {
            searchClearX = searchClearY = searchClearW = searchClearH = 0;
        }
        TabEntry active = getActiveTab();
        if (active != null) {
            active.searchInputX = searchX;
            active.searchInputY = searchY;
            active.searchInputW = searchW;
            active.searchInputH = searchH;
            active.searchClearX = searchClearX;
            active.searchClearY = searchClearY;
            active.searchClearW = searchClearW;
            active.searchClearH = searchClearH;
        }
    }

    private boolean matchesSearch(Module module, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String name = module.getName().toLowerCase();
        String normalizedName = name.replace("_", " ").replace("-", " ");
        String compactName = normalizedName.replace(" ", "");
        String compactQuery = query.replace(" ", "");
        if (normalizedName.contains(query) || compactName.contains(compactQuery)) {
            return true;
        }
        String description = module.getDescription();
        if (description != null && description.toLowerCase().contains(query)) {
            return true;
        }
        String[] queryParts = query.split("\\s+");
        if (queryParts.length > 1) {
            for (String part : queryParts) {
                if (part.isEmpty()) {
                    continue;
                }
                if (!normalizedName.contains(part) && !compactName.contains(part.replace(" ", ""))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private List<Module> getModulesForTab(TabEntry tab) {
        if (tab.search) {
            List<Module> all = new ArrayList<>();
            for (Module m : ModuleManager.getModules()) {
                if (ModuleManager.isModuleAccessible(m)) {
                    all.add(m);
                }
            }
            if (searchQuery.isEmpty()) {
                return all;
            }
            List<Module> filtered = new ArrayList<>();
            for (Module m : all) {
                if (matchesSearch(m, searchQuery)) {
                    filtered.add(m);
                }
            }
            return filtered;
        }
        List<Module> mods = ModuleManager.getModulesByCategoryKey(tab.categoryKey);
        if (searchQuery.isEmpty()) {
            return mods;
        }
        List<Module> filtered = new ArrayList<>();
        for (Module m : mods) {
            if (matchesSearch(m, searchQuery)) {
                filtered.add(m);
            }
        }
        return filtered;
    }

    private int getModuleListTop(TabEntry tab) {
        return moduleListY;
    }

    private int getModuleListX(TabEntry tab) {
        return moduleListX;
    }

    private void renderModuleList(GuiGraphicsExtractor ctx, TabEntry tab, int mx, int my, int alpha) {
        List<Module> mods = getModulesForTab(tab);
        int listX = moduleListX;
        int listY = moduleListY;
        int contentRows = mods.size() * MODULE_HEIGHT + 2;
        int visH = moduleListH;
        boolean hasScrollbar = contentRows > visH;

        tab.moduleListX = listX;
        tab.moduleListY = listY;
        tab.moduleListW = MODULE_LIST_WIDTH;
        tab.moduleListH = visH;

        maxModuleScroll = Math.max(0, contentRows - visH);
        setModuleScroll(Math.max(0, Math.min(getModuleScroll(), maxModuleScroll)));

        float smooth = smoothScrollByTab.getOrDefault(tab.id, (float) getModuleScroll());
        smooth = UiAnim.approach(smooth, getModuleScroll(), 16f, uiDelta);
        smoothScrollByTab.put(tab.id, smooth);
        int renderScroll = Math.round(smooth);

        ctx.fill(listX, listY, listX + MODULE_LIST_WIDTH, listY + visH, applyAlpha(PANEL_BG, alpha));
        RenderUtil.drawOutline(ctx, listX, listY, MODULE_LIST_WIDTH, visH, 1, applyAlpha(BORDER, alpha));

        if (mods.isEmpty()) {
            String emptyMsg = searchQuery.isEmpty() ? "No modules" : "No matches";
            ctx.text(font, emptyMsg, listX + 6, listY + 5, applyAlpha(DISABLED_COLOR, alpha), false);
            return;
        }

        int moduleAreaWidth = hasScrollbar ? MODULE_LIST_WIDTH - SCROLLBAR_WIDTH - 2 : MODULE_LIST_WIDTH;
        ctx.enableScissor(listX + 1, listY + 1, listX + moduleAreaWidth, listY + visH - 1);

        int curY = listY + 1 - renderScroll;
        for (Module m : mods) {
            float moduleHover = moduleHoverAnim.getOrDefault(m, 0f);
            boolean moduleHovered = mx >= listX + 1 && mx <= listX + moduleAreaWidth - 1
                    && my >= Math.max(curY, listY) && my <= Math.min(curY + MODULE_HEIGHT - 1, listY + visH);
            float targetHover = moduleHovered ? 1f : 0f;
            moduleHover = UiAnim.approach(moduleHover, targetHover, 12f, uiDelta);
            moduleHoverAnim.put(m, moduleHover);
            renderModule(ctx, m, listX, curY, mx, my, listY, listY + visH, moduleAreaWidth, moduleHover, alpha);
            curY += MODULE_HEIGHT;
        }

        ctx.disableScissor();

        if (hasScrollbar) {
            int scrollbarX = listX + MODULE_LIST_WIDTH - SCROLLBAR_WIDTH - 1;
            ctx.fill(scrollbarX, listY + 1, scrollbarX + SCROLLBAR_WIDTH, listY + visH - 1, applyAlpha(SCROLLBAR_BG, alpha));
            double scrollPct = maxModuleScroll > 0 ? (double) renderScroll / maxModuleScroll : 0;
            int thumbH = Math.max(8, (int) ((double) visH / contentRows * visH));
            int thumbY = listY + 1 + (int) (scrollPct * (visH - 2 - thumbH));
            ctx.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbH, applyAlpha(SCROLLBAR_COLOR, alpha));
        }
    }

    private void renderModule(GuiGraphicsExtractor ctx, Module m, int listX, int curY, int mx, int my,
                              int clipTop, int clipBot, int areaW, float hoverAnim, int alpha) {
        if (curY + MODULE_HEIGHT < clipTop || curY > clipBot) return;

        float enableAnim = moduleEnableAnim.getOrDefault(m, m.isEnabled() ? 1f : 0f);
        enableAnim = UiAnim.approach(enableAnim, m.isEnabled() ? 1f : 0f, 12f, uiDelta);
        moduleEnableAnim.put(m, enableAnim);

        if (enableAnim > 0.01f) {
            int enabledColor = applyAlpha(UiAnim.scaleAlpha(UiAnim.lerpColor(ENABLED_BG, HOVER, hoverAnim), enableAnim), alpha);
            ctx.fill(listX + 1, curY, listX + areaW - 1, curY + MODULE_HEIGHT, enabledColor);
            RenderUtil.drawAccentBar(ctx, listX + 1, curY + 2, MODULE_HEIGHT - 4, applyAlpha(ACCENT, alpha));
        } else if (hoverAnim > 0.01f) {
            ctx.fill(listX + 1, curY, listX + areaW - 1, curY + MODULE_HEIGHT,
                    applyAlpha(UiAnim.lerpColor(0x00000000, HOVER, hoverAnim), alpha));
        }

        RenderUtil.drawHLine(ctx, listX + 1, curY + MODULE_HEIGHT - 1, areaW - 2, applyAlpha(ROW_DIVIDER, alpha));

        int txtC = applyAlpha(UiAnim.lerpColor(m.isEnabled() ? ENABLED_COLOR : DISABLED_COLOR, ENABLED_COLOR, hoverAnim * 0.6f), alpha);
        int textX = listX + 6 + (m.isEnabled() ? 2 : 0);
        if (searchQuery.isEmpty()) {
            ctx.text(mc.font, m.getName(), textX, curY + 3, txtC, false);
        } else {
            navine_renderHighlightedModuleName(ctx, m, textX, curY + 3, txtC, areaW - 14, alpha);
        }

        if (!m.getSettings().isEmpty()) {
            ctx.text(mc.font, ">", listX + areaW - 8, curY + 3, applyAlpha(DISABLED_COLOR, alpha), false);
        }
    }

    private List<Setting<?>> visibleSettings(Module module) {
        List<Setting<?>> visible = new ArrayList<>();
        if (module == null) {
            return visible;
        }
        for (Setting<?> setting : module.getSettings()) {
            if (module.isSettingVisible(setting)) {
                visible.add(setting);
            }
        }
        return visible;
    }

    private int settingRowHeight(Setting<?> setting) {
        if (setting instanceof StringSetting) {
            return 34;
        }
        if (setting instanceof ColorSetting) {
            return 52;
        }
        if (setting instanceof ModeSetting) {
            return 28;
        }
        if (setting instanceof NumberSetting) {
            return 28;
        }
        if (setting instanceof BooleanSetting) {
            return 22;
        }
        return 28;
    }

    private int totalSettingsHeight(Module module) {
        int total = 0;
        for (Setting<?> setting : visibleSettings(module)) {
            total += settingRowHeight(setting) + 2;
        }
        return total;
    }

    private void updateSlider(double mx, double my) {
        if (settingsModule == null || draggingSlider == null) {
            return;
        }
        SettingsLayout layout = computeSettingsLayout(settingsAnim);
        double[] local = new double[2];
        toSettingsLocal(mx, my, layout, local);
        int barX = 8;
        int barW = layout.panelW - 16;
        double pct = Math.max(0, Math.min(1, (local[0] - barX) / (double) barW));
        draggingSlider.setValue(draggingSlider.getMin() + pct * (draggingSlider.getMax() - draggingSlider.getMin()));
        ConfigManager.save();
    }

    private void updateColorSlider(double mx, double my) {
        if (settingsModule == null || draggingColorSetting == null || draggingColorChannel < 0) {
            return;
        }
        SettingsLayout layout = computeSettingsLayout(settingsAnim);
        double[] local = new double[2];
        toSettingsLocal(mx, my, layout, local);
        int barX = 22;
        int barW = layout.panelW - 34;
        double pct = Math.max(0, Math.min(1, (local[0] - barX) / (double) barW));
        int value = (int) Math.round(pct * 255.0);
        int r = draggingColorSetting.getRed();
        int g = draggingColorSetting.getGreen();
        int b = draggingColorSetting.getBlue();
        if (draggingColorChannel == 0) {
            r = value;
        } else if (draggingColorChannel == 1) {
            g = value;
        } else {
            b = value;
        }
        draggingColorSetting.setRGB(r, g, b);
        ConfigManager.save();
    }

    private SettingsLayout computeSettingsLayout(float anim) {
        List<Setting<?>> visible = visibleSettings(settingsModule);
        int settingCount = visible.size();
        boolean widePanel = false;
        for (Setting<?> setting : visible) {
            if (setting instanceof StringSetting || setting instanceof ColorSetting) {
                widePanel = true;
                break;
            }
        }
        int panelW = widePanel ? 260 : (settingCount >= 5 ? 240 : (settingCount >= 3 ? 220 : 200));
        int baseH = 88;
        int settingH = totalSettingsHeight(settingsModule);
        int panelH = Math.min(baseH + settingH, 380);
        float scale = 1.0f;
        int scaledW = (int) (panelW * scale);
        int scaledH = (int) (panelH * scale);
        int panelX;
        int panelY;
        if (settingsPanelX >= 0 && settingsPanelY >= 0) {
            panelX = settingsPanelX;
            panelY = settingsPanelY;
        } else {
            panelX = frameX + frameW + 8;
            panelY = frameY;
            if (panelX + scaledW > width - 4) {
                panelX = Math.max(4, frameX - scaledW - 8);
            }
        }
        return new SettingsLayout(panelW, panelH, scale, scaledW, scaledH, panelX, panelY);
    }

    private void toSettingsLocal(double mx, double my, SettingsLayout layout, double[] out) {
        float cx = layout.panelX + layout.scaledW / 2f;
        float cy = layout.panelY + layout.scaledH / 2f;
        out[0] = (mx - cx) / layout.scale + layout.panelW / 2f;
        out[1] = (my - cy) / layout.scale + layout.panelH / 2f;
    }

    private void renderSettingsPanel(GuiGraphicsExtractor ctx, int mx, int my, float anim) {
        SettingsLayout layout = computeSettingsLayout(anim);
        int panelW = layout.panelW;
        int panelH = layout.panelH;
        int scaledW = layout.scaledW;
        int scaledH = layout.scaledH;
        int panelX = layout.panelX;
        int panelY = layout.panelY;
        List<Setting<?>> visible = visibleSettings(settingsModule);
        int alpha = Math.min(255, Math.max(0, (int) (anim * 255)));
        int settingsTop = panelY + 22;
        int settingsBottom = panelY + panelH - 42;
        int settingsAreaH = Math.max(0, settingsBottom - settingsTop);
        int totalSettingsH = totalSettingsHeight(settingsModule);
        int maxSettingsScroll = Math.max(0, totalSettingsH - settingsAreaH);
        settingsScroll = Math.max(0, Math.min(settingsScroll, maxSettingsScroll));

        ctx.fill(0, 0, width, height, NavineTheme.OVERLAY_DIM);
        RenderUtil.drawPanel(ctx, panelX, panelY, scaledW, scaledH, applyAlpha(NavineTheme.SETTINGS_BG, alpha), applyAlpha(BORDER, alpha));

        ctx.fill(panelX + 1, panelY + 1, panelX + scaledW - 1, panelY + 18, applyAlpha(HEADER_BG, alpha));
        RenderUtil.drawHLine(ctx, panelX + 1, panelY + 18, scaledW - 2, applyAlpha(BORDER, alpha));
        if (draggingSettingsPanel) {
            ctx.fill(panelX + 1, panelY + 1, panelX + scaledW - 1, panelY + 18, applyAlpha(HOVER, alpha));
        }
        String modName = settingsModule.getName();
        ctx.text(font, modName, panelX + 6, panelY + 5, applyAlpha(TEXT_PRIMARY, alpha), false);

        if (DevMod.shouldShowUiOutlines()) {
            RenderUtil.drawOutline(ctx, panelX, panelY, scaledW, scaledH, 2, applyAlpha(0xFFFF0000, alpha));
        }

        int settingY = settingsTop - settingsScroll;
        ctx.enableScissor(panelX + 6, settingsTop, panelX + scaledW - 6, settingsBottom);
        for (Setting<?> s : visible) {
            int rowH = settingRowHeight(s);
            if (settingY + rowH >= settingsTop && settingY <= settingsBottom) {
                renderSettingEntry(ctx, s, panelX + 8, settingY, panelW - 16, mx, my);
            }
            settingY += rowH + 2;
        }
        ctx.disableScissor();

        if (maxSettingsScroll > 0) {
            int sbX = panelX + scaledW - 6;
            int sbY = settingsTop;
            int sbH = settingsAreaH;
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, applyAlpha(SCROLLBAR_BG, alpha));
            double pct = (double) settingsScroll / maxSettingsScroll;
            int thumbH = Math.max(10, (int) ((double) settingsAreaH / totalSettingsH * sbH));
            int thumbY = sbY + (int) (pct * (sbH - thumbH));
            ctx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, applyAlpha(SCROLLBAR_COLOR, alpha));
        }

        int keyY = panelY + panelH - 34;
        String keyLabel = "Keybind: ";
        String keyName = listeningKey ? "..." : (settingsModule != null && settingsModule.getKey() == 0 ? "NONE" : (settingsModule != null ? getKeyName(settingsModule.getKey()) : "NONE"));
        ctx.text(font, keyLabel, panelX + 8, keyY, applyAlpha(0xFF888888, alpha), false);
        ctx.text(font, keyName, panelX + 8 + mc.font.width(keyLabel), keyY, applyAlpha(listeningKey ? 0xFFFF5555 : ACCENT, alpha), false);

        int btnY = panelY + panelH - 16;
        int btnW = panelW - 16;
        boolean btnHover = mx >= panelX + 8 && mx <= panelX + 8 + btnW && my >= btnY && my <= btnY + 12;
        closeHoverAnim = UiAnim.approach(closeHoverAnim, btnHover ? 1f : 0f, 14f, uiDelta);
        NavineButtonRenderer.renderPanelButton(ctx, panelX + 8, btnY, btnW, 12, "Close", closeHoverAnim, alpha);
    }

    private void renderSettingEntry(GuiGraphicsExtractor ctx, Setting<?> s, int x, int y, int w, int mx, int my) {
        if (s instanceof BooleanSetting bs) {
            ctx.text(font, s.getName(), x, y + 2, NavineTheme.TEXT_PRIMARY, false);
            float toggleAnim = settingToggleAnim.getOrDefault(s, bs.getValue() ? 1f : 0f);
            toggleAnim = UiAnim.approach(toggleAnim, bs.getValue() ? 1f : 0f, 14f, uiDelta);
            settingToggleAnim.put(s, toggleAnim);
            int trackX = x + w - 20;
            int trackW = 20;
            int trackH = 10;
            int trackY = y + 2;
            int trackColor = UiAnim.lerpColor(NavineTheme.HEADER_BG, ACCENT, toggleAnim);
            RenderUtil.drawRoundedRect(ctx, trackX, trackY, trackW, trackH, 5, trackColor);
            int knobX = trackX + 2 + (int) ((trackW - 10) * toggleAnim);
            int knobColor = UiAnim.lerpColor(0xFF666666, 0xFFFFFFFF, toggleAnim);
            RenderUtil.drawRoundedRect(ctx, knobX, trackY + 2, 6, trackH - 4, 3, knobColor);
        } else if (s instanceof NumberSetting ns) {
            String txt = s.getName();
            String val = String.format("%.2f", ns.getValue());
            ctx.text(font, txt, x, y + 1, NavineTheme.TEXT_SECONDARY, false);

            int resetBtnW = 10;
            int resetBtnX = x + w - resetBtnW;
            boolean resetHover = mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= y && my <= y + 10;
            RenderUtil.drawRoundedRect(ctx, resetBtnX, y, resetBtnW, 10, 2, resetHover ? 0xFFFF5555 : 0xFF444455);
            RenderUtil.drawRoundedOutline(ctx, resetBtnX, y, resetBtnW, 10, 2, 1, BORDER);
            ctx.text(font, "R", resetBtnX + 2, y + 1, 0xFFFFFFFF, false);

            ctx.text(font, val, resetBtnX - mc.font.width(val) - 4, y + 1, ACCENT, false);

            int barY = y + 12;
            int barW = w;
            int barH = 8;
            double pct = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            RenderUtil.drawRoundedRect(ctx, x, barY, barW, barH, 2f, NavineTheme.HEADER_BG);
            RenderUtil.drawRoundedRect(ctx, x, barY, (int) (barW * pct), barH, 2f, ACCENT);
        } else if (s instanceof ModeSetting ms) {
            ctx.text(font, s.getName(), x, y, NavineTheme.TEXT_PRIMARY, false);
            String val = ms.getValue();
            int valW = mc.font.width(val);
            int modeY = y + 9;
            int arrowW = 10;
            RenderUtil.drawRoundedRect(ctx, x, modeY, w, 9, 3, HEADER_BG);
            RenderUtil.drawRoundedOutline(ctx, x, modeY, w, 9, 3, 1, BORDER);
            boolean leftHover = mx >= x && mx <= x + arrowW && my >= modeY && my <= modeY + 9;
            boolean rightHover = mx >= x + w - arrowW && mx <= x + w && my >= modeY && my <= modeY + 9;
            ctx.text(font, "<", x + 2, modeY + 1, leftHover ? ACCENT2 : 0xFF888888, false);
            ctx.text(font, ">", x + w - 8, modeY + 1, rightHover ? ACCENT2 : 0xFF888888, false);
            ctx.text(font, val, x + (w - valW) / 2, modeY + 1, ACCENT, false);
        } else if (s instanceof ColorSetting cs) {
            ctx.text(font, s.getName(), x, y, NavineTheme.TEXT_PRIMARY, false);
            int colorBoxX = x + w - 44;
            int colorBoxW = 36;
            int color = cs.getValue();
            RenderUtil.drawRoundedRect(ctx, colorBoxX, y + 1, colorBoxW, 12, 3, color);
            RenderUtil.drawRoundedOutline(ctx, colorBoxX, y + 1, colorBoxW, 12, 3, 1, BORDER);

            renderColorChannelSlider(ctx, x, y + 16, w, "R", cs.getRed(), mx, my, 0);
            renderColorChannelSlider(ctx, x, y + 28, w, "G", cs.getGreen(), mx, my, 1);
            renderColorChannelSlider(ctx, x, y + 40, w, "B", cs.getBlue(), mx, my, 2);
        } else if (s instanceof StringSetting ss) {
            ctx.text(font, s.getName(), x, y, NavineTheme.TEXT_PRIMARY, false);
            int boxY = y + 12;
            int boxH = 14;
            boolean editing = editingStringSetting == ss;
            String display = editing ? stringEditBuffer : ss.getValue();
            if (display == null) {
                display = "";
            }
            RenderUtil.drawRoundedRect(ctx, x, boxY, w, boxH, 3, editing ? 0xFF1A1A42 : HEADER_BG);
            RenderUtil.drawRoundedOutline(ctx, x, boxY, w, boxH, 3, 1, editing ? ACCENT : BORDER);
            String shown = truncateSettingText(display, w - 8);
            ctx.text(font, shown, x + 4, boxY + 3, editing ? NavineTheme.TEXT_PRIMARY : NavineTheme.TEXT_SECONDARY, false);
            if (editing) {
                int cursorX = x + 4 + mc.font.width(shown);
                ctx.fill(cursorX, boxY + 2, cursorX + 1, boxY + boxH - 2, ACCENT);
            }
        }
    }

    private void renderColorChannelSlider(GuiGraphicsExtractor ctx, int x, int y, int w, String label, int value, int mx, int my, int channel) {
        ctx.text(font, label, x, y + 1, NavineTheme.TEXT_SECONDARY, false);
        int barX = x + 14;
        int barW = w - 34;
        int barH = 6;
        double pct = value / 255.0;
        RenderUtil.drawRoundedRect(ctx, barX, y + 2, barW, barH, 2f, NavineTheme.HEADER_BG);
        RenderUtil.drawRoundedRect(ctx, barX, y + 2, (int) (barW * pct), barH, 2f, ACCENT);
        String valText = String.valueOf(value);
        ctx.text(font, valText, x + w - mc.font.width(valText), y + 1, NavineTheme.TEXT_SECONDARY, false);
    }

    private String truncateSettingText(String text, int maxWidth) {
        if (text.isEmpty() || maxWidth <= 0) {
            return "";
        }
        if (mc.font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        String trimmed = text;
        while (trimmed.length() > 0 && mc.font.width(trimmed) + mc.font.width(ellipsis) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? ellipsis : trimmed + ellipsis;
    }

    private String getKeyName(int k) {
        String n = ClientAccess.keyName(k);
        if (n != null) return n.toUpperCase();
        return switch (k) {
            case InputConstants.KEY_LSHIFT -> "LSHIFT";
            case InputConstants.KEY_RSHIFT -> "RSHIFT";
            case InputConstants.KEY_LCONTROL -> "LCTRL";
            case InputConstants.KEY_LALT -> "LALT";
            case InputConstants.KEY_SPACE -> "SPACE";
            case InputConstants.KEY_TAB -> "TAB";
            case InputConstants.KEY_CAPSLOCK -> "CAPS";
            default -> k >= InputConstants.KEY_F1 && k <= InputConstants.KEY_F12 ? "F" + (k - InputConstants.KEY_F1 + 1) : "KEY" + k;
        };
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        int keyCode = keyInput.key();

        if (editingStringSetting != null) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                editingStringSetting = null;
                stringEditBuffer = "";
                return true;
            }
            if (keyCode == InputConstants.KEY_RETURN) {
                editingStringSetting.setValue(stringEditBuffer.trim());
                editingStringSetting = null;
                stringEditBuffer = "";
                ConfigManager.save();
                return true;
            }
            if (keyCode == InputConstants.KEY_BACKSPACE && !stringEditBuffer.isEmpty()) {
                stringEditBuffer = stringEditBuffer.substring(0, stringEditBuffer.length() - 1);
                return true;
            }
            return true;
        }

        if (listeningKey && selectedModule != null) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                listeningKey = false;
            } else if (keyCode == InputConstants.KEY_DELETE || keyCode == InputConstants.KEY_BACKSPACE) {
                selectedModule.setKey(0);
                listeningKey = false;
                ConfigManager.save();
            } else {
                selectedModule.setKey(keyCode);
                listeningKey = false;
                ConfigManager.save();
            }
            return true;
        }

        if (searchFocused) {
            if (keyCode == InputConstants.KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                searchQuery = searchText.toLowerCase();
                setModuleScroll(0);
                return true;
            }
            if (keyCode == InputConstants.KEY_ESCAPE) {
                searchText = "";
                searchQuery = "";
                setModuleScroll(0);
                searchFocused = false;
                return true;
            }
            if (keyCode == InputConstants.KEY_RETURN) {
                searchFocused = false;
                return true;
            }
            if (keyCode == NavineClient.getClickGuiKey()) {
                return true;
            }
        }

        if (keyCode == InputConstants.KEY_ESCAPE) {
            if (!searchQuery.isEmpty() || !searchText.isEmpty()) {
                searchText = "";
                searchQuery = "";
                setModuleScroll(0);
                searchFocused = false;
                return true;
            }
            if (selectedModule != null) {
                selectedModule = null;
                listeningKey = false;
                editingStringSetting = null;
                stringEditBuffer = "";
                settingsPanelX = -1;
                settingsPanelY = -1;
                return true;
            }
            onClose();
            return true;
        }
        if (keyCode == NavineClient.getClickGuiKey()) {
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean charTyped(CharacterEvent charInput) {
        if (listeningKey) return true;
        if (editingStringSetting != null) {
            if (charInput.isAllowedChatCharacter() && stringEditBuffer.length() < 64) {
                stringEditBuffer += charInput.codepointAsString();
                return true;
            }
            return true;
        }
        if (!searchFocused) return super.charTyped(charInput);

        if (charInput.isAllowedChatCharacter()) {
            searchText += charInput.codepointAsString();
            searchQuery = searchText.toLowerCase();
            setModuleScroll(0);
            return true;
        }
        return super.charTyped(charInput);
    }

    private void handleMouseClicked(double mx, double my, int button) {
        if (searchClearW > 0
                && mx >= searchClearX && mx <= searchClearX + searchClearW
                && my >= searchClearY && my <= searchClearY + searchClearH) {
            searchText = "";
            searchQuery = "";
            setModuleScroll(0);
            searchFocused = true;
            return;
        }
        if (mx >= searchX && mx <= searchX + searchW
                && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return;
        }
        searchFocused = false;

        if (selectedModule != null && settingsModule != null) {
            SettingsLayout layout = computeSettingsLayout(settingsAnim);
            double[] local = new double[2];
            toSettingsLocal(mx, my, layout, local);
            double localX = local[0];
            double localY = local[1];

            if (button == 0 && localX >= 0 && localX <= layout.panelW && localY >= 0 && localY <= 18) {
                pendingSettingsDrag = true;
                pendingSettingsDragX = mx;
                pendingSettingsDragY = my;
                settingsDragOffsetX = (int) mx - layout.panelX;
                settingsDragOffsetY = (int) my - layout.panelY;
                return;
            }

            int btnY = layout.panelH - 16;
            int btnW = layout.panelW - 16;
            if (localX >= 8 && localX <= 8 + btnW && localY >= btnY && localY <= btnY + 12) {
                NavineSoundManager.playClick();
                selectedModule = null;
                listeningKey = false;
                settingsScroll = 0;
                settingsPanelX = -1;
                settingsPanelY = -1;
                return;
            }

            int keyY = layout.panelH - 34;
            if (localY >= keyY - 2 && localY <= keyY + 10 && localX >= 8 && localX <= layout.panelW - 8) {
                listeningKey = !listeningKey;
                return;
            }

            int settingsTop = 22;
            int settingsBottom = layout.panelH - 42;
            int settingY = settingsTop - settingsScroll;
            for (Setting<?> s : visibleSettings(settingsModule)) {
                if (localY >= settingsTop && localY <= settingsBottom &&
                        handleSettingClick(s, 8, settingY, layout.panelW - 16, localX, localY, button)) {
                    return;
                }
                settingY += settingRowHeight(s) + 2;
            }

            if (localX < 0 || localX > layout.panelW || localY < 0 || localY > layout.panelH) {
                selectedModule = null;
                listeningKey = false;
                settingsScroll = 0;
                settingsPanelX = -1;
                settingsPanelY = -1;
                return;
            }
            return;
        }

        for (int i = tabs.size() - 1; i >= 0; i--) {
            TabEntry tab = tabs.get(i);
            if (mx >= tab.x && mx <= tab.x + tab.width && my >= tab.y && my <= tab.y + tab.height) {
                if (button == 0 || button == 1) {
                    activeTabId = tab.id;
                    for (TabEntry t : tabs) {
                        t.expanded = t.id.equals(activeTabId);
                        savedExp.put(t.id, t.expanded);
                    }
                    tabFadeAnim = 0f;
                    setModuleScroll(0);
                    if (tab.search) {
                        searchFocused = true;
                    }
                }
                return;
            }
        }

        TabEntry active = getActiveTab();
        if (active != null && handleModuleListClick(active, mx, my, button)) {
            return;
        }
    }

    private boolean handleModuleListClick(TabEntry tab, double mx, double my, int button) {
        if (mx < tab.moduleListX || mx > tab.moduleListX + tab.moduleListW
                || my < tab.moduleListY || my > tab.moduleListY + tab.moduleListH) {
            return false;
        }

        List<Module> mods = getModulesForTab(tab);
        int contentH = mods.size() * MODULE_HEIGHT + 2;
        int maxListHeight = ConfigManager.getModuleListMaxHeight();
        int visH = Math.min(contentH, maxListHeight);
        boolean hasScrollbar = contentH > maxListHeight;
        int moduleAreaWidth = hasScrollbar ? MODULE_LIST_WIDTH - SCROLLBAR_WIDTH - 2 : MODULE_LIST_WIDTH;

        int curY = tab.moduleListY + 1 - Math.round(smoothScrollByTab.getOrDefault(tab.id, (float) getModuleScroll()));
        for (Module m : mods) {
            int modTop = Math.max(curY, tab.moduleListY);
            int modBot = Math.min(curY + MODULE_HEIGHT, tab.moduleListY + visH);
            if (my >= modTop && my < modBot && mx >= tab.moduleListX && mx <= tab.moduleListX + moduleAreaWidth) {
                if (button == 0) {
                    if (ModuleManager.canToggleModule(m)) {
                        m.toggle();
                        ConfigManager.save();
                    }
                    return true;
                } else if (button == 1) {
                    selectedModule = m;
                    settingsPanelX = -1;
                    settingsPanelY = -1;
                    settingsScroll = 0;
                    return true;
                }
            }
            curY += MODULE_HEIGHT;
        }
        return true;
    }

    private boolean handleSettingClick(Setting<?> s, int x, int y, int w, double mx, double my, int btn) {
        if (s instanceof BooleanSetting bs) {
            int boxX = x + w - 20;
            if (mx >= boxX && mx <= boxX + 20 && my >= y + 1 && my <= y + 13) {
                bs.setValue(!bs.getValue());
                NavineSoundManager.playClick();
                ConfigManager.save();
                return true;
            }
        } else if (s instanceof NumberSetting ns) {
            int resetBtnW = 10;
            int resetBtnX = x + w - resetBtnW;
            if (mx >= resetBtnX && mx <= resetBtnX + resetBtnW && my >= y && my <= y + 10) {
                ns.reset();
                ConfigManager.save();
                return true;
            }

            int barY = y + 12;
            if (mx >= x && mx <= x + w && my >= barY - 6 && my <= barY + 14) {
                draggingSlider = ns;
                updateSlider(mx, my);
                ConfigManager.save();
                return true;
            }
        } else if (s instanceof ModeSetting ms) {
            int modeY = y + 9;
            int arrowW = 10;
            if (my >= modeY && my <= modeY + 9) {
                if (mx >= x && mx <= x + arrowW) {
                    ms.cycleBack();
                    ConfigManager.save();
                    return true;
                }
                if (mx >= x + w - arrowW && mx <= x + w) {
                    ms.cycle();
                    ConfigManager.save();
                    return true;
                }
            }
            if (mx >= x && mx <= x + w && my >= modeY && my <= modeY + 9) {
                if (btn == 0) ms.cycle();
                else if (btn == 1) ms.cycleBack();
                ConfigManager.save();
                return true;
            }
        } else if (s instanceof ColorSetting cs) {
            int colorBoxX = x + w - 44;
            int colorBoxW = 36;
            if (mx >= colorBoxX && mx <= colorBoxX + colorBoxW && my >= y + 1 && my <= y + 13) {
                int current = cs.getValue();
                int[] presets = {
                        0xFF87CEEB,
                        0xFFFF0000,
                        0xFF00FF00,
                        0xFF0000FF,
                        0xFFFFFF00,
                        0xFFFF00FF,
                        0xFF00FFFF,
                        0xFFFFFFFF,
                        0xFF8B5CF6,
                        0xFFFFA500,
                };
                int nextIndex = 0;
                int currentRgb = current & 0x00FFFFFF;
                for (int i = 0; i < presets.length; i++) {
                    if ((presets[i] & 0x00FFFFFF) == currentRgb) {
                        nextIndex = (i + 1) % presets.length;
                        break;
                    }
                }
                cs.setValue(presets[nextIndex]);
                ConfigManager.save();
                return true;
            }
            int[] channels = {cs.getRed(), cs.getGreen(), cs.getBlue()};
            for (int i = 0; i < 3; i++) {
                int barY = y + 16 + i * 12;
                int barX = x + 14;
                int barW = w - 34;
                if (mx >= barX && mx <= barX + barW && my >= barY && my <= barY + 10) {
                    draggingColorSetting = cs;
                    draggingColorChannel = i;
                    updateColorSlider(mx, my);
                    ConfigManager.save();
                    return true;
                }
            }
        } else if (s instanceof StringSetting ss) {
            int boxY = y + 12;
            int boxH = 14;
            if (mx >= x && mx <= x + w && my >= boxY && my <= boxY + boxH) {
                editingStringSetting = ss;
                stringEditBuffer = ss.getValue() == null ? "" : ss.getValue();
                searchFocused = false;
                listeningKey = false;
                return true;
            }
        }
        return false;
    }

    private void handleMouseReleased(double mx, double my, int button) {
        draggingSlider = null;
        draggingColorSetting = null;
        draggingColorChannel = -1;

        if (button == 0) {
            if (draggingSettingsPanel) {
                draggingSettingsPanel = false;
                pendingSettingsDrag = false;
                return;
            }
            pendingSettingsDrag = false;
            draggingTabId = null;
            pendingTabClickId = null;
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        if (settingsModule != null) {
            SettingsLayout layout = computeSettingsLayout(settingsAnim);
            double[] local = new double[2];
            toSettingsLocal(mx, my, layout, local);
            int settingsTop = 22;
            int settingsBottom = layout.panelH - 42;
            int settingsAreaH = Math.max(0, settingsBottom - settingsTop);
            int totalSettingsH = totalSettingsHeight(settingsModule);
            int maxSettingsScroll = Math.max(0, totalSettingsH - settingsAreaH);

            if (local[0] >= 0 && local[0] <= layout.panelW
                    && local[1] >= settingsTop && local[1] <= settingsBottom) {
                settingsScroll -= (int) (vAmount * 16);
                settingsScroll = Math.max(0, Math.min(settingsScroll, maxSettingsScroll));
                return true;
            }
        }

        TabEntry active = getActiveTab();
        if (active != null) {
            if (mx >= active.moduleListX && mx <= active.moduleListX + active.moduleListW
                    && my >= active.moduleListY && my <= active.moduleListY + active.moduleListH) {
                int scroll = getModuleScroll() - (int) (vAmount * 10);
                setModuleScroll(Math.max(0, Math.min(scroll, maxModuleScroll)));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hAmount, vAmount);
    }

    private void navine_renderSearchText(GuiGraphicsExtractor ctx, int x, int y, String text, String query, int alpha) {
        int cursorX = x;
        String lowerText = text.toLowerCase();
        int queryLen = query.length();
        int i = 0;
        while (i < text.length()) {
            int highlightStart = lowerText.indexOf(query, i);
            if (queryLen == 0 || highlightStart < 0) {
                ctx.text(font, text.substring(i), cursorX, y, applyAlpha(NavineTheme.TEXT_PRIMARY, alpha), false);
                break;
            }
            if (highlightStart > i) {
                String before = text.substring(i, highlightStart);
                ctx.text(font, before, cursorX, y, applyAlpha(NavineTheme.TEXT_PRIMARY, alpha), false);
                cursorX += mc.font.width(before);
            }
            String match = text.substring(highlightStart, highlightStart + queryLen);
            int matchW = mc.font.width(match);
            ctx.fill(cursorX, y - 1, cursorX + matchW, y + 9, applyAlpha(SEARCH_HIGHLIGHT, alpha));
            ctx.text(font, match, cursorX, y, applyAlpha(ACCENT, alpha), false);
            cursorX += matchW;
            i = highlightStart + queryLen;
        }
    }

    private void navine_renderHighlightedModuleName(GuiGraphicsExtractor ctx, Module m, int x, int y, int baseColor, int maxWidth, int alpha) {
        String name = m.getName();
        String lowerName = name.toLowerCase();
        String normalizedName = lowerName.replace("_", " ").replace("-", " ");
        String compactName = normalizedName.replace(" ", "");
        String compactQuery = searchQuery.replace(" ", "");
        int matchStart = -1;
        int matchLen = searchQuery.length();
        if (matchLen > 0) {
            matchStart = lowerName.indexOf(searchQuery);
            if (matchStart < 0) {
                matchStart = normalizedName.indexOf(searchQuery);
            }
            if (matchStart < 0 && !compactQuery.isEmpty()) {
                matchStart = compactName.indexOf(compactQuery);
                matchLen = compactQuery.length();
            }
        }
        if (matchStart < 0) {
            String desc = m.getDescription();
            if (desc != null && desc.toLowerCase().contains(searchQuery)) {
                ctx.text(mc.font, name, x, y, applyAlpha(NavineTheme.TEXT_SECONDARY, alpha), false);
                return;
            }
            ctx.text(mc.font, name, x, y, baseColor, false);
            return;
        }
        int cursorX = x;
        if (matchStart > 0) {
            String before = name.substring(0, matchStart);
            ctx.text(mc.font, before, cursorX, y, baseColor, false);
            cursorX += mc.font.width(before);
        }
        String match = name.substring(matchStart, Math.min(name.length(), matchStart + matchLen));
        int matchW = mc.font.width(match);
        ctx.fill(cursorX, y - 1, cursorX + matchW, y + 9, applyAlpha(SEARCH_HIGHLIGHT, alpha));
        ctx.text(mc.font, match, cursorX, y, applyAlpha(ACCENT, alpha), false);
        cursorX += matchW;
        if (matchStart + matchLen < name.length()) {
            String after = name.substring(matchStart + matchLen);
            if (cursorX + mc.font.width(after) <= x + maxWidth) {
                ctx.text(mc.font, after, cursorX, y, baseColor, false);
            }
        }
    }

    private int applyAlpha(int color, int alpha) {
        int a = (color >>> 24) & 0xFF;
        int newA = Math.min(255, (a * alpha) / 255);
        return (newA << 24) | (color & 0x00FFFFFF);
    }

    private static final class SettingsLayout {
        final int panelW;
        final int panelH;
        final float scale;
        final int scaledW;
        final int scaledH;
        final int panelX;
        final int panelY;

        SettingsLayout(int panelW, int panelH, float scale, int scaledW, int scaledH, int panelX, int panelY) {
            this.panelW = panelW;
            this.panelH = panelH;
            this.scale = scale;
            this.scaledW = scaledW;
            this.scaledH = scaledH;
            this.panelX = panelX;
            this.panelY = panelY;
        }
    }

    private static final class TabEntry {
        final String id;
        final String label;
        final CategoryKey categoryKey;
        final boolean search;
        boolean expanded;
        int x;
        int y;
        int width;
        int height;
        int moduleListX;
        int moduleListY;
        int moduleListW;
        int moduleListH;
        int searchInputX;
        int searchInputY;
        int searchInputW;
        int searchInputH;
        int searchClearX;
        int searchClearY;
        int searchClearW;
        int searchClearH;

        private TabEntry(String id, String label, CategoryKey categoryKey, boolean search) {
            this.id = id;
            this.label = label;
            this.categoryKey = categoryKey;
            this.search = search;
            this.expanded = savedExp.getOrDefault(id, false);
        }

        static TabEntry category(CategoryKey key) {
            return new TabEntry(key.storageId(), key.getDisplayName(), key, false);
        }

        static TabEntry search() {
            return new TabEntry(SEARCH_TAB_ID, "Search", null, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
