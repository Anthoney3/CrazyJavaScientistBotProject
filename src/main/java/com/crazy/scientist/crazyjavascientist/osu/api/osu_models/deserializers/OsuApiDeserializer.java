package com.crazy.scientist.crazyjavascientist.osu.api.osu_models.deserializers;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;

public class OsuApiDeserializer extends StdDeserializer<OsuApiEntity> {


    public OsuApiDeserializer(){

        this(null);
    }

    public OsuApiDeserializer(Class<?> vc){
        super(vc);
    }

    @Override
    public OsuApiEntity deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException, IOException {

        JsonNode osuNode = jp.getCodec().readTree(jp);
        OsuApiEntity osuApiEntity = new OsuApiEntity();
        osuApiEntity.setUsername(osuNode.findValue("username").asText());
        osuApiEntity.setPp(osuNode.findValue("pp").asDouble());
        osuApiEntity.setMonthlyPlaycount(osuNode.findValue("monthly_playcounts").asInt());
        osuApiEntity.setTotalTimePlayed(Duration.ofSeconds(osuNode.findValue("play_time").asLong()));
        osuApiEntity.setTotalChokes(osuNode.findValue("sh").asInt());
        osuApiEntity.setGlobalRanking(osuNode.findValue("global_rank").asInt());
        osuApiEntity.setHitAcc(osuNode.findValue("hit_accuracy").asDouble());
        osuApiEntity.setPfpPictureUrl(osuNode.findValue("avatar_url").asText());
        osuApiEntity.setLastRequestDateAndTime(ZonedDateTime.now());
        return osuApiEntity;
    }
}
