package org.yuemi.mmomobs.plugin.mob;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

@JsonDeserialize(using = MobSkillConfig.Deserializer.class)
public record MobSkillConfig(String skill, String trigger) {

    public static class Deserializer extends StdDeserializer<MobSkillConfig> {
        public Deserializer() {
            super(MobSkillConfig.class);
        }

        @Override
        public MobSkillConfig deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isTextual()) {
                return new MobSkillConfig(node.asText(), null);
            } else if (node.isObject()) {
                String skillName = node.has("skill") ? node.get("skill").asText() : "";
                String triggerName = node.has("trigger") ? node.get("trigger").asText() : null;
                return new MobSkillConfig(skillName, triggerName);
            }
            return new MobSkillConfig("", null);
        }
    }
}
