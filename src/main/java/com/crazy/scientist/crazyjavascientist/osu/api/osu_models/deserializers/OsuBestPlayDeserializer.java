package com.crazy.scientist.crazyjavascientist.osu.api.osu_models.deserializers;

import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuApiEntity;
import com.crazy.scientist.crazyjavascientist.osu.api.osu_entities.OsuBestPlayEntity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class OsuBestPlayDeserializer extends StdDeserializer<OsuBestPlayEntity> {

    public OsuBestPlayDeserializer(){

        this(null);
    }

    public OsuBestPlayDeserializer(Class<?> vc){
        super(vc);
    }

    @Override
    public OsuBestPlayEntity deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException, IOException {

        JsonNode osuNode = jp.getCodec().readTree(jp);
        OsuBestPlayEntity bestPlayEntity = new OsuBestPlayEntity();
        bestPlayEntity.setId(osuNode.findValue("user").get("id").asLong());
        bestPlayEntity.setUsername(osuNode.findValue("user").get("username").asText());
        bestPlayEntity.setMapRank(osuNode.findValue("rank").asText());
        bestPlayEntity.setMapHitAcc(osuNode.findValue("accuracy").asDouble());
        bestPlayEntity.setMapPpAmount(osuNode.findValue("pp").asDouble());
        bestPlayEntity.setMapTitle(osuNode.findValue("beatmapset").get("title").asText());
        bestPlayEntity.setBeatMapUrl(osuNode.findValue("beatmap").get("url").asText());
        bestPlayEntity.setBeatMapCardImage(osuNode.findValue("covers").get("card").asText());
        bestPlayEntity.setTimeUpdated(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a").withZone(ZoneId.of("America/New_York"))));
        return bestPlayEntity;
    }


}
