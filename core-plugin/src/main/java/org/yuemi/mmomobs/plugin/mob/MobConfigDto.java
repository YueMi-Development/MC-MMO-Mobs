package org.yuemi.mmomobs.plugin.mob;

import java.util.List;

public record MobConfigDto(
    String name,
    String type,
    Double health,
    Double speed,
    List<String> tags,
    List<MobSkillConfig> skills,
    EquipmentConfigDto equipment
) {}
