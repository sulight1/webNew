package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CraftTechnique;
import com.example.fingerartbackend.mapper.CraftTechniqueMapper;
import com.example.fingerartbackend.service.CraftTechniqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CraftTechniqueServiceImpl implements CraftTechniqueService {

    @Autowired
    private CraftTechniqueMapper craftTechniqueMapper;

    private volatile boolean defaultsReady = false;

    @Override
    public List<CraftTechnique> getTechniquesByCategory(String category) {
        ensureDefaultTechniquesOnce();
        return deduplicate(craftTechniqueMapper.findByCategory(category));
    }

    @Override
    public List<CraftTechnique> getAllTechniques() {
        ensureDefaultTechniquesOnce();
        return deduplicate(craftTechniqueMapper.findAllByOrderByCategoryAsc());
    }

    @Override
    public List<CraftTechnique> initSampleData() {
        ensureDefaultTechniquesOnce();
        return deduplicate(craftTechniqueMapper.findAllByOrderByCategoryAsc());
    }

    private void ensureDefaultTechniquesOnce() {
        if (defaultsReady) {
            return;
        }
        synchronized (this) {
            if (defaultsReady) {
                return;
            }
            removeDuplicateRecords();
            seedMissingDefaults();
            defaultsReady = true;
        }
    }

    /** 删除库中 category + name 重复的记录，保留最早一条 */
    private void removeDuplicateRecords() {
        List<CraftTechnique> all = craftTechniqueMapper.findAll();
        Map<String, CraftTechnique> keep = new LinkedHashMap<>();
        List<CraftTechnique> toDelete = new ArrayList<>();

        for (CraftTechnique t : all) {
            String key = techniqueKey(t.getCategory(), t.getName());
            CraftTechnique existing = keep.get(key);
            if (existing == null) {
                keep.put(key, t);
            } else if (t.getId() != null && existing.getId() != null && t.getId() < existing.getId()) {
                toDelete.add(existing);
                keep.put(key, t);
            } else {
                toDelete.add(t);
            }
        }

        if (!toDelete.isEmpty()) {
            craftTechniqueMapper.deleteAll(toDelete);
        }
    }

    private void seedMissingDefaults() {
        List<CraftTechnique> existing = craftTechniqueMapper.findAll();
        for (CraftTechnique t : buildDefaultTechniques()) {
            boolean exists = existing.stream()
                    .anyMatch(e -> Objects.equals(e.getCategory(), t.getCategory())
                            && Objects.equals(e.getName(), t.getName()));
            if (!exists) {
                craftTechniqueMapper.save(t);
            }
        }
    }

    private List<CraftTechnique> deduplicate(List<CraftTechnique> list) {
        Map<String, CraftTechnique> unique = new LinkedHashMap<>();
        for (CraftTechnique t : list) {
            unique.putIfAbsent(techniqueKey(t.getCategory(), t.getName()), t);
        }
        return new ArrayList<>(unique.values());
    }

    private String techniqueKey(String category, String name) {
        return category + "\0" + name;
    }

    private List<CraftTechnique> buildDefaultTechniques() {
        return Arrays.asList(
                // 钩织系列
                create("crochet", "钩织", "🧶 钩织编织"),
                create("crochet", "蕾丝", "🪡 蕾丝编织"),
                create("crochet", "梭编", "🧵 梭编工艺"),

                // 滴胶干花
                create("resin", "滴胶", "💎 滴胶工艺"),
                create("resin", "UV滴胶", "✨ UV滴胶"),
                create("resin", "押花", "🌿 干花押花"),

                // 精致穿戴甲
                create("nails", "穿戴甲", "💅 穿戴甲制作"),
                create("nails", "彩绘", "🎨 美甲彩绘"),
                create("nails", "甲片设计", "📐 甲片设计"),

                // 软陶超轻粘土
                create("clay", "粘土", "🏺 软陶粘土"),
                create("clay", "超轻粘土", "☁️ 超轻粘土"),
                create("clay", "石塑粘土", "🪨 石塑粘土"),

                // 古法缠花
                create("flower", "缠花", "🌸 古法缠花"),
                create("flower", "烧蓝", "🔥 烧蓝工艺"),
                create("flower", "金银细工", "✨ 金银细工"),
                create("flower", "绒花", "🌺 绒花制作"),

                // 拼豆系列
                create("perler", "拼豆", "🫘 拼豆像素"),
                create("perler", "立体拼豆", "🧩 立体拼豆"),
                create("perler", "烫豆定型", "🔥 烫豆定型"),

                // 刺绣布艺
                create("embroidery", "刺绣", "🪡 平面刺绣"),
                create("embroidery", "十字绣", "✖️ 十字绣"),
                create("embroidery", "戳戳绣", "🧵 戳戳绣"),

                // 串珠饰品
                create("bead", "串珠", "📿 手工串珠"),
                create("bead", "绕线", "💫 金属绕线"),
                create("bead", "编绳", "🎀 编绳手链"),

                // 皮艺皮雕
                create("leather", "皮雕", "🧵 皮雕工艺"),
                create("leather", "手工皮具", "👜 手工皮具"),
                create("leather", "皮艺染色", "🎨 皮艺染色"),

                // 木工雕绘
                create("wood", "木雕", "🪵 木雕工艺"),
                create("wood", "木作", "🔨 原木木作"),
                create("wood", "烧烙画", "🔥 烧烙画"),

                // 香薰蜡烛
                create("candle", "香薰蜡烛", "🕯️ 香薰蜡烛"),
                create("candle", "捏捏乐", "🫧 石膏捏捏"),
                create("candle", "扩香石", "💎 扩香石"),

                // 纸艺衍纸
                create("paper", "衍纸", "📄 衍纸艺术"),
                create("paper", "剪纸", "✂️ 手工剪纸"),
                create("paper", "纸雕", "🗂️ 立体纸雕")
        );
    }

    private CraftTechnique create(String category, String name, String label) {
        CraftTechnique t = new CraftTechnique();
        t.setCategory(category);
        t.setName(name);
        t.setLabel(label);
        return t;
    }
}
